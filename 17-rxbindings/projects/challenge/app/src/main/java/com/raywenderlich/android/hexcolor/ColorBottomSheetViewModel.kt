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
 *
 */
package com.raywenderlich.android.hexcolor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxbinding4.widget.TextViewAfterTextChangeEvent
import com.raywenderlich.android.hexcolor.networking.ColorApi
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ColorBottomSheetViewModel(startingColor: String) : ViewModel() {
  private val searchObservable = BehaviorSubject.create<String>()
  private val textChangeEventsObservable = BehaviorSubject.create<TextViewAfterTextChangeEvent>()

  val showLoadingLiveData = MutableLiveData<Boolean>()
  val colorNameLiveData = MutableLiveData<String>()
  val closestColorLiveData = MutableLiveData<String>()

  private val disposables = CompositeDisposable()

  init {
    textChangeEventsObservable
        .doOnNext { event ->
          event.editable?.let { editable ->
            if (editable.firstOrNull() != '#') {
              editable.insert(0, "#")
            }
            if (editable.length > 7) {
              editable.delete(7, editable.length)
            }
            editable.lastOrNull()?.let {
              if (it !in '0'..'9' && it !in 'A'..'F' && it != '#') {
                editable.delete(editable.length - 1, editable.length)
              }
            }
          }
        }
        .subscribe()
        .addTo(disposables)

    val colorObservable = searchObservable
        .startWithItem(startingColor)
        .filter { it.length == 7 }
        .flatMapSingle {
          ColorApi.getClosestColor(it)
              .subscribeOn(Schedulers.io())
              .doOnSubscribe { showLoadingLiveData.postValue(true) }
              .doAfterSuccess { showLoadingLiveData.postValue(false) }}
        .map { it.name }
        .share()

    colorObservable
        .subscribe { colorNameLiveData.postValue(it.value) }
        .addTo(disposables)

    colorObservable
        .subscribe { closestColorLiveData.postValue(it.closest_named_hex) }
        .addTo(disposables)
  }

  fun onTextChange(text: String) = searchObservable.onNext(text)

  fun afterTextChange(effect: TextViewAfterTextChangeEvent) =
      textChangeEventsObservable.onNext(effect)

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
