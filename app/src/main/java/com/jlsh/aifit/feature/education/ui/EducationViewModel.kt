package com.jlsh.aifit.feature.education.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.education.domain.usecase.GetExerciseExplanationUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetGlossaryTermUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetMealExplanationUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetWhyThisExerciseUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetWhyThisMealUseCase
import com.jlsh.aifit.feature.education.ui.state.ExplanationState
import com.jlsh.aifit.feature.education.ui.state.GlossaryState
import com.jlsh.aifit.feature.education.ui.state.WhyThisState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EducationViewModel @Inject constructor(
    private val getExerciseExplanationUseCase: GetExerciseExplanationUseCase,
    private val getMealExplanationUseCase: GetMealExplanationUseCase,
    private val getWhyThisExerciseUseCase: GetWhyThisExerciseUseCase,
    private val getWhyThisMealUseCase: GetWhyThisMealUseCase,
    private val getGlossaryTermUseCase: GetGlossaryTermUseCase,
) : ViewModel() {

    private val _explanationState = MutableStateFlow<ExplanationState>(ExplanationState.Idle)
    val explanationState: StateFlow<ExplanationState> = _explanationState.asStateFlow()

    private val _whyThisState = MutableStateFlow<WhyThisState>(WhyThisState.Idle)
    val whyThisState: StateFlow<WhyThisState> = _whyThisState.asStateFlow()

    private val _glossaryState = MutableStateFlow<GlossaryState>(GlossaryState.Idle)
    val glossaryState: StateFlow<GlossaryState> = _glossaryState.asStateFlow()

    fun loadExerciseExplanation(exerciseId: String) {
        viewModelScope.launch {
            _explanationState.value = ExplanationState.Loading
            when (val result = getExerciseExplanationUseCase(exerciseId)) {
                is Result.Success -> _explanationState.value = ExplanationState.Success(result.data)
                is Result.Error -> _explanationState.value = ExplanationState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    fun loadMealExplanation(mealId: String) {
        viewModelScope.launch {
            _explanationState.value = ExplanationState.Loading
            when (val result = getMealExplanationUseCase(mealId)) {
                is Result.Success -> _explanationState.value = ExplanationState.Success(result.data)
                is Result.Error -> _explanationState.value = ExplanationState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    fun loadWhyThisExercise(exerciseId: String) {
        viewModelScope.launch {
            _whyThisState.value = WhyThisState.Loading
            when (val result = getWhyThisExerciseUseCase(exerciseId)) {
                is Result.Success -> _whyThisState.value = WhyThisState.Success(result.data)
                is Result.Error -> _whyThisState.value = WhyThisState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    fun loadWhyThisMeal(mealId: String) {
        viewModelScope.launch {
            _whyThisState.value = WhyThisState.Loading
            when (val result = getWhyThisMealUseCase(mealId)) {
                is Result.Success -> _whyThisState.value = WhyThisState.Success(result.data)
                is Result.Error -> _whyThisState.value = WhyThisState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    fun loadGlossaryTerm(term: String) {
        viewModelScope.launch {
            _glossaryState.value = GlossaryState.Loading
            when (val result = getGlossaryTermUseCase(term)) {
                is Result.Success -> _glossaryState.value = GlossaryState.Success(result.data)
                is Result.Error -> _glossaryState.value = GlossaryState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    fun resetExplanationState() {
        _explanationState.value = ExplanationState.Idle
    }

    fun resetWhyThisState() {
        _whyThisState.value = WhyThisState.Idle
    }

    fun resetGlossaryState() {
        _glossaryState.value = GlossaryState.Idle
    }
}

