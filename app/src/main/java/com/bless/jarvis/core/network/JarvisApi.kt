package com.bless.jarvis.core.network
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface JarvisApi {
 @GET("api/health") suspend fun health(): HealthResponse
 @POST("api/chat") suspend fun chat(@Body request: ChatRequest): ChatResponse
 @POST("api/capabilities") suspend fun register(@Body request: CapabilityRequest): Map<String, Any?>
 @POST("api/world") suspend fun world(@Body request: WorldRequest): Map<String, Any?>
}