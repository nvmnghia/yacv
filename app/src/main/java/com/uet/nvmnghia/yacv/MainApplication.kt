package com.uet.nvmnghia.yacv

import android.app.Application
import com.uet.nvmnghia.yacv.ui.search.preview.initialize
import dagger.hilt.android.HiltAndroidApp


// Mandatory Application class for Hilt
// https://developer.android.com/training/dependency-injection/hilt-android#application-class
@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initialize(this)
    }
}