package com.github.bassaer.example.matcher

import android.view.View
import org.hamcrest.Matcher


object ColorMatcher {
    @JvmStatic
    fun withTextColor(color: Int): Matcher<View> {
        return TextColorMatcher(color)
    }
}
