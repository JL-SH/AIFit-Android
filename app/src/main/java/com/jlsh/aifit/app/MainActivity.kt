package com.jlsh.aifit.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jlsh.aifit.core.util.wrapWithAppLocale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity: apply light/dark theme and assemble the navigation graph [AppNavGraph].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore

    /**
     * Applies the app locale before creating the activity context.
     *
     * @param newBase System base context.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapWithAppLocale())
    }

    /**
     * Configure edge-to-edge, theme-based preferences, and Compose content with [AppNavGraph].
     *
     * @param savedInstanceState Saved state of the activity, if it exists.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by userPreferencesDataStore.isDarkTheme.collectAsState(initial = true)

            AIFitTheme(darkTheme = isDarkTheme) {
                AppNavGraph()
            }
        }
    }
}
