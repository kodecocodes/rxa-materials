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

package com.raywenderlich.android.wundercast

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class WeatherViewModel(private val lastKnownLocation: Maybe<Location>) : ViewModel() {

  private val disposables = CompositeDisposable()
  private val locationClicks = PublishSubject.create<Unit>()
  private val cityNameChanges = PublishSubject.create<CharSequence>()
  val cityLiveData = MutableLiveData<String>()
  val weatherLiveData = MutableLiveData<Weather>()
  val errorLiveData = MutableLiveData<String>()

  init {
    val locationObservable = locationClicks
        .doOnNext { cityLiveData.postValue("Current Location") }
        .flatMapMaybe { lastKnownLocation }
        .flatMapSingle { cityName ->
          WeatherApi.getWeather(cityName.toString())
        }
        .onErrorReturnItem(Weather.empty)

    val textObservable = cityNameChanges
        .skip(1)
        .debounce(1, TimeUnit.SECONDS)
        .filter { it.toString() != "Current Location" }
        .flatMapSingle { WeatherApi.getWeather(it.toString()) }
        .onErrorReturnItem(Weather.empty)

    Observable.merge(locationObservable, textObservable)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(weatherLiveData::postValue)
        .addTo(disposables)
  }

  fun locationClicked() = locationClicks.onNext(Unit)

  fun cityNameChanged(name: CharSequence) = cityNameChanges.onNext(name)

    override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
