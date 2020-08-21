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
  val listItemsLiveData = MutableLiveData<List<TodoListItem>>()
  val showEditTaskLiveData = MutableLiveData<Int>()

  private val disposables = CompositeDisposable()

  init {
    repository
      .taskStream()
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

    taskDoneToggles
      .flatMapSingle { newItemPair ->
        repository
          .insertTask(newItemPair.first.copy(isDone = newItemPair.second))
          .subscribeOn(backgroundScheduler)
      }
      .subscribe()
      .addTo(disposables)

    taskClicks
      .throttleFirst(1, TimeUnit.SECONDS, computationScheduler)
      .subscribe {
        val id = it.id ?: RoomTaskRepository.INVALID_ID
        showEditTaskLiveData.postValue(id)
      }
      .addTo(disposables)

    addClicks
      .throttleFirst(1, TimeUnit.SECONDS, computationScheduler)
      .subscribe { showEditTaskLiveData.postValue(RoomTaskRepository.INVALID_ID) }
      .addTo(disposables)
  }

  fun addClicked() = addClicks.onNext(Unit)

  fun taskClicked(taskItem: TaskItem) = taskClicks.onNext(taskItem)

  fun taskDoneToggled(taskItem: TaskItem, on: Boolean) = taskDoneToggles.onNext(Pair(taskItem, on))

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
