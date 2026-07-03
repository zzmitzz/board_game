package com.alantech.boardgame.di

import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.data.remote.HomeDataEndpoint
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"
    private const val API_KEY = "sb_publishable_li1QqF2ov_VHDuocLiwikg_rNff-vGM"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val content_type_desc = "application/json"

    private val contentType = content_type_desc.toMediaType()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. Create Auth Interceptor
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()
        chain.proceed(request)
    }

    // 3. Build OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val apiService by lazy {
        retrofit.create(BoardGameEndpoint::class.java)
    }

    val homeDataEndpoint by lazy {
        retrofit.create(HomeDataEndpoint::class.java)
    }
}