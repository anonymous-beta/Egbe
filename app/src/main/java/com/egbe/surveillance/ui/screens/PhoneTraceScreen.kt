package com.egbe.surveillance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTraceScreen(vm: SurveillanceViewModel) {
    var phone by remember { mutableStateOf("") }
    val result = vm.traceResult.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("PHONE TRACE ENGINE", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 14.sp, color = EGBEGreen, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("PHONE NUMBER", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp) },
            placeholder = { Text("+1 555 000 0000") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBEGreen,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.2f),
                focusedLabelColor = EGBEGreen
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { vm.tracePhone(phone) },
            enabled = !vm.isTracing.value && phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBEGreen)
        ) {
            Text(
                if (vm.isTracing.value) "TRACING..." else "INITIATE TRACE",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (result != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("TRACE RESULTS", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = EGBECyan, letterSpacing = 2.sp)

            ResultRow("Trace ID", result.trace_id, EGBECyan)
            ResultRow("Number", result.international)
            ResultRow("Country", result.country)
            ResultRow("Carrier", result.carrier)
            ResultRow("Device", "${result.device_info?.model} (${result.device_info?.os})")
            ResultRow("Network", result.device_info?.network_type ?: "Unknown")
            ResultRow("IMEI", result.device_info?.imei ?: "Unknown")

            if (result.location != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("Latitude", result.location.lat.toString(), EGBEAmber)
                ResultRow("Longitude", result.location.lon.toString(), EGBEAmber)
                ResultRow("Accuracy", "${result.location.accuracy}m", EGBEAmber)
                ResultRow("Confidence", "${result.location.confidence}%", EGBEGreen)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { vm.startLiveTracking(result.trace_id) },
                    colors = ButtonDefaults.buttonColors(containerColor = EGBEAmber)
                ) {
                    Text("START LIVE TRACKING", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }

            ResultRow("Risk Score", "${result.risk_score}/100", if (result.risk_score > 60) androidx.compose.ui.graphics.Color(0xFFEF4444) else EGBEGreen)
            ResultRow("Sources", result.sources.joinToString(", "))
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFFF1F5F9)) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8), letterSpacing = 1.sp)
        Text(value, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp, color = valueColor)
        Divider(color = androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}
