package com.jlsh.aifit.feature.chat.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.chat.data.dto.ChatMessageResponseDto
import com.jlsh.aifit.feature.chat.data.dto.ChatSessionResponseDto
import com.jlsh.aifit.feature.chat.data.dto.ChatSessionSummaryResponseDto
import com.jlsh.aifit.feature.chat.data.dto.GenerateTitleResponseDto
import com.jlsh.aifit.feature.chat.data.dto.RenameChatSessionRequestDto
import com.jlsh.aifit.feature.chat.data.dto.SendChatMessageRequestDto
import com.jlsh.aifit.feature.chat.data.dto.StartChatSessionRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {

    @POST("chat/sessions")
    suspend fun startSession(
        @Body request: StartChatSessionRequestDto,
    ): ApiResponse<ChatSessionResponseDto>

    @GET("chat/sessions")
    suspend fun getSessions(): ApiResponse<List<ChatSessionSummaryResponseDto>>

    @GET("chat/sessions/{id}")
    suspend fun getSession(
        @Path("id") id: String,
    ): ApiResponse<ChatSessionResponseDto>

    @POST("chat/sessions/{id}/messages")
    suspend fun sendMessage(
        @Path("id") sessionId: String,
        @Body request: SendChatMessageRequestDto,
    ): ApiResponse<ChatMessageResponseDto>

    @PATCH("chat/sessions/{id}/archive")
    suspend fun archiveSession(
        @Path("id") id: String,
    ): ApiResponse<Unit>

    @PATCH("chat/sessions/{id}")
    suspend fun renameSession(
        @Path("id") id: String,
        @Body request: RenameChatSessionRequestDto,
    ): ApiResponse<ChatSessionResponseDto>

    @POST("chat/sessions/{id}/generate-title")
    suspend fun generateTitle(
        @Path("id") id: String,
    ): ApiResponse<GenerateTitleResponseDto>

    @DELETE("chat/sessions/{id}")
    suspend fun deleteSession(
        @Path("id") id: String,
    ): Response<Unit>
}

