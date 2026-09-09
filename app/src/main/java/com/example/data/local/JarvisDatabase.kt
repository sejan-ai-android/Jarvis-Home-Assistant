package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CalendarTask
import com.example.data.model.EmailItem
import com.example.data.model.ProjectItem
import com.example.data.model.SmartDevice
import com.example.data.model.VoiceBiometricProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SmartDevice::class,
        CalendarTask::class,
        ProjectItem::class,
        EmailItem::class,
        VoiceBiometricProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_database"
                )
                    .addCallback(JarvisDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class JarvisDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.jarvisDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: JarvisDao) {
            // Initial Smart Devices
            val devices = listOf(
                SmartDevice(
                    id = "dev_arc_lights",
                    name = "Arc Reactor Ambient Grid",
                    room = "Command Lab",
                    category = "LIGHTING",
                    isOn = true,
                    value = 85f,
                    statusText = "Cyan 4200K / 85%",
                    iconName = "lightbulb",
                    powerWatts = 45
                ),
                SmartDevice(
                    id = "dev_living_lights",
                    name = "Penthouse Quantum LED",
                    room = "Living Quarters",
                    category = "LIGHTING",
                    isOn = true,
                    value = 60f,
                    statusText = "Warm White / 60%",
                    iconName = "lightbulb",
                    powerWatts = 28
                ),
                SmartDevice(
                    id = "dev_climate_lab",
                    name = "HVAC Climate Core",
                    room = "Command Lab",
                    category = "CLIMATE",
                    isOn = true,
                    value = 68f, // 68°F
                    statusText = "Target 68°F (Optimal)",
                    iconName = "thermostat",
                    powerWatts = 220
                ),
                SmartDevice(
                    id = "dev_perimeter_shield",
                    name = "Perimeter Security Shield",
                    room = "Perimeter",
                    category = "SECURITY",
                    isOn = true,
                    value = 100f,
                    statusText = "Locked & Armed",
                    iconName = "security",
                    powerWatts = 350
                ),
                SmartDevice(
                    id = "dev_holo_display",
                    name = "Holographic Workspace Array",
                    room = "Command Lab",
                    category = "MEDIA",
                    isOn = true,
                    value = 90f,
                    statusText = "3D Volumetric Active",
                    iconName = "display",
                    powerWatts = 180
                ),
                SmartDevice(
                    id = "dev_air_purifier",
                    name = "Ionic Atmospheric Scrubber",
                    room = "Living Quarters",
                    category = "CLIMATE",
                    isOn = true,
                    value = 75f,
                    statusText = "AQI: 12 (Pristine)",
                    iconName = "air",
                    powerWatts = 35
                ),
                SmartDevice(
                    id = "dev_workshop_forge",
                    name = "Mark Series Hydraulic Rig",
                    room = "Workshop",
                    category = "POWER",
                    isOn = false,
                    value = 0f,
                    statusText = "Standby Mode",
                    iconName = "build",
                    powerWatts = 10
                )
            )
            dao.insertDevices(devices)

            // Initial Calendar Tasks
            val tasks = listOf(
                CalendarTask(
                    title = "Arc Reactor Core Telemetry Review",
                    timeSlot = "09:00 - 09:45",
                    durationMinutes = 45,
                    category = "LAB",
                    priority = "CRITICAL",
                    isCompleted = false,
                    aiScheduleNote = "High mental focus required; suggested 15 min buffer after run.",
                    suggestedOrder = 1
                ),
                CalendarTask(
                    title = "Stark Industries Executive Briefing",
                    timeSlot = "10:30 - 11:15",
                    durationMinutes = 45,
                    category = "MEETING",
                    priority = "HIGH",
                    isCompleted = false,
                    aiScheduleNote = "Prep slides synced to holographic projector.",
                    suggestedOrder = 2
                ),
                CalendarTask(
                    title = "Mark 85 Flight Dynamics Calibration",
                    timeSlot = "13:00 - 14:30",
                    durationMinutes = 90,
                    category = "RESEARCH",
                    priority = "HIGH",
                    isCompleted = false,
                    aiScheduleNote = "Optimal afternoon high-energy test window.",
                    suggestedOrder = 3
                ),
                CalendarTask(
                    title = "Defense Grid Biometric Sync Protocol",
                    timeSlot = "15:30 - 16:15",
                    durationMinutes = 45,
                    category = "SECURITY",
                    priority = "NORMAL",
                    isCompleted = false,
                    aiScheduleNote = "Automated cross-device key re-issuance.",
                    suggestedOrder = 4
                ),
                CalendarTask(
                    title = "Daily System Diagnostics & Evening Wind-down",
                    timeSlot = "18:00 - 18:30",
                    durationMinutes = 30,
                    category = "FOCUS",
                    priority = "NORMAL",
                    isCompleted = false,
                    aiScheduleNote = "Auto-dim smart lights and set lab perimeter to defense mode.",
                    suggestedOrder = 5
                )
            )
            dao.insertTasks(tasks)

            // Initial Personal Projects
            val projects = listOf(
                ProjectItem(
                    id = "proj_mark85",
                    title = "Armor Mark 85 Flight Stabilization",
                    codename = "PROJECT VANGUARD",
                    status = "ACTIVE",
                    progressPercent = 88,
                    currentMilestone = "Micro-thruster vectoring tests at Mach 2.4",
                    pendingAction = "Requires live voice confirmation for supersonic flight envelope",
                    urgency = "HIGH",
                    lastTelemetryUpdate = "12m ago"
                ),
                ProjectItem(
                    id = "proj_clean_energy",
                    title = "Autonomous Arc Grid Clean Power",
                    codename = "PROJECT PROMETHEUS",
                    status = "ACTIVE",
                    progressPercent = 94,
                    currentMilestone = "Zero-emission grid load balancing operational",
                    pendingAction = "City municipal interconnect pending final sign-off",
                    urgency = "NORMAL",
                    lastTelemetryUpdate = "35m ago"
                ),
                ProjectItem(
                    id = "proj_holo_hud",
                    title = "Next-Gen Quantum Neural Link",
                    codename = "PROJECT SYNAPSE",
                    status = "TESTING",
                    progressPercent = 67,
                    currentMilestone = "Sub-second thought-to-command latency achieved",
                    pendingAction = "Spectral noise reduction on sensor node 4",
                    urgency = "NORMAL",
                    lastTelemetryUpdate = "2h ago"
                ),
                ProjectItem(
                    id = "proj_drone_defense",
                    title = "Autonomous Perimeter Sentry Swarm",
                    codename = "PROJECT AEGIS",
                    status = "ACTIVE",
                    progressPercent = 78,
                    currentMilestone = "Geofenced patrol algorithms verified",
                    pendingAction = "Awaiting encrypted biometric pairing on secondary terminals",
                    urgency = "CRITICAL",
                    lastTelemetryUpdate = "5m ago"
                )
            )
            dao.insertProjects(projects)

            // Initial Pending Emails with AI Summaries & Drafts
            val emails = listOf(
                EmailItem(
                    id = "email_1",
                    sender = "Pepper Potts",
                    senderRole = "CEO, Stark Industries",
                    subject = "Q3 Clean Energy Expansion - Board Sign-off Required",
                    snippet = "Tony, the board has cleared the funding allocations for the Pacific clean energy corridor. We just need your digital seal before 2 PM.",
                    receivedTime = "18m ago",
                    isUnread = true,
                    isUrgent = true,
                    aiSummary = "Board approved $4.2B clean energy expansion. Requires Tony's final digital sign-off by 14:00 today.",
                    proposedReply = "Approved. Cryptographic authorization token attached. Proceed with corridor rollout immediately."
                ),
                EmailItem(
                    id = "email_2",
                    sender = "Dr. Bruce Banner",
                    senderRole = "Research Partner",
                    subject = "Gamma Sensor Drift in Sector 4 Cryo-Lab",
                    snippet = "JARVIS flagged an unexpected oscillation in the containment dampeners. I ran preliminary scans, looks like a 0.04% harmonic shift.",
                    receivedTime = "1h ago",
                    isUnread = true,
                    isUrgent = true,
                    aiSummary = "0.04% sensor harmonic anomaly detected in Sector 4. Dr. Banner advises recalibrating dampeners before next cycle.",
                    proposedReply = "I have recalibrated dampeners remotely and engaged secondary magnetic shielding. Let's sync at 14:00."
                ),
                EmailItem(
                    id = "email_3",
                    sender = "Col. James Rhodes",
                    senderRole = "War Machine Tactical",
                    subject = "Avionics Joint Diagnostics File",
                    snippet = "Sending telemetry logs from yesterday's low-altitude exercise. The repulsor cooling rate on the right gauntlet is slightly lagging.",
                    receivedTime = "3h ago",
                    isUnread = false,
                    isUrgent = false,
                    aiSummary = "Right gauntlet repulsor thermal heat dissipation is 8% below target during rapid burst maneuvers.",
                    proposedReply = "Received telemetry, Rhodey. Running fluid dynamics simulation on the cooling manifold now."
                )
            )
            dao.insertEmails(emails)

            // Initial Voice Biometrics Profile
            val profile = VoiceBiometricProfile(
                id = "primary_user",
                userName = "Sir",
                isEnrolled = true,
                voiceprintHash = "AES256:E9A28B71C43F05D829A167E429DF0B7C6E8F19",
                spectralCentroidHz = 142.5f,
                harmonicsRatio = 0.96f,
                encryptionKeyType = "AES-256 Hardware Keystore",
                syncedDevicesCount = 3,
                lastAuthenticated = System.currentTimeMillis(),
                confidenceScore = 0.992f
            )
            dao.saveBiometricProfile(profile)
        }
    }
}
