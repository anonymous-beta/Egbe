package com.egbe.surveillance.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.data.model.PhishingCampaign
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhishingScreen(vm: SurveillanceViewModel) {
    var title by remember { mutableStateOf("") }
    var redirect by remember { mutableStateOf("https://google.com") }
    val campaign = vm.phishingCampaign.value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "LURE GENERATOR",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 15.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Create tracking links • Clicks stored locally",
            fontSize = 11.sp,
            color = androidx.compose.ui.graphics.Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Campaign Title") },
            placeholder = { Text("Security Update") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EGBECyan, focusedLabelColor = EGBECyan)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = redirect,
            onValueChange = { redirect = it },
            label = { Text("Redirect URL") },
            placeholder = { Text("https://example.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EGBECyan, focusedLabelColor = EGBECyan)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { vm.createPhishingLink(title, redirect) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EGBECyan)
        ) {
            Text("GENERATE TRACKING LINK", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        if (campaign != null) {
            Spacer(modifier = Modifier.height(28.dp))
            CampaignCard(campaign, context)
        }
    }
}

@Composable
fun CampaignCard(campaign: PhishingCampaign, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                campaign.title,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 15.sp,
                color = EGBEGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Tracking URL", color = androidx.compose.ui.graphics.Color(0xFF94A3B8), fontSize = 11.sp)
            Text(
                campaign.trackingUrl,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 13.sp,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("tracking", campaign.trackingUrl))
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("Copy Link") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = androidx.compose.ui.graphics.Color(0xFF334155))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem("Clicks", campaign.clicks.toString(), Modifier.weight(1f))
                StatItem("Unique", campaign.uniqueClicks.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EGBEAmber)
        Text(label, fontSize = 11.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8))
    }
}