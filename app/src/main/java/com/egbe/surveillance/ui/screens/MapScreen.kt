package com.egbe.surveillance.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.viewmodel.SurveillanceViewModel
import com.google.gson.Gson

private val MapMuted = Color(0xFF94A3B8)
private val MapPanel = Color(0xFF111827)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(vm: SurveillanceViewModel) {
    val satellites by vm.satellites
    val loading by vm.satelliteLoading

    val webViewState = remember { mutableStateOf<WebView?>(null) }
    val pageReady = remember { mutableStateOf(false) }
    val mapError = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (satellites.isEmpty() && !loading) {
            vm.loadSatellites()
        }
    }

    fun sendSatelliteData(view: WebView) {
        val payload = satellites.map { satellite ->
            mapOf(
                "name" to satellite.name,
                "noradId" to satellite.noradId,
                "latitude" to satellite.latitude,
                "longitude" to satellite.longitude,
                "altitudeKm" to satellite.altitudeKm,
                "source" to satellite.source
            )
        }

        val json = Gson().toJson(payload)

        view.evaluateJavascript(
            """
            if (typeof window.setSatellites === 'function') {
                window.setSatellites($json);
            }
            """.trimIndent(),
            null
        )
    }

    LaunchedEffect(satellites, pageReady.value) {
        val view = webViewState.value
        if (view != null && pageReady.value) {
            sendSatelliteData(view)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = false
                    settings.allowFileAccess = true
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.mixedContentMode =
                        android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW

                    webChromeClient = WebChromeClient()

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(
                            view: WebView,
                            url: String
                        ) {
                            super.onPageFinished(view, url)

                            pageReady.value = true
                            mapError.value = false
                            sendSatelliteData(view)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            // Keep top-level navigation inside the local map.
                            // External resources may still load as page assets.
                            return request.url.scheme != "file"
                        }
                    }

                    webViewState.value = this
                    loadUrl("file:///android_asset/cesium_map.html")
                }
            },
            update = { view ->
                if (pageReady.value) {
                    sendSatelliteData(view)
                }
            }
        )

        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MapPanel.copy(alpha = 0.96f)
            ),
            border = BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "ORBITAL TRACKER",
                    color = EGBECyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Text(
                    text = when {
                        loading -> "Loading current orbital data…"
                        satellites.isEmpty() ->
                            "No orbital records available. Check the API connection."
                        else ->
                            "${satellites.size} orbital records received"
                    },
                    color = MapMuted,
                    fontSize = 11.sp
                )

                Text(
                    text = "Positions are based on public orbital data, " +
                        "not live phone tracking.",
                    color = MapMuted,
                    fontSize = 10.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            pageReady.value = false
                            mapError.value = false
                            webViewState.value?.reload()
                            vm.loadSatellites()
                        },
                        enabled = !loading,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1A2332),
                            contentColor = EGBEGreen
                        )
                    ) {
                        Text(
                            "REFRESH",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }

                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = EGBECyan,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}