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

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.raywenderlich.android.bookcollector.database.BookRoomDatabase
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Completable as Completable2
import io.reactivex.rxjava3.core.Completable as Completable3
import io.reactivex.Single as Single2
import io.reactivex.rxjava3.core.Single as Single3
import io.reactivex.Observable as Observable2
import io.reactivex.rxjava3.core.Observable as Observable3


fun Fragment.mainViewModel(context: Context): MainViewModel {
  return ViewModelProvider(this, object : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      val db = BookRoomDatabase.fetchDatabase(context)
      return MainViewModel(db) as T
    }
  }).get(MainViewModel::class.java)
}

fun Completable2.toV3Completable(): Completable3 {
  return RxJavaBridge.toV3Completable(this)
}

fun <T> Single2<T>.toV3Single(): Single3<T> {
  return RxJavaBridge.toV3Single(this)
}

fun <T> Observable2<T>.toV3Observable(): Observable3<T> {
  return RxJavaBridge.toV3Observable(this)
}
