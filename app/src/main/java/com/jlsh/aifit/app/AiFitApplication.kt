package com.jlsh.aifit.app

import android.app.Application
import com.jlsh.aifit.core.util.applyAppLocale
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point: Initializes Hilt and the global locale.
 */
@HiltAndroidApp
class AiFitApplication : Application() {

    /** Applies the configured locale before any screen boots.*/
    override fun onCreate() {
        super.onCreate()
        applyAppLocale()
    }
}