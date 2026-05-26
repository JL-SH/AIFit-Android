package com.jlsh.aifit.feature.home.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeBootstrapUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(): Result<HomeBootstrap> = repository.getBootstrap()
}
