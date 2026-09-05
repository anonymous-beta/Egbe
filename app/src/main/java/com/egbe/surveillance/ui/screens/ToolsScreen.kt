package com.egbe.surveillance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.data.remote.RetrofitClient
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(vm: SurveillanceViewModel) {
    val context = LocalContext.current
    var ipInput by remember { mutableStateOf("") }
    val ipResult = vm.ipResult.value

    // Backend URL state
    var backendUrl by remember { mutableStateOf(RetrofitClient.getCurrentBaseUrl()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ========== BACKEND SETTINGS ==========
        Text(
            "BACKEND SETTINGS",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 14.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = backendUrl,
            onValueChange = { backendUrl = it },
            label = { Text("Backend URL") },
            placeholder = { Text("http://192.168.1.45:7777/") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBECyan,
                focusedLabelColor = EGBECyan
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                RetrofitClient.updateBaseUrl(context, backendUrl)
                Toast.makeText(context, "Backend URL updated", Toast.LENGTH_SHORT).show()
                vm.addLog("Backend URL set to: ${RetrofitClient.getCurrentBaseUrl()}")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBECyan)
        ) {
            Text(
                "SAVE BACKEND URL",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ========== OSINT TOOLKIT ==========
        Text(
            "OSINT TOOLKIT",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 14.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ipInput,
            onValueChange = { ipInput = it },
            label = { Text("IP ADDRESS") },
            placeholder = { Text("8.8.8.8") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBECyan,
                focusedLabelColor = EGBECyan
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { vm.lookupIp(ipInput) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBECyan)
        ) {
            Text(
                "GEOLOCATE IP",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (ipResult.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.08f)
                )
            ) {
                Text(
                    ipResult,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp),
                    color = EGBEGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "UTILITY",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp,
            color = androidx.compose.ui.graphics.Color(0xFF94A3B8)
        )

        ToolButton("WHOIS Lookup") { vm.addLog("WHOIS: feature requires backend endpoint") }
        ToolButton("DNS Resolver") { vm.addLog("DNS: feature requires backend endpoint") }
        ToolButton("MAC Vendor Lookup") { vm.addLog("MAC: feature requires backend endpoint") }
        ToolButton("Port Scanner") { vm.addLog("Port scan: feature requires backend endpoint") }
    }
}

@Composable
fun ToolButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = androidx.compose.ui.graphics.Color(0xFF94A3B8)
        )
    ) {
        Text(
            label,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}