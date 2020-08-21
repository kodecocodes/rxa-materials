package com.raywenderlich.android.bestgif

import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LocationUtilsKtTest {
  @Test
  fun locationUpdatesRemoveOnComplete() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val locationProvider = mockk<FusedLocationProviderClient>(relaxed = true)

    val locationObservable = locationUpdates(context, locationProvider)
    verify(exactly = 0) { locationProvider.removeLocationUpdates(any<LocationCallback>()) }
    locationObservable
      .take(0)
      .test()
      .assertComplete()
    verify(exactly = 1) { locationProvider.removeLocationUpdates(any<LocationCallback>()) }
  }
}
