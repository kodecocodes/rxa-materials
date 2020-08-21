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
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.raywenderlich.android.jsonplayground

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import org.json.JSONObject

class JsonViewModel : ViewModel() {

  private val clicks = PublishSubject.create<Unit>()
  private val keyChanges = BehaviorSubject.create<CharSequence>()
  private val valueChanges = BehaviorSubject.create<CharSequence>()

  val jsonTextLiveData = MutableLiveData<String>()
  val errorLiveData = MutableLiveData<String>()

  private val disposables = CompositeDisposable()

  init {
    val buttonObservable = clicks
      .flatMap { Observables.combineLatest(keyChanges, valueChanges).take(1) }
      .share()

    val creationObservable = buttonObservable
      .take(1)
      .map { "{\"${it.first}\":\"${it.second}\"}" }
      .doOnNext { jsonTextLiveData.postValue(it) }
      .flatMap { JsonBinApi.createJson(it).subscribeOn(Schedulers.io()) }
      .map { it.substringAfterLast("/") }
      .cache()


    val updateObservable = creationObservable
      .flatMap { binId ->
        buttonObservable
          .map { createNewJsonString(it.first, it.second, jsonTextLiveData.value!!) }
          .map { binId to it }
      }
      .flatMap { pair ->
        JsonBinApi.updateJson(pair.first, pair.second)
          .andThen(JsonBinApi.getJson(pair.first))
          .toObservable()
          .subscribeOn(Schedulers.io())
      }

    updateObservable
        .subscribe {
          if (it.isSuccessful) {
            val prettyJson = JSONObject(it.body()!!).toString(4)
            jsonTextLiveData.postValue(prettyJson)
          } else {
            errorLiveData.postValue("Woops, we got an error!")
          }
        }
        .addTo(disposables)
  }

  fun onClick() = clicks.onNext(Unit)

  fun onKeyChange(key: CharSequence) = keyChanges.onNext(key)

  fun onValueChange(value: CharSequence) = valueChanges.onNext(value)

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}

