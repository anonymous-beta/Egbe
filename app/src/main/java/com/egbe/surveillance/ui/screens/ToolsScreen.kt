package com.egbe.surveillance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.data.model.IpInfo
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.viewmodel.SurveillanceViewModel
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

private val ToolsMuted = Color(0xFF94A3B8)
private val ToolsPanel = Color(0xFF1A2332)

@Composable
fun ToolsScreen(vm: SurveillanceViewModel) {
    var ipInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val ipInfo by vm.ipResult
    val loading by vm.isLoadingIp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "OSINT TOOLKIT",
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )

        Text(
            text = "IP intelligence",
            fontSize = 12.sp,
            color = ToolsMuted
        )

        Text(
            text = "IP geolocation estimates the network's registered location. " +
                "It does not identify a person's precise physical location.",
            fontSize = 11.sp,
            color = ToolsMuted
        )

        OutlinedTextField(
            value = ipInput,
            onValueChange = {
                ipInput = it.take(64)
                validationError = null
            },
            label = { Text("IPv4 or IPv6 address") },
            placeholder = { Text("8.8.8.8") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBECyan,
                focusedLabelColor = EGBECyan
            )
        )

        validationError?.let { message ->
            Text(
                text = message,
                color = Color(0xFFEF4444),
                fontSize = 12.sp
            )
        }

        Button(
            onClick = {
                val candidate = ipInput.trim()

                val isIpAddress = runCatching {
                    val parsed = InetAddress.getByName(candidate)

                    when {
                        ':' in candidate -> parsed is Inet6Address
                        '.' in candidate -> parsed is Inet4Address
                        else -> false
                    }
                }.getOrDefault(false)

                if (!isIpAddress) {
                    validationError =
                        "Enter a valid IPv4 or IPv6 address."
                } else {
                    validationError = null
                    vm.lookupIp(candidate)
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = EGBECyan,
                contentColor = Color(0xFF0A0E17)
            )
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    color = Color(0xFF0A0E17),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "LOOK UP IP",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ipInfo?.let { info ->
            IpResultCard(info)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "MODULE AVAILABILITY",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = EGBEGreen
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ToolsPanel
            ),
            border = BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModuleStatus("IP geolocation", "IMPLEMENTED", EGBEGreen)
                ModuleStatus("WHOIS / RDAP", "NOT IMPLEMENTED", ToolsMuted)
                ModuleStatus("DNS records", "NOT IMPLEMENTED", ToolsMuted)
                ModuleStatus("Reverse IP", "NOT IMPLEMENTED", ToolsMuted)
                ModuleStatus("Port checking", "NOT IMPLEMENTED", ToolsMuted)

                Text(
                    text = "Unavailable modules are intentionally not presented " +
                        "as working tools. They can be implemented in a later release.",
                    color = ToolsMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun IpResultCard(info: IpInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ToolsPanel
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = info.ip,
                fontFamily = FontFamily.Monospace,
                fontSize = 17.sp,
                color = EGBEGreen,
                fontWeight = FontWeight.Bold
            )

            InfoLine(
                "Location",
                listOf(info.city, info.region, info.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .ifBlank { "Unknown" }
            )

            InfoLine("Country code", info.countryCode)
            InfoLine("ISP", info.isp)
            InfoLine("Organization", info.org)
            InfoLine("ASN / AS name", info.asName)

            InfoLine(
                "Coordinates",
                if (info.lat != null && info.lon != null) {
                    "${info.lat}, ${info.lon}"
                } else {
                    "Unavailable"
                }
            )

            InfoLine("Timezone", info.timezone)
            InfoLine("Mobile network", info.mobile.asDisplayValue())
            InfoLine("Proxy / VPN", info.proxy.asDisplayValue())
            InfoLine("Hosting", info.hosting.asDisplayValue())
            InfoLine("Data source", info.source)

            if (info.accuracyNotice.isNotBlank()) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = info.accuracyNotice,
                    fontSize = 11.sp,
                    color = ToolsMuted
                )
            }
        }
    }
}

@Composable
private fun ModuleStatus(
    name: String,
    status: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp
        )

        Text(
            text = status,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.width(108.dp),
            color = ToolsMuted,
            fontSize = 11.sp
        )

        Text(
            text = value.ifBlank { "Unknown" },
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun Boolean?.asDisplayValue(): String {
    return when (this) {
        true -> "Yes"
        false -> "No"
        null -> "Unknown"
    }
}