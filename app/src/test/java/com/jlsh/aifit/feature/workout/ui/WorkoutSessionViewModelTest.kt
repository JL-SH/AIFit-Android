package com.jlsh.aifit.feature.workout.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.usecase.GetExerciseSubstitutionsUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetWarmUpProtocolUseCase
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.domain.usecase.AddSetToLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.DeleteWorkoutLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.FindOpenLogForDayUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.FinalizeWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetPreviousSessionForDayUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.LogWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.ui.state.WorkoutSessionUiState
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeTrainingDay
import com.jlsh.aifit.testutil.fakeTrainingExercise
import com.jlsh.aifit.testutil.fakeTrainingPlan
import com.jlsh.aifit.testutil.fakeWorkoutLog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutSessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getWarmUpProtocolUseCase: GetWarmUpProtocolUseCase = mockk()
    private val logWorkoutSessionUseCase: LogWorkoutSessionUseCase = mockk()
    private val addSetToLogUseCase: AddSetToLogUseCase = mockk()
    private val deleteWorkoutLogUseCase: DeleteWorkoutLogUseCase = mockk()
    private val finalizeWorkoutSessionUseCase: FinalizeWorkoutSessionUseCase = mockk()
    private val getPreviousSessionForDayUseCase: GetPreviousSessionForDayUseCase = mockk()
    private val findOpenLogForDayUseCase: FindOpenLogForDayUseCase = mockk()
    private val getExerciseSubstitutionsUseCase: GetExerciseSubstitutionsUseCase = mockk()
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase = mockk()

    private val planId = "plan-1"
    private val dayId = "day-1"
    private val exerciseId = "ex-1"
    private val today = LocalDate.now().toString()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun createViewModel(): WorkoutSessionViewModel = WorkoutSessionViewModel(
        getWarmUpProtocolUseCase = getWarmUpProtocolUseCase,
        logWorkoutSessionUseCase = logWorkoutSessionUseCase,
        addSetToLogUseCase = addSetToLogUseCase,
        deleteWorkoutLogUseCase = deleteWorkoutLogUseCase,
        finalizeWorkoutSessionUseCase = finalizeWorkoutSessionUseCase,
        getPreviousSessionForDayUseCase = getPreviousSessionForDayUseCase,
        findOpenLogForDayUseCase = findOpenLogForDayUseCase,
        getExerciseSubstitutionsUseCase = getExerciseSubstitutionsUseCase,
        getTrainingPlanDetailUseCase = getTrainingPlanDetailUseCase,
        savedStateHandle = SavedStateHandle(mapOf("planId" to planId, "dayId" to dayId)),
    )

    private fun stubPlanAndSessionLoad(openLogOnLoad: Result<com.jlsh.aifit.feature.workout.domain.model.WorkoutLog?> = Result.Success(null)) {
        val day = fakeTrainingDay(
            id = dayId,
            dayType = TrainingDayType.TRAINING,
            exercises = listOf(fakeTrainingExercise(id = exerciseId, sets = 3)),
        )
        val plan = fakeTrainingPlan(id = planId, days = listOf(day))
        coEvery { getTrainingPlanDetailUseCase(planId) } returns Result.Success(plan)
        coEvery { getPreviousSessionForDayUseCase(planId, dayId) } returns Result.Success(null)
        coEvery { findOpenLogForDayUseCase(planId, dayId, today) } returns openLogOnLoad
        coEvery { getWarmUpProtocolUseCase(planId, dayId) } returns Result.Error(AppException.NetworkException)
    }

    @Test
    fun `registerSet recovers from 409 and calls addSetToLog`() = runTest {
        val existingLog = fakeWorkoutLog(
            id = "log-existing",
            trainingPlanId = planId,
            trainingDayId = dayId,
            date = LocalDate.now(),
            isLocked = false,
        )

        stubPlanAndSessionLoad()
        coEvery { logWorkoutSessionUseCase(any()) } returns Result.Error(AppException.ConflictException)
        coEvery { findOpenLogForDayUseCase(planId, dayId, today) } returnsMany listOf(
            Result.Success(null),
            Result.Success(existingLog),
        )
        coEvery { addSetToLogUseCase("log-existing", any()) } returns Result.Success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is WorkoutSessionUiState.SessionActive)

        vm.registerSet(exerciseId = exerciseId, weightKg = 60.0, reps = 10, rpe = 8)
        advanceUntilIdle()

        coVerify { logWorkoutSessionUseCase(any()) }
        coVerify(exactly = 1) { addSetToLogUseCase("log-existing", any()) }
    }

    @Test
    fun `finalize does not re-upload sets already flushed after delayed log creation`() = runTest {
        stubPlanAndSessionLoad()

        val createdLog = fakeWorkoutLog(
            id = "log-new",
            trainingPlanId = planId,
            trainingDayId = dayId,
            date = LocalDate.now(),
            isLocked = false,
        ).copy(
            sets = listOf(
                WorkoutSetLog(
                    id = "srv-3",
                    trainingExerciseId = exerciseId,
                    exerciseName = "Press",
                    exerciseSetNumber = 3,
                    repsCompleted = 8,
                    weightUsed = 60.0,
                    durationSeconds = null,
                    completed = true,
                ),
            ),
        )

        coEvery { logWorkoutSessionUseCase(any()) } returnsMany listOf(
            Result.Error(AppException.ConflictException),
            Result.Error(AppException.ConflictException),
            Result.Success(createdLog),
        )
        coEvery { findOpenLogForDayUseCase(planId, dayId, today) } returnsMany listOf(
            Result.Success(null),
            Result.Success(null),
            Result.Success(null),
            Result.Success(null),
        )
        coEvery { addSetToLogUseCase("log-new", any()) } returns Result.Success(Unit)
        coEvery { finalizeWorkoutSessionUseCase("log-new", any(), any()) } returns Result.Success(createdLog)

        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is WorkoutSessionUiState.SessionActive)

        vm.registerSet(exerciseId = exerciseId, weightKg = 60.0, reps = 10, rpe = 8)
        vm.registerSet(exerciseId = exerciseId, weightKg = 62.0, reps = 9, rpe = 8)
        vm.registerSet(exerciseId = exerciseId, weightKg = 64.0, reps = 8, rpe = 9)
        advanceUntilIdle()

        vm.finalizeSession(systemicFatigue = 5, jointPainReport = emptyList())
        advanceUntilIdle()

        coVerify(exactly = 2) { addSetToLogUseCase("log-new", any()) }
        coVerify(exactly = 1) { finalizeWorkoutSessionUseCase("log-new", any(), any()) }
    }
}
