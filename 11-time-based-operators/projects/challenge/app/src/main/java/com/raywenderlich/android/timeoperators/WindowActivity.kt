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

package com.raywenderlich.android.timeoperators

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.raywenderlich.android.timeoperators.utils.timer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_window.*
import java.util.concurrent.TimeUnit

class WindowActivity : AppCompatActivity() {
  private val elementsPerSecond = 3
  private val windowTimeSpan = 4L
  private val windowMaxCount = 10L

  private val compositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_window)

    val sourceObservable = PublishSubject.create<String>()

    timer(elementsPerSecond) {
      sourceObservable.onNext("🐱")
    }.addTo(compositeDisposable)

    sourceObservable.subscribe(windowSource)

    // Extract the windowed observable itself

    val windowedObservable = sourceObservable
        .window(windowTimeSpan, TimeUnit.SECONDS, AndroidSchedulers.mainThread(), windowMaxCount)

    // the marbleview observable produces a new marble view every time
    // windowedObservable produces a new observable
    val marbleViewObservable = windowedObservable
        .doOnNext {
          val marbleView = MarbleView(this)
          marble_views.addView(marbleView)
        }
        .map { marble_views.getChildAt(marble_views.childCount - 1) as MarbleView }

    // Take one of each, guaranteeing that we get the observable
    // produced by window AND the latest timeline view

    Observables.zip(windowedObservable, marbleViewObservable) { obs, marbleView ->
      obs to marbleView
    }.flatMap { (obs, marbleView) ->
          obs
              .map { value -> marbleView to (value as String?) }
              .concatWith(Observable.just(marbleView to null))
        }
        .subscribe { (marbleView, value) ->
          if (value != null) {
            marbleView.onNext(value)
          } else {
            marbleView.onComplete()
          }
        }
        .addTo(compositeDisposable)
  }

  override fun onDestroy() {
    super.onDestroy()
    compositeDisposable.dispose()
  }
}
