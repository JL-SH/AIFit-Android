package com.jlsh.aifit.app

import android.app.Application
import android.os.SystemClock
import android.util.Log
import com.jlsh.aifit.core.di.AppWarmupEntryPoint
import com.jlsh.aifit.core.util.applyAppLocale
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point: Initializes Hilt and the global locale.
 */
@HiltAndroidApp
class AiFitApplication : Application() {

    private val warmupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Applies the configured locale before any screen boots.*/
    override fun onCreate() {
        super.onCreate()
        applyAppLocale()
        warmupInfrastructure()
    }

    private fun warmupInfrastructure() {
        val entryPoint = EntryPointAccessors.fromApplication(this, AppWarmupEntryPoint::class.java)
        warmupScope.launch {
            val start = SystemClock.elapsedRealtime()
            entryPoint.database().openHelper.writableDatabase
            entryPoint.authDataStore().warmup()
            Log.d(TAG_PERF, "app_warmup_ms=${SystemClock.elapsedRealtime() - start}")
        }
    }

    private companion object {
        const val TAG_PERF = "AIFIT_PERF"
    }
}