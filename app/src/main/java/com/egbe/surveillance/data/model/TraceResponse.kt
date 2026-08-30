package com.egbe.surveillance.data.model

data class TraceResponse(
    val trace_id: String,
    val phone: String,
    val international: String,
    val country: String,
    val carrier: String,
    val location: LocationData?,
    val device_info: DeviceInfo?,
    val risk_score: Int,
    val sources: List<String>,
    val timestamp: String
)

data class LocationData(
    val lat: Double,
    val lon: Double,
    val accuracy: Int,
    val confidence: Int,
    val source: String
)

data class DeviceInfo(
    val model: String,
    val os: String,
    val network_type: String,
    val imei: String
)

data class Satellite(
    val name: String,
    val type: String,
    val lat: Double,
    val lon: Double,
    val alt: Int,
    val signal: Int
)

data class PhishingCampaign(
    val id: String,
    val url: String,
    val created: String,
    val clicks: List<ClickData>
)

data class ClickData(
    val ip: String,
    val user_agent: String,
    val lat: Double?,
    val lon: Double?,
    val accuracy: Int?,
    val timestamp: String
)
