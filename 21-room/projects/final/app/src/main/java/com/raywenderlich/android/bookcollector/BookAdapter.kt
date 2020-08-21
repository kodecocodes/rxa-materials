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
package com.raywenderlich.android.bookcollector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.synthetic.main.adapter_book_row.view.*

class BookAdapter(private val bookClickListener: BookClickListener) :
  PagedListAdapter<Book, BookViewHolder>(getDiffUtil()) {

  companion object {
    fun getDiffUtil(): DiffUtil.ItemCallback<Book> {
      return object : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
          return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
          return oldItem == newItem
        }
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_book_row, parent, false)
    return BookViewHolder(view)
  }

  override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
    val book = getItem(position) ?: return
    holder.itemView.title.text = book.title
    holder.itemView.author.text = book.authorName
    holder.itemView.subject.text = book.subject
    holder.itemView.publisher.text = book.publisher
    holder.itemView.favorite.setOnClickListener { bookClickListener.favoriteClicked(book) }
    holder.itemView.already_read.setOnClickListener { bookClickListener.alreadyReadClicked(book) }

    val starResource = if (book.isFavorited) R.drawable.ic_star_black_24dp else R.drawable.ic_star_border_black_24dp
    holder.itemView.favorite.setImageResource(starResource)

    val readResource =
      if (book.isAlreadyRead) R.drawable.ic_markunread_black_24dp else R.drawable.ic_mail_outline_black_24dp
    holder.itemView.already_read.setImageResource(readResource)
  }

  interface BookClickListener {
    fun favoriteClicked(book: Book)
    fun alreadyReadClicked(book: Book)
  }
}
