
package com.egbe.surveillance.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egbe.surveillance.data.model.*
import com.egbe.surveillance.data.repository.SurveillanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SurveillanceViewModel : ViewModel() {
    private val repo = SurveillanceRepository()
    private var trackingJob: Job? = null

    var traceResult = mutableStateOf<TraceResponse?>(null)
        private set

    var isTracing = mutableStateOf(false)
        private set

    var satellites = mutableStateOf<List<Satellite>>(emptyList())
        private set

    var visibleSatellites = mutableStateOf<List<Satellite>>(emptyList())
        private set

    var satelliteLoading = mutableStateOf(false)
        private set

    var phishingCampaign = mutableStateOf<PhishingCampaign?>(null)
        private set

    var logs = mutableStateOf<List<String>>(emptyList())
        private set

    var ipResult = mutableStateOf<IpInfo?>(null)
        private set

    var isLoadingIp = mutableStateOf(false)
        private set

    var campaignLoading = mutableStateOf(false)
        private set

    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        logs.value = (listOf("[$timestamp] $message") + logs.value).take(60)
    }

    fun loadSatellites() {
        viewModelScope.launch {
            satelliteLoading.value = true
            addLog("Requesting current satellite orbital data...")
            try {
                repo.getSatellites().fold(
                    onSuccess = { response ->
                        satellites.value = response.all
                        visibleSatellites.value = response.visible
                        addLog(
                            "Loaded ${response.all.size} satellite records from " +
                                response.source +
                                ". Observer coordinates supplied: ${response.observerProvided}."
                        )
                    },
                    onFailure = {
                        satellites.value = emptyList()
                        visibleSatellites.value = emptyList()
                        addLog("Satellite request failed: ${it.message}")
                    }
                )
            } finally {
                satelliteLoading.value = false
            }
        }
    }

    fun tracePhone(phone: String) {
        if (phone.isBlank()) {
            addLog("Enter a phone number first.")
            return
        }
        if (isTracing.value) return

        viewModelScope.launch {
            isTracing.value = true
            traceResult.value = null
            addLog("Requesting phone-number metadata...")
            try {
                repo.analyzePhone(phone).fold(
                    onSuccess = { result ->
                        traceResult.value = result
                        addLog(
                            "Metadata returned: ${result.country}, " +
                                "${result.carrier}. No live location is provided."
                        )
                    },
                    onFailure = {
                        addLog("Phone metadata request failed: ${it.message}")
                    }
                )
            } finally {
                isTracing.value = false
            }
        }
    }

    /**
     * Kept for compatibility with the existing UI.
     * A phone-number lookup cannot authorize live device tracking.
     */
    fun startLiveTracking(traceId: String = "") {
        trackingJob?.cancel()
        addLog(
            "Live tracking unavailable: phone-number metadata does not " +
                "provide live GPS access."
        )
    }

    fun stopLiveTracking() {
        trackingJob?.cancel()
        trackingJob = null
        addLog("Live tracking is not active.")
    }

    fun lookupIp(ip: String) {
        if (ip.isBlank() || isLoadingIp.value) return

        viewModelScope.launch {
            isLoadingIp.value = true
            ipResult.value = null
            addLog("Requesting public IP intelligence for ${ip.trim()}...")
            try {
                repo.lookupIp(ip).fold(
                    onSuccess = {
                        ipResult.value = it
                        addLog("IP metadata returned from ${it.source}.")
                    },
                    onFailure = {
                        addLog("IP lookup failed: ${it.message}")
                    }
                )
            } finally {
                isLoadingIp.value = false
            }
        }
    }

    fun createPhishingLink(title: String, redirect: String) {
        if (campaignLoading.value) return

        viewModelScope.launch {
            campaignLoading.value = true
            phishingCampaign.value = null
            addLog("Creating security-awareness campaign on the backend...")
            try {
                repo.createCampaign(title, redirect).fold(
                    onSuccess = {
                        phishingCampaign.value = it
                        addLog("Campaign created by the backend.")
                        addLog("Campaign links count visits only; no GPS or credentials.")
                    },
                    onFailure = {
                        addLog("Campaign creation failed: ${it.message}")
                    }
                )
            } finally {
                campaignLoading.value = false
            }
        }
    }

    fun refreshCampaign(id: String) {
        if (id.isBlank()) return

        viewModelScope.launch {
            repo.refreshCampaign(id).fold(
                onSuccess = { phishingCampaign.value = it },
                onFailure = {
                    addLog("Campaign refresh failed: ${it.message}")
                }
            )
        }
    }

    override fun onCleared() {
        trackingJob?.cancel()
        super.onCleared()
    }
}
