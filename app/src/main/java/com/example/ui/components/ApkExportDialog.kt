package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HologramGold
import com.example.ui.theme.HudTextCyan
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TitaniumBackground
import com.example.ui.theme.TitaniumSurface
import com.example.util.ApkExportHelper

@Composable
fun ApkExportDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val apkInfo = ApkExportHelper.getApkInfo(context)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("apk_export_dialog"),
            colors = CardDefaults.cardColors(containerColor = TitaniumBackground),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, CyberCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "APK EXPORT PACKAGE",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "STANDALONE ANDROID INSTALLER",
                                color = HudTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp).testTag("close_apk_export_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = HudTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // APK File Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TitaniumSurface)
                        .border(0.8.dp, CyberBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PACKAGE NAME:", color = HudTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(apkInfo.fileName, color = NeonEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BUILD SIZE:", color = HudTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(apkInfo.sizeFormatted, color = HudTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("RELEASE VERSION:", color = HudTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("v${apkInfo.version} (Debug signed)", color = HologramGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MINIMUM OS:", color = HudTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Android 8.0+ (API 26)", color = HudTextCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Direct File Tree Location Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonEmerald.copy(alpha = 0.08f))
                        .border(0.8.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                text = "DIRECT PROJECT FILE LOCATIONS",
                                color = NeonEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "• /jarvis-assist.apk (Project Root)\n• /releases/jarvis-assist-v1.0.apk\n• /app-debug.apk",
                                color = HudTextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You can download directly from the AI Studio File Tree on the left sidebar.",
                                color = HudTextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = {
                        ApkExportHelper.exportAndShareApk(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("share_apk_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan.copy(alpha = 0.25f),
                        contentColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share APK",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPORT / SHARE APK FILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("/jarvis-assist.apk"))
                        Toast.makeText(context, "Path copied: /jarvis-assist.apk", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("copy_apk_path_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HologramGold
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, HologramGold.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Copy Path",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COPY PROJECT FILE PATH",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
