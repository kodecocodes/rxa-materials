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

package com.raywenderlich.android.gitfeed

import android.content.Context
import androidx.annotation.LayoutRes
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.*


typealias AnyDict = Map<String, Any>

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun getGson(): Gson {
  val builder = GsonBuilder()
  builder.registerTypeAdapter(Event::class.java, EventTypeAdapter())
  return builder.create()
}

private fun eventsDirectory() = GitFeedApplication.getAppContext().getDir("events", Context.MODE_PRIVATE)

fun eventsFile() = File(eventsDirectory(), "events.txt")

fun eventsOutputStream(): FileOutputStream {
  return FileOutputStream(eventsFile())
}

fun eventsInputStream(): FileInputStream {
  return FileInputStream(eventsFile())
}

fun lastModifiedFile() = File(eventsDirectory(), "modified.txt")

fun lastModifiedOutputStream(): FileOutputStream {
  return FileOutputStream(lastModifiedFile())
}

fun lastModifiedInputStream(): FileInputStream {
  return FileInputStream(lastModifiedFile())
}


@Throws(IOException::class)
private fun convertStreamToString(inputStream: InputStream): String {
  val reader = BufferedReader(InputStreamReader(inputStream))
  val sb = StringBuilder()
  var line: String? = reader.readLine()
  while (line != null) {
    sb.append(line).append("\n")
    line = reader.readLine()
  }
  reader.close()
  return sb.toString()
}

object EventsStore {

  fun saveEvents(events: List<Event>) {
    val gson = getGson()
    try {
      val json = gson.toJson(events)
      val eventsStream = eventsOutputStream()
      eventsStream.write(json.toByteArray())
      eventsStream.close()
    } catch (e: IOException) {
      Log.e("OutputError", "Error saving events")
    }
  }

  fun readEvents(): List<Event>? {
    val gson = getGson()
    val eventListType = object : TypeToken<ArrayList<Event>>() {}.type

    try {
      val json = convertStreamToString(eventsInputStream())
      return gson.fromJson<List<Event>>(json, eventListType)
    } catch (e: IOException) {
      Log.e("Input Error", "Error reading events")
    }

    return null
  }

  fun saveLastModified(lastModified: String) {
    try {
      val lastModifiedStream = lastModifiedOutputStream()
      lastModifiedStream.write(lastModified.toByteArray())
      lastModifiedStream.close()
    } catch (e: IOException) {
      Log.e("OutputError", "Error saving last modified")
    }
  }

  fun readLastModified(): String? {
    if (!lastModifiedFile().exists()) {
      return null
    }

    return try {
      convertStreamToString(lastModifiedInputStream())
    } catch (e: IOException) {
      Log.e("Input Error", "Error reading last modified")
      null
    }
  }
}
