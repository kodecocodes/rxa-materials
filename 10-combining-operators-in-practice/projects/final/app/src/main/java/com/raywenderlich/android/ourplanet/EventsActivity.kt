
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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.SeekBar
import com.raywenderlich.android.ourplanet.model.EOCategory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_events.*
import java.util.Date
import kotlin.math.abs

class EventsActivity : AppCompatActivity() {

  private val adapter = EventAdapter(mutableListOf())

  private val days = BehaviorSubject.createDefault(360)

  private val subscriptions = CompositeDisposable()

  companion object {
    private const val CATEGORY_KEY = "CATEGORY_KEY"

    fun newIntent(context: Context, category: EOCategory): Intent {
      val intent = Intent(context, EventsActivity::class.java)
      intent.putExtra(CATEGORY_KEY, category)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_events)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    title = intent.getParcelableExtra<EOCategory>(CATEGORY_KEY).title

    eventsRecyclerView.layoutManager =
      androidx.recyclerview.widget.LinearLayoutManager(this)
    eventsRecyclerView.adapter = adapter

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        days.onNext(progress)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {}
      override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    val allEvents = intent.getParcelableExtra<EOCategory>(CATEGORY_KEY)!!.events
    val eventsObservable = Observable.just(allEvents)

    Observables.combineLatest(days, eventsObservable) { days, events ->
      val maxInterval = (days.toLong() * 24L * 3600000L)

      events.filter { event ->
        val date = event.closeDate
        if (date != null) {
          abs(date.time - Date().time) < maxInterval
        } else {
          true
        }
      }
    }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          adapter.updateEvents(it)
        }
        .addTo(subscriptions)

    days
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          daysTextView.text = String.format(getString(R.string.last_days_format), it)
        }
        .addTo(subscriptions)
  }

  override fun onDestroy() {
    subscriptions.dispose()
    super.onDestroy()
  }
}
