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

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

private const val REPO = "repo"
private const val NAME = "name"
private const val IMAGE_URL = "imageUrl"
private const val ACTION = "action"

class EventTypeAdapter : TypeAdapter<Event>() {
  @Throws(IOException::class)
  override fun write(out: JsonWriter, event: Event) {
    out.beginObject()
    out.name(REPO).value(event.repo)
    out.name(NAME).value(event.name)
    out.name(IMAGE_URL).value(event.imageUrl)
    out.name(ACTION).value(event.action)
    out.endObject()
  }

  @Throws(IOException::class)
  override fun read(reader: JsonReader): Event {
    var repo = ""
    var name = ""
    var imageUrl = ""
    var action = ""

    reader.beginObject()
    while (reader.hasNext()) {
      when (reader.nextName()) {
        REPO -> repo = reader.nextString()
        NAME -> name = reader.nextString()
        IMAGE_URL -> imageUrl = reader.nextString()
        ACTION -> action = reader.nextString()
      }
    }
    reader.endObject()

    return Event(repo, name, imageUrl, action)
  }
}
