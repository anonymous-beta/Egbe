package com.egbe.surveillance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.data.model.TraceResponse
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.ui.theme.EGBERed
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTraceScreen(vm: SurveillanceViewModel) {
    var phoneInput by remember { mutableStateOf("") }
    val result = vm.traceResult.value
    val isTracing = vm.isTracing.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "PHONE OSINT",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 15.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "On-device analysis • No external backend required",
            fontSize = 11.sp,
            color = androidx.compose.ui.graphics.Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Phone Number") },
            placeholder = { Text("+234 801 234 5678") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBECyan,
                focusedLabelColor = EGBECyan
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { vm.tracePhone(phoneInput) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTracing && phoneInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBECyan)
        ) {
            if (isTracing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = androidx.compose.ui.graphics.Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("ANALYZING...")
            } else {
                Text("START ANALYSIS", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        if (result != null) {
            Spacer(modifier = Modifier.height(24.dp))
            TraceResultCard(result)

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { vm.startLiveTracking() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EGBEGreen)
            ) {
                Text("START LIVE TRACKING", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun TraceResultCard(data: TraceResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    data.status,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = EGBEGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                RiskBadge(data.riskScore)
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Number", data.phone)
            InfoRow("Country", "\( {data.country} ( \){data.countryCode})")
            InfoRow("City", data.city)
            InfoRow("Carrier", data.carrier)
            InfoRow("Line Type", data.lineType)
            InfoRow("Coordinates", "${"%.5f".format(data.location.lat)}, ${"%.5f".format(data.location.lon)}")
            InfoRow("Accuracy", "±${data.location.accuracy}m")
            InfoRow("Source", data.location.source)

            if (data.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Notes", color = androidx.compose.ui.graphics.Color(0xFF94A3B8), fontSize = 12.sp)
                data.notes.forEach {
                    Text("• $it", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFFCBD5E1), modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
fun RiskBadge(score: Int) {
    val color = when {
        score >= 60 -> EGBERed
        score >= 30 -> EGBEAmber
        else -> EGBEGreen
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            "Risk $score",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            label,
            modifier = Modifier.width(100.dp),
            color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
            fontSize = 12.sp
        )
        Text(
            value,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}