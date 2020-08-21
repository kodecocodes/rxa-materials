package com.raywenderlich.android.hexcolor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


class ViewModelTest {

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  @Test
  fun `color is red when hex string is FF0000`() {
    val digitObservable = PublishSubject.create<String>()
    val viewModel = buildViewModel(digitStream = digitObservable)

    digitObservable.onNext("F")
    digitObservable.onNext("F")
    digitObservable.onNext("0")
    digitObservable.onNext("0")
    digitObservable.onNext("0")
    digitObservable.onNext("0")

    Assert.assertEquals(viewModel.colorNameLiveData.value, ColorName.RED.toString())
  }

  @Test
  fun `color is red when hex string is FF0000 using test scheduler`() {
    val digitObservable = PublishSubject.create<String>()
    val clearObservable = PublishSubject.create<Unit>()
    val testScheduler = TestScheduler()
    val viewModel = buildViewModel(
      clearStream = clearObservable,
      digitStream = digitObservable,
      backgroundScheduler = testScheduler,
      mainScheduler = testScheduler
    )

    digitObservable.onNext("F")
    digitObservable.onNext("F")
    digitObservable.onNext("0")
    digitObservable.onNext("0")
    digitObservable.onNext("0")
    digitObservable.onNext("0")

    Assert.assertEquals(viewModel.colorNameLiveData.value, null)
    testScheduler.triggerActions()
    Assert.assertEquals(viewModel.colorNameLiveData.value, ColorName.RED.toString())
  }


  @Test
  fun `hex subject is reset after clear is clicked`() {
    val digitObservable = PublishSubject.create<String>()
    val clearObservable = PublishSubject.create<Unit>()
    val viewModel = buildViewModel(clearStream = clearObservable, digitStream = digitObservable)

    digitObservable.onNext("F")
    digitObservable.onNext("F")
    digitObservable.onNext("0")
    digitObservable.onNext("0")
    digitObservable.onNext("0")
    digitObservable.onNext("0")

    Assert.assertEquals("#FF0000", viewModel.hexStringSubject.value)

    clearObservable.onNext(Unit)

    Assert.assertEquals("#", viewModel.hexStringSubject.value)
  }

  private fun buildViewModel(
    backStream: Observable<Unit> = Observable.empty(),
    clearStream: Observable<Unit> = Observable.empty(),
    digitStream: Observable<String> = Observable.empty(),
    backgroundScheduler: Scheduler = TrampolineScheduler.instance(),
    mainScheduler: Scheduler = TrampolineScheduler.instance(),
    colorCoordinator: ColorCoordinator = mockk(relaxed = true)
  ): ColorViewModel {
    return ColorViewModel(
      backStream,
      clearStream,
      digitStream,
      backgroundScheduler,
      mainScheduler,
      mockk(),
      colorCoordinator
    )
  }
}
