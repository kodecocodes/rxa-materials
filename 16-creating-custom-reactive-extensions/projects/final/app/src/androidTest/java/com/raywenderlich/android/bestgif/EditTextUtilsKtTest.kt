package com.raywenderlich.android.bestgif

import android.widget.EditText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test

class EditTextUtilsKtTest {
  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun newStringsReachObserver() {
    val view = EditText(context)
    val testObserver = view.textChanges().test()
    view.setText("Test 1")
    view.setText("Test 2")
    view.setText("Test 3")
    view.setText("Test 4")
    testObserver.assertValueCount(4)
    testObserver.assertValues("Test 1", "Test 2", "Test 3", "Test 4")
  }
}
