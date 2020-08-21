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

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.jakewharton.rxbinding4.view.clicks
import com.raywenderlich.android.hexcolor.networking.ColorApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_color.*


class ColorActivity : AppCompatActivity() {

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_color)

    val preferences = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(this))
    val viewModel = ViewModelProvider(this, object : ViewModelProvider.NewInstanceFactory() {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ColorViewModel(Schedulers.io(), AndroidSchedulers.mainThread(), ColorApi, ColorCoordinator(), preferences) as T
      }
    }).get(ColorViewModel::class.java)

    val digits = listOf(zero, one, two, three, four, five, six, seven, eight, nine, A, B, C, D, E, F)
        .map { digit -> digit.clicks().map { digit.text.toString() } }
    val digitStreams = Observable.merge(digits)

    back.clicks().subscribe { viewModel.backClicked() }.addTo(disposables)
    clear.clicks().subscribe { viewModel.clearClicked() }.addTo(disposables)
    digitStreams.subscribe(viewModel::digitClicked).addTo(disposables)

    viewModel.hexStringLiveData.observe(this, Observer {
      hex.text = it
    })
    viewModel.rgbStringLiveData.observe(this, Observer {
      rgb.text = it
    })
    viewModel.colorNameLiveData.observe(this, Observer {
      color_name.text = it
    })
    viewModel.backgroundColorLiveData.observe(this, Observer {
      if(it != -1){
        animateColorChange(it)
      } else{
          showError(getString(R.string.no_color_error))
      }
    })

    color_name.setOnClickListener {
      val bottomSheetDialog = ColorBottomSheet.newInstance(hex.text.toString())
      bottomSheetDialog.show(supportFragmentManager, "Custom Bottom Sheet")
    }
  }

  private fun showError(error: String){
    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
  }

  private fun animateColorChange(newColor: Int) {
    val colorFrom = (root_layout.background as ColorDrawable).color
    colorAnimator(colorFrom, newColor)
      .subscribe { color ->
        root_layout.setBackgroundColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          window.statusBarColor = color
        }
      }
      .addTo(disposables)
  }

  override fun onStop() {
    super.onStop()
    disposables.clear()
  }
}
