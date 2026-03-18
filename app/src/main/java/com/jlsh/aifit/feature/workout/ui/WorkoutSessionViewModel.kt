package com.jlsh.aifit.feature.workout.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.training.domain.usecase.GetExerciseSubstitutionsUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetWarmUpProtocolUseCase
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.domain.usecase.AddSetToLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.FinalizeWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetPreviousSessionForDayUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.LogWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.domain.util.calculateAccumulatedVolume
import com.jlsh.aifit.feature.workout.domain.util.calculateAutoregulatedWeight
import com.jlsh.aifit.feature.workout.domain.util.calculateOneRepMax
import com.jlsh.aifit.feature.workout.domain.util.calculateRestSeconds
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import com.jlsh.aifit.feature.workout.ui.state.SubstitutionLoadState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionData
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiEvent
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val getWarmUpProtocolUseCase: GetWarmUpProtocolUseCase,
    private val logWorkoutSessionUseCase: LogWorkoutSessionUseCase,
    private val addSetToLogUseCase: AddSetToLogUseCase,
    private val finalizeWorkoutSessionUseCase: FinalizeWorkoutSessionUseCase,
    private val getPreviousSessionForDayUseCase: GetPreviousSessionForDayUseCase,
    private val getExerciseSubstitutionsUseCase: GetExerciseSubstitutionsUseCase,
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutSessionUiState>(WorkoutSessionUiState.Idle)
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WorkoutSessionUiEvent>()
    val events: SharedFlow<WorkoutSessionUiEvent> = _events.asSharedFlow()

    private val _restTimerSeconds = MutableStateFlow<Int?>(null)
    val restTimerSeconds: StateFlow<Int?> = _restTimerSeconds.asStateFlow()

    private val _substitutionsState = MutableStateFlow<SubstitutionLoadState>(SubstitutionLoadState.Idle)
    val substitutionsState: StateFlow<SubstitutionLoadState> = _substitutionsState.asStateFlow()

    private var restTimerJob: Job? = null

    private var currentPlanId: String = ""
    private var currentDayId: String = ""
    private var backendLogId: String? = null
    private var sessionExercises: List<SessionExercise> = emptyList()
    private var ghostSets: List<WorkoutSetLog> = emptyList()
    private var warmUpProtocol: WarmUpProtocol? = null

    init {
        val planId = savedStateHandle.get<String>("planId") ?: ""
        val dayId = savedStateHandle.get<String>("dayId") ?: ""
        if (planId.isNotBlank() && dayId.isNotBlank()) {
            loadSession(planId, dayId)
        }
    }

    fun loadSession(planId: String, dayId: String) {
        if (_uiState.value !is WorkoutSessionUiState.Idle) return

        currentPlanId = planId
        currentDayId = dayId

        _uiState.value = WorkoutSessionUiState.LoadingWarmUp

        viewModelScope.launch {
            // Load exercises from the training plan
            val planResult = getTrainingPlanDetailUseCase(planId)
            if (planResult is Result.Error) {
                _uiState.value = WorkoutSessionUiState.Error(planResult.exception.toMessage())
                return@launch
            }

            val plan = (planResult as Result.Success).data
            val day = plan.days.find { it.id == dayId }
            if (day == null) {
                _uiState.value = WorkoutSessionUiState.Error("Training day not found")
                return@launch
            }

            sessionExercises = day.exercises.map { exercise ->
                SessionExercise(
                    exerciseId = exercise.id,
                    name = exercise.name,
                    primaryMuscle = exercise.primaryMuscle,
                    targetSets = exercise.sets,
                    targetReps = exercise.repsMin,
                    targetRpe = exercise.targetRpe,
                    restSeconds = exercise.restSeconds,
                    completedSets = 0,
                )
            }

            val ghostResult = getPreviousSessionForDayUseCase(planId, dayId)
            if (ghostResult is Result.Success) {
                ghostSets = ghostResult.data?.sets ?: emptyList()
            }

            when (val warmUpResult = getWarmUpProtocolUseCase(planId, dayId)) {
                is Result.Success -> {
                    warmUpProtocol = warmUpResult.data
                    _uiState.value = WorkoutSessionUiState.WarmUpReady(warmUpResult.data)
                }
                else -> {
                    _uiState.value = WorkoutSessionUiState.SessionActive(
                        WorkoutSessionData(
                            exercises = sessionExercises,
                            currentExerciseIndex = 0,
                            registeredSets = emptyList(),
                            autoregulationSuggestion = null,
                            restTimerSeconds = null,
                            volumeByMuscleGroup = emptyMap(),
                            ghostSets = ghostSets,
                            substitutions = null,
                        )
                    )
                }
            }
        }
    }

    fun startWorkout() {
        if (_uiState.value !is WorkoutSessionUiState.WarmUpReady) return

        _uiState.value = WorkoutSessionUiState.SessionActive(
            WorkoutSessionData(
                exercises = sessionExercises,
                currentExerciseIndex = 0,
                registeredSets = emptyList(),
                autoregulationSuggestion = null,
                restTimerSeconds = null,
                volumeByMuscleGroup = emptyMap(),
                ghostSets = ghostSets,
                substitutions = null,
            )
        )

        // Create the session on the backend; store the backend-assigned log ID.
        // On failure, emit a snackbar but do not block the user — creation will be retried at finalize time.
        viewModelScope.launch {
            val logRequest = LogWorkoutSessionRequestDto(
                trainingPlanId = currentPlanId,
                trainingDayId = currentDayId,
                date = LocalDate.now().toString(),
                exercises = emptyList(),
            )
            when (val result = logWorkoutSessionUseCase(logRequest)) {
                is Result.Success -> {
                    backendLogId = result.data.id
                }
                is Result.Error -> {
                    _events.emit(
                        WorkoutSessionUiEvent.ShowSnackbar(result.exception.toMessage())
                    )
                }
                else -> Unit
            }
        }
    }

    fun registerSet(exerciseId: String, weightKg: Double, reps: Int, rpe: Int? = null) {
        val currentState = _uiState.value
        if (currentState !is WorkoutSessionUiState.SessionActive) return

        val sessionData = currentState.sessionData
        val exercise = sessionData.exercises.find { it.exerciseId == exerciseId } ?: return

        val estimatedOneRepMax = calculateOneRepMax(weightKg, reps)

        val autoregulatedWeight = exercise.targetRpe?.let { targetRpe ->
            rpe?.let { calculateAutoregulatedWeight(weightKg, it, targetRpe) }
        }

        val setLog = WorkoutSetLog(
            id = UUID.randomUUID().toString(),
            trainingExerciseId = exerciseId,
            exerciseName = exercise.name,
            exerciseSetNumber = exercise.completedSets + 1,
            repsCompleted = reps,
            weightUsed = weightKg,
            durationSeconds = null,
            completed = true,
            estimatedOneRepMax = estimatedOneRepMax,
            wasAutoregulated = autoregulatedWeight != null,
            rpe = rpe,
        )

        val updatedSets = sessionData.registeredSets + setLog

        val updatedExercises = sessionData.exercises.map {
            if (it.exerciseId == exerciseId) {
                it.copy(completedSets = it.completedSets + 1)
            } else it
        }

        val exerciseMuscleMap = sessionData.exercises.associate { it.exerciseId to it.primaryMuscle }
        val affectedMuscleGroups = exerciseMuscleMap.values.toSet()
        val updatedVolume = affectedMuscleGroups.associateWith { muscleGroup ->
            calculateAccumulatedVolume(updatedSets, muscleGroup, exerciseMuscleMap)
        }.filter { it.value > 0.0 }

        val restSeconds = if (rpe != null) {
            calculateRestSeconds(rpe, exercise.restSeconds)
        } else {
            exercise.restSeconds
        }

        _uiState.value = WorkoutSessionUiState.SessionActive(
            sessionData.copy(
                exercises = updatedExercises,
                registeredSets = updatedSets,
                autoregulationSuggestion = autoregulatedWeight,
                restTimerSeconds = restSeconds,
                volumeByMuscleGroup = updatedVolume,
            )
        )

        startRestTimer(restSeconds)

        // Fire-and-forget: persist the set to the backend if the session was already created.
        backendLogId?.let { logId ->
            viewModelScope.launch {
                addSetToLogUseCase(
                    logId,
                    LogWorkoutSetRequestDto(
                        trainingExerciseId = setLog.trainingExerciseId,
                        exerciseName = setLog.exerciseName,
                        exerciseSetNumber = setLog.exerciseSetNumber,
                        repsCompleted = setLog.repsCompleted,
                        weightUsed = setLog.weightUsed,
                        durationSeconds = setLog.durationSeconds,
                        completed = setLog.completed,
                    ),
                )
            }
        }
    }

    fun finalizeSession(systemicFatigue: Int, jointPainReport: List<JointPainEntry>) {
        val currentState = _uiState.value
        if (currentState !is WorkoutSessionUiState.SessionActive) return

        val sessionData = currentState.sessionData

        viewModelScope.launch {
            // If the backend log was never created (startWorkout() call failed), retry now.
            if (backendLogId == null) {
                val logRequest = LogWorkoutSessionRequestDto(
                    trainingPlanId = currentPlanId,
                    trainingDayId = currentDayId,
                    date = LocalDate.now().toString(),
                    exercises = sessionData.registeredSets.map { set ->
                        LogWorkoutSetRequestDto(
                            trainingExerciseId = set.trainingExerciseId,
                            exerciseName = set.exerciseName,
                            exerciseSetNumber = set.exerciseSetNumber,
                            repsCompleted = set.repsCompleted,
                            weightUsed = set.weightUsed,
                            durationSeconds = set.durationSeconds,
                            completed = set.completed,
                        )
                    },
                )
                when (val retryResult = logWorkoutSessionUseCase(logRequest)) {
                    is Result.Success -> {
                        backendLogId = retryResult.data.id
                    }
                    is Result.Error -> {
                        _events.emit(
                            WorkoutSessionUiEvent.ShowSnackbar(retryResult.exception.toMessage())
                        )
                        return@launch
                    }
                    else -> return@launch
                }
            }

            val logId = backendLogId ?: return@launch

            _uiState.value = WorkoutSessionUiState.Finalizing

            when (val finalizeResult = finalizeWorkoutSessionUseCase(logId, systemicFatigue, jointPainReport)) {
                is Result.Success -> {
                    _uiState.value = WorkoutSessionUiState.SessionFinalized(finalizeResult.data)
                }
                is Result.Error -> {
                    _events.emit(WorkoutSessionUiEvent.ShowSnackbar(finalizeResult.exception.toMessage()))
                    _uiState.value = WorkoutSessionUiState.SessionActive(sessionData)
                }
                else -> Unit
            }
        }
    }

    // ===== REST TIMER =====

    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _restTimerSeconds.value = remaining
                if (remaining > 0) delay(1000L)
            }
            _events.emit(WorkoutSessionUiEvent.ShowSnackbar("Rest complete"))
            _restTimerSeconds.value = null
        }
    }

    fun cancelRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        _restTimerSeconds.value = null
    }

    // ===== SUBSTITUTIONS =====

    fun loadSubstitutions(exerciseId: String) {
        _substitutionsState.value = SubstitutionLoadState.Loading
        viewModelScope.launch {
            when (val result = getExerciseSubstitutionsUseCase(exerciseId)) {
                is Result.Success -> {
                    _substitutionsState.value = SubstitutionLoadState.Success(result.data)
                }
                is Result.Error -> {
                    _substitutionsState.value = SubstitutionLoadState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    fun applySubstitution(originalExerciseId: String, substitution: ExerciseSubstitution) {
        val currentState = _uiState.value
        if (currentState !is WorkoutSessionUiState.SessionActive) return

        val sessionData = currentState.sessionData
        val updatedExercises = sessionData.exercises.map { exercise ->
            if (exercise.exerciseId == originalExerciseId) {
                exercise.copy(
                    name = substitution.name,
                    primaryMuscle = substitution.primaryMuscle,
                )
            } else exercise
        }

        _uiState.value = WorkoutSessionUiState.SessionActive(
            sessionData.copy(exercises = updatedExercises)
        )
    }
}
