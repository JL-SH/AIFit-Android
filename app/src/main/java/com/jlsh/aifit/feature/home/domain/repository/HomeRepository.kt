package com.jlsh.aifit.feature.home.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap

interface HomeRepository {
    suspend fun getBootstrap(): Result<HomeBootstrap>
    suspend fun getCachedBootstrap(): HomeBootstrap?
}
