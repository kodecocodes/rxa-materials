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

package com.raywenderlich.android.wundercast

import android.location.Location
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import retrofit2.Response

object WeatherApi {
  val API = "https://api.openweathermap.org/data/2.5/"
  val apiKey = BehaviorSubject.createDefault("INSERT_YOUR_API_KEY_HERE")

  private val weather by lazy {
    WeatherService.create()
  }

  fun getWeather(city: String): Single<Weather> {
    return weather.getWeather(city, apiKey.value)
        .flatMap(this::weatherResponseObservable)
  }

  fun getWeather(location: Location): Single<Weather> {
    return weather.getWeather(location.latitude, location.longitude, apiKey.value)
        .flatMap(this::weatherResponseObservable)
  }

  private fun weatherResponseObservable(response: Response<WeatherNetworkModel>): Single<Weather> {
    return if (!response.isSuccessful) {
      Single.error(IllegalStateException(response.message()))
    } else {
      Single.just(response.body()!!).map {
        val weather = it.toWeather()
        weather.copy(icon = iconNameToChar(weather.icon))
      }
    }
  }

  fun iconNameToChar(icon: String): String {
    return when (icon) {
      "01d" -> "\uf11b"
      "01n" -> "\uf110"
      "02d" -> "\uf112"
      "02n" -> "\uf104"
      "03d", "03n" -> "\uf111"
      "04d", "04n" -> "\uf111"
      "09d", "09n" -> "\uf116"
      "10d", "10n" -> "\uf113"
      "11d", "11n" -> "\uf10d"
      "13d", "13n" -> "\uf119"
      "50d", "50n" -> "\uf10e"
      else -> "E"
    }
  }

  sealed class NetworkResult {
    class Success(val weather: Weather) : NetworkResult()
    class Failure(val error: NetworkError) : NetworkResult()
  }

  sealed class NetworkError : Exception() {
    object ServerFailure : NetworkError()
    object CityNotFound : NetworkError()
  }
}
