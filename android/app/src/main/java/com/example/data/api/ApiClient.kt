package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    private var currentBaseUrl = DEFAULT_BASE_URL

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit = buildRetrofit(currentBaseUrl)
    var apiService: ApiService = retrofit.create(ApiService::class.java)
        private set

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newUrl: String): Boolean {
        val trimmed = newUrl.trim()
        if (trimmed.isBlank()) return false
        val normalized = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
        val sanitized = if (!normalized.endsWith("/")) "$normalized/" else normalized
        return try {
            val newRetrofit = buildRetrofit(sanitized)
            val newService = newRetrofit.create(ApiService::class.java)
            currentBaseUrl = sanitized
            retrofit = newRetrofit
            apiService = newService
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun buildRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
