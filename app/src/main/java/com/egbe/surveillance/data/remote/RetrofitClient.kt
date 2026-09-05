package com.egbe.surveillance.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Default value (used only the first time)
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:7777/"

    private var currentBaseUrl: String = DEFAULT_BASE_URL
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /** Call this once at app start (from Application or MainActivity) */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("egbe_prefs", Context.MODE_PRIVATE)
        currentBaseUrl = prefs.getString("base_url", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        rebuild()
    }

    /** Call this when the user changes the URL in the app */
    fun updateBaseUrl(context: Context, newUrl: String) {
        var url = newUrl.trim()
        if (!url.endsWith("/")) url += "/"
        if (!url.startsWith("http")) url = "http://$url"

        currentBaseUrl = url

        val prefs = context.getSharedPreferences("egbe_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("base_url", currentBaseUrl).apply()

        rebuild()
    }

    fun getCurrentBaseUrl(): String = currentBaseUrl

    private fun rebuild() {
        retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit!!.create(ApiService::class.java)
    }

    val api: ApiService
        get() {
            if (apiService == null) {
                // Fallback if init() was never called
                rebuild()
            }
            return apiService!!
        }
}