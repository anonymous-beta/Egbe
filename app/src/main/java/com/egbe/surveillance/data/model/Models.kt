package com.egbe.surveillance.data.model

data class TraceResponse(
    val phone: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val carrier: String,
    val lineType: String,
    val valid: Boolean,
    val riskScore: Int,
    val location: Location,
    val status: String,
    val timestamp: Long,
    val notes: List<String> = emptyList()
) {
    data class Location(
        val lat: Double,
        val lon: Double,
        val accuracy: Int,
        val source: String = "estimated"
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
    val type: String = "Unknown"
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
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val mobile: Boolean,
    val proxy: Boolean,
    val hosting: Boolean
)