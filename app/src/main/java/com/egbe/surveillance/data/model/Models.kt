
package com.egbe.surveillance.data.model

data class TraceResponse(
    val phone: String,
    val international: String = phone,
    val country: String,
    val countryCode: String,
    val city: String,
    val carrier: String,
    val lineType: String,
    val valid: Boolean,
    val riskScore: Int? = null,
    val location: Location? = null,
    val status: String,
    val timestamp: Long,
    val notes: List<String> = emptyList(),
    val timeZones: List<String> = emptyList()
) {
    data class Location(
        val lat: Double,
        val lon: Double,
        val accuracy: Int,
        val source: String = "unknown"
    )
}

data class Satellite(
    val name: String,
    val noradId: Int,
    val internationalDesignator: String = "",
    val inclination: Double,
    val eccentricity: Double,
    val altitudeKm: Double,
    val periodMin: Double = 0.0,
    val visible: Boolean,
    val type: String = "Unknown",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val elevationDegrees: Double? = null,
    val azimuthDegrees: Double? = null,
    val rangeKm: Double? = null,
    val source: String = "unknown",
    val timestamp: String? = null
)

data class PhishingCampaign(
    val id: String,
    val title: String,
    val trackingUrl: String,
    val redirectUrl: String,
    val clicks: Int = 0,
    val uniqueClicks: Int = 0,
    val createdAt: Long,
    val lastClickAt: Long? = null
)

data class IpInfo(
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
    val source: String = "unknown",
    val accuracyNotice: String = ""
)
