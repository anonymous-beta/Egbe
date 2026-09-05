package com.egbe.surveillance.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egbe.surveillance.data.model.*
import com.egbe.surveillance.data.repository.SurveillanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SurveillanceViewModel : ViewModel() {

    private val repo = SurveillanceRepository()

    var traceResult = mutableStateOf<TraceResponse?>(null)
        private set

    var isTracing = mutableStateOf(false)
        private set

    var satellites = mutableStateOf<List<Satellite>>(emptyList())
        private set

    var phishingCampaign = mutableStateOf<PhishingCampaign?>(null)
        private set

    var logs = mutableStateOf<List<String>>(emptyList())
        private set

    var ipResult = mutableStateOf<IpInfo?>(null)
        private set

    var isLoadingIp = mutableStateOf(false)
        private set

    fun addLog(msg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        logs.value = listOf("[$timestamp] $msg") + logs.value.take(60)
    }

    // ===================== SATELLITES =====================
    fun loadSatellites() {
        viewModelScope.launch {
            try {
                satellites.value = repo.getSatellites()
                val visibleCount = satellites.value.count { it.visible }
                addLog("Loaded ${satellites.value.size} satellites ($visibleCount currently visible)")
            } catch (e: Exception) {
                addLog("Satellite error: ${e.message}")
            }
        }
    }

    // ===================== PHONE TRACE / OSINT =====================
    fun tracePhone(phone: String) {
        if (phone.isBlank()) {
            addLog("Empty phone number")
            return
        }

        viewModelScope.launch {
            isTracing.value = true
            addLog("Analyzing $phone ...")
            delay(1600) // realistic feel

            try {
                val result = repo.analyzePhone(phone)
                traceResult.value = result
                addLog("Result → ${result.city}, ${result.country} | ${result.carrier} | Risk: ${result.riskScore}")
            } catch (e: Exception) {
                addLog("Analysis failed: ${e.message}")
            } finally {
                isTracing.value = false
            }
        }
    }

    fun startLiveTracking(traceId: String = "live") {
        viewModelScope.launch {
            addLog("Live tracking started")
            while (true) {
                delay(4500)
                traceResult.value?.let { current ->
                    val jitter = 0.0015
                    val newLat = current.location.lat + (Random().nextDouble() - 0.5) * jitter
                    val newLon = current.location.lon + (Random().nextDouble() - 0.5) * jitter
                    traceResult.value = current.copy(
                        location = current.location.copy(
                            lat = newLat,
                            lon = newLon,
                            accuracy = (150..900).random()
                        ),
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    // ===================== IP GEOLOCATION =====================
    fun lookupIp(ip: String) {
        if (ip.isBlank()) return

        viewModelScope.launch {
            isLoadingIp.value = true
            addLog("Looking up $ip ...")
            try {
                val result = repo.lookupIp(ip.trim())
                result.onSuccess {
                    ipResult.value = it
                    addLog("IP resolved: ${it.city}, \( {it.country} ( \){it.isp})")
                }.onFailure {
                    addLog("IP lookup failed: ${it.message}")
                    ipResult.value = null
                }
            } finally {
                isLoadingIp.value = false
            }
        }
    }

    // ===================== LURE / PHISHING =====================
    fun createPhishingLink(title: String, redirect: String) {
        viewModelScope.launch {
            addLog("Creating tracking link...")
            delay(900)

            val id = UUID.randomUUID().toString().replace("-", "").take(10)
            val trackingUrl = "https://egbe.app/l/$id"

            phishingCampaign.value = PhishingCampaign(
                id = id,
                title = title.ifBlank { "Campaign" },
                trackingUrl = trackingUrl,
                redirectUrl = redirect.ifBlank { "https://google.com" },
                clicks = 0,
                uniqueClicks = 0,
                createdAt = System.currentTimeMillis()
            )
            addLog("Link ready: $trackingUrl")
        }
    }

    fun refreshCampaign(id: String) {
        // Placeholder for future click sync
    }
}