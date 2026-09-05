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
import com.egbe.surveillance.data.model.IpInfo
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(vm: SurveillanceViewModel) {
    var ipInput by remember { mutableStateOf("") }
    val ipInfo = vm.ipResult.value
    val isLoading = vm.isLoadingIp.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "OSINT TOOLKIT",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 15.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        // IP Lookup
        Text("IP GEOLOCATION", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ipInput,
            onValueChange = { ipInput = it },
            label = { Text("IP Address") },
            placeholder = { Text("8.8.8.8") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBECyan,
                focusedLabelColor = EGBECyan
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { vm.lookupIp(ipInput) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = EGBECyan)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = androidx.compose.ui.graphics.Color.Black, strokeWidth = 2.dp)
            } else {
                Text("LOOKUP IP", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        if (ipInfo != null) {
            Spacer(modifier = Modifier.height(16.dp))
            IpResultCard(ipInfo)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("QUICK TOOLS", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(8.dp))

        ToolButton("WHOIS Lookup") { vm.addLog("WHOIS: coming in next upgrade") }
        ToolButton("DNS Records") { vm.addLog("DNS: coming in next upgrade") }
        ToolButton("Reverse IP") { vm.addLog("Reverse IP: coming in next upgrade") }
        ToolButton("Port Check") { vm.addLog("Port check: coming in next upgrade") }
    }
}

@Composable
fun IpResultCard(info: IpInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(info.ip, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 16.sp, color = EGBEGreen, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            InfoLine("Location", "${info.city}, ${info.region}, ${info.country}")
            InfoLine("ISP", info.isp)
            InfoLine("Org", info.org)
            InfoLine("ASN", info.asName)
            InfoLine("Coords", "${info.lat}, ${info.lon}")
            InfoLine("Timezone", info.timezone)
            InfoLine("Mobile", if (info.mobile) "Yes" else "No")
            InfoLine("Proxy/VPN", if (info.proxy) "Yes" else "No")
            InfoLine("Hosting", if (info.hosting) "Yes" else "No")
        }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label:", modifier = Modifier.width(90.dp), color = androidx.compose.ui.graphics.Color(0xFF94A3B8), fontSize = 12.sp)
        Text(value, color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@Composable
fun ToolButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFF94A3B8))
    ) {
        Text(label, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp)
    }
}