package com.github.bassaer.example.matcher

import android.view.View
import org.hamcrest.Matcher


object ImageViewDrawableMatcher {
    @JvmStatic
    fun withDrawable(resourceId: Int): Matcher<View> {
        return DrawableMatcher(resourceId)
    }
}
