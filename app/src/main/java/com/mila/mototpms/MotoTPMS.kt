package com.mila.mototpms

import android.app.Application
import androidx.compose.runtime.mutableStateOf


class MotoTPMS : Application() {
    companion object {
        fun activityResumed() {
            isActivityVisible = true
        }

        fun activityPaused() {
            isActivityVisible = false
        }

        fun serviceStarted() {
            isServiceRunning.value = true
        }

        var isActivityVisible = false
            private set
        var isServiceRunning = mutableStateOf(false)
            private set

        var dataProvider: DataProvider? = null
    }
}