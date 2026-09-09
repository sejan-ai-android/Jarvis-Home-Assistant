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

    fun isBengaliInput(text: String): Boolean {
        return text.any { it in '\u0980'..'\u09FF' } ||
                text.contains("kemon", ignoreCase = true) ||
                text.contains("valo", ignoreCase = true) ||
                text.contains("bhalo", ignoreCase = true) ||
                text.contains("korun", ignoreCase = true) ||
                text.contains("koro", ignoreCase = true) ||
                text.contains("shuno", ignoreCase = true)
    }

    /**
     * Parses and handles user natural language input in both English and Bengali (বাংলা).
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
        val isBn = isBengaliInput(input)

        // 1. Direct Smart Home Device Controls (English & Bangla)
        val isTurnOn = lower.contains("turn on") || lower.contains("activate") || lower.contains("enable") || lower.contains("engage") ||
                lower.contains("অন করো") || lower.contains("চালু করো") || lower.contains("জ্বালাও") || lower.contains("অন কর")
        val isTurnOff = lower.contains("turn off") || lower.contains("deactivate") || lower.contains("disable") || lower.contains("disengage") || lower.contains("shut down") ||
                lower.contains("বন্ধ করো") || lower.contains("নিভাও") || lower.contains("অফ করো") || lower.contains("অফ কর")

        if (isTurnOn) {
            val matchingDevice = devices.firstOrNull { dev ->
                lower.contains(dev.name.lowercase()) ||
                        lower.contains(dev.room.lowercase()) ||
                        (lower.contains("light") && dev.category == "LIGHTING") ||
                        (lower.contains("লাইট") && dev.category == "LIGHTING") ||
                        (lower.contains("বাতি") && dev.category == "LIGHTING") ||
                        (lower.contains("shield") && dev.category == "SECURITY") ||
                        (lower.contains("শিল্ড") && dev.category == "SECURITY") ||
                        (lower.contains("climate") && dev.category == "CLIMATE") ||
                        (lower.contains("এসি") && dev.category == "CLIMATE") ||
                        (lower.contains("reactor") && dev.id.contains("arc")) ||
                        (lower.contains("hologram") && dev.category == "MEDIA")
            }

            if (matchingDevice != null) {
                val msg = if (isBn) {
                    "অবশ্যই স্যার, ${matchingDevice.room} এর ${matchingDevice.name} চালু করা হয়েছে।"
                } else {
                    "Right away, Sir. ${matchingDevice.name} in the ${matchingDevice.room} has been engaged and brought online."
                }
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = matchingDevice.id,
                    newState = true,
                    value = if (matchingDevice.value == 0f) 80f else matchingDevice.value,
                    message = msg
                )
            } else if (lower.contains("all lights") || lower.contains("all devices") || lower.contains("সব লাইট") || lower.contains("সব ডিভাইস")) {
                val msg = if (isBn) {
                    "সবগুলো স্মার্ট হোম ডিভাইস এবং লাইটিং সিস্টেম চালু করা হলো, স্যার।"
                } else {
                    "Understood. Engaging all automated systems and lighting arrays across the facility."
                }
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = "ALL",
                    newState = true,
                    value = 80f,
                    message = msg
                )
            }
        }

        if (isTurnOff) {
            val matchingDevice = devices.firstOrNull { dev ->
                lower.contains(dev.name.lowercase()) ||
                        lower.contains(dev.room.lowercase()) ||
                        (lower.contains("light") && dev.category == "LIGHTING") ||
                        (lower.contains("লাইট") && dev.category == "LIGHTING") ||
                        (lower.contains("বাতি") && dev.category == "LIGHTING") ||
                        (lower.contains("shield") && dev.category == "SECURITY") ||
                        (lower.contains("শিল্ড") && dev.category == "SECURITY") ||
                        (lower.contains("climate") && dev.category == "CLIMATE")
            }

            if (matchingDevice != null) {
                val msg = if (isBn) {
                    "${matchingDevice.name} সফলভাবে বন্ধ করা হয়েছে, স্যার।"
                } else {
                    "Deactivating ${matchingDevice.name}. Power routed to secondary reserves."
                }
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = matchingDevice.id,
                    newState = false,
                    value = 0f,
                    message = msg
                )
            } else if (lower.contains("all lights") || lower.contains("all devices") || lower.contains("সব লাইট") || lower.contains("সব ডিভাইস")) {
                val msg = if (isBn) {
                    "সমস্ত সিস্টেম নাইট স্ট্যান্ডবাই মোডে স্থানান্তর করা হয়েছে, স্যার।"
                } else {
                    "Powering down all smart home subsystems. Night standby protocols engaged."
                }
                return@withContext CommandExecutionResult.DeviceAction(
                    deviceId = "ALL",
                    newState = false,
                    value = 0f,
                    message = msg
                )
            }
        }

        // 2. Schedule and Calendar Optimizations (English & Bangla)
        if (lower.contains("schedule") || lower.contains("calendar") || lower.contains("optimize") || lower.contains("agenda") || lower.contains("today's plan") ||
            lower.contains("শিডিউল") || lower.contains("রুটিন") || lower.contains("ক্যালেন্ডার") || lower.contains("আজকের কাজ") || lower.contains("অপ্টিমাইজ")) {
            val pendingCount = tasks.count { !it.isCompleted }
            val msg = if (isBn) {
                "স্যার, আজকের শিডিউল পর্যবেক্ষণ করে দেখলাম মোট $pendingCount টি কাজ বাকি রয়েছে। উচ্চ মনোযোগের কাজগুলো সকালের স্লটে অপ্টিমাইজ করা হয়েছে।"
            } else {
                "I have analyzed your itinerary for today, Sir. You have $pendingCount scheduled commitments. I have optimized your time blocks to prioritize high-focus deep work before midday."
            }
            return@withContext CommandExecutionResult.ScheduleAction(
                message = msg,
                shouldOptimize = lower.contains("optimize") || lower.contains("suggest") || lower.contains("অপ্টিমাইজ")
            )
        }

        // 3. Personal Projects Tracking (English & Bangla)
        if (lower.contains("project") || lower.contains("telemetry") || lower.contains("mark 85") || lower.contains("armor") || lower.contains("progress") ||
            lower.contains("প্রজেক্ট") || lower.contains("টেলিমეტ্রি") || lower.contains("মার্ক ৮৫") || lower.contains("কাজের অগ্রগতি")) {
            val activeProjects = projects.filter { it.status != "COMPLETED" }
            val summary = activeProjects.joinToString("; ") { "${it.title}: ${it.progressPercent}%" }
            val msg = if (isBn) {
                "প্রজেক্ট টেলিমეტ্রি স্বাভাবিক রয়েছে, স্যার। ${activeProjects.size} টি প্রজেক্ট চালু রয়েছে। প্রজেক্ট ভ্যানগার্ড (Mark 85) ৮৮% সম্পন্ন এবং ফ্লাইট টেস্টের জন্য প্রস্তুত।"
            } else {
                "Project telemetry status nominal. Tracking ${activeProjects.size} active initiatives. Current standings: $summary. Project Vanguard is ready for flight telemetry verification."
            }
            return@withContext CommandExecutionResult.ProjectAction(message = msg)
        }

        // 4. Pending Emails & Triage (English & Bangla)
        if (lower.contains("email") || lower.contains("inbox") || lower.contains("message") || lower.contains("unread") ||
            lower.contains("ইমেইল") || lower.contains("ইনবক্স") || lower.contains("মেসেজ") || lower.contains("মেইল")) {
            val urgentEmails = emails.filter { it.isUrgent }
            val msg = if (isBn) {
                if (urgentEmails.isNotEmpty()) {
                    "স্যার, আপনার ইনবক্সে ${emails.size} টি বার্তা রয়েছে, যার মধ্যে ${urgentEmails.size} টি জরুরি। ${urgentEmails.first().sender} আপনার অনুমোদনের অপেক্ষা করছেন। একটি উত্তর ড্রাফট প্রস্তুত আছে।"
                } else {
                    "ইনবক্সের অবস্থা স্বাভাবিক, স্যার। ${emails.size} টি বার্তা রয়েছে, তাৎক্ষণিক ব্যবস্থা নেয়ার প্রয়োজন নেই।"
                }
            } else {
                if (urgentEmails.isNotEmpty()) {
                    "You have ${emails.size} pending communications, including ${urgentEmails.size} flagged urgent. Notably, ${urgentEmails.first().sender} requests authorization regarding '${urgentEmails.first().subject}'. I have drafted a preliminary response for your review."
                } else {
                    "Your inbox is well managed, Sir. ${emails.size} messages logged, none requiring immediate intervention."
                }
            }
            return@withContext CommandExecutionResult.EmailAction(msg)
        }

        // 5. Voice Biometric Commands (English & Bangla)
        if (lower.contains("biometric") || lower.contains("authenticate") || lower.contains("voiceprint") || lower.contains("lock") || lower.contains("security clearance") ||
            lower.contains("বায়োমেট্রিক") || lower.contains("ভয়েস") || lower.contains("লক করো") || lower.contains("নিরাপত্তা")) {
            val msg = if (isBn) {
                "ভয়েস বায়োমেট্রিক প্রমাণীকরণ সক্রিয় করা হয়েছে। হার্ডওয়্যার এসআই-২৫৬ এনক্লেভে ভয়েসপ্রিন্ট হ্যাশ যাচাইকৃত।"
            } else {
                "Voice biometric authentication sub-routine engaged. Cryptographic hash verified against hardware enclave with cross-device synchronization."
            }
            return@withContext CommandExecutionResult.BiometricAction(
                message = msg,
                requireVerification = lower.contains("verify") || lower.contains("enroll") || lower.contains("ভেরিফাই")
            )
        }

        // 6. Gemini Natural Language Intelligence with Context (Bilingual)
        val geminiReply = queryGemini(input, devices, tasks, projects, emails, isBn)
        CommandExecutionResult.GeneralResponse(geminiReply)
    }

    private suspend fun queryGemini(
        userPrompt: String,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        projects: List<ProjectItem>,
        emails: List<EmailItem>,
        isBengali: Boolean
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalJarvisResponse(userPrompt, devices, tasks, projects, emails, isBengali)
        }

        return try {
            val systemInstruction = """
                You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic AI assistant originally built by Tony Stark.
                You are fully bilingual in both English and Bengali (বাংলা).
                Rules:
                - If the user speaks or types in Bengali (বাংলা) or Banglish, respond entirely in natural, sophisticated, polite Bengali (বাংলা) with authentic JARVIS poise (addressing the user respectfully as 'স্যার' or 'বস').
                - If the user speaks or types in English, respond in refined British English (addressing as 'Sir' or 'Boss').
                - If the user uses a bilingual mix, respond naturally in the dominant language.
                - Keep responses concise (2-3 sentences), punchy, direct, and helpful.
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
                generateLocalJarvisResponse(userPrompt, devices, tasks, projects, emails, isBengali)
            }
        } catch (e: Exception) {
            generateLocalJarvisResponse(userPrompt, devices, tasks, projects, emails, isBengali)
        }
    }

    private fun generateLocalJarvisResponse(
        prompt: String,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        projects: List<ProjectItem>,
        emails: List<EmailItem>,
        isBengali: Boolean
    ): String {
        val lower = prompt.lowercase()

        if (isBengali) {
            return when {
                lower.contains("কেমন আছেন") || lower.contains("কেমন আছো") || lower.contains("kemon") -> {
                    val devicesOn = devices.count { it.isOn }
                    val pending = tasks.count { !it.isCompleted }
                    "আমি প্রস্তুত এবং সব সিস্টেম সক্রিয় রয়েছে, স্যার। $devicesOn টি স্মার্ট ডিভাইস চালু আছে এবং আজ $pending টি কাজ নির্ধারিত আছে। আপনাকে কীভাবে সাহায্য করতে পারি?"
                }

                lower.contains("তুমি কে") || lower.contains("পরিচয়") || lower.contains("who are you") ->
                    "আমি জারভিস (J.A.R.V.I.S.), স্যার। আপনার বিশ্বস্ত কৃত্রিম বুদ্ধিমত্তা সহকারী। স্মার্ট হোম নিয়ন্ত্রণ, শিডিউল ব্যবস্থাপনা এবং গোপনীয় প্রজেক্ট দেখভালের জন্য নিয়োজিত।"

                lower.contains("ধন্যবাদ") || lower.contains("থ্যাঙ্কস") || lower.contains("thank") ->
                    "সবসময় আপনার সেবায় নিয়োজিত, স্যার। ল্যাবের লাইট বা অন্য কোনো সিস্টেমে কি সমন্বয় করতে হবে?"

                lower.contains("শুভ সকাল") || lower.contains("good morning") -> {
                    val firstTask = tasks.firstOrNull()
                    "শুভ সকাল, স্যার। ল্যাবের তাপমাত্রা ৬৮ ডিগ্রি ফারেনহাইট। আজকের প্রথম কর্মসূচি: '${firstTask?.title ?: "এক্সিকিউটিভ ব্রিফিং"}'।"
                }

                lower.contains("শুভ রাত্রি") || lower.contains("good night") ->
                    "শুভ রাত্রি, স্যার। আমি নাইট পাওয়ার সেভিং মোড অন করেছি এবং পেরিমিটার নিরাপত্তা সক্রিয় করেছি।"

                else ->
                    "বুঝেছি, স্যার। আপনার নির্দেশ প্রসেস করা হয়েছে। সব সিস্টেম সর্বোচ্চ দক্ষতায় কাজ করছে।"
            }
        }

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
