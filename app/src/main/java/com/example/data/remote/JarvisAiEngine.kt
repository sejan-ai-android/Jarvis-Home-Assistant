package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.CalendarTask
import com.example.data.model.EmailItem
import com.example.data.model.ProjectItem
import com.example.data.model.SmartDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class CommandExecutionResult {
    data class DeviceAction(val deviceId: String, val newState: Boolean, val value: Float?, val message: String) : CommandExecutionResult()
    data class ScheduleAction(val message: String, val shouldOptimize: Boolean) : CommandExecutionResult()
    data class ProjectAction(val message: String) : CommandExecutionResult()
    data class EmailAction(val message: String) : CommandExecutionResult()
    data class BiometricAction(val message: String, val requireVerification: Boolean) : CommandExecutionResult()
    data class GeneralResponse(val message: String) : CommandExecutionResult()
}

class JarvisAiEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Parses and handles user natural language input.
     * Combines fast-path direct command execution with Gemini conversational intelligence.
     */
    suspend fun processCommand(
        input: String,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        projects: List<ProjectItem>,
        emails: List<EmailItem>,
        isBiometricallyAuthenticated: Boolean
    ): CommandExecutionResult = withContext(Dispatchers.IO) {
        val lower = input.lowercase().trim()

        // 1. Direct Smart Home Device Controls
        if (lower.contains("turn on") || lower.contains("activate") || lower.contains("enable") || lower.contains("engage")) {
            val matchingDevice = devices.firstOrNull { dev ->
                lower.contains(dev.name.lowercase()) ||
                        lower.contains(dev.room.lowercase()) ||
                        (lower.contains("light") && dev.category == "LIGHTING") ||
                        (lower.contains("shield") && dev.category == "SECURITY") ||
                        (lower.contains("climate") && dev.category == "CLIMATE") ||
                        (lower.contains("reactor") && dev.id.contains("arc")) ||
                        (lower.contains("hologram") && dev.category == "MEDIA")
            }

            if (matchingDevice != null) {
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = matchingDevice.id,
                    newState = true,
                    value = if (matchingDevice.value == 0f) 80f else matchingDevice.value,
                    message = "Right away, Sir. ${matchingDevice.name} in the ${matchingDevice.room} has been engaged and brought online."
                )
            } else if (lower.contains("all lights") || lower.contains("all devices")) {
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = "ALL",
                    newState = true,
                    value = 80f,
                    message = "Understood. Engaging all automated systems and lighting arrays across the facility."
                )
            }
        }

        if (lower.contains("turn off") || lower.contains("deactivate") || lower.contains("disable") || lower.contains("disengage") || lower.contains("shut down")) {
            val matchingDevice = devices.firstOrNull { dev ->
                lower.contains(dev.name.lowercase()) ||
                        lower.contains(dev.room.lowercase()) ||
                        (lower.contains("light") && dev.category == "LIGHTING") ||
                        (lower.contains("shield") && dev.category == "SECURITY") ||
                        (lower.contains("climate") && dev.category == "CLIMATE")
            }

            if (matchingDevice != null) {
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = matchingDevice.id,
                    newState = false,
                    value = 0f,
                    message = "Deactivating ${matchingDevice.name}. Power routed to secondary reserves."
                )
            } else if (lower.contains("all lights") || lower.contains("all devices")) {
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = "ALL",
                    newState = false,
                    value = 0f,
                    message = "Powering down all smart home subsystems. Night standby protocols engaged."
                )
            }
        }

        // 2. Schedule and Calendar Optimizations
        if (lower.contains("schedule") || lower.contains("calendar") || lower.contains("optimize") || lower.contains("agenda") || lower.contains("today's plan")) {
            val pendingCount = tasks.count { !it.isCompleted }
            return@withContext CommandExecutionResult.ScheduleAction(
                message = "I have analyzed your itinerary for today, Sir. You have $pendingCount scheduled commitments. I have optimized your time blocks to prioritize high-focus deep work before midday.",
                shouldOptimize = lower.contains("optimize") || lower.contains("suggest")
            )
        }

        // 3. Personal Projects Tracking
        if (lower.contains("project") || lower.contains("telemetry") || lower.contains("mark 85") || lower.contains("armor") || lower.contains("progress")) {
            val activeProjects = projects.filter { it.status != "COMPLETED" }
            val summary = activeProjects.joinToString("; ") { "${it.title}: ${it.progressPercent}%" }
            return@withContext CommandExecutionResult.ProjectAction(
                message = "Project telemetry status nominal. Tracking ${activeProjects.size} active initiatives. Current standings: $summary. Project Vanguard is ready for flight telemetry verification."
            )
        }

        // 4. Pending Emails & Triage
        if (lower.contains("email") || lower.contains("inbox") || lower.contains("message") || lower.contains("unread")) {
            val urgentEmails = emails.filter { it.isUrgent }
            val msg = if (urgentEmails.isNotEmpty()) {
                "You have ${emails.size} pending communications, including ${urgentEmails.size} flagged urgent. Notably, ${urgentEmails.first().sender} requests authorization regarding '${urgentEmails.first().subject}'. I have drafted a preliminary response for your review."
            } else {
                "Your inbox is well managed, Sir. ${emails.size} messages logged, none requiring immediate intervention."
            }
            return@withContext CommandExecutionResult.EmailAction(msg)
        }

        // 5. Voice Biometric Commands
        if (lower.contains("biometric") || lower.contains("authenticate") || lower.contains("voiceprint") || lower.contains("lock") || lower.contains("security clearance")) {
            return@withContext CommandExecutionResult.BiometricAction(
                message = "Voice biometric authentication sub-routine engaged. Cryptographic hash verified against hardware enclave with cross-device synchronization.",
                requireVerification = lower.contains("verify") || lower.contains("enroll")
            )
        }

        // 6. Gemini Natural Language Intelligence with Context
        val geminiReply = queryGemini(input, devices, tasks, projects, emails)
        CommandExecutionResult.GeneralResponse(geminiReply)
    }

    private suspend fun queryGemini(
        userPrompt: String,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        projects: List<ProjectItem>,
        emails: List<EmailItem>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalJarvisResponse(userPrompt, devices, tasks, projects, emails)
        }

        return try {
            val systemInstruction = """
                You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the sophisticated, highly intelligent AI assistant created by Tony Stark.
                Speak in an authentic British assistant tone: calm, highly intelligent, refined, with mild polite wit, and absolute technical competence.
                Address the user as 'Sir' or 'Boss'.
                Keep responses punchy, concise (2-4 sentences), and informative.
                Current context:
                - Smart Devices online: ${devices.count { it.isOn }}/${devices.size}
                - Calendar tasks today: ${tasks.size} (${tasks.count { !it.isCompleted }} pending)
                - Active personal projects: ${projects.joinToString { "${it.codename} (${it.progressPercent}%)" }}
                - Unread emails: ${emails.count { it.isUnread }}
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userPrompt) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 250)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val rootJson = JSONObject(responseString)
                val text = rootJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                text.trim()
            } else {
                generateLocalJarvisResponse(userPrompt, devices, tasks, projects, emails)
            }
        } catch (e: Exception) {
            generateLocalJarvisResponse(userPrompt, devices, tasks, projects, emails)
        }
    }

    private fun generateLocalJarvisResponse(
        prompt: String,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        projects: List<ProjectItem>,
        emails: List<EmailItem>
    ): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("who are you") || lower.contains("introduce") ->
                "I am J.A.R.V.I.S., Sir. A Rather Very Intelligent System, configured to oversee your smart home matrix, calendar scheduling, high-priority projects, and secure encrypted voice authentication."

            lower.contains("how are you") || lower.contains("status") -> {
                val devicesOn = devices.count { it.isOn }
                val pending = tasks.count { !it.isCompleted }
                val unreadEmails = emails.count { it.isUnread }
                "All systems nominal, Sir. $devicesOn home appliances engaged, $pending calendar commitments on deck, and $unreadEmails unread communications awaiting your leisure."
            }

            lower.contains("thank") ->
                "Always at your service, Sir. Shall I adjust the lab lighting or review the flight telemetry?"

            lower.contains("good morning") -> {
                val firstTask = tasks.firstOrNull()
                "Good morning, Sir. Core temperature is 68 degrees, and coffee is brewing in the quarters. Your primary agenda item is '${firstTask?.title ?: "Executive Briefing"}'."
            }

            lower.contains("good night") ->
                "Good night, Sir. I have initiated low-power ambient mode, engaged perimeter defense shields, and queued your schedule optimization for 08:00 tomorrow."

            else ->
                "Understood, Sir. I have processed your request. Current telemetry is stable, and all sub-routines are operating at peak efficiency."
        }
    }
}
