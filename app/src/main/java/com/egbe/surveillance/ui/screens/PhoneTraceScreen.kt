
package com.egbe.surveillance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.data.model.TraceResponse
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.ui.theme.EGBERed
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

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
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Numbering-plan metadata, not live phone tracking.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Phone number") },
            placeholder = { Text("+2348012345678") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.tracePhone(phoneInput) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTracing && phoneInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBECyan)
        ) {
            if (isTracing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("REQUESTING METADATA...")
            } else {
                Text(
                    "LOOK UP NUMBER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (result != null) {
            Spacer(Modifier.height(24.dp))
            TraceResultCard(result)
        }
    }
}

@Composable
fun TraceResultCard(data: TraceResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2332)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                data.status.replace('_', ' '),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = EGBEGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            InfoRow("Number", data.international)
            InfoRow("Country", "${data.country} (${data.countryCode})")
            InfoRow("Number area", data.city)
            InfoRow("Carrier", data.carrier)
            InfoRow("Line type", data.lineType)
            InfoRow(
                "Coordinates",
                data.location?.let {
                    "${"%.5f".format(it.lat)}, ${"%.5f".format(it.lon)}"
                } ?: "Not available from phone metadata"
            )
            InfoRow(
                "Accuracy",
                data.location?.let { "±${it.accuracy} m" } ?: "Not available"
            )
            InfoRow(
                "Risk score",
                data.riskScore?.toString() ?: "Not assessed"
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "DATA LIMITATIONS",
                color = EGBECyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            data.notes.forEach { note ->
                Text(
                    "• $note",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (data.timeZones.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                InfoRow("Time zones", data.timeZones.joinToString())
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            modifier = Modifier.width(105.dp),
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )
        Text(
            value,
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
