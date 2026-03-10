package com.jlsh.aifit.feature.auth.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.auth.data.dto.AuthResponseDto
import com.jlsh.aifit.feature.auth.data.dto.GoogleLoginRequestDto
import com.jlsh.aifit.feature.auth.data.dto.LoginRequestDto
import com.jlsh.aifit.feature.auth.data.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponse<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponse<AuthResponseDto>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequestDto): ApiResponse<AuthResponseDto>
}

