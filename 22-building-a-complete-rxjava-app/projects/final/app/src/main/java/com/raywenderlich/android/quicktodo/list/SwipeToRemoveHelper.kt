package com.raywenderlich.android.quicktodo.list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.quicktodo.model.TaskItem
import io.reactivex.rxjava3.subjects.PublishSubject

class SwipeToRemoveHelper(private val adapter: TodoAdapter) :
  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {

  private val swipeSubject = PublishSubject.create<TaskItem>()
  val swipeStream = swipeSubject.hide()

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    return false
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//    val position = viewHolder.adapterPosition
//    val item = adapter.getListItem(position) as TodoListItem.TaskListItem
//    swipeSubject.onNext(item.task)
  }

  override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
//    val position = viewHolder.adapterPosition
//    val item = adapter.getListItem(position)
//    return if (item is TodoListItem.TaskListItem) {
//      makeMovementFlags(0, ItemTouchHelper.START or ItemTouchHelper.END)
//    } else {
//      makeMovementFlags(0, 0)
//    }
      return 0
  }
}
