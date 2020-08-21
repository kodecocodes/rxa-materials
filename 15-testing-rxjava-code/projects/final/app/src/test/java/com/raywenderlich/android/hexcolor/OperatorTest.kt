
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

package com.raywenderlich.android.hexcolor

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit

class OperatorTest {

  @Test
  fun `test concat`() {
    val observableA = Observable.just(1)
    val observableB = Observable.just(2)
    val observableC = observableA.concatWith(observableB)
    observableC.test()
      .assertResult(1, 2)
      .assertComplete()
  }

  @Test
  fun `test amb`() {
    val scheduler = TestScheduler()

    // 1
    val observableA = Observable.interval(1, TimeUnit.SECONDS, scheduler)
      .take(3)
      .map { 5 * it }
    val observableB = Observable.interval(500, TimeUnit.MILLISECONDS, scheduler)
      .take(3)
      .map { 10 * it }

    // 2
    val ambObservable = observableA.ambWith(observableB)

    // 3
    val testObserver = ambObservable.test()

    scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
    testObserver.assertValueCount(1)

    scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
    testObserver.assertValueCount(3)
    testObserver.assertResult(0L, 10L, 20L)
    testObserver.assertComplete()
  }

  @Test
  fun `using trampoline schedulers`() {
    val observableA = Observable.just(1)
      .subscribeOn(TrampolineScheduler.instance())
    val observableB = Observable.just(1)
      .subscribeOn(Schedulers.io())

    observableA.test().assertResult(1)
    observableB.test().assertEmpty()
  }
}
