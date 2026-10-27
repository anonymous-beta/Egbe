
package com.egbe.surveillance.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:7777/"

    @Volatile
    private var currentBaseUrl = DEFAULT_BASE_URL

    @Volatile
    private var apiService: ApiService? = null

    private val logging = HttpLoggingInterceptor().apply {
        // Do not log request/response bodies: they may contain personal data.
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    @Synchronized
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("egbe_prefs", Context.MODE_PRIVATE)
        currentBaseUrl = normalizeBaseUrl(
            prefs.getString("base_url", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        )
        rebuild()
    }

    @Synchronized
    fun updateBaseUrl(context: Context, newUrl: String) {
        val normalized = normalizeBaseUrl(newUrl)
        currentBaseUrl = normalized
        context.getSharedPreferences("egbe_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("base_url", normalized)
            .apply()
        rebuild()
    }

    fun getCurrentBaseUrl(): String = currentBaseUrl

    private fun normalizeBaseUrl(raw: String): String {
        val value = raw.trim()
        require(value.isNotEmpty()) { "Backend URL cannot be empty." }

        val withScheme = if (
            value.startsWith("https://", true) ||
            value.startsWith("http://", true)
        ) value else "https://$value"

        val uri = URI(withScheme)
        require(uri.scheme == "https" || uri.scheme == "http") {
            "Backend URL must use HTTP or HTTPS."
        }
        require(uri.host != null && uri.userInfo == null) {
            "Backend URL is invalid."
        }
        require(uri.query == null && uri.fragment == null) {
            "Backend URL must not contain a query or fragment."
        }

        return withScheme.trimEnd('/') + "/"
    }

    @Synchronized
    private fun rebuild() {
        apiService = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val api: ApiService
        get() {
            if (apiService == null) {
                synchronized(this) {
                    if (apiService == null) rebuild()
                }
            }
            return apiService!!
        }
}
