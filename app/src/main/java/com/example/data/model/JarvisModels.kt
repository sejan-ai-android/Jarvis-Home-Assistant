package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_devices")
data class SmartDevice(
    @PrimaryKey val id: String,
    val name: String,
    val room: String,
    val category: String, // LIGHTING, CLIMATE, SECURITY, MEDIA, POWER
    val isOn: Boolean,
    val value: Float, // 0f - 100f (e.g. brightness %, temp 60-80, or volume)
    val statusText: String,
    val iconName: String,
    val powerWatts: Int = 15,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_tasks")
data class CalendarTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timeSlot: String, // e.g. "09:00 - 10:00"
    val durationMinutes: Int = 60,
    val category: String, // FOCUS, MEETING, LAB, REVIEW, SECURITY
    val priority: String, // CRITICAL, HIGH, NORMAL
    val isCompleted: Boolean = false,
    val aiScheduleNote: String? = null,
    val suggestedOrder: Int = 0
)

@Entity(tableName = "personal_projects")
data class ProjectItem(
    @PrimaryKey val id: String,
    val title: String,
    val codename: String,
    val status: String, // ACTIVE, IN_REVIEW, COMPLETED, TESTING
    val progressPercent: Int, // 0 - 100
    val currentMilestone: String,
    val pendingAction: String,
    val urgency: String, // CRITICAL, HIGH, NORMAL
    val lastTelemetryUpdate: String
)

@Entity(tableName = "email_items")
data class EmailItem(
    @PrimaryKey val id: String,
    val sender: String,
    val senderRole: String,
    val subject: String,
    val snippet: String,
    val receivedTime: String,
    val isUnread: Boolean,
    val isUrgent: Boolean,
    val aiSummary: String,
    val proposedReply: String,
    val replied: Boolean = false
)

@Entity(tableName = "voice_biometrics")
data class VoiceBiometricProfile(
    @PrimaryKey val id: String = "primary_user",
    val userName: String = "Sir",
    val isEnrolled: Boolean = true,
    val voiceprintHash: String = "SHA256:8f4c2e1b9a7d3f0e5c8b2a1d4e7f9a0c2b3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f",
    val spectralCentroidHz: Float = 142.5f,
    val harmonicsRatio: Float = 0.94f,
    val encryptionKeyType: String = "AES-256-GCM Hardware-Backed Keystore",
    val syncedDevicesCount: Int = 3,
    val lastAuthenticated: Long = System.currentTimeMillis(),
    val confidenceScore: Float = 0.985f
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null,
    val actionSummary: String? = null,
    val isAudioPlaying: Boolean = false
) {
    val formattedTimestamp: String
        get() {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM
}

data class BiometricAuthResult(
    val success: Boolean,
    val confidence: Float,
    val spectralMatchScore: Float,
    val message: String,
    val token: String? = null
)
