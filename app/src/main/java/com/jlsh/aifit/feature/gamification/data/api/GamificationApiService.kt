package com.jlsh.aifit.feature.gamification.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.gamification.data.dto.AchievementDefinitionResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.PersonalRecordResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.ProgressExportResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.StreakResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.UserAchievementResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GamificationApiService {

    @GET("gamification/streaks")
    suspend fun getStreaks(): ApiResponse<List<StreakResponseDto>>

    @GET("gamification/achievements")
    suspend fun getAchievements(): ApiResponse<List<UserAchievementResponseDto>>

    @GET("gamification/achievements/all")
    suspend fun getAllAchievementDefinitions(): ApiResponse<List<AchievementDefinitionResponseDto>>

    @GET("gamification/personal-records")
    suspend fun getPersonalRecords(): ApiResponse<List<PersonalRecordResponseDto>>

    @GET("gamification/export")
    suspend fun getExport(@Query("period") period: String): ApiResponse<ProgressExportResponseDto>
}

