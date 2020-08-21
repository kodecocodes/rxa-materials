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
package com.raywenderlich.android.bookcollector

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.android.bookcollector.database.BookDatabase
import com.raywenderlich.android.bookcollector.networking.OpenLibraryApi
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

class MainViewModel(private val database: BookDatabase) : ViewModel() {

  val allBooksLiveData = MutableLiveData<List<Book>>()
  val favoriteBooksLiveData = MutableLiveData<List<Book>>()
  val alreadyReadBooksLiveData = MutableLiveData<List<Book>>()
  private val disposables = CompositeDisposable()

  init {
    val observable = OpenLibraryApi.searchBooks("Lord of the Rings")
        .subscribeOn(Schedulers.io())
        .cache()

    observable
        .subscribeBy(
            onSuccess = { item -> allBooksLiveData.postValue(item) },
            onError = { print("Error: $it") }
        )
        .addTo(disposables)

    observable
        .map { books -> books.filter { it.isFavorited } }
        .subscribeBy(
            onSuccess = { item -> favoriteBooksLiveData.postValue(item) },
            onError = { print("Error: $it") }
        )
        .addTo(disposables)

    observable
        .map { books -> books.filter { it.isAlreadyRead } }
        .subscribeBy(
            onSuccess = { item -> alreadyReadBooksLiveData.postValue(item) },
            onError = { print("Error: $it") }
        )
        .addTo(disposables)
  }

  fun favoriteClicked(book: Book) {
    TODO()
  }

  fun readClicked(book: Book) {
    TODO()
  }
}
