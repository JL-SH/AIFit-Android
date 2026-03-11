package com.jlsh.aifit.feature.shopping.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.data.dto.ShoppingListResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ShoppingApiService {

    @GET("shopping-lists")
    suspend fun getLists(): ApiResponse<List<ShoppingListResponseDto>>

    @POST("shopping-lists/generate")
    suspend fun generateList(
        @Body request: GenerateShoppingListRequestDto,
    ): ApiResponse<ShoppingListResponseDto>

    @GET("shopping-lists/{id}")
    suspend fun getList(
        @Path("id") id: String,
    ): ApiResponse<ShoppingListResponseDto>

    @DELETE("shopping-lists/{id}")
    suspend fun deleteList(
        @Path("id") id: String,
    ): ApiResponse<Unit>
}

