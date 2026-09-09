package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.audio.ConversationLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorVisualizer
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
fun CommandScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isProcessingAi by viewModel.isProcessingAi.collectAsStateWithLifecycle()
    val isTtsMuted by viewModel.isTtsMuted.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isBiometricallyAuthenticated.collectAsStateWithLifecycle()
    val speechRms by viewModel.speechRmsLevel.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isLiveAudioActive by viewModel.isLiveAudioConversation.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickCommands = listOf(
        "Status report",
        "কেমন আছেন জারভিস?",
        "ল্যাবের লাইট অন করো",
        "আজকের শিডিউল কেমন?",
        "Optimize today's schedule",
        "জরুরি ইমেইল চেক করো",
        "প্রজেক্ট আপডেট দিন",
        "Mark 85 flight telemetry",
        "বায়োমেট্রিক সিকিউরিটি চেক"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top HUD Reactor Card
        HudCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reactor_hud_card"),
            borderColor = if (!isAuthenticated) NeonCrimson else if (isLiveAudioActive) NeonEmerald else CyberCyan.copy(alpha = 0.4f),
            glowColor = if (!isAuthenticated) NeonCrimson.copy(alpha = 0.5f) else if (isLiveAudioActive) NeonEmerald.copy(alpha = 0.4f) else CyberCyan.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudStatusPill(
                        text = if (!isAuthenticated) "SECURITY LOCKED" else if (isLiveAudioActive) "LIVE AUDIO // সক্রিয়" else "SYSTEMS NOMINAL",
                        statusColor = if (!isAuthenticated) NeonCrimson else if (isLiveAudioActive) NeonEmerald else CyberCyan
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleSpeechMute() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isTtsMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Toggle voice audio",
                                tint = if (isTtsMuted) HudTextMuted else CyberCyan
                            )
                        }

                        if (isAuthenticated) {
                            IconButton(
                                onClick = { viewModel.lockTerminal() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Lock terminal",
                                    tint = HologramGold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Interactive Arc Reactor Core
                ArcReactorVisualizer(
                    size = 130.dp,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    isLocked = !isAuthenticated,
                    audioLevel = speechRms
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Real-time Audio Frequency Waveform
                AudioWaveformBar(
                    isAnimating = isSpeaking || isListening || isProcessingAi || isLiveAudioActive,
                    color = if (!isAuthenticated) NeonCrimson else if (isLiveAudioActive) NeonEmerald else if (isListening) HologramGold else CyberCyan
                )

                Text(
                    text = when {
                        isProcessingAi -> "JARVIS // PROCESSING QUANTUM SUB-ROUTINES..."
                        isListening -> "JARVIS // LISTENING... (বলুন, শুনছি...)"
                        isSpeaking -> "JARVIS // AUDIO SYNTHESIS ACTIVE (কথা বলছি...)"
                        isLiveAudioActive -> "LIVE AUDIO MODE // SPEAK IN BANGLA OR ENGLISH (অডিও কথোপকথন সক্রিয়)"
                        !isAuthenticated -> "RESTRICTED // VOICE BIOMETRIC AUTHENTICATION REQUIRED"
                        else -> "JARVIS // STANDING BY (বাংলা অথবা ইংরেজিতে কথা বলুন)"
                    },
                    color = if (!isAuthenticated) NeonCrimson else if (isLiveAudioActive) NeonEmerald else if (isListening) HologramGold else HudTextCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hands-Free Live Audio Conversation Toggle Button
                Button(
                    onClick = { viewModel.toggleLiveAudioConversation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("live_audio_toggle_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLiveAudioActive) NeonEmerald.copy(alpha = 0.25f) else CyberCyan.copy(alpha = 0.15f),
                        contentColor = if (isLiveAudioActive) NeonEmerald else CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLiveAudioActive) NeonEmerald else CyberCyan.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiveAudioActive) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Live voice conversation",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isLiveAudioActive) "LIVE AUDIO ACTIVE // TAP TO PAUSE" else "START LIVE AUDIO CONVERSATION (বাংলা / EN)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Language Mode Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LANGUAGE // ভাষা:",
                color = HudTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ConversationLanguage.entries.forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setConversationLanguage(lang) },
                        label = {
                            Text(
                                text = when (lang) {
                                    ConversationLanguage.AUTO -> "Auto (বাংলা/EN)"
                                    ConversationLanguage.BANGLA -> "বাংলা"
                                    ConversationLanguage.ENGLISH -> "English"
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) HologramGold.copy(alpha = 0.2f) else TitaniumSurface,
                            labelColor = if (isSelected) HologramGold else HudTextSecondary,
                            selectedContainerColor = HologramGold.copy(alpha = 0.25f),
                            selectedLabelColor = HologramGold
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) HologramGold else CyberBorder.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Command Action Chips (Bilingual)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickCommands.forEach { cmd ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.sendUserMessage(cmd) },
                    label = {
                        Text(
                            text = cmd,
                            color = HudTextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = TitaniumSurface,
                        labelColor = HudTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = CyberBorder
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Conversation History Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TitaniumSurface)
                .border(0.8.dp, CyberBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isListening) {
                        viewModel.stopVoiceListening()
                    } else {
                        viewModel.startVoiceListening()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isListening) HologramGold.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.15f))
                    .testTag("voice_mic_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice speech input in Bangla or English",
                    tint = if (isListening) HologramGold else CyberCyan
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                placeholder = {
                    Text(
                        if (selectedLanguage == ConversationLanguage.BANGLA) "জারভিসকে কমান্ড দিন..." else "Command Jarvis in English or বাংলা...",
                        color = HudTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendUserMessage(inputText)
                            inputText = ""
                        }
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = HudTextPrimary,
                    unfocusedTextColor = HudTextPrimary,
                    cursorColor = CyberCyan
                )
            )

            if (isProcessingAi) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(4.dp),
                    color = CyberCyan,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendUserMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier.size(40.dp).testTag("command_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send command",
                        tint = if (inputText.isNotBlank()) CyberCyan else HudTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isUser) "COMMAND // USER" else "J.A.R.V.I.S.",
                color = if (isUser) HologramGold else CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "• ${message.formattedTimestamp}",
                color = HudTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(
                    if (isUser) CyberCyan.copy(alpha = 0.12f) else TitaniumSurface
                )
                .border(
                    0.8.dp,
                    if (isUser) CyberCyan.copy(alpha = 0.4f) else CyberBorder.copy(alpha = 0.6f),
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = HudTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontFamily = FontFamily.SansSerif
                )

                if (message.actionSummary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberCyan.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                        Text(
                            text = message.actionSummary,
                            color = HudTextCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
