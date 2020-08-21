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
package com.raywenderlich.android.bookcollector.paging

import androidx.paging.PagedList
import com.raywenderlich.android.bookcollector.Book
import com.raywenderlich.android.bookcollector.database.BookDatabase
import com.raywenderlich.android.bookcollector.networking.OpenLibraryApi
import com.raywenderlich.android.bookcollector.toV3Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executors

class BookBoundaryCallback(private val searchTerm: String, private val db: BookDatabase) :
  PagedList.BoundaryCallback<Book>() {
  private val executor = Executors.newSingleThreadExecutor()
  private var currentPage = 1
  private val helper = PagingRequestHelper(executor)

  override fun onZeroItemsLoaded() {
    loadItems(PagingRequestHelper.RequestType.INITIAL)
  }

  override fun onItemAtEndLoaded(itemAtEnd: Book) {
    loadItems(PagingRequestHelper.RequestType.AFTER)
  }

  private fun loadItems(requestType: PagingRequestHelper.RequestType) {
    helper.runIfNotRunning(requestType) { callback ->
      OpenLibraryApi.searchBooks(searchTerm, currentPage)
        .flatMapCompletable { db.bookDao().insertBooks(it).toV3Completable() }
        .subscribeOn(Schedulers.io())
        .subscribe {
          currentPage++
          callback.recordSuccess()
        }
    }
  }
}
