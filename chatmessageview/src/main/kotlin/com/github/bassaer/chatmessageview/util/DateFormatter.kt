package com.github.bassaer.chatmessageview.util

import java.util.*

class DateFormatter : ITimeFormatter {
    override fun getFormattedTimeText(createdAt: Calendar): String {
        return TimeUtils.calendarToString(createdAt, "yyyy/MM/dd")
    }
}
