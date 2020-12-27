package com.uet.nvmnghia.yacv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


// Mandatory Application class for Hilt
// https://developer.android.com/training/dependency-injection/hilt-android#application-class
@HiltAndroidApp
class MainApplication : Application() {
}