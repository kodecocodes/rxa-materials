package com.raywenderlich.android.hexcolor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.mockk
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


class ViewModelTest {

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  @Test
  fun `color is red when hex string is FF0000`() {
    val viewModel = buildViewModel()

    viewModel.digitClicked("F")
    viewModel.digitClicked("F")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")

    Assert.assertEquals(viewModel.colorNameLiveData.value, ColorName.RED.toString())
  }

  @Test
  fun `color is red when hex string is FF0000 using test scheduler`() {
    val testScheduler = TestScheduler()
    val viewModel = buildViewModel(
        backgroundScheduler = testScheduler,
        mainScheduler = testScheduler
    )

    viewModel.digitClicked("F")
    viewModel.digitClicked("F")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")
    viewModel.digitClicked("0")

    Assert.assertEquals(viewModel.colorNameLiveData.value, null)
    testScheduler.triggerActions()
    Assert.assertEquals(viewModel.colorNameLiveData.value, ColorName.RED.toString())
  }


  @Test
  fun `hex subject is reset after clear is clicked`() {
    val viewModel = buildViewModel()

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

  private fun buildViewModel(
      backgroundScheduler: Scheduler = TrampolineScheduler.instance(),
      mainScheduler: Scheduler = TrampolineScheduler.instance(),
      colorCoordinator: ColorCoordinator = mockk(relaxed = true)
  ): ColorViewModel {
    return ColorViewModel(
        backgroundScheduler,
        mainScheduler,
        colorCoordinator
    )
  }
}
