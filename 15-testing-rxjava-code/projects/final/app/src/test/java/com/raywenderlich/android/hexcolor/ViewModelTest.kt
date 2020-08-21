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

package com.raywenderlich.android.hexcolor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.mockk
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ViewModelTest {

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  private val colorCoordinator: ColorCoordinator = mockk(relaxed = true)

  @Test
  fun `color is red when hex string is FF0000`() {
    val trampolineScheduler = TrampolineScheduler.instance()
    val viewModel = ColorViewModel(trampolineScheduler, trampolineScheduler, colorCoordinator)

    viewModel.digitClicked("F")
    viewModel.digitClicked("F")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")

    Assert.assertEquals(ColorName.RED.toString(), viewModel.colorNameLiveData.value)
  }

  @Test
  fun `color is red when hex string is FF0000 using test scheduler`() {
    val testScheduler = TestScheduler()
    val viewModel = ColorViewModel(testScheduler, testScheduler, colorCoordinator)

    viewModel.digitClicked("F")
    viewModel.digitClicked("F")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")

    Assert.assertEquals(null, viewModel.colorNameLiveData.value)
    testScheduler.triggerActions()
    Assert.assertEquals(ColorName.RED.toString(), viewModel.colorNameLiveData.value)
  }

  @Test
  fun `hex subject is reset after clear is clicked`() {
    val trampolineScheduler = TrampolineScheduler.instance()
    val viewModel = ColorViewModel(trampolineScheduler, trampolineScheduler, colorCoordinator)

    viewModel.digitClicked("F")
    viewModel.digitClicked("F")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")

    Assert.assertEquals("#FF0000", viewModel.hexStringSubject.value)

    viewModel.clearClicked()

    Assert.assertEquals("#", viewModel.hexStringSubject.value)
  }
}
