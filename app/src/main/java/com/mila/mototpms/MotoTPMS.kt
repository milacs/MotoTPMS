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
        var isServiceRunning = mutableStateOf(false)
        var dataProvider: DataProvider? = null
    }
}