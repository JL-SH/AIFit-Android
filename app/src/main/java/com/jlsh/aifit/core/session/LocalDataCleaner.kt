package com.jlsh.aifit.core.session

import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataCleaner @Inject constructor(
    private val trainingPlanDao: TrainingPlanDao,
    private val dietPlanDao: DietPlanDao,
) {
    suspend fun clearDataForUser(userId: String) {
        trainingPlanDao.deleteAllByUserId(userId)
        dietPlanDao.deleteAllByUserId(userId)
    }
}

