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

import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject

fun main(args: Array<String>) {

  exampleOf("PublishSubject") {

    val publishSubject = PublishSubject.create<Int>()

    publishSubject.onNext(1)

    val subscriptionOne = publishSubject.subscribeBy(
      onNext = { printWithLabel("1)", it) },
      onComplete = { printWithLabel("1)", "Complete") }
    )

    publishSubject.onNext(2)

    val subscriptionTwo = publishSubject.subscribeBy(
      onNext = { printWithLabel("2)", it) },
      onComplete = { printWithLabel("2)", "Complete") }
    )

    publishSubject.onNext(3)

    subscriptionOne.dispose()

    publishSubject.onNext(4)

    publishSubject.onComplete()

    publishSubject.onNext(5)

    subscriptionTwo.dispose()

    val subscriptionThree = publishSubject.subscribeBy(
      onNext = { printWithLabel("3)", it) },
      onComplete = { printWithLabel("3)", "Complete") }
    )

    publishSubject.onNext(6)

    subscriptionThree.dispose()
  }


  exampleOf("BehaviorSubject") {

    val subscriptions = CompositeDisposable()

    val behaviorSubject = BehaviorSubject.createDefault("Initial value")

    behaviorSubject.onNext("X")

    val subscriptionOne = behaviorSubject.subscribeBy(
      onNext = { printWithLabel("1)", it) },
      onError = { printWithLabel("1)", it) }
    )

    subscriptions.add(subscriptionOne)

    behaviorSubject.onError(RuntimeException("Error!"))

    subscriptions.add(behaviorSubject.subscribeBy(
      onNext = { printWithLabel("2)", it) },
      onError = { printWithLabel("2)", it) }
    ))

    subscriptions.dispose()
  }

  exampleOf("BehaviorSubject State") {

    val subscriptions = CompositeDisposable()

    val behaviorSubject = BehaviorSubject.createDefault(0)

    println(behaviorSubject.value)

    subscriptions.add(behaviorSubject.subscribeBy {
      printWithLabel("1)", it)
    })

    behaviorSubject.onNext(1)
    println(behaviorSubject.value)

    subscriptions.dispose()
  }

  exampleOf("ReplaySubject") {

    val subscriptions = CompositeDisposable()

    val replaySubject = ReplaySubject.createWithSize<String>(2)

    replaySubject.onNext("1")

    replaySubject.onNext("2")

    replaySubject.onNext("3")

    subscriptions.add(replaySubject.subscribeBy(
      onNext = { printWithLabel("1)", it) },
      onError = { printWithLabel("1)", it)}
    ))

    subscriptions.add(replaySubject.subscribeBy(
      onNext = { printWithLabel("2)", it) },
      onError = { printWithLabel("2)", it)}
    ))

    replaySubject.onNext("4")

    replaySubject.onError(RuntimeException("Error!"))

    subscriptions.add(replaySubject.subscribeBy(
      onNext = { printWithLabel("3)", it) },
      onError = { printWithLabel("3)", it)}
    ))

    subscriptions.dispose()
  }

  exampleOf("AsyncSubject") {
    val subscriptions = CompositeDisposable()

    val asyncSubject = AsyncSubject.create<Int>()

    subscriptions.add(asyncSubject.subscribeBy(
      onNext = { printWithLabel("1)", it) },
      onComplete = { printWithLabel("1)", "Complete") }
    ))

    asyncSubject.onNext(0)
    asyncSubject.onNext(1)
    asyncSubject.onNext(2)
    asyncSubject.onComplete()

    subscriptions.dispose()
  }

  exampleOf("RxRelay") {
    val subscriptions = CompositeDisposable()

    val publishRelay = PublishRelay.create<Int>()

    subscriptions.add(publishRelay.subscribeBy(
        onNext = { printWithLabel("1)", it) }
    ))

    publishRelay.accept(1)
    publishRelay.accept(2)
    publishRelay.accept(3)
  }
}