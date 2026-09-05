package com.egbe.surveillance.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(vm: SurveillanceViewModel) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) {
        vm.loadSatellites()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    loadUrl("file:///android_asset/cesium_map.html")
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay controls
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    val loc = vm.traceResult.value?.location
                    if (loc != null && webView != null) {
                        webView?.evaluateJavascript(
                            "flyToTarget(${loc.lat}, ${loc.lon}, ${loc.accuracy});",
                            null
                        )
                        vm.addLog("Map focused on target")
                    }
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332))
            ) {
                Text("FOCUS TARGET", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    }
}