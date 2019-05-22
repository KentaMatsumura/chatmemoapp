package com.github.bassaer.chatmessageview.util

import java.util.*

interface ITimeFormatter {

    /**
     * Format the time text which is next to the chat bubble.
     * @param createdAt The time that message was created
     * @return Formatted time text
     */
    fun getFormattedTimeText(createdAt: Calendar): String
}
