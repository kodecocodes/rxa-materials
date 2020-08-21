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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.android.quicktodo.model.TaskItem
import com.raywenderlich.android.quicktodo.repository.TaskRepository
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class EditTaskViewModel(
  taskRepository: TaskRepository,
  backgroundScheduler: Scheduler,
  taskId: Int
): ViewModel() {

  private val disposables = CompositeDisposable()
  private val finishedClicks = PublishSubject.create<Unit>()
  private val taskTitleTextChanges = BehaviorSubject.create<CharSequence>()
  val finishLiveData = MutableLiveData<Unit>()
  val textLiveData = MutableLiveData<String>()

  init {
    val existingTask = taskRepository.getTask(taskId).cache()
    existingTask
      .subscribeOn(backgroundScheduler)
      .subscribe { textLiveData.postValue(it.text) }
      .addTo(disposables)

    Observables.combineLatest(finishedClicks, taskTitleTextChanges)
      .map { it.second }
      .flatMapSingle { title ->
        existingTask
          .defaultIfEmpty(TaskItem(null, title.toString(), Date(), false))
          .flatMap {
            val task = TaskItem(it.id, title.toString(), Date(), it.isDone)
            taskRepository.insertTask(task)
          }
          .subscribeOn(backgroundScheduler)
      }
      .subscribe { finishLiveData.postValue(Unit) }
      .addTo(disposables)
  }

  fun onFinishClicked() = finishedClicks.onNext(Unit)

  fun onTextChanged(text: CharSequence) = taskTitleTextChanges.onNext(text)

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
