package com.jlsh.aifit.feature.workout.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
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
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.domain.usecase.AddSetToLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.DeleteWorkoutLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.FindOpenLogForDayUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.FinalizeWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetPreviousSessionForDayUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.LogWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.domain.util.areAllExercisesComplete
import com.jlsh.aifit.feature.workout.domain.util.calculateAccumulatedVolume
import com.jlsh.aifit.feature.workout.domain.util.calculateAutoregulatedWeight
import com.jlsh.aifit.feature.workout.domain.util.calculateOneRepMax
import com.jlsh.aifit.feature.workout.domain.util.calculateRestSeconds
import com.jlsh.aifit.feature.workout.domain.util.isExerciseComplete
import com.jlsh.aifit.feature.workout.domain.util.resolveCurrentExerciseIndex
import com.jlsh.aifit.feature.workout.domain.util.resolveNextExerciseIndexAfterCompletion
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import com.jlsh.aifit.feature.workout.ui.state.SubstitutionLoadState
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionData
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiEvent
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel of the live training session: load, sets, warm-up and closing.
 *
 * Exposed states:
 * - [uiState]: main flow ([WorkoutSessionUiState]) from idle to finished.
 * - [restTimerSeconds]: Rest timer remaining seconds, or null if inactive.
 * - [substitutionsState]: load exercise alternatives ([SubstitutionLoadState]).
 *
 * Events ([events], type [WorkoutSessionUiEvent]):
 * - [WorkoutSessionUiEvent.NavigateBack] when leaving the session.
 * - [WorkoutSessionUiEvent.ShowSnackbar] for errors and end of rest.
 * - [WorkoutSessionUiEvent.SessionAlreadyLocked] if the day has already ended today.
 * - [WorkoutSessionUiEvent.RequestFinalizeSession] when all exercises are complete.
 * - [WorkoutSessionUiEvent.ShowSubstitutionSheet] (reserved; the UI opens the sheet directly).
 */
