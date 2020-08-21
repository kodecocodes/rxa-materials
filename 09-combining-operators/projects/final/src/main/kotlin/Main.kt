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
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {

  exampleOf("startWith") {

    val subscriptions = CompositeDisposable()
    val missingNumbers = Observable.just(3, 4, 5)
    val completeSet = missingNumbers.startWithIterable(listOf(1, 2))

    completeSet
        .subscribe { number ->
          println(number)
        }
        .addTo(subscriptions)
  }

  exampleOf("concat") {

    val subscriptions = CompositeDisposable()
    val first = Observable.just(1, 2, 3, 4)
    val second = Observable.just(4, 5, 6)
    Observable.concat(first, second)
        .subscribe { number ->
          println(number)
        }
        .addTo(subscriptions)
  }

  exampleOf("concatWith") {

    val subscriptions = CompositeDisposable()

    val germanCities = Observable.just("Berlin", "Münich", "Frankfurt")
    val spanishCities = Observable.just("Madrid", "Barcelona", "Valencia")


    germanCities
        .concatWith(spanishCities)
        .subscribe { number ->
          println(number)
        }
        .addTo(subscriptions)
  }

  exampleOf("concatMap") {
    val subscriptions = CompositeDisposable()

    val countries = Observable.just("Germany", "Spain")

    val observable = countries
        .concatMap {
          when (it) {
            "Germany" -> Observable.just("Berlin", "Münich", "Frankfurt")
            "Spain" -> Observable.just("Madrid", "Barcelona", "Valencia")
            else -> Observable.empty<String>()
          }
        }


    observable
        .subscribe { city ->
          println(city)
        }
        .addTo(subscriptions)
  }

  exampleOf("merge") {

    val subscriptions = CompositeDisposable()

    val left = PublishSubject.create<Int>()
    val right = PublishSubject.create<Int>()

    Observable.merge(left, right)
        .subscribe {
          println(it)
        }
        .addTo(subscriptions)

    left.onNext(0)
    left.onNext(1)
    right.onNext(3)
    left.onNext(4)
    right.onNext(5)
    right.onNext(6)
  }

  exampleOf("mergeWith") {

    val subscriptions = CompositeDisposable()

    val germanCities = PublishSubject.create<String>()
    val spanishCities = PublishSubject.create<String>()

    germanCities.mergeWith(spanishCities)
        .subscribe {
          println(it)
        }
        .addTo(subscriptions)

    germanCities.onNext("Frankfurt")
    germanCities.onNext("Berlin")
    spanishCities.onNext("Madrid")
    germanCities.onNext("Münich")
    spanishCities.onNext("Barcelona")
    spanishCities.onNext("Valencia")
  }

  exampleOf("combineLatest") {

    val subscriptions = CompositeDisposable()

    val left = PublishSubject.create<String>()
    val right = PublishSubject.create<String>()

    Observables.combineLatest(left, right) { leftString, rightString ->
      "$leftString $rightString"
    }.subscribe {
      println(it)
    }.addTo(subscriptions)

    left.onNext("Hello")
    right.onNext("World")
    left.onNext("It's nice to")
    right.onNext("be here!")
    left.onNext("Actually, it's super great to")
  }

  exampleOf("zip") {

    val subscriptions = CompositeDisposable()

    val left = PublishSubject.create<String>()
    val right = PublishSubject.create<String>()

    Observables.zip(left, right) { weather, city ->
      "It's $weather in $city"
    }.subscribe {
      println(it)
    }.addTo(subscriptions)

    left.onNext("sunny")
    right.onNext("Lisbon")
    left.onNext("cloudy")
    right.onNext("Copenhagen")
    left.onNext("cloudy")
    right.onNext("London")
    left.onNext("sunny")
    right.onNext("Madrid")
    right.onNext("Vienna")
  }

  exampleOf("withLatestFrom") {
    val subscriptions = CompositeDisposable()

    val button = PublishSubject.create<Unit>()
    val editText = PublishSubject.create<String>()

    button.withLatestFrom(editText) { _: Unit, value: String ->
      value
    }.subscribe {
      println(it)
    }.addTo(subscriptions)

    editText.onNext("Par")
    editText.onNext("Pari")
    editText.onNext("Paris")
    button.onNext(Unit)
    button.onNext(Unit)
  }

  exampleOf("sample") {
    val subscriptions = CompositeDisposable()

    val button = PublishSubject.create<Unit>()
    val editText = PublishSubject.create<String>()

    editText.sample(button)
        .subscribe {
          println(it)
        }.addTo(subscriptions)

    editText.onNext("Par")
    editText.onNext("Pari")
    editText.onNext("Paris")
    button.onNext(Unit)
    button.onNext(Unit)
  }

  exampleOf("amb") {

    val subscriptions = CompositeDisposable()

    val left = PublishSubject.create<String>()
    val right = PublishSubject.create<String>()


    left.ambWith(right)
        .subscribe {
          println(it)
        }
        .addTo(subscriptions)

    left.onNext("Lisbon")
    right.onNext("Copenhagen")
    left.onNext("London")
    left.onNext("Madrid")
    right.onNext("Vienna")
  }

  exampleOf("reduce") {

    val subscriptions = CompositeDisposable()

    val source = Observable.just(1, 3, 5, 7, 9)
    source
        .reduce(0) { a, b -> a + b }
        .subscribeBy(onSuccess = {
          println(it)
        })
        .addTo(subscriptions)
  }

  exampleOf("scan") {

    val subscriptions = CompositeDisposable()

    val source = Observable.just(1, 3, 5, 7, 9)

    source
        .scan(0) { a, b -> a + b }
        .subscribe {
          println(it)
        }
        .addTo(subscriptions)
  }
}
