package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ConnectedDevice
import com.example.ui.JarvisViewModel
import com.example.ui.components.AudioWaveformBar
import com.example.ui.components.HudCard
import com.example.ui.components.HudStatusPill
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HologramGold
import com.example.ui.theme.HudTextCyan
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TitaniumSurface

@Composable
fun BiometricsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.biometricProfile.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isBiometricallyAuthenticated.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessingAi.collectAsStateWithLifecycle()
    val isEnrolling by viewModel.isEnrollingVoice.collectAsStateWithLifecycle()
    val biometricResult by viewModel.biometricResult.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.connectedDevices.collectAsStateWithLifecycle()

    var testPhrase by remember { mutableStateOf("Jarvis, authorize protocol Mark 85") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Biometric Security Clearance Status
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isAuthenticated) CyberCyan.copy(alpha = 0.5f) else NeonCrimson,
                glowColor = if (isAuthenticated) CyberCyan.copy(alpha = 0.3f) else NeonCrimson.copy(alpha = 0.4f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isAuthenticated) CyberCyan.copy(alpha = 0.15f) else NeonCrimson.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAuthenticated) Icons.Default.Fingerprint else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isAuthenticated) CyberCyan else NeonCrimson,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isAuthenticated) "SECURITY LEVEL 5 // ALPHA CLEARED" else "CLEARANCE RESTRICTED // LOCKED",
                                    color = if (isAuthenticated) CyberCyan else NeonCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (isAuthenticated) "Encrypted Voiceprint Active" else "Voice Verification Required",
                                    color = HudTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HudStatusPill(
                            text = if (isAuthenticated) "ARMED" else "LOCKED",
                            statusColor = if (isAuthenticated) NeonEmerald else NeonCrimson
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Encryption Token Details
                    profile?.let { p ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TitaniumSurface)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cipher Scheme:", color = HudTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(p.encryptionKeyType, color = HologramGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Spectral Centroid:", color = HudTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("${String.format("%.1f", p.spectralCentroidHz)} Hz (Optimal)", color = HudTextCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Harmonic Match:", color = HudTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("${String.format("%.1f", p.harmonicsRatio * 100)}% Fidelity", color = NeonEmerald, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Text(
                                text = "Acoustic Fingerprint Hash:\n${p.voiceprintHash}",
                                color = HudTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Voice Biometric Verification Terminal
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HologramGold.copy(alpha = 0.4f)
            ) {
                Column {
                    Text(
                        text = "VOICE BIOMETRIC VERIFICATION TEST",
                        color = HologramGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Target Passphrase: \"$testPhrase\"",
                        color = HudTextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AudioWaveformBar(
                        isAnimating = isProcessing || isEnrolling,
                        color = HologramGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.verifyVoiceBiometric(testPhrase) },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("verify_voice_button")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Speak & Authenticate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.enrollVoiceBiometric() },
                            enabled = !isEnrolling,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TitaniumSurface,
                                contentColor = HologramGold
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("enroll_voice_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Enroll", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    biometricResult?.let { result ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (result.success) NeonEmerald.copy(alpha = 0.15f) else NeonCrimson.copy(alpha = 0.15f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = result.message,
                                    color = if (result.success) NeonEmerald else NeonCrimson,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                result.token?.let { token ->
                                    Text(
                                        text = "Sync Token: $token",
                                        color = HudTextCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Multi-Device Seamless Synchronization Matrix
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyberCyan.copy(alpha = 0.35f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Text(
                                text = "CROSS-DEVICE AUTHENTICATION MATRIX",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "${connectedDevices.size} Synced",
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your encrypted voiceprint signature is securely propagated across authorized peripheral nodes using AES-256 tokens for instant zero-friction command execution.",
                        color = HudTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        connectedDevices.forEach { dev ->
                            ConnectedDeviceRow(device = dev)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectedDeviceRow(device: ConnectedDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TitaniumSurface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = device.name,
                color = HudTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = device.type,
                    color = HudTextCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "• ${device.syncStatus}",
                    color = HudTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Synced",
                tint = NeonEmerald,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = device.lastSyncTime,
                color = NeonEmerald,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
