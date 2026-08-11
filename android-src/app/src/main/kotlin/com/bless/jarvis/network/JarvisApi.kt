package com.bless.jarvis.network

import retrofit2.http.Body
import retrofit2.http.POST

interface JarvisApi {
    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("api/action-result")
    suspend fun reportActionResult(@Body request: ActionResultRequest): ActionResultResponse
}
