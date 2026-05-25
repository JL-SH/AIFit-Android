package com.jlsh.aifit.feature.user.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toEntity
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GenerateDietPlanUseCase
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toEntity
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.usecase.DeleteTrainingPlanUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GenerateTrainingPlanUseCase
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.usecase.CompleteOnboardingUseCase
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel of the onboarding flow: generation and regeneration of plans.
 *
 * **Exposed state** ([state] — [OnboardingState]):
 * - [OnboardingState.Idle]: No generation in progress.
 * - [OnboardingState.Generating]: completing onboarding on the server.
 * - [OnboardingState.RegeneratingTraining]: regenerating only training plan.
 * - [OnboardingState.RegeneratingDiet]: regenerating diet plan only.
 * - [OnboardingState.Ready]: plans and nutritional goal ready.
 * - [OnboardingState.Error]: error message.
 *
 * Does not emit navigation events; the UI looks at [state] directly.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val generateTrainingPlanUseCase: GenerateTrainingPlanUseCase,
    private val generateDietPlanUseCase: GenerateDietPlanUseCase,
    private val deleteTrainingPlanUseCase: DeleteTrainingPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val sessionManager: SessionManager,
    private val trainingPlanDao: TrainingPlanDao,
    private val dietPlanDao: DietPlanDao,
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)

    /** Onboarding status (see class documentation).*/
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    /**
     * Complete backend onboarding and get initial plans.
     *
     * @param feedback Optional user feedback for the AI.
     */
    fun generatePlan(feedback: String? = null) {
        viewModelScope.launch {
            _state.value = OnboardingState.Generating
            when (val result = completeOnboardingUseCase(feedback)) {
                is Result.Success -> {
                    persistPlansToCache(result.data.trainingPlan, result.data.dietPlan)
                    _state.value = OnboardingState.Ready(result.data)
                }
                is Result.Error -> _state.value =
                    OnboardingState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    /**
     * Regenerate the training plan while maintaining current diet and nutrition.
     *
     * @param feedback Additional notes for the build.
     */
    fun regenerateTraining(feedback: String? = null) {
        val previous = _state.value as? OnboardingState.Ready
        viewModelScope.launch {
            _state.value = OnboardingState.RegeneratingTraining

            previous?.result?.trainingPlan?.id?.let { oldId ->
                deleteTrainingPlanUseCase(oldId)
            }

            when (val profileResult = getUserProfileUseCase().first { it !is Result.Loading }) {
                is Result.Success -> {
                    val profile = profileResult.data
                    val request = GenerateTrainingPlanRequestDto(
                        frequencyDaysPerWeek = profile.weeklyWorkoutDays ?: 3,
                        sessionDurationMinutes = profile.availableMinutesPerSession ?: 60,
                        durationWeeks = 8,
                        goalType = profile.goalType?.name ?: "",
                        fitnessLevel = profile.fitnessLevel?.name ?: "",
                        location = profile.workoutLocation?.name ?: "",
                        injuries = profile.injuries,
                        additionalNotes = feedback,
                    )
                    when (val planResult = generateTrainingPlanUseCase(request)) {
                        is Result.Success -> {
                            val current = previous ?: (_state.value as? OnboardingState.Ready)
                            if (current != null) {
                                persistTrainingPlanToCache(planResult.data)
                                _state.value = OnboardingState.Ready(
                                    current.result.copy(trainingPlan = planResult.data)
                                )
                            } else {
                                _state.value = OnboardingState.Error("Estado inválido")
                            }
                        }
                        is Result.Error -> _state.value =
                            OnboardingState.Error(planResult.exception.toMessage())
                        else -> Unit
                    }
                }
                is Result.Error -> _state.value =
                    OnboardingState.Error(profileResult.exception.toMessage())
                else -> Unit
            }
        }
    }

    /**
     * Regenerate the diet plan by maintaining the current training plan.
     *
     * @param feedback Additional notes for the build.
     */
    fun regenerateDiet(feedback: String? = null) {
        val previous = _state.value as? OnboardingState.Ready
        viewModelScope.launch {
            _state.value = OnboardingState.RegeneratingDiet

            previous?.result?.dietPlan?.id?.let { oldId ->
                deleteDietPlanUseCase(oldId)
            }

            when (val profileResult = getUserProfileUseCase().first { it !is Result.Loading }) {
                is Result.Success -> {
                    val profile = profileResult.data
                    val request = GenerateDietPlanRequestDto(
                        durationWeeks = 8,
                        mealsPerDay = 3,
                        dietPreference = profile.dietPreference?.name ?: "NONE",
                        goalType = profile.goalType?.name,
                        additionalNotes = feedback,
                    )
                    when (val planResult = generateDietPlanUseCase(request)) {
                        is Result.Success -> {
                            val current = previous ?: (_state.value as? OnboardingState.Ready)
                            if (current != null) {
                                persistDietPlanToCache(planResult.data)
                                _state.value = OnboardingState.Ready(
                                    current.result.copy(dietPlan = planResult.data)
                                )
                            } else {
                                _state.value = OnboardingState.Error("Estado inválido")
                            }
                        }
                        is Result.Error -> _state.value =
                            OnboardingState.Error(planResult.exception.toMessage())
                        else -> Unit
                    }
                }
                is Result.Error -> _state.value =
                    OnboardingState.Error(profileResult.exception.toMessage())
                else -> Unit
            }
        }
    }

    /** Mark profile as complete in session after confirming plans.*/
    fun confirmOnboarding() {
        sessionManager.setProfileComplete(true)
    }

    /** Restores the state to [OnboardingState.Idle].*/
    fun reset() {
        _state.value = OnboardingState.Idle
    }

    // ── Cache persistence helpers ──

    private suspend fun persistPlansToCache(
        trainingPlan: TrainingPlan,
        dietPlan: DietPlan,
    ) {
        val userId = sessionManager.getUserId() ?: return
        trainingPlanDao.upsertAll(listOf(trainingPlan.toEntity(userId)))
        dietPlanDao.upsertAll(listOf(dietPlan.toEntity(userId)))
    }

    private suspend fun persistTrainingPlanToCache(plan: TrainingPlan) {
        val userId = sessionManager.getUserId() ?: return
        trainingPlanDao.upsertAll(listOf(plan.toEntity(userId)))
    }

    private suspend fun persistDietPlanToCache(plan: DietPlan) {
        val userId = sessionManager.getUserId() ?: return
        dietPlanDao.upsertAll(listOf(plan.toEntity(userId)))
    }
}

/**
 * Plan generation flow states in onboarding.
 */
sealed class OnboardingState {

    /** Waiting for user action.*/
    data object Idle : OnboardingState()

    /** First generation (training + diet + nutrition).*/
    data object Generating : OnboardingState()

    /** Regeneration only of the training plan.*/
    data object RegeneratingTraining : OnboardingState()

    /** Regeneration only from the diet plan.*/
    data object RegeneratingDiet : OnboardingState()

    /**
     * Plans ready for review.
     *
     * @property result Plans and nutritional goal returned by the backend.
     */
    data class Ready(val result: OnboardingResult) : OnboardingState()

    /**
     * Error in generation or regeneration.
     *
     * @property message Message to display to the user.
     */
    data class Error(val message: String) : OnboardingState()
}
