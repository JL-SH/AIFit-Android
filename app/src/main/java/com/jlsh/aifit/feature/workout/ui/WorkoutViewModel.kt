package com.jlsh.aifit.feature.workout.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlansUseCase
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.domain.usecase.DeleteWorkoutLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutLogDetailUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.LogWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.ui.state.LoggingUiState
import com.jlsh.aifit.feature.workout.ui.state.SetEntryState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutDetailUiState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutHistoryUiState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val logWorkoutSessionUseCase: LogWorkoutSessionUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    private val getWorkoutLogDetailUseCase: GetWorkoutLogDetailUseCase,
    private val deleteWorkoutLogUseCase: DeleteWorkoutLogUseCase,
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase,
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
) : ViewModel() {

    // --- States ---
    private val _loggingState = MutableStateFlow<LoggingUiState>(LoggingUiState.Loading)
    val loggingState: StateFlow<LoggingUiState> = _loggingState.asStateFlow()

    private val _historyState = MutableStateFlow<WorkoutHistoryUiState>(WorkoutHistoryUiState.Loading)
    val historyState: StateFlow<WorkoutHistoryUiState> = _historyState.asStateFlow()

    private val _detailState = MutableStateFlow<WorkoutDetailUiState>(WorkoutDetailUiState.Loading)
    val detailState: StateFlow<WorkoutDetailUiState> = _detailState.asStateFlow()

    // --- Events ---
    private val _events = Channel<WorkoutUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // --- Timer ---
    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()
    private var timerRunning = false

    // --- Plan context ---
    private var currentPlanId: String? = null
    private var currentDayId: String? = null

    // --- History filters ---
    private val _availablePlans = MutableStateFlow<List<TrainingPlan>>(emptyList())
    val availablePlans: StateFlow<List<TrainingPlan>> = _availablePlans.asStateFlow()

    private val _selectedPlanFilter = MutableStateFlow<String?>(null)
    val selectedPlanFilter: StateFlow<String?> = _selectedPlanFilter.asStateFlow()

    private val _dateFromFilter = MutableStateFlow<String?>(null)
    val dateFromFilter: StateFlow<String?> = _dateFromFilter.asStateFlow()

    private val _dateToFilter = MutableStateFlow<String?>(null)
    val dateToFilter: StateFlow<String?> = _dateToFilter.asStateFlow()

    private val _dayOfWeekFilter = MutableStateFlow<DayOfWeek?>(null)
    val dayOfWeekFilter: StateFlow<DayOfWeek?> = _dayOfWeekFilter.asStateFlow()

    // ===== LOGGING =====

    fun loadPlanDay(planId: String) {
        currentPlanId = planId
        viewModelScope.launch {
            _loggingState.value = LoggingUiState.Loading
            when (val result = getTrainingPlanDetailUseCase(planId)) {
                is Result.Success -> {
                    val plan = result.data
                    val day = plan.days.firstOrNull()
                    if (day == null) {
                        _loggingState.value = LoggingUiState.Error("No training days found")
                        return@launch
                    }
                    currentDayId = day.id
                    val setStates = day.exercises.flatMap { exercise ->
                        (1..exercise.sets).map { setNum ->
                            SetEntryState(
                                trainingExerciseId = exercise.id,
                                exerciseName = exercise.name,
                                exerciseSetNumber = setNum,
                            )
                        }
                    }
                    _loggingState.value = LoggingUiState.Ready(
                        planDay = day,
                        setStates = setStates,
                    )
                    startTimer()
                }
                is Result.Error -> {
                    _loggingState.value = LoggingUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    fun onSetRepsChanged(index: Int, reps: String) {
        updateSetState(index) { it.copy(repsCompleted = reps) }
    }

    fun onSetWeightChanged(index: Int, weight: String) {
        updateSetState(index) { it.copy(weightUsed = weight) }
    }

    fun onSetCompletedToggled(index: Int) {
        updateSetState(index) { it.copy(completed = !it.completed) }
    }

    fun onFinishSession() {
        val state = _loggingState.value
        if (state !is LoggingUiState.Ready) return
        val planId = currentPlanId ?: return
        val dayId = currentDayId ?: return

        viewModelScope.launch {
            _loggingState.value = state.copy(isSaving = true)

            val sets = state.setStates.map { entry ->
                LogWorkoutSetRequestDto(
                    trainingExerciseId = entry.trainingExerciseId,
                    exerciseName = entry.exerciseName,
                    exerciseSetNumber = entry.exerciseSetNumber,
                    repsCompleted = entry.repsCompleted.toIntOrNull(),
                    weightUsed = entry.weightUsed.toDoubleOrNull(),
                    durationSeconds = null,
                    completed = entry.completed,
                )
            }

            val request = LogWorkoutSessionRequestDto(
                trainingPlanId = planId,
                trainingDayId = dayId,
                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                durationMinutes = (_timerSeconds.value / 60).toInt().takeIf { it > 0 },
                perceivedExertion = null,
                notes = null,
                exercises = sets,
            )

            when (val result = logWorkoutSessionUseCase(request)) {
                is Result.Success -> {
                    stopTimer()
                    val log = result.data
                    val gamResult = log.gamificationResult
                    if (gamResult != null && gamResult.unlockedAchievements.isNotEmpty()) {
                        val first = gamResult.unlockedAchievements.first()
                        emitEvent(WorkoutUiEvent.ShowAchievementDialog(first.name, first.description))
                    }
                    emitEvent(WorkoutUiEvent.SessionSaved(gamResult))
                    emitEvent(WorkoutUiEvent.NavigateToDetail(log.id))
                }
                is Result.Error -> {
                    _loggingState.value = state.copy(isSaving = false)
                    emitEvent(WorkoutUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onBackPressed() {
        val state = _loggingState.value
        if (state is LoggingUiState.Ready && state.hasPendingSets) {
            emitEvent(WorkoutUiEvent.DiscardConfirmation)
        } else {
            emitEvent(WorkoutUiEvent.NavigateBack)
        }
    }

    fun onConfirmDiscard() {
        stopTimer()
        emitEvent(WorkoutUiEvent.NavigateBack)
    }

    // ===== HISTORY =====

    fun loadHistory() {
        viewModelScope.launch {
            getWorkoutHistoryUseCase(
                planId = _selectedPlanFilter.value,
                from = _dateFromFilter.value,
                to = _dateToFilter.value,
            ).collect { result ->
                _historyState.value = when (result) {
                    is Result.Success -> {
                        val dayFilter = _dayOfWeekFilter.value
                        val filtered = if (dayFilter != null) {
                            result.data.filter { it.date.dayOfWeek == dayFilter }
                        } else {
                            result.data
                        }
                        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))
                        val grouped = filtered
                            .sortedByDescending { it.date }
                            .groupBy { it.date.format(monthFormatter).replaceFirstChar { c -> c.uppercase() } }
                        val nameMap = _availablePlans.value.associate { it.id to it.name }
                        WorkoutHistoryUiState.Success(
                            logs = filtered,
                            logsByMonth = grouped,
                            planNameMap = nameMap,
                            selectedDayOfWeek = dayFilter,
                        )
                    }
                    is Result.Error -> WorkoutHistoryUiState.Error(result.exception.toMessage())
                    is Result.Loading -> WorkoutHistoryUiState.Loading
                }
            }
        }
    }

    fun loadAvailablePlans() {
        viewModelScope.launch {
            getTrainingPlansUseCase().collect { result ->
                if (result is Result.Success) {
                    _availablePlans.value = result.data
                }
            }
        }
    }

    fun onPlanFilterChanged(planId: String?) {
        _selectedPlanFilter.value = planId
        loadHistory()
    }

    fun onDateRangeFilterChanged(from: String?, to: String?) {
        _dateFromFilter.value = from
        _dateToFilter.value = to
        loadHistory()
    }

    fun onDayOfWeekFilterChanged(day: DayOfWeek?) {
        _dayOfWeekFilter.value = day
        loadHistory()
    }

    fun applyFilters(startDate: String?, endDate: String?, dayOfWeek: DayOfWeek? = null) {
        _dateFromFilter.value = startDate
        _dateToFilter.value = endDate
        _dayOfWeekFilter.value = dayOfWeek
        loadHistory()
    }

    fun onLogClicked(logId: String) {
        emitEvent(WorkoutUiEvent.NavigateToDetail(logId))
    }

    // ===== DETAIL =====

    fun loadLogDetail(logId: String) {
        viewModelScope.launch {
            _detailState.value = WorkoutDetailUiState.Loading
            when (val result = getWorkoutLogDetailUseCase(logId)) {
                is Result.Success -> {
                    _detailState.value = WorkoutDetailUiState.Success(log = result.data)
                }
                is Result.Error -> {
                    _detailState.value = WorkoutDetailUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    fun onDeleteLog(logId: String) {
        viewModelScope.launch {
            // Optimistic UI update: remove the log from the history state immediately
            // so it never flashes when the user navigates back.
            val previousHistoryState = _historyState.value
            val currentHistory = previousHistoryState as? WorkoutHistoryUiState.Success
            if (currentHistory != null) {
                _historyState.value = currentHistory.copy(
                    logs = currentHistory.logs.filter { it.id != logId }
                )
            }

            when (val result = deleteWorkoutLogUseCase(logId)) {
                is Result.Success -> {
                    emitEvent(WorkoutUiEvent.ShowSnackbar("Session deleted"))
                    emitEvent(WorkoutUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    // Rollback the optimistic UI update
                    _historyState.value = previousHistoryState
                    emitEvent(WorkoutUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ===== PRIVATE HELPERS =====

    private fun updateSetState(index: Int, transform: (SetEntryState) -> SetEntryState) {
        _loggingState.update { current ->
            if (current is LoggingUiState.Ready) {
                val updated = current.setStates.toMutableList()
                if (index in updated.indices) {
                    updated[index] = transform(updated[index])
                }
                current.copy(setStates = updated)
            } else current
        }
    }

    private fun startTimer() {
        if (timerRunning) return
        timerRunning = true
        viewModelScope.launch {
            while (timerRunning) {
                delay(1000)
                _timerSeconds.value++
                _loggingState.update { current ->
                    if (current is LoggingUiState.Ready) {
                        current.copy(timerSeconds = _timerSeconds.value)
                    } else current
                }
            }
        }
    }

    private fun stopTimer() {
        timerRunning = false
    }

    private fun emitEvent(event: WorkoutUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}

