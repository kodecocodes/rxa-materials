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

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Flowables
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

  exampleOf("Zipping observable") {
    val fastObservable = Observable.interval(1, TimeUnit.MILLISECONDS)
    val slowObservable = Observable.interval(1, TimeUnit.SECONDS)
    val disposable = Observables.zip(slowObservable, fastObservable)
        .subscribeOn(Schedulers.io())
        .subscribe { (first, second) ->
          println("Got $first and $second")
        }

    safeSleep(5000)
    disposable.dispose()
  }

  exampleOf("Overflowing observer") {
    val disposable = Observable.range(1, 10_000_000)
        .subscribeOn(Schedulers.io())
        .map {
          LongArray(1024 * 8)
        }
        .observeOn(Schedulers.computation())
        .subscribe {
          println("Free memory: ${freeMemory()}")
          safeSleep(100)
        }

    safeSleep(20_000)
    disposable.dispose()
  }

  exampleOf("Zipping flowable") {
    val slowFlowable = Flowable.interval(1, TimeUnit.SECONDS)
    val fastFlowable = Flowable.interval(1, TimeUnit.MILLISECONDS)
        .onBackpressureDrop { println("Dropping $it") }

    val disposable = Flowables.zip(slowFlowable, fastFlowable)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.newThread())
        .subscribe { (first, second) ->
          println("Got $first and $second")
        }

    safeSleep(5000)
    disposable.dispose()
  }

  exampleOf("onBackPressureBuffer") {
    val disposable = Flowable.range(1, 100)
        .subscribeOn(Schedulers.io())
        .onBackpressureBuffer(
            50,
            { println("Buffer overrun; dropping latest") },
            BackpressureOverflowStrategy.DROP_LATEST
        )
        .observeOn(Schedulers.newThread(), false, 1)
        .doOnComplete { println("We're done!") }
        .subscribe {
          println("Integer: $it")
          safeSleep(50)
        }

    safeSleep(1000)
    disposable.dispose()
  }

  exampleOf("onBackPressureLatest") {
    val disposable = Flowable.range(1, 100)
        .subscribeOn(Schedulers.io())
        .onBackpressureLatest()
        .observeOn(Schedulers.newThread(), false, 1)
        .doOnComplete { println("We're done!") }
        .subscribe {
          println("Integer: $it")
          safeSleep(50)
        }

    safeSleep(1000)
    disposable.dispose()
  }
  
  exampleOf("No backpressure") {
    val disposable = Flowable.range(1, 100)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.newThread(), false, 1)
        .doOnComplete { println("We're done!") }
        .subscribe {
          println("Integer: $it")
          safeSleep(50)
        }

    safeSleep(1000)
    disposable.dispose()
  }

  exampleOf("toFlowable") {
    val disposable = Observable.range(1, 100)
        .toFlowable(BackpressureStrategy.DROP)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.newThread(), false, 1)
        .subscribe {
          println("Integer: $it")
          safeSleep(50)
        }

    safeSleep(1000)
    disposable.dispose()
  }

  exampleOf("Processor") {
    val processor = PublishProcessor.create<Int>()

    val disposable = processor
        .onBackpressureDrop { println("Dropping $it") }
        .observeOn(Schedulers.newThread(), false, 1)
        .subscribe {
          println("Integer: $it")
          safeSleep(50)
        }

    Thread().run {
      for (i in 0..100) {
        processor.onNext(i)
        safeSleep(5)
      }
    }

    safeSleep(1000)
    disposable.dispose()
  }
}
