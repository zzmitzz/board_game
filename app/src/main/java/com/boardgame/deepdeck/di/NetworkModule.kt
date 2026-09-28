package com.boardgame.deepdeck.di

import com.boardgame.deepdeck.BuildConfig
import com.boardgame.deepdeck.data.remote.BoardGameEndpoint
import com.boardgame.deepdeck.data.remote.HomeDataEndpoint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Only the X-API-Key header is sent (no apikey / Bearer duplicates).
        val authInterceptor = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("X-API-Key", BuildConfig.API_KEY)
                    .build()
            )
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                // Request logging only in debug builds; never log bodies/keys in release.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                            redactHeader("X-API-Key")
                        }
                    )
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideBoardGameAPI(retrofit: Retrofit): BoardGameEndpoint =
        retrofit.create(BoardGameEndpoint::class.java)

    @Provides
    @Singleton
    fun provideHomeDataAPI(retrofit: Retrofit): HomeDataEndpoint =
        retrofit.create(HomeDataEndpoint::class.java)
}
