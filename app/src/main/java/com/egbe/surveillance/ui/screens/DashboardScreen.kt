package com.egbe.surveillance.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.components.EagleLogo
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.ui.theme.EGBERed
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

private val MutedText = Color(0xFF94A3B8)
private val CardBackground = Color(0xFF1A2332)

@Composable
fun DashboardScreen(vm: SurveillanceViewModel) {
    val satellites by vm.satellites
    val satelliteLoading by vm.satelliteLoading
    val isTracing by vm.isTracing
    val campaign by vm.phishingCampaign
    val campaignLoading by vm.campaignLoading
    val logs by vm.logs

    LaunchedEffect(Unit) {
        if (satellites.isEmpty() && !satelliteLoading) {
            vm.loadSatellites()
        }
    }

    val satelliteStatus = when {
        satelliteLoading -> "LOADING"
        satellites.isNotEmpty() -> "${satellites.size} RECORDS"
        else -> "UNAVAILABLE"
    }

    val traceStatus = when {
        isTracing -> "RUNNING"
        else -> "IDLE"
    }

    val campaignStatus = when {
        campaignLoading -> "CREATING"
        campaign != null -> "CREATED"
        else -> "NONE"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        EagleLogo(size = 100.dp)

        Spacer(Modifier.height(16.dp))

        Text(
            text = "EGBE",
            fontFamily = FontFamily.Monospace,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = EGBEGreen,
            letterSpacing = 4.sp
        )

        Text(
            text = "OSINT & SATELLITE INTELLIGENCE",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MutedText,
            letterSpacing = 1.5.sp
        )

        Text(
            text = "by Anonymous-beta",
            fontSize = 10.sp,
            color = MutedText.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "SATELLITES",
                value = satelliteStatus,
                icon = Icons.Filled.Satellite,
                color = EGBECyan,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "PHONE LOOKUP",
                value = traceStatus,
                icon = Icons.Filled.TrackChanges,
                color = EGBEGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "CAMPAIGN",
                value = campaignStatus,
                icon = Icons.Filled.Link,
                color = EGBEAmber,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "IP TOOL",
                value = "AVAILABLE",
                icon = Icons.Filled.Build,
                color = EGBERed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "These indicators describe local application state. " +
                "They do not confirm that every backend service is online.",
            color = MutedText,
            fontSize = 11.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM LOG",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = EGBEGreen,
                letterSpacing = 2.sp
            )

            Text(
                text = "${logs.size} EVENTS",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MutedText
            )
        }

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            border = BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No events recorded yet.",
                        color = MutedText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                } else {
                    logs.take(10).forEach { entry ->
                        Text(
                            text = entry,
                            color = MutedText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(132.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(26.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = MutedText,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 2
            )
        }
    }
}