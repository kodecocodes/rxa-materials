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

package com.raywenderlich.android.ourplanet.model

import android.os.Parcelable
import com.raywenderlich.android.ourplanet.AnyMap
import kotlinx.android.parcel.Parcelize
import java.net.URL
import java.util.*

@Parcelize
data class EOEvent(
    val id: String,
    val title: String,
    val description: String,
    val link: URL?,
    val closeDate: Date?,
    val categories: List<Int>
) : Parcelable {
  companion object {
    @Suppress("UNCHECKED_CAST")
    fun fromJson(json: AnyMap): EOEvent? {
      val id = json["id"] as? String
      val title = json["title"] as? String
      val description = json["description"] as? String
      val linkString = json["link"] as? String
      val closeString = json["closed"] as? String
      val categoriesList = json["categories"] as? List<AnyMap>
      if (id == null || title == null || description == null
          || linkString == null || categoriesList == null) {
        return null
      }

      val closeDate = if (closeString == null) {
        null
      } else {
        EONET.formatter.parse(closeString)
      }
      val link = URL(linkString)
      val categories = categoriesList.mapNotNull {
        val categoryId = (it["id"] as? Double)?.toInt()
        categoryId
      }

      return EOEvent(id, title, description, link, closeDate, categories)
    }

    val compareByDates = Comparator<EOEvent> { x, y ->
      if (x.closeDate == null || y.closeDate == null) {
        0
      } else {
        x.closeDate.compareTo(y.closeDate)
      }
    }
  }
}
