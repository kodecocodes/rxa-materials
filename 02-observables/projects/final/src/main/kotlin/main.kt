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

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.roundToInt
import kotlin.text.Charsets.UTF_8

fun main(args: Array<String>) {

  exampleOf("just") {
    val observable: Observable<Int> = Observable.just(1)
  }

  exampleOf("fromIterable") {
    val observable: Observable<Int> = Observable.fromIterable(listOf(1, 2, 3))
  }

  exampleOf("subscribe") {

    val observable = Observable.just(1, 2, 3)

    observable.subscribe { element ->
      println(element)
    }
  }

  exampleOf("empty") {

    val observable = Observable.empty<Unit>()

    observable.subscribeBy(
        onNext = { println(it) },
        onComplete = { println("Completed") }
    )
  }

  exampleOf("never") {

    val observable = Observable.never<Any>()

    observable.subscribeBy(
        onNext = { println(it) },
        onComplete = { println("Completed") }
    )
  }

  exampleOf("range") {
    val observable = Observable.range(1, 10)

    observable.subscribe {
      val n = it.toDouble()
      val fibonacci = ((Math.pow(1.61803, n) - Math.pow(0.61803, n)) / 2.23606).roundToInt()
      println(fibonacci)
    }
  }

  exampleOf("dispose") {

    val mostPopular: Observable<String> = Observable.just("A", "B", "C")

    val subscription = mostPopular.subscribe {
      println(it)
    }

    subscription.dispose()
  }

  exampleOf("CompositeDisposable") {

    val subscriptions = CompositeDisposable()
    val disposable = Observable.just("A", "B", "C")
        .subscribe {
          println(it)
        }

    subscriptions.add(disposable)

    subscriptions.dispose()
  }

  exampleOf("create") {
    Observable.create<String> { emitter ->
      emitter.onNext("1")

//    emitter.onError(RuntimeException("Error"))
//    emitter.onComplete()

      emitter.onNext("?")
    }.subscribeBy(
        onNext = { println(it) },
        onComplete = { println("Completed") },
        onError = { println("Error") }
    )
  }

  exampleOf("defer") {

    val disposables = CompositeDisposable()

    var flip = false

    val factory: Observable<Int> = Observable.defer {
      flip = !flip

      if (flip) {
        Observable.just(1, 2, 3)
      } else {
        Observable.just(4, 5, 6)
      }
    }

    for (i in 0..3) {
      disposables.add(
          factory.subscribe {
            println(it)
          }
      )
    }

    disposables.dispose()
  }

  exampleOf("Single") {
    val subscriptions = CompositeDisposable()

    fun loadText(filename: String): Single<String> {
      return Single.create create@{ emitter ->
        val file = File(filename)

        if (!file.exists()) {
          emitter.onError(FileNotFoundException("Can't find $filename"))
          return@create
        }

        val contents = file.readText(UTF_8)

        emitter.onSuccess(contents)
      }
    }

    val observer = loadText("Copyright.txt")
        .subscribeBy(
            onSuccess = { println(it) },
            onError = { println("Error, $it") }
        )

    subscriptions.add(observer)
  }
}
