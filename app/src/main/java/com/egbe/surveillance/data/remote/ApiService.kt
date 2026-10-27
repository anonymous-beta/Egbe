
package com.egbe.surveillance.data.remote

import com.egbe.surveillance.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/trace")
    suspend fun tracePhone(
        @Body request: TraceRequest
    ): Response<TraceResponse>

    @GET("api/trace/live/{trace_id}")
    suspend fun getLiveTrace(
        @Path("trace_id") traceId: String
    ): Response<TraceResponse>

    @GET("api/satellites")
    suspend fun getSatellites(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): Response<SatelliteResponse>

    @POST("api/phishing/create")
    suspend fun createPhishingCampaign(
        @Body request: PhishingCreateRequest
    ): Response<PhishingCampaign>

    @GET("api/phishing/campaign/{id}")
    suspend fun getCampaign(
        @Path("id") id: String
    ): Response<PhishingCampaign>

    @POST("api/geolocate/ip")
    suspend fun geolocateIp(
        @Body request: IpRequest
    ): Response<IpResponse>

    @GET("api/health")
    suspend fun health(): Response<HealthResponse>
}

data class TraceRequest(
    val phone: String,
    val region: String = "NG"
)

data class SatelliteResponse(
    val all: List<Satellite>,
    val visible: List<Satellite>,
    val observerProvided: Boolean = false,
    val source: String = "unknown",
    val timestamp: String = "",
    val notice: String = ""
)

data class PhishingCreateRequest(
    val title: String,
    val redirect_url: String
)

data class IpRequest(
    val ip: String
)

data class IpResponse(
    val ip: String,
    val city: String,
    val region: String,
    val country: String,
    val countryCode: String,
    val isp: String,
    val org: String,
    val asName: String,
    val lat: Double?,
    val lon: Double?,
    val timezone: String,
    val mobile: Boolean?,
    val proxy: Boolean?,
    val hosting: Boolean?,
    val source: String,
    val accuracyNotice: String
) {
    fun toIpInfo() = IpInfo(
        ip = ip,
        city = city,
        region = region,
        country = country,
        countryCode = countryCode,
        isp = isp,
        org = org,
        asName = asName,
        lat = lat,
        lon = lon,
        timezone = timezone,
        mobile = mobile,
        proxy = proxy,
        hosting = hosting,
        source = source,
        accuracyNotice = accuracyNotice
    )
}

data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: String
)
