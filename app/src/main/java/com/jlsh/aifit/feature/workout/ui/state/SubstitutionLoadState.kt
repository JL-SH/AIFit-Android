package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution

sealed interface SubstitutionLoadState {
    data object Idle : SubstitutionLoadState
    data object Loading : SubstitutionLoadState
    data class Success(val substitutions: List<ExerciseSubstitution>) : SubstitutionLoadState
    data class Error(val message: String) : SubstitutionLoadState
}

