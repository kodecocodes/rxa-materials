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

package com.raywenderlich.android.schedulers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class SchedulersActivity : AppCompatActivity() {

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_schedulers)

    val animal = BehaviorSubject.createDefault("[dog]")

    animal
      .subscribeOn(AndroidSchedulers.mainThread())
      .dump()
      .observeOn(Schedulers.io())
      .dumpingSubscription()
      .addTo(disposables)

    val fruit = Observable.create<String> { observer ->
      observer.onNext("[apple]")
      Thread.sleep(2000)
      observer.onNext("[pineapple]")
      Thread.sleep(2000)
      observer.onNext("[strawberry]")
    }

    fruit
      .subscribeOn(Schedulers.io())
      .dump()
      .observeOn(AndroidSchedulers.mainThread())
      .dumpingSubscription()
      .addTo(disposables)

    val animalsThread = Thread {
      Thread.sleep(3000)
      animal.onNext("[cat]")
      Thread.sleep(3000)
      animal.onNext("[tiger]")
      Thread.sleep(3000)
      animal.onNext("[fox]")
      Thread.sleep(3000)
      animal.onNext("[leopard]")
    }

    animalsThread.name = "Animals Thread"
    animalsThread.start()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }
}