@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val getWarmUpProtocolUseCase: GetWarmUpProtocolUseCase,
    private val logWorkoutSessionUseCase: LogWorkoutSessionUseCase,
    private val addSetToLogUseCase: AddSetToLogUseCase,
    private val deleteWorkoutLogUseCase: DeleteWorkoutLogUseCase,
    private val finalizeWorkoutSessionUseCase: FinalizeWorkoutSessionUseCase,
    private val getPreviousSessionForDayUseCase: GetPreviousSessionForDayUseCase,
    private val findOpenLogForDayUseCase: FindOpenLogForDayUseCase,
    private val getExerciseSubstitutionsUseCase: GetExerciseSubstitutionsUseCase,
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutSessionUiState>(WorkoutSessionUiState.Idle)

    /** Observable state of the session (warming up, active, ending, error).*/
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    private val _events = Channel<WorkoutSessionUiEvent>(Channel.BUFFERED)

    /** Single-drink navigation and feedback events.*/
    val events = _events.receiveAsFlow()

    private val _restTimerSeconds = MutableStateFlow<Int?>(null)

    /** Seconds left of rest between sets; null if there is no active timer.*/
    val restTimerSeconds: StateFlow<Int?> = _restTimerSeconds.asStateFlow()

    private val _substitutionsState = MutableStateFlow<SubstitutionLoadState>(SubstitutionLoadState.Idle)

    /** Fiscal year substitutions loading status for the bottom sheet.*/
    val substitutionsState: StateFlow<SubstitutionLoadState> = _substitutionsState.asStateFlow()

    private var restTimerJob: Job? = null
    private val pendingSetJobs = mutableListOf<Job>()

    private var currentPlanId: String = ""
    private var currentDayId: String = ""
    private var backendLogId: String? = null
    private var sessionExercises: List<SessionExercise> = emptyList()
    private var ghostSets: List<WorkoutSetLog> = emptyList()
    private var warmUpProtocol: WarmUpProtocol? = null
    private var existingBackendSets: List<WorkoutSetLog> = emptyList()
    private var backendLogCreationInFlight = false
    private val pendingSets = mutableListOf<PendingSetUpload>()
    private val persistedSetIds = mutableSetOf<String>()
    /** Server-side (exerciseId, setNumber) keys already stored — avoids duplicate POSTs. */
    private val serverPersistedSetKeys = mutableSetOf<Pair<String, Int>>()
    private val logCreationMutex = Mutex()
    /** Sets included in the last successful bulk POST /workout-logs (finalize path). */
    private var lastBulkCreateSetCount = 0

    private data class PendingSetUpload(
        val localSetId: String,
        val dto: LogWorkoutSetRequestDto,
    )

    init {
        val planId = savedStateHandle.get<String>("planId") ?: ""
        val dayId = savedStateHandle.get<String>("dayId") ?: ""
        if (planId.isNotBlank() && dayId.isNotBlank()) {
            loadSession(planId, dayId)
        }
    }

    /**
     * Load the day's exercises, warm-up, previous session of the day and resumption if applicable.
     * Only acts if the current state is [WorkoutSessionUiState.Idle].
     *
     * @param planId Identifier of the training plan.
     * @param dayId Identifier of the training day within the plan.
     */
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
                    requiresExternalWeight = exercise.requiresExternalWeight,
                )
            }

            val ghostResult = getPreviousSessionForDayUseCase(planId, dayId)
            if (ghostResult is Result.Success) {
                ghostSets = ghostResult.data?.sets ?: emptyList()
            }

            // Check if a session log already exists for today (direct API; not filtered by pending deletes).
            val today = LocalDate.now().toString()
            when (val openLogResult = findOpenLogForDayUseCase(currentPlanId, currentDayId, today)) {
                is Result.Success -> {
                    val todaysLog = openLogResult.data
                    when {
                        todaysLog != null && todaysLog.isLocked -> {
                            Log.i("AIFIT_LOAD", "Session for dayId=$currentDayId is already locked, navigating away")
                            _events.send(WorkoutSessionUiEvent.SessionAlreadyLocked)
                            return@launch
                        }
                        todaysLog != null -> {
                            Log.i("AIFIT_LOAD", "Resuming existing session for dayId=$currentDayId, logId=${todaysLog.id}")
                            applyRecoveredLog(todaysLog)
                            existingBackendSets = todaysLog.sets
                        }
                        else -> {
                            existingBackendSets = emptyList()
                        }
                    }
                }
                is Result.Error -> {
                    Log.w("AIFIT_LOAD", "findOpenLogForDay failed: ${openLogResult.exception.message}")
                    existingBackendSets = emptyList()
                }
                else -> existingBackendSets = emptyList()
            }

            when (val warmUpResult = getWarmUpProtocolUseCase(planId, dayId)) {
                is Result.Success -> {
                    warmUpProtocol = warmUpResult.data
                    _uiState.value = WorkoutSessionUiState.WarmUpReady(warmUpResult.data)
                }
                else -> {
                    val exercisesWithProgress = sessionExercises.map { exercise ->
                        val alreadyDone = existingBackendSets.count { it.trainingExerciseId == exercise.exerciseId }
                        exercise.copy(completedSets = alreadyDone)
                    }
                    val startIndex = resolveCurrentExerciseIndex(
                        exercisesWithProgress.map { it.completedSets },
                        exercisesWithProgress.map { it.targetSets },
                    )
                    _uiState.value = WorkoutSessionUiState.SessionActive(
                        WorkoutSessionData(
                            exercises = exercisesWithProgress,
                            currentExerciseIndex = startIndex,
                            registeredSets = existingBackendSets,
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

    /**
     * Go from warm-up to active session with progress of already saved series.
     *
     * @param warmupCompleted Whether the user completed the guided warmup.
     */
    fun startWorkout(warmupCompleted: Boolean = false) {
        if (_uiState.value !is WorkoutSessionUiState.WarmUpReady) return

        // Pre-populate completedSets for each exercise from any sets already on the backend
        // (e.g. when the user is resuming a session that was partially saved).
        val exercisesWithProgress = sessionExercises.map { exercise ->
            val alreadyDone = existingBackendSets.count { it.trainingExerciseId == exercise.exerciseId }
            exercise.copy(completedSets = alreadyDone)
        }
        val startIndex = resolveCurrentExerciseIndex(
            exercisesWithProgress.map { it.completedSets },
            exercisesWithProgress.map { it.targetSets },
        )

        _uiState.value = WorkoutSessionUiState.SessionActive(
            WorkoutSessionData(
                exercises = exercisesWithProgress,
                currentExerciseIndex = startIndex,
                registeredSets = existingBackendSets,
                autoregulationSuggestion = null,
                restTimerSeconds = null,
                volumeByMuscleGroup = emptyMap(),
                ghostSets = ghostSets,
                substitutions = null,
                warmupCompleted = warmupCompleted,
            )
        )
        // Backend log is created on the first registerSet() call (if backendLogId is still null).
    }

    /**
     * Logs a completed series: updates UI, timer, volume and persists to backend.
     *
     * @param exerciseId Identifier of the training exercise.
     * @param weightKg Weight in kg; null if the exercise requires no external load.
     * @param reps Completed repetitions.
     * @param rpe Optional RPE (1–10) for self-regulation and adaptive rest.
     */
    fun registerSet(exerciseId: String, weightKg: Double?, reps: Int, rpe: Int? = null) {
        val currentState = _uiState.value
        if (currentState !is WorkoutSessionUiState.SessionActive) return

        val sessionData = currentState.sessionData
        val exercise = sessionData.exercises.find { it.exerciseId == exerciseId } ?: return
        if (isExerciseComplete(exercise.completedSets, exercise.targetSets)) return

        // Capture BEFORE any async work so the first-set branch is stable.
        val isFirstSet = backendLogId == null && !backendLogCreationInFlight

        val effectiveWeight = if (exercise.requiresExternalWeight) weightKg else null

        val estimatedOneRepMax = effectiveWeight?.let { calculateOneRepMax(it, reps) }

        val autoregulatedWeight = if (effectiveWeight != null) {
            exercise.targetRpe?.let { targetRpe ->
                rpe?.let { calculateAutoregulatedWeight(effectiveWeight, it, targetRpe) }
            }
        } else {
            null
        }

        val setLog = WorkoutSetLog(
            id = UUID.randomUUID().toString(),
            trainingExerciseId = exerciseId,
            exerciseName = exercise.name,
            exerciseSetNumber = exercise.completedSets + 1,
            repsCompleted = reps,
            weightUsed = effectiveWeight,
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

        val completedSetsList = updatedExercises.map { it.completedSets }
        val targetSetsList = updatedExercises.map { it.targetSets }
        val completedExerciseIndex = updatedExercises.indexOfFirst { it.exerciseId == exerciseId }
        val exerciseJustCompleted = isExerciseComplete(
            completedSetsList[completedExerciseIndex],
            targetSetsList[completedExerciseIndex],
        )
        val updatedCurrentIndex = if (exerciseJustCompleted) {
            resolveNextExerciseIndexAfterCompletion(
                completedSetsList,
                targetSetsList,
                completedExerciseIndex,
            ) ?: resolveCurrentExerciseIndex(completedSetsList, targetSetsList)
        } else {
            sessionData.currentExerciseIndex
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

        val setDto = LogWorkoutSetRequestDto(
            trainingExerciseId = setLog.trainingExerciseId,
            exerciseName = setLog.exerciseName,
            exerciseSetNumber = setLog.exerciseSetNumber,
            repsCompleted = setLog.repsCompleted,
            weightUsed = setLog.weightUsed,
            durationSeconds = setLog.durationSeconds,
            completed = setLog.completed,
        )

        _uiState.value = WorkoutSessionUiState.SessionActive(
            sessionData.copy(
                exercises = updatedExercises,
                currentExerciseIndex = updatedCurrentIndex,
                registeredSets = updatedSets,
                autoregulationSuggestion = autoregulatedWeight,
                restTimerSeconds = restSeconds,
                volumeByMuscleGroup = updatedVolume,
            )
        )
        startRestTimer(restSeconds)

        viewModelScope.launch {
            persistSetAfterRegister(
                setDto = setDto,
                localSetId = setLog.id,
                createLogWithSets = if (isFirstSet) listOf(setDto) else emptyList(),
            )
        }

        if (areAllExercisesComplete(updatedExercises)) {
            viewModelScope.launch {
                _events.send(WorkoutSessionUiEvent.RequestFinalizeSession)
            }
        }
    }

    /**
     * Close the session by sending systemic fatigue and joint report to the backend.
     *
     * @param systemicFatigue Fatigue value reported by the user.
     * @param jointPainReport List of pain entries per joint.
     */
    fun finalizeSession(systemicFatigue: Int, jointPainReport: List<JointPainEntry>) {
        val currentState = _uiState.value
        if (currentState !is WorkoutSessionUiState.SessionActive) return

        val sessionData = currentState.sessionData

        viewModelScope.launch {
            _uiState.value = WorkoutSessionUiState.Finalizing

            val needBulkCreate = backendLogId == null
            val allSetDtos = sessionData.registeredSets.map { it.toRequestDto() }

            val logId = ensureBackendLogId(
                setsForCreate = if (needBulkCreate && allSetDtos.isNotEmpty()) allSetDtos else emptyList(),
            )
            if (logId == null) {
                _uiState.value = WorkoutSessionUiState.SessionActive(sessionData)
                return@launch
            }

            when {
                needBulkCreate && lastBulkCreateSetCount == sessionData.registeredSets.size -> {
                    markAllLocalSetsPersisted(sessionData.registeredSets)
                }
                else -> {
                    for (set in sessionData.registeredSets.filter { it.id !in persistedSetIds }) {
                        uploadSet(logId, set.toRequestDto(), set.id)
                    }
                }
            }
            lastBulkCreateSetCount = 0

            // Await all pending set upload jobs before finalizing (with timeout)
            withTimeoutOrNull(5_000L) { pendingSetJobs.toList().joinAll() }

            // TODO: remove diagnostic logs below
            Log.d("AIFIT_SESSION", "finalizeSession called — logId=$logId, fatigue=$systemicFatigue")
            when (val finalizeResult = finalizeWorkoutSessionUseCase(logId, systemicFatigue, jointPainReport)) {
                is Result.Success -> {
                    Log.d("AIFIT_SESSION", "finalize SUCCESS — returned log id=${finalizeResult.data.id} isLocked=${finalizeResult.data.isLocked}")
                    Log.d("AIFIT_SESSION", "transitioning to SessionFinalized")
                    _uiState.value = WorkoutSessionUiState.SessionFinalized(finalizeResult.data)
                }
                is Result.Error -> {
                    Log.e("AIFIT_SESSION", "finalize ERROR — ${finalizeResult.exception.message}")
                    Log.e("AIFIT_FINALIZE", "Finalize failed for logId=$logId: ${finalizeResult.exception.message}")
                    _events.send(WorkoutSessionUiEvent.ShowSnackbar(finalizeResult.exception.toMessage()))
                    _uiState.value = WorkoutSessionUiState.SessionActive(sessionData)
                }
                else -> {
                    Log.e("AIFIT_FINALIZE", "Unexpected result state during finalize for logId=$logId")
                    _uiState.value = WorkoutSessionUiState.SessionActive(sessionData)
                }
            }
        }
    }

    private suspend fun persistSetAfterRegister(
        setDto: LogWorkoutSetRequestDto,
        localSetId: String,
        createLogWithSets: List<LogWorkoutSetRequestDto>,
    ) {
        if (backendLogId != null) {
            enqueueSetUpload(backendLogId!!, setDto, localSetId)
            return
        }
        if (backendLogCreationInFlight) {
            enqueuePendingSet(localSetId, setDto)
            return
        }
        val logId = ensureBackendLogId(setsForCreate = createLogWithSets)
        if (logId == null) {
            enqueuePendingSet(localSetId, setDto)
            return
        }
        if (createLogWithSets.isNotEmpty() && lastBulkCreateSetCount > 0) {
            markLocalSetPersisted(localSetId, setDto)
            lastBulkCreateSetCount = 0
        } else {
            enqueueSetUpload(logId, setDto, localSetId)
        }
    }

    private suspend fun ensureBackendLogId(
        setsForCreate: List<LogWorkoutSetRequestDto>,
    ): String? = logCreationMutex.withLock {
        backendLogId?.let { return it }

        withTimeoutOrNull(5_000L) {
            while (backendLogCreationInFlight) {
                delay(50)
            }
        }
        backendLogId?.let { return it }

        if (setsForCreate.isEmpty()) {
            return recoverExistingLogId()
        }

        backendLogCreationInFlight = true
        try {
            val logRequest = LogWorkoutSessionRequestDto(
                trainingPlanId = currentPlanId,
                trainingDayId = currentDayId,
                date = LocalDate.now().toString(),
                exercises = setsForCreate,
            )
            when (val result = logWorkoutSessionUseCase(logRequest)) {
                is Result.Success -> {
                    backendLogId = result.data.id
                    lastBulkCreateSetCount = setsForCreate.size
                    registerServerSets(result.data.sets)
                    setsForCreate.forEach { dto ->
                        serverPersistedSetKeys.add(setKey(dto))
                    }
                    Log.i("AIFIT_REGISTER", "Backend log created: ${result.data.id}")
                    flushPendingSetQueue()
                    backendLogId
                }
                is Result.Error -> {
                    if (result.exception is AppException.ConflictException) {
                        Log.w("AIFIT_REGISTER", "Conflict creating log, recovering for dayId=$currentDayId")
                        val recovered = recoverExistingLogId()
                        if (recovered != null) {
                            backendLogId = recovered
                            lastBulkCreateSetCount = 0
                            flushPendingSetQueue()
                            recovered
                        } else {
                            Log.e("AIFIT_REGISTER", "409 but no existing log for dayId=$currentDayId")
                            _events.send(WorkoutSessionUiEvent.ShowSnackbar(result.exception.toMessage()))
                            null
                        }
                    } else {
                        Log.e("AIFIT_REGISTER", "Failed to create backend log: ${result.exception.message}")
                        _events.send(WorkoutSessionUiEvent.ShowSnackbar(result.exception.toMessage()))
                        null
                    }
                }
                else -> null
            }
        } finally {
            backendLogCreationInFlight = false
        }
    }

    private suspend fun recoverExistingLogId(): String? {
        val today = LocalDate.now().toString()
        return when (val result = findOpenLogForDayUseCase(currentPlanId, currentDayId, today)) {
            is Result.Success -> {
                val log = result.data ?: return null
                applyRecoveredLog(log)
                log.id
            }
            else -> null
        }
    }

    private fun applyRecoveredLog(log: WorkoutLog) {
        backendLogId = log.id
        persistedSetIds.clear()
        serverPersistedSetKeys.clear()
        registerServerSets(log.sets)
    }

    private fun registerServerSets(sets: List<WorkoutSetLog>) {
        sets.forEach { set ->
            serverPersistedSetKeys.add(set.trainingExerciseId to set.exerciseSetNumber)
        }
    }

    private fun enqueuePendingSet(localSetId: String, dto: LogWorkoutSetRequestDto) {
        if (pendingSets.none { it.localSetId == localSetId }) {
            pendingSets.add(PendingSetUpload(localSetId, dto))
        }
    }

    private suspend fun flushPendingSetQueue() {
        val logId = backendLogId ?: return
        val queued = pendingSets.toList()
        pendingSets.clear()
        for (pending in queued) {
            if (pending.localSetId in persistedSetIds) continue
            uploadSet(logId, pending.dto, pending.localSetId)
        }
    }

    private fun setKey(dto: LogWorkoutSetRequestDto): Pair<String, Int> =
        dto.trainingExerciseId to dto.exerciseSetNumber

    private fun markLocalSetPersisted(localSetId: String, dto: LogWorkoutSetRequestDto) {
        persistedSetIds.add(localSetId)
        serverPersistedSetKeys.add(setKey(dto))
    }

    private fun markAllLocalSetsPersisted(sets: List<WorkoutSetLog>) {
        sets.forEach { set ->
            markLocalSetPersisted(set.id, set.toRequestDto())
        }
    }

    private fun enqueueSetUpload(logId: String, setDto: LogWorkoutSetRequestDto, localSetId: String) {
        val job = viewModelScope.launch {
            uploadSet(logId, setDto, localSetId)
        }
        pendingSetJobs.add(job)
        job.invokeOnCompletion { pendingSetJobs.remove(job) }
    }

    private suspend fun uploadSet(
        logId: String,
        setDto: LogWorkoutSetRequestDto,
        localSetId: String? = null,
    ) {
        val key = setKey(setDto)
        if (key in serverPersistedSetKeys) {
            localSetId?.let { markLocalSetPersisted(it, setDto) }
            return
        }
        when (val result = addSetToLogUseCase(logId, setDto)) {
            is Result.Success -> localSetId?.let { markLocalSetPersisted(it, setDto) }
            is Result.Error -> {
                Log.e("AIFIT_REGISTER", "addSetToLog failed: ${result.exception.message}")
                _events.send(WorkoutSessionUiEvent.ShowSnackbar(result.exception.toMessage()))
            }
            else -> Unit
        }
    }

    private fun WorkoutSetLog.toRequestDto(): LogWorkoutSetRequestDto = LogWorkoutSetRequestDto(
        trainingExerciseId = trainingExerciseId,
        exerciseName = exerciseName,
        exerciseSetNumber = exerciseSetNumber,
        repsCompleted = repsCompleted,
        weightUsed = weightUsed,
        durationSeconds = durationSeconds,
        completed = completed,
    )

    // ===== ABANDON SESSION =====

    /**
     * Leave session: cancel timer, delete incomplete log in backend
     * and emits [WorkoutSessionUiEvent.NavigateBack].
     */
    fun abandonSession() {
        viewModelScope.launch {
            cancelRestTimer()

            // Delete incomplete backend log if one was created (fire-and-forget)
            backendLogId?.let { logId ->
                viewModelScope.launch { deleteWorkoutLogUseCase(logId) }
            }
            backendLogId = null
            pendingSets.clear()
            persistedSetIds.clear()
            serverPersistedSetKeys.clear()

            _events.send(WorkoutSessionUiEvent.NavigateBack)
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
            _events.send(WorkoutSessionUiEvent.ShowSnackbar("Rest complete"))
            _restTimerSeconds.value = null
        }
    }

    /** Cancels the active sleep timer and hides the overlay.*/
    fun cancelRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        _restTimerSeconds.value = null
    }

    // ===== SUBSTITUTIONS =====

    /**
     * Load suggested substitutions for an exercise from the backend.
     *
     * @param exerciseId Identifier of the exercise to replace.
     */
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

    /**
     * Applies a substitution updating the name and muscle of the exercise in the active session.
     *
     * @param originalExerciseId Exercise to be replaced.
     * @param substitution Alternative chosen by the user.
     */
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
