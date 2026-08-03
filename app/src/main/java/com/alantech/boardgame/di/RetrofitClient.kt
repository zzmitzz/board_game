package com.alantech.boardgame.di

import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.data.remote.HomeDataEndpoint
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool

object RetrofitClient {

    private const val BASE_URL = "http://localhost:8000/"
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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
        .eventListener(object : EventListener() {
            override fun callStart(call: Call) {
                println("OkHttpTrace: Call Started")
            }

            override fun dnsStart(call: Call, domainName: String) {
                println("OkHttpTrace: DNS Lookup Started for $domainName")
            }

            override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
                println("OkHttpTrace: DNS Resolved to $inetAddressList")
            }

            override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
                println("OkHttpTrace: Attempting socket connection to $inetSocketAddress via proxy: $proxy")
            }

            override fun connectFailed(
                call: Call,
                inetSocketAddress: InetSocketAddress,
                proxy: Proxy,
                protocol: okhttp3.Protocol?,
                ioe: IOException
            ) {
                println("OkHttpTrace: Connection FAILED to $inetSocketAddress. Error: ${ioe.message}")
                ioe.printStackTrace() // This forces the full internal stack trace into Logcat
            }

            override fun connectEnd(
                call: Call,
                inetSocketAddress: InetSocketAddress,
                proxy: Proxy,
                protocol: okhttp3.Protocol?
            ) {
                println("OkHttpTrace: Connection successfully established!")
            }
        })
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