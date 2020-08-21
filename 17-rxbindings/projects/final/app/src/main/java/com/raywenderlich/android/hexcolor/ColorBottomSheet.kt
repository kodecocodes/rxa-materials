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
 *
 */
package com.raywenderlich.android.hexcolor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.android.synthetic.main.dialog_color.*

class ColorBottomSheet : BottomSheetDialogFragment() {
  private val disposables = CompositeDisposable()

  companion object {
    fun newInstance(colorString: String): ColorBottomSheet {
      val bundle = Bundle().apply {
        putString("ColorString", colorString)
      }
      return ColorBottomSheet().apply {
        arguments = bundle
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.dialog_color, container, true)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val viewModel = ViewModelProvider(this, object : ViewModelProvider.NewInstanceFactory() {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val colorString = arguments?.getString("ColorString") ?: ""
        return ColorBottomSheetViewModel(colorString) as T
      }
    }).get(ColorBottomSheetViewModel::class.java)

    hex_input.textChanges()
        .map { it.toString() }
        .subscribe { viewModel.onTextChange(it) }
        .addTo(disposables)

    viewModel.showLoadingLiveData.observe(viewLifecycleOwner, Observer {
      loading.visibility = if (it) View.VISIBLE else View.GONE
    })
    viewModel.closestColorLiveData.observe(viewLifecycleOwner, Observer {
      closest_color_hex.text = it
    })
    viewModel.colorNameLiveData.observe(viewLifecycleOwner, Observer {
      color_name.text = it
    })
  }

  override fun onStop() {
    super.onStop()
    disposables.clear()
  }
}
