/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.hexcolor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.raywenderlich.android.hexcolor.networking.ColorApi
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class ColorViewModel(
  backgroundScheduler: Scheduler,
  mainScheduler: Scheduler,
  colorApi: ColorApi,
  colorCoordinator: ColorCoordinator,
  sharedPreferences: RxSharedPreferences
) : ViewModel() {

  private val backStream = PublishSubject.create<Unit>()
  private val clearStream = PublishSubject.create<Unit>()
  private val digitsStream = BehaviorSubject.create<String>()

  val hexStringSubject = BehaviorSubject.createDefault("#")
  val hexStringLiveData = MutableLiveData<String>()
  val backgroundColorLiveData = MutableLiveData<Int>()
  val rgbStringLiveData = MutableLiveData<String>()
  val colorNameLiveData = MutableLiveData<String>()

  private var colorNameDisposable: Disposable? = null
  private val disposables = CompositeDisposable()

  init {
    hexStringSubject
      .subscribeOn(backgroundScheduler)
      .observeOn(mainScheduler)
      .subscribe(hexStringLiveData::postValue)
      .addTo(disposables)

    hexStringSubject
      .subscribeOn(backgroundScheduler)
      .observeOn(mainScheduler)
      .map { if (it.length < 7) "#FFFFFF" else it }
      .map { colorCoordinator.parseColor(it) }
      .subscribe(backgroundColorLiveData::postValue)
      .addTo(disposables)

    hexStringSubject
      .subscribeOn(backgroundScheduler)
      .observeOn(mainScheduler)
      .filter { it.length < 7 }
      .map { "--" }
      .subscribe(colorNameLiveData::postValue)
      .addTo(disposables)

    hexStringSubject
      .subscribeOn(backgroundScheduler)
      .observeOn(mainScheduler)
      .map {
        if (it.length == 7) {
          colorCoordinator.parseRgbColor(it)
        } else {
          RGBColor(255, 255, 255)
        }
      }
      .map { "${it.red},${it.green},${it.blue}" }
      .subscribe(rgbStringLiveData::postValue)
      .addTo(disposables)

    clearStream
      .subscribe { hexStringSubject.onNext("#") }
      .addTo(disposables)

    backStream
      .filter { currentHexValue().length >= 2 }
      .subscribe {
        hexStringSubject.onNext(currentHexValue().substring(0, currentHexValue().lastIndex))
      }
      .addTo(disposables)

    digitsStream
      .filter { currentHexValue().length < 7 }
      .subscribe {
        hexStringSubject.onNext(currentHexValue() + it)
        if (currentHexValue().length == 7) {
          colorNameDisposable?.dispose()
          colorNameDisposable = colorApi.getClosestColor(currentHexValue())
            .subscribeOn(backgroundScheduler)
            .subscribe { response -> colorNameLiveData.postValue(response.name.value) }
        }
      }
      .addTo(disposables)

    sharedPreferences.getObject("favoriteColor", defaultColorResponse, ColorResponseConverter())
      .asObservable()
      .toV3Observable()
      .map { it.name }
      .subscribe {
        colorNameLiveData.postValue(it.value)
        hexStringSubject.onNext(it.closest_named_hex)
      }
      .addTo(disposables)
  }

  fun backClicked() = backStream.onNext(Unit)

  fun clearClicked() = clearStream.onNext(Unit)

  fun digitClicked(digit: String) = digitsStream.onNext(digit)

  private fun currentHexValue(): String {
    return hexStringSubject.value ?: ""
  }

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
