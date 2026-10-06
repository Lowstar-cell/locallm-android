package com.example.myapplication.model

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

interface LmStudioApiService {
    @GET("v1/models")
    suspend fun getModels(): ModelsResponse

    @GET("v1/models")
    suspend fun checkAuth(): Response<ResponseBody>

    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: ChatRequest): ChatResponse

    @POST("api/v1/chat")
    suspend fun v1Chat(@Body request: V1ChatRequest): V1ChatResponse

    @Streaming
    @POST("api/v1/chat")
    suspend fun v1ChatStream(@Body request: V1ChatRequest): Response<ResponseBody>

    @Streaming
    @POST("v1/chat/completions")
    suspend fun chatCompletionStream(@Body request: ChatRequest): Response<ResponseBody>
}
