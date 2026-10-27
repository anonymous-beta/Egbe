
package com.egbe.surveillance.data.repository

import com.egbe.surveillance.data.model.*
import com.egbe.surveillance.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class SurveillanceRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun analyzePhone(phone: String): Result<TraceResponse> =
        withContext(Dispatchers.IO) {
            safeCall {
                val response = api.tracePhone(TraceRequest(phone = phone.trim()))
                if (!response.isSuccessful) {
                    throw IOException(
                        response.errorBody()?.string()?.take(300)
                            ?: "Phone metadata request failed: ${response.code()}"
                    )
                }
                response.body() ?: throw IOException("Empty phone metadata response.")
            }
        }

    suspend fun getSatellites(
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<SatelliteResponse> = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.getSatellites(latitude, longitude)
            if (!response.isSuccessful) {
                throw IOException(
                    response.errorBody()?.string()?.take(300)
                        ?: "Satellite request failed: ${response.code()}"
                )
            }
            response.body() ?: throw IOException("Empty satellite response.")
        }
    }

    suspend fun lookupIp(ip: String): Result<IpInfo> =
        withContext(Dispatchers.IO) {
            safeCall {
                val response = api.geolocateIp(IpRequest(ip.trim()))
                if (!response.isSuccessful) {
                    throw IOException(
                        response.errorBody()?.string()?.take(300)
                            ?: "IP lookup failed: ${response.code()}"
                    )
                }
                (response.body()
                    ?: throw IOException("Empty IP response.")).toIpInfo()
            }
        }

    suspend fun createCampaign(
        title: String,
        redirectUrl: String
    ): Result<PhishingCampaign> = withContext(Dispatchers.IO) {
        safeCall {
            val response = api.createPhishingCampaign(
                PhishingCreateRequest(
                    title = title.trim().ifBlank { "Security Awareness Exercise" },
                    redirect_url = redirectUrl.trim()
                )
            )
            if (!response.isSuccessful) {
                throw IOException(
                    response.errorBody()?.string()?.take(300)
                        ?: "Campaign creation failed: ${response.code()}"
                )
            }
            response.body() ?: throw IOException("Empty campaign response.")
        }
    }

    suspend fun refreshCampaign(id: String): Result<PhishingCampaign> =
        withContext(Dispatchers.IO) {
            safeCall {
                val response = api.getCampaign(id)
                if (!response.isSuccessful) {
                    throw IOException(
                        response.errorBody()?.string()?.take(300)
                            ?: "Campaign lookup failed: ${response.code()}"
                    )
                }
                response.body() ?: throw IOException("Empty campaign response.")
            }
        }

    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
}
