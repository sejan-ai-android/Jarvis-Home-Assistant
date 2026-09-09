package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Lock
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

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickCommands = listOf(
        "Status report",
        "Optimize today's schedule",
        "Engage lab arc lights",
        "Check urgent emails",
        "Mark 85 flight telemetry",
        "Lock terminal biometrics"
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
            borderColor = if (!isAuthenticated) NeonCrimson else CyberCyan.copy(alpha = 0.4f),
            glowColor = if (!isAuthenticated) NeonCrimson.copy(alpha = 0.5f) else CyberCyan.copy(alpha = 0.3f)
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
                        text = if (isAuthenticated) "SYSTEMS NOMINAL" else "SECURITY LOCKED",
                        statusColor = if (isAuthenticated) NeonEmerald else NeonCrimson
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleSpeechMute() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isTtsMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
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

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive Arc Reactor Core
                ArcReactorVisualizer(
                    size = 140.dp,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    isLocked = !isAuthenticated,
                    audioLevel = speechRms
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Real-time Audio Frequency Waveform
                AudioWaveformBar(
                    isAnimating = isSpeaking || isListening || isProcessingAi,
                    color = if (!isAuthenticated) NeonCrimson else if (isListening) HologramGold else CyberCyan
                )

                Text(
                    text = when {
                        isProcessingAi -> "JARVIS // PROCESSING QUANTUM SUB-ROUTINES..."
                        isListening -> "JARVIS // LISTENING FOR VOICE INPUT..."
                        isSpeaking -> "JARVIS // AUDIO SYNTHESIS ACTIVE"
                        !isAuthenticated -> "RESTRICTED // VOICE BIOMETRIC AUTHENTICATION REQUIRED"
                        else -> "JARVIS // STANDING BY FOR COMMANDS"
                    },
                    color = if (!isAuthenticated) NeonCrimson else if (isListening) HologramGold else HudTextCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Action Chips
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
                            fontSize = 12.sp,
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
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Conversation History Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Voice & Text Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TitaniumSurface, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Mic Button
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
                    contentDescription = "Voice speech input",
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
                        "Command Jarvis...",
                        color = HudTextMuted,
                        fontSize = 13.sp,
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.95f)
                .clip(RoundedCornerShape(if (isUser) 14.dp else 12.dp))
                .background(if (isUser) TitaniumSurface else Color(0xFF0F1E32))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = HudTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (message.actionType != null || message.actionSummary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        message.actionType?.let {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = it,
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        message.actionSummary?.let {
                            Text(
                                text = it,
                                color = HudTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
