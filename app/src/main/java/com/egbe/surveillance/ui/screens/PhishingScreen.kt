package com.egbe.surveillance.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egbe.surveillance.data.model.PhishingCampaign
import com.egbe.surveillance.ui.theme.EGBECyan
import com.egbe.surveillance.ui.theme.EGBEGreen
import com.egbe.surveillance.ui.theme.EGBEAmber
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

private val CampaignMuted = Color(0xFF94A3B8)
private val CampaignPanel = Color(0xFF1A2332)

@Composable
fun PhishingScreen(vm: SurveillanceViewModel) {
    var title by remember { mutableStateOf("") }
    var redirect by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val campaign by vm.phishingCampaign
    val loading by vm.campaignLoading
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SECURITY AWARENESS",
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp,
            color = EGBECyan,
            letterSpacing = 2.sp
        )

        Text(
            text = "Create a transparent training campaign that counts visits " +
                "to its campaign link.",
            fontSize = 12.sp,
            color = CampaignMuted
        )

        Text(
            text = "Use this only for authorized exercises. The campaign does " +
                "not collect GPS coordinates or passwords.",
            fontSize = 11.sp,
            color = CampaignMuted
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it.take(100)
                validationError = null
            },
            label = { Text("Campaign title") },
            placeholder = { Text("Security awareness exercise") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EGBECyan,
                focusedLabelColor = EGBECyan
            )
        )

        OutlinedTextField(
            value = redirect,
            onValueChange = {
                redirect = it.take(2048)
                validationError = null
            },
            label = { Text("HTTPS destination") },
            placeholder = { Text("https://example.org/training") },
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
                val destination = redirect.trim()
                val uri = runCatching { Uri.parse(destination) }.getOrNull()

                val validDestination =
                    uri != null &&
                    uri.scheme.equals("https", ignoreCase = true) &&
                    !uri.host.isNullOrBlank() &&
                    uri.userInfo == null

                if (title.isBlank()) {
                    validationError = "Enter a campaign title."
                } else if (!validDestination) {
                    validationError =
                        "Enter a valid HTTPS destination without embedded credentials."
                } else {
                    validationError = null
                    vm.createPhishingLink(title.trim(), destination)
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
                    text = "CREATE TRAINING CAMPAIGN",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        campaign?.let {
            CampaignCard(
                campaign = it,
                context = context,
                onRefresh = { vm.refreshCampaign(it.id) }
            )
        }
    }
}

@Composable
private fun CampaignCard(
    campaign: PhishingCampaign,
    context: Context,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CampaignPanel
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = campaign.title,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                color = EGBEGreen,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "CAMPAIGN LINK",
                fontSize = 10.sp,
                color = CampaignMuted
            )

            Text(
                text = campaign.trackingUrl,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE)
                                as ClipboardManager

                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                "EGBE training campaign",
                                campaign.trackingUrl
                            )
                        )

                        Toast.makeText(
                            context,
                            "Campaign link copied",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    label = { Text("Copy link") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                    }
                )

                AssistChip(
                    onClick = onRefresh,
                    label = { Text("Refresh statistics") }
                )
            }

            Divider(color = Color(0xFF334155))

            Text(
                text = "RECORDED VISITS",
                fontSize = 10.sp,
                color = CampaignMuted
            )

            Text(
                text = campaign.clicks.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = EGBEAmber
            )

            Text(
                text = "This is a visit count, not a verified unique-person count. " +
                    "Campaign records are held by the backend and may disappear " +
                    "if its in-memory storage is reset.",
                fontSize = 11.sp,
                color = CampaignMuted
            )
        }
    }
}