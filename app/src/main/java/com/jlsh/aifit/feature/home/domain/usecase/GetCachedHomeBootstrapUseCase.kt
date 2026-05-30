package com.jlsh.aifit.feature.home.domain.usecase

import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

/** Reads the last persisted home bootstrap snapshot from Room (no network). */
class GetCachedHomeBootstrapUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(): HomeBootstrap? = repository.getCachedBootstrap()
}
