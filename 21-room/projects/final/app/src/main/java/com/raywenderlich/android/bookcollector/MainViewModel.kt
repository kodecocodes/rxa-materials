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
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.raywenderlich.android.bookcollector.database.BookDatabase
import com.raywenderlich.android.bookcollector.paging.BookBoundaryCallback
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers

class MainViewModel(private val database: BookDatabase) : ViewModel() {
  val allBooksLiveData = MutableLiveData<PagedList<Book>>()
  val favoriteBooksLiveData = MutableLiveData<PagedList<Book>>()
  val alreadyReadBooksLiveData = MutableLiveData<PagedList<Book>>()
  private val disposables = CompositeDisposable()

  init {
    val config = PagedList.Config.Builder()
      .setEnablePlaceholders(false)
      .setPageSize(20)
      .build()

    RxPagedListBuilder<Int, Book>(database.bookDao().bookStream(), config)
      .setBoundaryCallback(BookBoundaryCallback("The lord of the rings", database))
      .buildObservable()
      .toV3Observable()
      .subscribe(allBooksLiveData::postValue)
      .addTo(disposables)

    RxPagedListBuilder<Int, Book>(database.bookDao().favoritesStream(), config)
      .setBoundaryCallback(BookBoundaryCallback("The lord of the rings", database))
      .buildObservable()
      .toV3Observable()
      .subscribe(favoriteBooksLiveData::postValue)
      .addTo(disposables)

    RxPagedListBuilder<Int, Book>(database.bookDao().alreadyReadStream(), config)
      .setBoundaryCallback(BookBoundaryCallback("The lord of the rings", database))
      .buildObservable()
      .toV3Observable()
      .subscribe(alreadyReadBooksLiveData::postValue)
      .addTo(disposables)
  }

  fun favoriteClicked(book: Book) {
    database.bookDao()
      .updateBook(book.copy(isFavorited = !book.isFavorited))
      .toV3Single()
      .subscribeOn(Schedulers.io())
      .subscribe()
      .addTo(disposables)
  }

  fun readClicked(book: Book) {
    database.bookDao()
      .updateBook(book.copy(isAlreadyRead = !book.isAlreadyRead))
      .toV3Single()
      .subscribeOn(Schedulers.io())
      .subscribe()
      .addTo(disposables)
  }
}
