package com.mila.mototpms

import android.app.Application
import androidx.compose.runtime.mutableIntStateOf


class MotoTPMS : Application() {
    companion object {
        fun activityResumed() {
            isActivityVisible = true
        }

        fun activityPaused() {
            isActivityVisible = false
        }

        fun serviceStarted() {
            isServiceRunning.value = 1
        }

        var isActivityVisible = false
        var isServiceRunning = mutableIntStateOf(0)
        var dataProvider: DataProvider? = null
    }
}