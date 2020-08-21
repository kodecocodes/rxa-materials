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
package com.raywenderlich.android.quicktodo.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.android.quicktodo.model.TaskItem
import com.raywenderlich.android.quicktodo.repository.RoomTaskRepository
import com.raywenderlich.android.quicktodo.repository.TaskRepository
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class TodoListViewModel(
  repository: TaskRepository,
  backgroundScheduler: Scheduler,
  computationScheduler: Scheduler
) : ViewModel() {
  private val taskClicks = PublishSubject.create<TaskItem>()
  private val addClicks = PublishSubject.create<Unit>()
  private val taskDoneToggles = PublishSubject.create<Pair<TaskItem, Boolean>>()
  private val removeStream = PublishSubject.create<TaskItem>()
  val listItemsLiveData = MutableLiveData<List<TodoListItem>>()
  val statisticsLiveData = MutableLiveData<String>()
  val showEditTaskLiveData = MutableLiveData<Int>()

  private val disposables = CompositeDisposable()

  init {
    val taskStream = repository
      .taskStream()
      .cache()

    taskStream
      .map { tasks -> tasks.map { TodoListItem.TaskListItem(it) } }
      .map { listItems ->
        val finishedTasks = listItems.filter { it.task.isDone }
        val todoTasks = listItems - finishedTasks
        listOf(
          TodoListItem.DueTasks,
          *todoTasks.toTypedArray(),
          TodoListItem.DoneTasks,
          *finishedTasks.toTypedArray()
        )
      }
      .subscribeOn(backgroundScheduler)
      .subscribe(listItemsLiveData::postValue)
      .addTo(disposables)

    taskStream
      .map { tasks ->
        val unfinishedItems = tasks.filter { !it.isDone }
        "${tasks.size} tasks, ${unfinishedItems.size} due"
      }
      .subscribe(statisticsLiveData::postValue)
      .addTo(disposables)

    addClicks
      .throttleFirst(1, TimeUnit.SECONDS, computationScheduler)
      .subscribe { showEditTaskLiveData.postValue(RoomTaskRepository.INVALID_ID) }
      .addTo(disposables)

    taskClicks
      .throttleFirst(1, TimeUnit.SECONDS, computationScheduler)
      .subscribe {
        showEditTaskLiveData.postValue(it.id ?: RoomTaskRepository.INVALID_ID)
      }
      .addTo(disposables)

    taskDoneToggles
      .flatMap {
        repository
          .insertTask(it.first.copy(isDone = it.second))
          .toObservable()
          .subscribeOn(backgroundScheduler)
      }
      .subscribe()
      .addTo(disposables)

    removeStream
      .flatMapCompletable {
        repository
          .deleteTask(it)
          .subscribeOn(backgroundScheduler)
      }
      .subscribe()
      .addTo(disposables)
  }

  fun addClicked() = addClicks.onNext(Unit)

  fun taskClicked(taskItem: TaskItem) = taskClicks.onNext(taskItem)

  fun taskDoneToggled(taskItem: TaskItem, on: Boolean) = taskDoneToggles.onNext(Pair(taskItem, on))

  fun taskSwiped(taskItem: TaskItem) = removeStream.onNext(taskItem)

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
