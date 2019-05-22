package com.github.bassaer.chatmessageview.util

import java.util.*

class DefaultTimeFormatter : ITimeFormatter {
    override fun getFormattedTimeText(createdAt: Calendar): String {
        return TimeUtils.calendarToString(createdAt, "HH:mm")
    }
}
