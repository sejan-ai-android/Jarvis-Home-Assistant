package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SmartDevice
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudCard
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
fun SmartHomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    var selectedRoom by remember { mutableStateOf("All") }

    val rooms = listOf("All", "Command Lab", "Living Quarters", "Perimeter", "Workshop")

    val filteredDevices = if (selectedRoom == "All") {
        devices
    } else {
        devices.filter { it.room == selectedRoom }
    }

    val totalWatts = devices.filter { it.isOn }.sumOf { it.powerWatts }
    val activeCount = devices.count { it.isOn }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Facility Power & Status Banner
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
                    Column {
                        Text(
                            text = "SMART HOME MATRIX",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$activeCount of ${devices.size} Systems Active",
                            color = HudTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ENERGY LOAD",
                            color = HologramGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$totalWatts W",
                            color = HudTextCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Master Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleAllDevicesPower(true) },
                        modifier = Modifier.weight(1f).testTag("master_engage_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan.copy(alpha = 0.2f),
                            contentColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Engage All", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = { viewModel.toggleAllDevicesPower(false) },
                        modifier = Modifier.weight(1f).testTag("master_standby_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TitaniumSurface,
                            contentColor = HudTextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Eco Standby", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Room Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rooms.forEach { room ->
                val isSelected = selectedRoom == room
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedRoom = room },
                    label = {
                        Text(
                            text = room,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = TitaniumSurface,
                        selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                        labelColor = HudTextSecondary,
                        selectedLabelColor = CyberCyan
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) CyberCyan else CyberBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Devices List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredDevices, key = { it.id }) { device ->
                SmartDeviceCard(
                    device = device,
                    onTogglePower = { viewModel.toggleDevicePower(device.id, device.isOn) },
                    onValueChange = { viewModel.setDeviceValue(device.id, it) }
                )
            }
        }
    }
}

@Composable
fun SmartDeviceCard(
    device: SmartDevice,
    onTogglePower: () -> Unit,
    onValueChange: (Float) -> Unit
) {
    val icon = when (device.category) {
        "LIGHTING" -> Icons.Default.Lightbulb
        "CLIMATE" -> if (device.iconName == "air") Icons.Default.Air else Icons.Default.Thermostat
        "SECURITY" -> Icons.Default.Security
        "MEDIA" -> Icons.Default.Tv
        else -> Icons.Default.Build
    }

    HudCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        borderColor = if (device.isOn) CyberCyan.copy(alpha = 0.45f) else CyberBorder.copy(alpha = 0.5f)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (device.isOn) CyberCyan.copy(alpha = 0.15f) else TitaniumSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = device.name,
                            tint = if (device.isOn) CyberCyan else HudTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = device.name,
                            color = HudTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = device.room,
                                color = HudTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "• ${device.powerWatts}W",
                                color = HologramGold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Switch(
                    checked = device.isOn,
                    onCheckedChange = { onTogglePower() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyberCyan,
                        uncheckedThumbColor = HudTextMuted,
                        uncheckedTrackColor = TitaniumSurface
                    ),
                    modifier = Modifier.testTag("device_switch_${device.id}")
                )
            }

            if (device.isOn) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (device.category) {
                            "CLIMATE" -> if (device.iconName == "air") "Purity: ${device.value.toInt()}%" else "Target Temp: ${device.value.toInt()}°F"
                            "SECURITY" -> "Shield Integrity: ${device.value.toInt()}%"
                            else -> "Level: ${device.value.toInt()}%"
                        },
                        color = HudTextCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = device.statusText,
                        color = NeonEmerald,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = device.value,
                    onValueChange = onValueChange,
                    valueRange = if (device.category == "CLIMATE" && device.iconName != "air") 60f..80f else 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberCyan,
                        inactiveTrackColor = CyberBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_slider_${device.id}")
                )
            }
        }
    }
}
