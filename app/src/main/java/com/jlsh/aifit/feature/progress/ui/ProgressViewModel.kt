package com.jlsh.aifit.feature.progress.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.usecase.GetBodyWeightHistoryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetProgressDashboardUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetWeeklyProgressSummaryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.LogBodyWeightUseCase
import com.jlsh.aifit.feature.progress.ui.state.BodyWeightUiState
import com.jlsh.aifit.feature.progress.ui.state.DashboardUiState
import com.jlsh.aifit.feature.progress.ui.state.ProgressUiEvent
import com.jlsh.aifit.feature.progress.ui.state.WeeklySummaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel of the progress module: dashboard, body weight and weekly summary.
 *
 * **UiState exposed**:
 * - [dashboardState] — [DashboardUiState]: main panel (loading, error or [ProgressDashboard]).
 * - [bodyWeightState] — [BodyWeightUiState]: Weight record history and form.
 * - [weeklySummaryState] — [WeeklySummaryUiState]: weekly summary.
 * - [selectedPeriod]: dashboard temporal filter label (7 / 30 / 90 days).
 *
 * **Emitted events** ([events] — [ProgressUiEvent]):
 * - [ProgressUiEvent.NavigateToBodyWeight]: Open weight screen.
 * - [ProgressUiEvent.NavigateToWeeklySummary]: Open weekly summary.
 * - [ProgressUiEvent.NavigateToMetabolic]: Open metabolic analysis.
 * - [ProgressUiEvent.NavigateBack]: go back.
 * - [ProgressUiEvent.ShowSnackbar]: transient message (success or error).
 *
 * @param getProgressDashboardUseCase Loading the added dashboard.
 * @param getWeeklyProgressSummaryUseCase Weekly summary.
 * @param logBodyWeightUseCase Log a new weight.
 * @param getBodyWeightHistoryUseCase Reactive body-weight history.
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressDashboardUseCase: GetProgressDashboardUseCase,
    private val getWeeklyProgressSummaryUseCase: GetWeeklyProgressSummaryUseCase,
    private val logBodyWeightUseCase: LogBodyWeightUseCase,
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _dashboardState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    /** Status of the progress dashboard.*/
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _bodyWeightState = MutableStateFlow(BodyWeightUiState())
    /** History status and body weight form.*/
    val bodyWeightState: StateFlow<BodyWeightUiState> = _bodyWeightState.asStateFlow()

    private val _weeklySummaryState = MutableStateFlow<WeeklySummaryUiState>(WeeklySummaryUiState.Loading)
    /** Weekly summary status.*/
    val weeklySummaryState: StateFlow<WeeklySummaryUiState> = _weeklySummaryState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<ProgressUiEvent>(Channel.BUFFERED)
    /** Unique flow of navigation and snackbar events.*/
    val events = _events.receiveAsFlow()

    // 3. LOCAL UI STATE
    private val _selectedPeriod = MutableStateFlow("30 días")
    /** Period selected in the dashboard (“7 days”, “30 days”, “90 days”).*/
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private var bodyWeightHistoryJob: Job? = null

    // 4. INIT BLOCK
    init {
        loadDashboard()
    }

    // 5. PUBLIC FUNCTIONS

    /** Update the temporary filter and reload the dashboard.*/
    fun onPeriodSelected(period: String) {
        _selectedPeriod.value = period
        loadDashboard()
    }

    /** Force the dashboard to reload with the current period.*/
    fun onRefreshDashboard() {
        loadDashboard()
    }

    /** Request navigation to body weight screen.*/
    fun onNavigateToBodyWeight() {
        emitEvent(ProgressUiEvent.NavigateToBodyWeight)
    }

    /** Request navigation to the weekly summary.*/
    fun onNavigateToWeeklySummary() {
        emitEvent(ProgressUiEvent.NavigateToWeeklySummary)
    }

    /** Request navigation to metabolic analysis.*/
    fun onNavigateToMetabolic() {
        emitEvent(ProgressUiEvent.NavigateToMetabolic)
    }

    // ===== BODY WEIGHT =====

    /** Start observing weight history for the last six months.*/
    fun loadBodyWeightHistory() {
        bodyWeightHistoryJob?.cancel()
        bodyWeightHistoryJob = viewModelScope.launch {
            _bodyWeightState.value = _bodyWeightState.value.copy(isLoading = true)
            val from = LocalDate.now().minusMonths(6).format(dateFormatter)
            val to = LocalDate.now().format(dateFormatter)
            getBodyWeightHistoryUseCase(from, to).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _bodyWeightState.value = _bodyWeightState.value.copy(
                            weightHistory = result.data,
                            isLoading = false,
                        )
                    }
                    is Result.Error -> {
                        _bodyWeightState.value = _bodyWeightState.value.copy(isLoading = false)
                        emitEvent(ProgressUiEvent.ShowSnackbar(result.exception.toMessage()))
                    }
                    is Result.Loading -> {
                        _bodyWeightState.value = _bodyWeightState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    /** Updates the value of the weight field on the form.*/
    fun onWeightChanged(value: String) {
        _bodyWeightState.value = _bodyWeightState.value.copy(formWeight = value)
    }

    /** Updates the date of the weight record.*/
    fun onWeightDateChanged(date: LocalDate) {
        _bodyWeightState.value = _bodyWeightState.value.copy(formDate = date)
    }

    /** Update optional log notes.*/
    fun onWeightNotesChanged(notes: String) {
        _bodyWeightState.value = _bodyWeightState.value.copy(formNotes = notes)
    }

    /** Validate the form, send the weight and refresh the history if successful.*/
    fun onLogWeight() {
        val state = _bodyWeightState.value
        val weight = state.formWeight.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _bodyWeightState.value = state.copy(isSaving = true)
            val request = LogBodyWeightRequestDto(
                weight = weight,
                date = state.formDate.format(dateFormatter),
                notes = state.formNotes.ifBlank { null },
            )
            when (val result = logBodyWeightUseCase(request)) {
                is Result.Success -> {
                    _bodyWeightState.value = _bodyWeightState.value.copy(
                        formWeight = "",
                        formNotes = "",
                        formDate = LocalDate.now(),
                        isSaving = false,
                    )
                    emitEvent(ProgressUiEvent.WeightLoggedSuccessfully)
                    loadBodyWeightHistory()
                }
                is Result.Error -> {
                    _bodyWeightState.value = _bodyWeightState.value.copy(isSaving = false)
                    emitEvent(ProgressUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ===== WEEKLY SUMMARY =====

    /** Loads the progress summary for the current week.*/
    fun loadWeeklySummary() {
        viewModelScope.launch {
            _weeklySummaryState.value = WeeklySummaryUiState.Loading
            when (val result = getWeeklyProgressSummaryUseCase()) {
                is Result.Success -> {
                    _weeklySummaryState.value = WeeklySummaryUiState.Success(summary = result.data)
                }
                is Result.Error -> {
                    _weeklySummaryState.value = WeeklySummaryUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    // 6. PRIVATE HELPERS

    private fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = DashboardUiState.Loading
            val days = when (_selectedPeriod.value) {
                "7 días" -> 7L
                "90 días" -> 90L
                else -> 30L
            }
            val to = LocalDate.now().format(dateFormatter)
            val from = LocalDate.now().minusDays(days).format(dateFormatter)

            when (val result = getProgressDashboardUseCase(from, to)) {
                is Result.Success -> {
                    _dashboardState.value = DashboardUiState.Success(
                        dashboard = result.data,
                        selectedPeriod = _selectedPeriod.value,
                    )
                }
                is Result.Error -> {
                    _dashboardState.value = DashboardUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    private fun emitEvent(event: ProgressUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}

