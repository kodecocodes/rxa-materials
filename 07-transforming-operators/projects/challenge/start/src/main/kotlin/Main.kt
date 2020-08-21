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

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

fun main(args: Array<String>) {

  exampleOf("Challenge 1") {

    val subscriptions = CompositeDisposable()

    val contacts = mapOf(
        "603-555-1212" to "Florent",
        "212-555-1212" to "Junior",
        "408-555-1212" to "Marin",
        "617-555-1212" to "Scott")

    val convert: (String) -> Int = { value ->
      val number = try {
        value.toInt()
      } catch (e: NumberFormatException) {
        val keyMap = mapOf(
            "abc" to 2, "def" to 3, "ghi" to 4, "jkl" to 5,
            "mno" to 6, "pqrs" to 7, "tuv" to 8, "wxyz" to 9)

        keyMap.filter { it.key.contains(value.toLowerCase()) }.map { it.value }.first()
      }

      if (number < 10) {
        number
      } else {
        sentinel // RxJava 3 does not allow null in stream, so return sentinel value
      }
    }

    val format: (List<Int>) -> String = { inputs ->
      val phone = inputs.map { it.toString() }.toMutableList()
      phone.add(3, "-")
      phone.add(7, "-")
      phone.joinToString("")
    }

    val dial: (String) -> String = { phone ->
      val contact = contacts[phone]
      if (contact != null) {
        "Dialing $contact ($phone)..."
      } else {
        "Contact not found"
      }
    }

    val input = BehaviorSubject.createDefault<String>("$sentinel")

    // Add your code here


    input.onNext("617")
    input.onNext("0")
    input.onNext("408")

    input.onNext("6")
    input.onNext("212")
    input.onNext("0")
    input.onNext("3")

    "JKL1A1B".forEach {
      input.onNext(it.toString()) // Need toString() or else Char conversion is done
    }

    input.onNext("9")
  }
}