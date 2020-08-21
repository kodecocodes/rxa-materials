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

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.*
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

fun lastKnownLocation(context: Context): Maybe<Location> {
  if (ContextCompat.checkSelfPermission(context,
      Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
    return Maybe.create { emitter ->
      val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
      fusedLocationClient.requestLocationUpdates(LocationRequest(), object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
          if (locationResult != null && locationResult.lastLocation != null) {
            emitter.onSuccess(locationResult.lastLocation)
            fusedLocationClient.removeLocationUpdates(this)
          }
        }
      }, null)
    }
  } else {
    return Maybe.empty()
  }
}

fun Weather.save(filesDirectory: File): Completable {
  return Completable.create { emitter ->
    val path = filesDirectory.absolutePath + "/" + "weather.txt"
    val serializedWeather = Gson().toJson(this)
    val stream = FileOutputStream(File(path))
    stream.use {
      it.write(serializedWeather.toByteArray())
      emitter.onComplete()
    }
  }
}

fun readLastWeather(filesDirectory: File): Maybe<Weather> {
  return Maybe.create { emitter ->
    val path = filesDirectory.absolutePath + "/" + "weather.txt"
    val file = File(path)
    try {
      val inputStream = FileInputStream(File(path))
      inputStream.use {
        val bytes = ByteArray(file.length().toInt())
        it.read(bytes)
        val weather = Gson().fromJson<Weather>(String(bytes), Weather::class.java)
        emitter.onSuccess(weather)
      }
    } catch (ex: FileNotFoundException) {
      emitter.onComplete()
    } catch (ex: Exception) {
      emitter.onError(ex)
    }
  }
}

fun connectivityStream(context: Context): Observable<NetworkState> {
  return Observable.create { emitter ->
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (getIsConnected(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)) {
      emitter.onNext(NetworkState.CONNECTED)
    } else {
      emitter.onNext(NetworkState.DISCONNECTED)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      cm.registerNetworkCallback(
        NetworkRequest.Builder()
          .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
          .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
          .build(),
        object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) {
            emitter.onNext(NetworkState.CONNECTED)
          }
        })
    } else {
      @Suppress("DEPRECATION")
      context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          val networkInfo = intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
          val newlyConnected = networkInfo.isConnectedOrConnecting
          if (newlyConnected) {
            emitter.onNext(NetworkState.CONNECTED)
          } else {
            emitter.onNext(NetworkState.DISCONNECTED)
          }
        }
      }, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }
  }
}

@Suppress("DEPRECATION")
private fun getIsConnected(connectivityManager: ConnectivityManager): Boolean {
  var isConnected = false
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    val networkCapabilities = connectivityManager.activeNetwork ?: null
    val actNw =
      connectivityManager.getNetworkCapabilities(networkCapabilities)
        ?: null
    isConnected = if (actNw != null) {
      when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
      }
    } else false
  } else {
    connectivityManager.run {
      connectivityManager.activeNetworkInfo?.run {
        isConnected = when (type) {
          ConnectivityManager.TYPE_WIFI -> true
          ConnectivityManager.TYPE_MOBILE -> true
          ConnectivityManager.TYPE_ETHERNET -> true
          else -> false
        }

      }
    }
  }
  return isConnected
}

enum class NetworkState {
  CONNECTED,
  DISCONNECTED
}
