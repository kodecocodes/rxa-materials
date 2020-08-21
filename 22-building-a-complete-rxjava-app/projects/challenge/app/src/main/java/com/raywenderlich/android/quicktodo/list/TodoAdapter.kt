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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.quicktodo.R
import com.raywenderlich.android.quicktodo.model.TaskItem
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.adapter_section_header.view.*
import kotlinx.android.synthetic.main.adapter_todo_item.view.*

class TodoAdapter : ListAdapter<TodoListItem, RecyclerView.ViewHolder>(TodoDiffUtil()) {

  private val taskClickSubject = PublishSubject.create<TaskItem>()
  private val taskToggledSubject = PublishSubject.create<Pair<TaskItem, Boolean>>()
  val taskClickStream = taskClickSubject.hide()
  val taskToggledStream = taskToggledSubject.hide()

  override fun getItemViewType(position: Int): Int {
    return getItem(position).viewType
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      0,
      1 -> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_section_header, parent, false)
        TodoSectionViewHolder(view)
      }
      else -> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_todo_item, parent, false)
        TodoViewHolder(view)
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = getItem(position)
    val resources = holder.itemView.context.resources
    when (item) {
      TodoListItem.DueTasks -> {
        holder.itemView.section_title.text = resources.getString(R.string.due_tasks)
      }
      TodoListItem.DoneTasks -> {
        holder.itemView.section_title.text = resources.getString(R.string.done_tasks)
      }
      is TodoListItem.TaskListItem -> {
        holder.itemView.task_title.text = item.task.text
        holder.itemView.task_done.isChecked = item.task.isDone
        holder.itemView.task_done.setOnClickListener {
          taskToggledSubject.onNext(item.task to holder.itemView.task_done.isChecked)
        }
        holder.itemView.setOnClickListener {
          taskClickSubject.onNext(item.task)
        }
      }
    }
  }

  fun getListItem(position: Int): TodoListItem {
    return getItem(position)
  }
}
