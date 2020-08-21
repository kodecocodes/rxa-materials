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

package com.raywenderlich.android.combinestagram

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.math.ceil
import kotlin.math.sqrt

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun combineImages(bitmaps: List<Bitmap>): Bitmap? {
  val cs: Bitmap?

  val count = bitmaps.size
  val gridSize = ceil(sqrt(count.toFloat()))
  var numRows = gridSize.toInt()
  val numCols = gridSize.toInt()

  if ((gridSize * gridSize - count) >= gridSize) {
    numRows -= 1
  }

  val bitmap0 = bitmaps[0]
  val width = numCols * bitmap0.width
  val height = numRows * bitmap0.height

  cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

  val comboImage = Canvas(cs)

  for (row in 0 until numRows) {
    for (col in 0 until numCols) {
      val index = row * numCols + col
      if (index < count) {
        val bitmap = bitmaps[row * numCols + col]
        val left = col * bitmap0.width
        val top = row * bitmap0.height
        comboImage.drawBitmap(bitmap, left.toFloat(), top.toFloat(), null)
      }
    }
  }

  return cs
}