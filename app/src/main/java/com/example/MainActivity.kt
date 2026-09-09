package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.data.audio.ConversationLanguage
import com.example.ui.components.ApkExportDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HudTab
import com.example.ui.JarvisViewModel
import com.example.ui.screens.BiometricsScreen
import com.example.ui.screens.CommandScreen
import com.example.ui.screens.ProjectsEmailsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SmartHomeScreen
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HologramGold
import com.example.ui.theme.HudTextCyan
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TitaniumBackground
import com.example.ui.theme.TitaniumSurface

class MainActivity : ComponentActivity() {
    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isMuted by viewModel.isTtsMuted.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isBiometricallyAuthenticated.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isLiveAudioActive by viewModel.isLiveAudioConversation.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = TitaniumBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isAuthenticated) NeonEmerald else NeonCrimson)
                        )
                        Column {
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = CyberCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = if (isAuthenticated) "ONLINE // LEVEL 5 CLEARANCE" else "SECURITY LOCKED // ENCRYPTED",
                                color = if (isAuthenticated) HudTextSecondary else NeonCrimson,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                actions = {
                    // Live Audio Conversation indicator
                    if (isLiveAudioActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonEmerald.copy(alpha = 0.2f))
                                .border(0.8.dp, NeonEmerald, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LIVE AUDIO",
                                color = NeonEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else if (isSpeaking) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "VOICE ACTIVE",
                                color = CyberCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Interactive Language Toggle Button (Auto -> Bangla -> English)
                    IconButton(
                        onClick = {
                            val nextLang = when (selectedLanguage) {
                                ConversationLanguage.AUTO -> ConversationLanguage.BANGLA
                                ConversationLanguage.BANGLA -> ConversationLanguage.ENGLISH
                                ConversationLanguage.ENGLISH -> ConversationLanguage.AUTO
                            }
                            viewModel.setConversationLanguage(nextLang)
                        },
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HologramGold.copy(alpha = 0.15f))
                                .border(0.6.dp, HologramGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (selectedLanguage) {
                                    ConversationLanguage.AUTO -> "AUTO"
                                    ConversationLanguage.BANGLA -> "বাংলা"
                                    ConversationLanguage.ENGLISH -> "EN"
                                },
                                color = HologramGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleSpeechMute() },
                        modifier = Modifier.testTag("app_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle Speech Voice",
                            tint = if (isMuted) HudTextMuted else CyberCyan
                        )
                    }

                    // Export / Download APK Button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_apk_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export APK Package",
                            tint = HologramGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TitaniumSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TitaniumSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(androidx.compose.foundation.BorderStroke(0.8.dp, CyberBorder.copy(alpha = 0.5f)))
            ) {
                NavigationBarItem(
                    selected = activeTab == HudTab.COMMAND,
                    onClick = { viewModel.selectTab(HudTab.COMMAND) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Command HUD") },
                    label = { Text("Command", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = HudTextMuted,
                        unselectedTextColor = HudTextMuted,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_command")
                )

                NavigationBarItem(
                    selected = activeTab == HudTab.SMART_HOME,
                    onClick = { viewModel.selectTab(HudTab.SMART_HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Smart Home") },
                    label = { Text("Home", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = HudTextMuted,
                        unselectedTextColor = HudTextMuted,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_smart_home")
                )

                NavigationBarItem(
                    selected = activeTab == HudTab.SCHEDULE,
                    onClick = { viewModel.selectTab(HudTab.SCHEDULE) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule") },
                    label = { Text("Schedule", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = HudTextMuted,
                        unselectedTextColor = HudTextMuted,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_schedule")
                )

                NavigationBarItem(
                    selected = activeTab == HudTab.PROJECTS_EMAILS,
                    onClick = { viewModel.selectTab(HudTab.PROJECTS_EMAILS) },
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Projects & Emails") },
                    label = { Text("Projects", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = HudTextMuted,
                        unselectedTextColor = HudTextMuted,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_projects")
                )

                NavigationBarItem(
                    selected = activeTab == HudTab.BIOMETRICS,
                    onClick = { viewModel.selectTab(HudTab.BIOMETRICS) },
                    icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Voice Biometrics") },
                    label = { Text("Biometrics", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isAuthenticated) CyberCyan else NeonCrimson,
                        selectedTextColor = if (isAuthenticated) CyberCyan else NeonCrimson,
                        unselectedIconColor = HudTextMuted,
                        unselectedTextColor = HudTextMuted,
                        indicatorColor = (if (isAuthenticated) CyberCyan else NeonCrimson).copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_biometrics")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                HudTab.COMMAND -> CommandScreen(viewModel = viewModel)
                HudTab.SMART_HOME -> SmartHomeScreen(viewModel = viewModel)
                HudTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                HudTab.PROJECTS_EMAILS -> ProjectsEmailsScreen(viewModel = viewModel)
                HudTab.BIOMETRICS -> BiometricsScreen(viewModel = viewModel)
            }
        }

        if (showExportDialog) {
            ApkExportDialog(onDismissRequest = { showExportDialog = false })
        }
    }
}
