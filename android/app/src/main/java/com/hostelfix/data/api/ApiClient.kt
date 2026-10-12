package com.hostelfix.data.api

import android.content.Context
import com.hostelfix.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Injected per build type (debug: 10.0.2.2:8000, release: production domain)
    private val BASE_URL = try {
        com.hostelfix.BuildConfig.BASE_URL
    } catch (e: Throwable) {
        "http://10.0.2.2:8000/"
    }

    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    fun getService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            val service = createService(context)
            apiService = service
            service
        }
    }

    private fun createService(context: Context): ApiService {
        val tokenManager = TokenManager(context.applicationContext)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val instance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit = instance
        return instance.create(ApiService::class.java)
    }
}
