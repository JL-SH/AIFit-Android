package com.jlsh.aifit.feature.home.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.home.data.dto.HomeBootstrapResponseDto
import retrofit2.http.GET

interface HomeApiService {

    @GET("home/bootstrap")
    suspend fun getBootstrap(): ApiResponse<HomeBootstrapResponseDto>
}
