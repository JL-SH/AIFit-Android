package com.jlsh.aifit.app

import android.app.Application
import com.jlsh.aifit.core.util.applyAppLocale
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AiFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyAppLocale()
    }
}