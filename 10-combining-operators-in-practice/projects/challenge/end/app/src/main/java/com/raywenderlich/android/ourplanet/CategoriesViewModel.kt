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

package com.raywenderlich.android.ourplanet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import com.raywenderlich.android.ourplanet.model.EOCategory
import com.raywenderlich.android.ourplanet.model.EONET
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers

class CategoriesViewModel : ViewModel() {

  val categoriesLiveData = MutableLiveData<List<EOCategory>>()

  val progressBarLiveData = MutableLiveData<Boolean>()

  private val disposables = CompositeDisposable()

  fun startDownload() {
    progressBarLiveData.postValue(false)

    val eoCategories = EONET.fetchCategories()
        .map { response ->
          val categories = response.categories
          categories.mapNotNull {
            EOCategory.fromJson(it)
          }
        }
    val downloadedEvents = Observable.merge(eoCategories.flatMap { categories ->
      Observable.fromIterable(categories.map { category ->
        EONET.fetchEvents(category)
      })
    }, 2)

    val updatedCategories = eoCategories.flatMap { categories ->
      downloadedEvents.scan(categories) { updated, events ->
        updated.map { category ->
          val eventsForCategory = EONET.filterEventsForCategory(events, category)

          if (!eventsForCategory.isEmpty()) {
            val cat = category.copy()
            cat.events.addAll(eventsForCategory.filter { it.closeDate != null })
            cat
          } else {
            category
          }
        }
      }
    }.doOnComplete {
      progressBarLiveData.postValue(true)
    }

    eoCategories.concatWith(updatedCategories)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          categoriesLiveData.value = it
        }, {
          Log.e("CategoriesViewModel", it.localizedMessage)
        })
        .addTo(disposables)
  }

  override fun onCleared() {
    super.onCleared()
    disposables.dispose()
  }
}
