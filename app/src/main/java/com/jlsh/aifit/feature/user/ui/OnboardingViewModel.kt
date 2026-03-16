package com.jlsh.aifit.feature.user.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GenerateDietPlanUseCase
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
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

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val generateTrainingPlanUseCase: GenerateTrainingPlanUseCase,
    private val generateDietPlanUseCase: GenerateDietPlanUseCase,
    private val deleteTrainingPlanUseCase: DeleteTrainingPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun generatePlan(feedback: String? = null) {
        viewModelScope.launch {
            _state.value = OnboardingState.Generating
            when (val result = completeOnboardingUseCase(feedback)) {
                is Result.Success -> _state.value = OnboardingState.Ready(result.data)
                is Result.Error -> _state.value =
                    OnboardingState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

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

    fun confirmOnboarding() {
        sessionManager.setProfileComplete(true)
    }

    fun reset() {
        _state.value = OnboardingState.Idle
    }
}

sealed class OnboardingState {
    data object Idle : OnboardingState()
    data object Generating : OnboardingState()
    data object RegeneratingTraining : OnboardingState()
    data object RegeneratingDiet : OnboardingState()
    data class Ready(val result: OnboardingResult) : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}
