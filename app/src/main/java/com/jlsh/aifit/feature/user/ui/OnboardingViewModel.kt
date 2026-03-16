package com.jlsh.aifit.feature.user.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
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
    data class Ready(val result: OnboardingResult) : OnboardingState()
    data class Error(val message: String) : OnboardingState()
}

