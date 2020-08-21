package com.raywenderlich.android.hexcolor

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    val viewModel = ViewModelProvider(this, object : ViewModelProvider.NewInstanceFactory() {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ColorViewModel(Schedulers.io(), AndroidSchedulers.mainThread(), ColorApi, ColorCoordinator()) as T
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
      animateColorChange(it)
    })

    color_name.clicks().subscribe {
      val bottomSheetDialog = ColorBottomSheet.newInstance(hex.text.toString())
      bottomSheetDialog.show(supportFragmentManager, "Custom Bottom Sheet")
    }.addTo(disposables)
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

