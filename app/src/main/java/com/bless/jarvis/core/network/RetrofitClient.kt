package com.bless.jarvis.core.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient { const val BASE_URL = "https://jarvis-ai-27rf.onrender.com/"; val api: JarvisApi by lazy { Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(JarvisApi::class.java) } }