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
 * ViewModel of the gamification screen and progress export.
 *
 * **UiState exposed** ([uiState] — [GamificationUiState]):
 * - [GamificationUiState.Loading]: Parallel loading of streaks, achievements, definitions and records.
 * - [GamificationUiState.Success]: Data ready with selectable tab.
 * - [GamificationUiState.Error]: Error message loading.
 *
 * **Export UiState** ([exportState] — [ExportUiState]):
 * - [ExportUiState.Idle]: no report generated yet.
 * - [ExportUiState.Loading]: generating report for the chosen period.
 * - [ExportUiState.Success]: Report ready to display or share.
 * - [ExportUiState.Error]: Failed to generate the report.
 *
 * **Emitted events** ([events] — [GamificationUiEvent]):
 * - [GamificationUiEvent.NavigateToExport]: Open export screen.
 * - [GamificationUiEvent.NavigateBack]: go back.
 * - [GamificationUiEvent.ShowSnackbar]: message to user.
 *
 * @param getUserStreaksUseCase Gets the user's active streaks.
 * @param getUserAchievementsUseCase Gets the unlocked achievements.
 * @param getAllDefinitionsUseCase Gets all achievement definitions.
 * @param getPersonalRecordsUseCase Gets the personal records.
 * @param getProgressExportUseCase Generates the progress report by period.
 * @param savedStateHandle Read the initial tab from the path (`tab`).
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

    /** Main status of streaks, achievements and personal bests.*/
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)

    /** Exportable progress report status (export screen).*/
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _events = Channel<GamificationUiEvent>(Channel.BUFFERED)

    /** Navigation flow and snackbars; consume once per screen.*/
    val events = _events.receiveAsFlow()

    private val initialTab: Int = when (savedStateHandle.get<String>("tab")) {
        "ACHIEVEMENTS" -> 1
        "RECORDS" -> 2
        else -> 0
    }

    init {
        loadAll()
    }

    /** Recharge streaks, achievements, definitions and personal records in parallel.*/
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
     * Changes the visible tab when the state is [GamificationUiState.Success].
     *
     * @param index Tab index: 0 streaks, 1 achievements, 2 records.
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
     * Generates the progress report for the indicated period and updates [exportState].
     *
     * @param period Time period selected by the user.
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
