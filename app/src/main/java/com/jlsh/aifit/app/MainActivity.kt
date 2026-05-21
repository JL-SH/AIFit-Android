package com.jlsh.aifit.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jlsh.aifit.core.util.wrapWithAppLocale
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Actividad principal: aplica tema claro/oscuro y monta el grafo de navegación [AppNavGraph].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore

    /**
     * Aplica el locale de la app antes de crear el contexto de la actividad.
     *
     * @param newBase Contexto base del sistema.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapWithAppLocale())
    }

    /**
     * Configura edge-to-edge, tema según preferencias y contenido Compose con [AppNavGraph].
     *
     * @param savedInstanceState Estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
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
