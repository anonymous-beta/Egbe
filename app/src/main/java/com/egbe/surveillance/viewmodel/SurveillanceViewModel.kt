package com.egbe.surveillance.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egbe.surveillance.data.model.*
import com.egbe.surveillance.data.remote.RetrofitClient
import com.egbe.surveillance.data.remote.TraceRequest
import com.egbe.surveillance.data.remote.PhishingCreateRequest
import com.egbe.surveillance.data.remote.IpRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SurveillanceViewModel : ViewModel() {
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
    
    var ipResult = mutableStateOf<String>("")
        private set

    fun addLog(msg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        logs.value = listOf("[$timestamp] $msg") + logs.value.take(49)
    }

    fun tracePhone(phone: String) {
        viewModelScope.launch {
            isTracing.value = true
            addLog("Initiating trace on $phone...")
            try {
                val resp = RetrofitClient.api.tracePhone(TraceRequest(phone))
                if (resp.isSuccessful) {
                    traceResult.value = resp.body()
                    addLog("Trace complete: ${resp.body()?.country}")
                } else {
                    addLog("Trace failed: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                addLog("Error: ${e.message}")
            }
            isTracing.value = false
        }
    }

    fun startLiveTracking(traceId: String) {
        viewModelScope.launch {
            addLog("Live tracking activated")
            while (true) {
                try {
                    val resp = RetrofitClient.api.getLiveTrace(traceId)
                    if (resp.isSuccessful) {
                        traceResult.value = resp.body()
                    }
                } catch (_: Exception) {}
                delay(3000)
            }
        }
    }

    fun loadSatellites() {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getSatellites()
                if (resp.isSuccessful) {
                    satellites.value = resp.body()?.all ?: emptyList()
                }
            } catch (e: Exception) {
                addLog("Satellite load failed: ${e.message}")
            }
        }
    }

    fun createPhishingLink(title: String, redirect: String) {
        viewModelScope.launch {
            addLog("Generating tracking link...")
            try {
                val resp = RetrofitClient.api.createPhishingCampaign(PhishingCreateRequest(title, redirect))
                if (resp.isSuccessful) {
                    phishingCampaign.value = resp.body()
                    addLog("Link generated: ${resp.body()?.url}")
                }
            } catch (e: Exception) {
                addLog("Link gen failed: ${e.message}")
            }
        }
    }

    fun refreshCampaign(id: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getCampaign(id)
                if (resp.isSuccessful) {
                    phishingCampaign.value = resp.body()
                }
            } catch (_: Exception) {}
        }
    }

    fun lookupIp(ip: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.geolocateIp(IpRequest(ip))
                if (resp.isSuccessful) {
                    val data = resp.body()
                    ipResult.value = "${data?.ip} → ${data?.city}, ${data?.country} (${data?.org})"
                    addLog("IP resolved: ${data?.city}")
                }
            } catch (e: Exception) {
                addLog("IP lookup failed: ${e.message}")
            }
        }
    }
}
