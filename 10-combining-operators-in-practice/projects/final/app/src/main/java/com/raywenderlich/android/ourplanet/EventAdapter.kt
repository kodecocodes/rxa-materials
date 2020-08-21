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

package com.raywenderlich.android.ourplanet

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.raywenderlich.android.ourplanet.model.EOEvent
import com.raywenderlich.android.ourplanet.model.EONET

class EventAdapter(private val events: MutableList<EOEvent>) : androidx.recyclerview.widget.RecyclerView.Adapter<EventAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent.inflate(R.layout.list_item_event))
  }

  override fun getItemCount() = events.size

  fun updateEvents(events: List<EOEvent>?) {
    this.events.clear()
    this.events.addAll(events ?: emptyList())
    notifyDataSetChanged()
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(events[position])
  }

  class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    private val eventTitle = itemView.findViewById<TextView>(R.id.eventTitle)
    private val eventDate = itemView.findViewById<TextView>(R.id.eventDate)
    private val eventDescription = itemView.findViewById<TextView>(R.id.eventDescription)

    fun bind(event: EOEvent) {
      eventTitle.text = event.title
      eventDate.text = if (event.closeDate != null) {
        EONET.displayFormatter.format(event.closeDate)
      } else {
        ""
      }
      eventDescription.visibility = if (event.description.isEmpty()) {
        View.GONE
      } else {
        View.VISIBLE
      }
      eventDescription.text = event.description
    }
  }
}
