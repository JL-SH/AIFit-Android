package com.jlsh.aifit.feature.training.ui.state

import com.jlsh.aifit.feature.training.domain.model.TrainingDay

sealed interface TrainingDayItem {
    data class Training(val day: TrainingDay) : TrainingDayItem
    data class Rest(val day: TrainingDay) : TrainingDayItem
}

