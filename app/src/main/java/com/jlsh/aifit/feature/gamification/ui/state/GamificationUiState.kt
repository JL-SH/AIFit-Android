package com.jlsh.aifit.feature.gamification.ui.state

import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.PersonalRecord
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement

sealed class GamificationUiState {
    data object Loading : GamificationUiState()
    data class Error(val message: String) : GamificationUiState()
    data class Success(
        val streaks: List<Streak>,
        val achievements: List<UserAchievement>,
        val allDefinitions: List<AchievementDefinition>,
        val personalRecords: List<PersonalRecord>,
        val selectedTabIndex: Int = 0,
    ) : GamificationUiState()
}

sealed class ExportUiState {
    data object Idle : ExportUiState()
    data object Loading : ExportUiState()
    data class Error(val message: String) : ExportUiState()
    data class Success(val export: ProgressExport) : ExportUiState()
}

