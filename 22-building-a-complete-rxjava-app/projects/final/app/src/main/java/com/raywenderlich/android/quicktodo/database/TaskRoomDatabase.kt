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
package com.raywenderlich.android.quicktodo.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.raywenderlich.android.quicktodo.model.TaskItem
import com.raywenderlich.android.quicktodo.utils.toV3Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

object TaskRoomDatabase {
  private var database: TaskDatabase? = null

  fun fetchDatabase(context: Context): TaskDatabase {
    val localDatabaseCopy = database
    return if (localDatabaseCopy != null) {
      localDatabaseCopy
    } else {
      val localDatabase = Room.databaseBuilder(context.applicationContext,
          TaskDatabase::class.java, "book_database")
          .addCallback(object : RoomDatabase.Callback() {
            @SuppressLint("CheckResult")
            override fun onCreate(db: SupportSQLiteDatabase) {
              database!!.taskDao().insertTasks(
                listOf(
                  TaskItem(null, "Chapter 1: Hello, RxJava!", Date(), false),
                  TaskItem(null, "Chapter 2: Observables", Date(), false),
                  TaskItem(null, "Chapter 3: Subjects", Date(), false),
                  TaskItem(null, "Chapter 4: Observables and Subjects in practice", Date(), false),
                  TaskItem(null, "Chapter 5: Filtering operators", Date(), false)
                 )
                )
                .toV3Completable()
                .subscribeOn(Schedulers.io())
                .subscribe()
            }
          })
          .build()
      database = localDatabase
      localDatabase
    }
  }
}
