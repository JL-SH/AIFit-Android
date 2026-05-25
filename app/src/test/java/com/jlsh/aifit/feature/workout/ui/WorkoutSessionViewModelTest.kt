package com.jlsh.aifit.feature.workout.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.usecase.GetExerciseSubstitutionsUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetWarmUpProtocolUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.AddSetToLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.DeleteWorkoutLogUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.FinalizeWorkoutSessionUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetPreviousSessionForDayUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
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
import kotlinx.coroutines.flow.flowOf
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
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase = mockk()
    private val getExerciseSubstitutionsUseCase: GetExerciseSubstitutionsUseCase = mockk()
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase = mockk()

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

    @Test
    fun `registerSet recovers from 409 and calls addSetToLog`() = runTest {
        val day = fakeTrainingDay(
            id = "day-1",
            dayType = TrainingDayType.TRAINING,
            exercises = listOf(fakeTrainingExercise(id = "ex-1", sets = 4)),
        )
        val plan = fakeTrainingPlan(id = "plan-1", days = listOf(day))
        val existingLog = fakeWorkoutLog(
            id = "log-existing",
            trainingPlanId = "plan-1",
            trainingDayId = "day-1",
            date = LocalDate.now(),
            isLocked = false,
        )

        coEvery { getTrainingPlanDetailUseCase("plan-1") } returns Result.Success(plan)
        coEvery { getPreviousSessionForDayUseCase("plan-1", "day-1") } returns Result.Success(null)
        every {
            getWorkoutHistoryUseCase(planId = "plan-1", from = any(), to = any())
        } returnsMany listOf(
            flowOf(Result.Success(emptyList())),
            flowOf(Result.Success(listOf(existingLog))),
        )
        coEvery { getWarmUpProtocolUseCase("plan-1", "day-1") } returns Result.Error(AppException.NetworkException)
        coEvery { logWorkoutSessionUseCase(any()) } returns Result.Error(AppException.ConflictException)
        coEvery { addSetToLogUseCase("log-existing", any()) } returns Result.Success(Unit)

        val vm = WorkoutSessionViewModel(
            getWarmUpProtocolUseCase = getWarmUpProtocolUseCase,
            logWorkoutSessionUseCase = logWorkoutSessionUseCase,
            addSetToLogUseCase = addSetToLogUseCase,
            deleteWorkoutLogUseCase = deleteWorkoutLogUseCase,
            finalizeWorkoutSessionUseCase = finalizeWorkoutSessionUseCase,
            getPreviousSessionForDayUseCase = getPreviousSessionForDayUseCase,
            getWorkoutHistoryUseCase = getWorkoutHistoryUseCase,
            getExerciseSubstitutionsUseCase = getExerciseSubstitutionsUseCase,
            getTrainingPlanDetailUseCase = getTrainingPlanDetailUseCase,
            savedStateHandle = SavedStateHandle(
                mapOf("planId" to "plan-1", "dayId" to "day-1"),
            ),
        )

        advanceUntilIdle()
        assertTrue(vm.uiState.value is WorkoutSessionUiState.SessionActive)

        vm.registerSet(exerciseId = "ex-1", weightKg = 60.0, reps = 10, rpe = 8)
        advanceUntilIdle()

        coVerify { logWorkoutSessionUseCase(any()) }
        coVerify { addSetToLogUseCase("log-existing", any()) }
    }
}
