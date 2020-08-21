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
package com.raywenderlich.android.quicktodo.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import com.raywenderlich.android.quicktodo.R
import com.raywenderlich.android.quicktodo.database.TaskRoomDatabase
import com.raywenderlich.android.quicktodo.repository.RoomTaskRepository
import com.raywenderlich.android.quicktodo.utils.buildViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_edit_todo.*

class EditTaskActivity : AppCompatActivity() {
  private val disposables = CompositeDisposable()

  companion object {
    const val TASK_ID_KEY = "taskIdKey"
    fun launch(context: Context, taskId: Int) {
      val intent = Intent(context, EditTaskActivity::class.java)
      intent.putExtra(TASK_ID_KEY, taskId)
      context.startActivity(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_todo)

    val viewModel = buildViewModel {
      val repository = RoomTaskRepository(TaskRoomDatabase.fetchDatabase(this))
      val taskIdKey = intent.getIntExtra(TASK_ID_KEY, RoomTaskRepository.INVALID_ID)
      EditTaskViewModel(
        repository,
        Schedulers.io(),
        taskIdKey
      )
    }

    done.clicks().subscribe { viewModel.onFinishClicked() }.addTo(disposables)
    title_input.textChanges().subscribe { viewModel.onTextChanged(it) }.addTo(disposables)

    viewModel.textLiveData.observe(this, Observer(title_input::append))
    viewModel.finishLiveData.observe(this, Observer { finish() })
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }
}
