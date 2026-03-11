package com.jlsh.aifit.feature.education.ui.state

import com.jlsh.aifit.feature.education.domain.model.ContextualExplanation
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation

sealed class ExplanationState {
    data object Idle : ExplanationState()
    data object Loading : ExplanationState()
    data class Success(val data: ContextualExplanation) : ExplanationState()
    data class Error(val message: String) : ExplanationState()
}

sealed class WhyThisState {
    data object Idle : WhyThisState()
    data object Loading : WhyThisState()
    data class Success(val data: WhyThisExplanation) : WhyThisState()
    data class Error(val message: String) : WhyThisState()
}

sealed class GlossaryState {
    data object Idle : GlossaryState()
    data object Loading : GlossaryState()
    data class Success(val data: GlossaryDefinition) : GlossaryState()
    data class Error(val message: String) : GlossaryState()
}

