package com.jlsh.aifit.feature.gamification.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.gamification.domain.model.ExportPeriod
import com.jlsh.aifit.feature.gamification.domain.usecase.GetAllAchievementDefinitionsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetProgressExportUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.gamification.ui.state.ExportUiState
import com.jlsh.aifit.feature.gamification.ui.state.GamificationUiEvent
import com.jlsh.aifit.feature.gamification.ui.state.GamificationUiState
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de gamificación y exportación de progreso.
 *
 * **UiState expuesto** ([uiState] — [GamificationUiState]):
 * - [GamificationUiState.Loading]: carga paralela de rachas, logros, definiciones y récords.
 * - [GamificationUiState.Success]: datos listos con pestaña seleccionable.
 * - [GamificationUiState.Error]: mensaje de error al cargar.
 *
 * **UiState de exportación** ([exportState] — [ExportUiState]):
 * - [ExportUiState.Idle]: sin informe generado aún.
 * - [ExportUiState.Loading]: generando informe para el período elegido.
 * - [ExportUiState.Success]: informe listo para mostrar o compartir.
 * - [ExportUiState.Error]: fallo al generar el informe.
 *
 * **Eventos emitidos** ([events] — [GamificationUiEvent]):
 * - [GamificationUiEvent.NavigateToExport]: abrir pantalla de exportación.
 * - [GamificationUiEvent.NavigateBack]: volver atrás.
 * - [GamificationUiEvent.ShowSnackbar]: mensaje al usuario.
 *
 * @param getUserStreaksUseCase Obtiene las rachas activas del usuario.
 * @param getUserAchievementsUseCase Obtiene los logros desbloqueados.
 * @param getAllDefinitionsUseCase Obtiene todas las definiciones de logros.
 * @param getPersonalRecordsUseCase Obtiene los récords personales.
 * @param getProgressExportUseCase Genera el informe de progreso por período.
 * @param savedStateHandle Permite leer la pestaña inicial desde la ruta (`tab`).
 */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val getUserStreaksUseCase: GetUserStreaksUseCase,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val getAllDefinitionsUseCase: GetAllAchievementDefinitionsUseCase,
    private val getPersonalRecordsUseCase: GetPersonalRecordsUseCase,
    private val getProgressExportUseCase: GetProgressExportUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GamificationUiState>(GamificationUiState.Loading)

    /** Estado principal de rachas, logros y récords personales. */
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)

    /** Estado del informe de progreso exportable (pantalla de exportación). */
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _events = Channel<GamificationUiEvent>(Channel.BUFFERED)

    /** Flujo de navegación y snackbars; consumir una vez por pantalla. */
    val events = _events.receiveAsFlow()

    private val initialTab: Int = when (savedStateHandle.get<String>("tab")) {
        "ACHIEVEMENTS" -> 1
        "RECORDS" -> 2
        else -> 0
    }

    init {
        loadAll()
    }

    /** Recarga en paralelo rachas, logros, definiciones y récords personales. */
    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = GamificationUiState.Loading
            try {
                val streaksDeferred = async { getUserStreaksUseCase() }
                val achievementsDeferred = async { getUserAchievementsUseCase() }
                val definitionsDeferred = async { getAllDefinitionsUseCase() }
                val recordsDeferred = async { getPersonalRecordsUseCase() }

                val streaksResult = streaksDeferred.await()
                val achievementsResult = achievementsDeferred.await()
                val definitionsResult = definitionsDeferred.await()
                val recordsResult = recordsDeferred.await()

                if (streaksResult is Result.Error) {
                    _uiState.value = GamificationUiState.Error(streaksResult.exception.toMessage())
                    return@launch
                }

                _uiState.value = GamificationUiState.Success(
                    streaks = (streaksResult as? Result.Success)?.data.orEmpty(),
                    achievements = (achievementsResult as? Result.Success)?.data.orEmpty(),
                    allDefinitions = (definitionsResult as? Result.Success)?.data.orEmpty(),
                    personalRecords = (recordsResult as? Result.Success)?.data.orEmpty(),
                    selectedTabIndex = initialTab,
                )
            } catch (e: Exception) {
                Log.e("AIFIT_DEBUG", "loadAll: unexpected exception — ${e.javaClass.simpleName}: ${e.message}", e)
                _uiState.value = GamificationUiState.Error(e.message ?: "Error al cargar la gamificación")
            }
        }
    }

    /**
     * Cambia la pestaña visible cuando el estado es [GamificationUiState.Success].
     *
     * @param index Índice de pestaña: 0 rachas, 1 logros, 2 récords.
     */
    fun onTabSelected(index: Int) {
        val current = _uiState.value
        if (current is GamificationUiState.Success) {
            _uiState.value = current.copy(selectedTabIndex = index)
        }
    }

    /** Emite [GamificationUiEvent.NavigateToExport]. */
    fun onNavigateToExport() {
        viewModelScope.launch {
            _events.send(GamificationUiEvent.NavigateToExport)
        }
    }

    /**
     * Genera el informe de progreso para el período indicado y actualiza [exportState].
     *
     * @param period Período temporal seleccionado por el usuario.
     */
    fun loadExport(period: ExportPeriod) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            try {
                when (val result = getProgressExportUseCase(period.apiValue)) {
                    is Result.Success -> _exportState.value = ExportUiState.Success(result.data)
                    is Result.Error -> _exportState.value = ExportUiState.Error(result.exception.toMessage())
                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e("AIFIT_DEBUG", "loadExport: unexpected exception — ${e.javaClass.simpleName}: ${e.message}", e)
                _exportState.value = ExportUiState.Error(e.message ?: "Error al generar el informe")
            }
        }
    }
}
