package com.github.bassaer.chatmessageview.util

import org.junit.Assert.assertEquals
import org.junit.Test

import java.util.*

internal class DateFormatterTest {

    @Test
    fun getFormattedTimeText() {
        val formatter = DateFormatter()
        val calendar = Calendar.getInstance()
        calendar.set(2017, 10, 12)
        assertEquals("Nov. 12, 2017", formatter.getFormattedTimeText(calendar))
    }

}
