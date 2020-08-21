package com.raywenderlich.android.quicktodo.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> AppCompatActivity.buildViewModel(crossinline viewModelFactory: () -> T): T {
  return ViewModelProvider(this, object : ViewModelProvider.NewInstanceFactory() {
    override fun <A : ViewModel?> create(modelClass: Class<A>): A {
      return viewModelFactory() as A
    }
  }).get(T::class.java)
}
