package com.raywenderlich.android.hexcolor

import io.reactivex.Observable
import io.reactivex.internal.schedulers.TrampolineScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class OperatorTest {

  @Test
  fun `test amb`() {
    val scheduler = TestScheduler()

    val observableA = Observable.interval(1, TimeUnit.SECONDS, scheduler)
        .take(3)
        .map { 5 * it }
    val observableB = Observable.interval(500, TimeUnit.MILLISECONDS, scheduler)
        .take(3)
        .map { 10 * it }

    val ambObservable = observableA.ambWith(observableB)
    val testObserver = ambObservable.test()

    testObserver.assertValueCount(0)
    testObserver.assertNotComplete()

    scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)

    testObserver.assertValueCount(1)

    scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)

    testObserver.assertValueCount(3)
    Assert.assertArrayEquals(arrayOf(0L, 10L, 20L), testObserver.values().toTypedArray())
    testObserver.assertComplete()
  }

  @Test
  fun `test filter`() {
    val scheduler = TestScheduler()
    val observable = Observable.interval(1, TimeUnit.SECONDS, scheduler)
        .take(10)
        .filter { it < 5 }

    val testObserver = observable.test()

    testObserver.assertNotComplete()
    testObserver.assertValueCount(0)

    scheduler.advanceTimeBy(10, TimeUnit.SECONDS)

    testObserver.assertValueCount(5)
    Assert.assertArrayEquals(arrayOf(0L, 1L, 2L, 3L, 4L), testObserver.values().toTypedArray())
    testObserver.assertComplete()
  }

  @Test
  fun `using trampoline schedulers`() {
    var trampolineObservableHasEmitted = false
    var ioObservableHasEmitted = false
    Observable.just(1)
        .subscribeOn(TrampolineScheduler.instance())
        .subscribe { trampolineObservableHasEmitted = true }

    Observable.just(1)
        .subscribeOn(Schedulers.io())
        .subscribe { ioObservableHasEmitted = true }

    Assert.assertTrue(trampolineObservableHasEmitted)
    Assert.assertFalse(ioObservableHasEmitted)
  }

  @Test
  fun `testing concat`() {
    val observableA = Observable.just(1)
    val observableB = observableA.concatWith(Observable.just(2))

    val testObserver = observableB.test()
    testObserver.assertValueCount(2)
    Assert.assertArrayEquals(arrayOf(1, 2), testObserver.values().toTypedArray())
    testObserver.assertComplete()
  }
}