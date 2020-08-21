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
package com.raywenderlich.android.quicktodo.repository

import com.raywenderlich.android.quicktodo.database.TaskDatabase
import com.raywenderlich.android.quicktodo.model.TaskItem
import com.raywenderlich.android.quicktodo.utils.toV3Maybe
import com.raywenderlich.android.quicktodo.utils.toV3Observable
import com.raywenderlich.android.quicktodo.utils.toV3Single
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class RoomTaskRepository(private val database: TaskDatabase): TaskRepository {

  companion object {
    const val INVALID_ID = -1
  }

  override fun insertTask(taskItem: TaskItem): Single<Long> {
    val validIdTask = if (taskItem.id == INVALID_ID) {
      taskItem.copy(id = null)
    } else {
      taskItem
    }

    return database.taskDao().insertTask(validIdTask).toV3Single()
  }

  override fun getTask(id: Int): Maybe<TaskItem> {
    return database.taskDao().fetchTask(id).toV3Maybe()
  }

  override fun taskStream(): Observable<List<TaskItem>> {
    return database.taskDao().taskStream().toV3Observable()
  }
}
