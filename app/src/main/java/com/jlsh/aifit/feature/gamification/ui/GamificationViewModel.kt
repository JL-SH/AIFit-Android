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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _events = Channel<GamificationUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val initialTab: Int = when (savedStateHandle.get<String>("tab")) {
        "ACHIEVEMENTS" -> 1
        "RECORDS" -> 2
        else -> 0
    }

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = GamificationUiState.Loading

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
        }
    }

    fun onTabSelected(index: Int) {
        val current = _uiState.value
        if (current is GamificationUiState.Success) {
            _uiState.value = current.copy(selectedTabIndex = index)
        }
    }

    fun onNavigateToExport() {
        viewModelScope.launch {
            _events.send(GamificationUiEvent.NavigateToExport)
        }
    }

    fun loadExport(period: ExportPeriod) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            when (val result = getProgressExportUseCase(period.apiValue)) {
                is Result.Success -> _exportState.value = ExportUiState.Success(result.data)
                is Result.Error -> _exportState.value = ExportUiState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }
}

