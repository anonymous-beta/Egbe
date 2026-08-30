package com.egbe.surveillance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.components.EagleLogo
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.ui.theme.EGBERed
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

@Composable
fun DashboardScreen(vm: SurveillanceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        EagleLogo(size = 100.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("EGBE", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = EGBEGreen, letterSpacing = 4.sp)
        Text("SURVEILLANCE PLATFORM v3.0", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8), letterSpacing = 2.sp)
        Text("by Anonymous-beta", fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8).copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("SATELLITES", "10 LIVE", Icons.Filled.Satellite, EGBECyan)
            StatCard("TRACES", "ACTIVE", Icons.Filled.TrackChanges, EGBEGreen)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("LURES", "READY", Icons.Filled.Link, EGBEAmber)
            StatCard("TOOLS", "ONLINE", Icons.Filled.Build, EGBERed)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("SYSTEM LOG", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = EGBEGreen, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            vm.logs.value.take(6).forEach { log ->
                Text(log, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8), modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.size(140.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp, color = androidx.compose.ui.graphics.Color(0xFF94A3B8), letterSpacing = 1.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
