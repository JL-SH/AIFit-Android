package com.jlsh.aifit.app

import android.app.Application
import com.jlsh.aifit.core.util.applyAppLocale
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de la aplicación: inicializa Hilt y el locale global.
 */
@HiltAndroidApp
class AiFitApplication : Application() {

    /** Aplica el locale configurado antes de que arranque cualquier pantalla. */
    override fun onCreate() {
        super.onCreate()
        applyAppLocale()
    }
}