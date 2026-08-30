package com.egbe.surveillance.data.remote

import com.egbe.surveillance.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/trace")
    suspend fun tracePhone(@Body request: TraceRequest): Response<TraceResponse>

    @GET("api/trace/live/{trace_id}")
    suspend fun getLiveTrace(@Path("trace_id") traceId: String): Response<TraceResponse>

    @GET("api/satellites")
    suspend fun getSatellites(
        @Query("lat") lat: Double = 0.0,
        @Query("lon") lon: Double = 0.0
    ): Response<SatelliteResponse>

    @POST("api/phishing/create")
    suspend fun createPhishingCampaign(@Body request: PhishingCreateRequest): Response<PhishingCampaign>

    @GET("api/phishing/campaign/{id}")
    suspend fun getCampaign(@Path("id") id: String): Response<PhishingCampaign>

    @POST("api/geolocate/ip")
    suspend fun geolocateIp(@Body request: IpRequest): Response<IpResponse>
}

data class TraceRequest(val phone: String)
data class SatelliteResponse(val all: List<Satellite>, val visible: List<Satellite>)
data class PhishingCreateRequest(val title: String, val redirect_url: String)
data class IpRequest(val ip: String)
data class IpResponse(val ip: String, val city: String, val region: String, val country: String, val loc: String, val org: String)
