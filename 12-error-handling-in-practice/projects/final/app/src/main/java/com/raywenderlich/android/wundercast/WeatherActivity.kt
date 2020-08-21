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
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_weather.*

class WeatherActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_weather)

    ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        101)

    val model = ViewModelProvider(this, object : ViewModelProvider.Factory {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeatherViewModel(lastKnownLocation(this@WeatherActivity)) as T
      }
    }).get(WeatherViewModel::class.java)

    model.weatherLiveData.observe(this, Observer<Weather> { weather ->
        if (weather != null) {
          updateWeatherReadings(weather)
        }
      })
    model.cityLiveData.observe(this, Observer<String> { cityText ->
      if (cityText!= null) {
        city.setText(cityText)
      }
    })
    model.errorLiveData.observe(this, Observer<String> { error ->
      if (error!= null) {
        Snackbar.make(root, error, Snackbar.LENGTH_SHORT).show()
      }
    })

    val keyEditText = EditText(this)
    key.setOnClickListener {
      AlertDialog.Builder(this)
          .setTitle("ApiKey")
          .setMessage("Add the api key")
          .setView(keyEditText)
          .setCancelable(true)
          .setPositiveButton("Ok") { _, _ ->
            WeatherApi.apiKey.onNext(keyEditText.text.toString())
          }
          .show()
    }

    location.setOnClickListener { model.locationClicked() }
    city.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {}
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        model.cityNameChanged(s!!)
      }
    })
  }

  @SuppressLint("SetTextI18n")
  private fun updateWeatherReadings(weather: Weather) {
    temperature.text = "${weather.temperature} ° C"
    humidity.text = "${weather.humidity}%"
    icon.text = weather.icon
  }
}
