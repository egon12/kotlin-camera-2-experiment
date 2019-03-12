package org.egon12.renderscripttutorial

import android.os.SystemClock
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

class SaturationFragmentTest {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testFragment() {

        SystemClock.sleep(10_000L)

    }

}