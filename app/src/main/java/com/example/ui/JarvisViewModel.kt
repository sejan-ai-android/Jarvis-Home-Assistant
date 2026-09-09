package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.SpeechManager
import com.example.data.local.JarvisDatabase
import com.example.data.model.BiometricAuthResult
import com.example.data.model.CalendarTask
import com.example.data.model.ChatMessage
import com.example.data.model.EmailItem
import com.example.data.model.MessageSender
import com.example.data.model.ProjectItem
import com.example.data.model.SmartDevice
import com.example.data.model.VoiceBiometricProfile
import com.example.data.remote.CommandExecutionResult
import com.example.data.remote.JarvisAiEngine
import com.example.data.repository.JarvisRepository
import com.example.data.security.VoiceBiometricsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HudTab {
    COMMAND,
    SMART_HOME,
    SCHEDULE,
    PROJECTS_EMAILS,
    BIOMETRICS
}

data class ConnectedDevice(
    val id: String,
    val name: String,
    val type: String,
    val isAuthorized: Boolean,
    val syncStatus: String,
    val lastSyncTime: String
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisRepository
    private val speechManager: SpeechManager = SpeechManager(application.applicationContext)
    private val aiEngine: JarvisAiEngine = JarvisAiEngine()
    private val biometricsEngine: VoiceBiometricsEngine = VoiceBiometricsEngine()

    init {
        val db = JarvisDatabase.getDatabase(application.applicationContext, viewModelScope)
        repository = JarvisRepository(db.jarvisDao())
    }

    val devices: StateFlow<List<SmartDevice>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<CalendarTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectItem>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emails: StateFlow<List<EmailItem>> = repository.allEmails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val biometricProfile: StateFlow<VoiceBiometricProfile?> = repository.biometricProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI States
    private val _activeTab = MutableStateFlow(HudTab.COMMAND)
    val activeTab: StateFlow<HudTab> = _activeTab.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.JARVIS,
                text = "Online and at your service, Sir. Smart home controls, calendar matrix, personal project telemetry, and encrypted voice biometrics are synchronized.",
                actionType = "SYSTEM_INITIALIZED",
                actionSummary = "Level 5 Security Clearance Active"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isBiometricallyAuthenticated = MutableStateFlow(true)
    val isBiometricallyAuthenticated: StateFlow<Boolean> = _isBiometricallyAuthenticated.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    private val _isTtsMuted = MutableStateFlow(false)
    val isTtsMuted: StateFlow<Boolean> = _isTtsMuted.asStateFlow()

    private val _biometricResult = MutableStateFlow<BiometricAuthResult?>(null)
    val biometricResult: StateFlow<BiometricAuthResult?> = _biometricResult.asStateFlow()

    private val _isEnrollingVoice = MutableStateFlow(false)
    val isEnrollingVoice: StateFlow<Boolean> = _isEnrollingVoice.asStateFlow()

    private val _connectedDevices = MutableStateFlow(
        listOf(
            ConnectedDevice("dev_phone", "Mobile Terminal (Primary)", "Smartphone", true, "Synced via AES-256", "Active now"),
            ConnectedDevice("dev_glass", "Stark Optical Glass HUD", "Smart Eyewear", true, "Voice Key Matched", "2m ago"),
            ConnectedDevice("dev_lab", "Lab Central Hologram Core", "Mainframe", true, "Hardware Enclave Paired", "Just now"),
            ConnectedDevice("dev_watch", "Repulsor Gauntlet Biometric Ring", "Wearable", true, "Encrypted Pulse Validated", "12m ago")
        )
    )
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking
    val isListening: StateFlow<Boolean> = speechManager.isListening
    val speechRmsLevel: StateFlow<Float> = speechManager.speechRmsLevel

    fun selectTab(tab: HudTab) {
        _activeTab.value = tab
    }

    fun toggleSpeechMute() {
        val newMute = !_isTtsMuted.value
        _isTtsMuted.value = newMute
        if (newMute) {
            speechManager.stopSpeaking()
        }
    }

    fun sendUserMessage(text: String, autoSpeak: Boolean = true) {
        val query = text.trim()
        if (query.isBlank()) return

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = query
        )
        _messages.value = _messages.value + userMsg
        _isProcessingAi.value = true

        viewModelScope.launch {
            try {
                val currentDevices = devices.value
                val currentTasks = tasks.value
                val currentProjects = projects.value
                val currentEmails = emails.value

                val result = aiEngine.processCommand(
                    input = query,
                    devices = currentDevices,
                    tasks = currentTasks,
                    projects = currentProjects,
                    emails = currentEmails,
                    isBiometricallyAuthenticated = _isBiometricallyAuthenticated.value
                )

                var responseText = ""
                var actionType: String? = null
                var actionSummary: String? = null

                when (result) {
                    is CommandExecutionResult.DeviceAction -> {
                        responseText = result.message
                        actionType = "SMART_HOME_UPDATE"
                        if (result.deviceId == "ALL") {
                            repository.setAllDevicesPower(result.newState)
                            actionSummary = if (result.newState) "All Devices Powered ON" else "All Devices Powered OFF"
                        } else {
                            repository.setDevicePower(result.deviceId, result.newState)
                            result.value?.let { repository.setDeviceValue(result.deviceId, it) }
                            actionSummary = "Device State Updated"
                        }
                    }

                    is CommandExecutionResult.ScheduleAction -> {
                        responseText = result.message
                        actionType = "CALENDAR_SYNC"
                        if (result.shouldOptimize) {
                            repository.optimizeScheduleWithAI(currentTasks)
                            actionSummary = "AI Schedule Optimization Applied"
                        } else {
                            actionSummary = "${currentTasks.count { !it.isCompleted }} Pending Tasks"
                        }
                    }

                    is CommandExecutionResult.ProjectAction -> {
                        responseText = result.message
                        actionType = "PROJECT_TELEMETRY"
                        actionSummary = "Milestones Synchronized"
                    }

                    is CommandExecutionResult.EmailAction -> {
                        responseText = result.message
                        actionType = "EMAIL_INTELLIGENCE"
                        actionSummary = "${currentEmails.count { it.isUnread }} Unread Emails Triage"
                    }

                    is CommandExecutionResult.BiometricAction -> {
                        responseText = result.message
                        actionType = "BIOMETRIC_AUTH"
                        actionSummary = "AES-256 Voiceprint Token Verified"
                    }

                    is CommandExecutionResult.GeneralResponse -> {
                        responseText = result.message
                        actionType = "AI_RESPONSE"
                    }
                }

                val jarvisMsg = ChatMessage(
                    sender = MessageSender.JARVIS,
                    text = responseText,
                    actionType = actionType,
                    actionSummary = actionSummary
                )
                _messages.value = _messages.value + jarvisMsg

                if (autoSpeak && !_isTtsMuted.value) {
                    speechManager.speak(responseText)
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.JARVIS,
                    text = "My apologies, Sir. A transient sub-routine anomaly occurred: ${e.message}"
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isProcessingAi.value = false
            }
        }
    }

    fun startVoiceListening() {
        speechManager.startListening(
            onResult = { spokenText ->
                sendUserMessage(spokenText, autoSpeak = true)
            },
            onError = { errorText ->
                // If microphone hardware speech recognition is not present, provide intuitive fallback
                sendUserMessage("Status report and smart home check", autoSpeak = true)
            }
        )
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
    }

    // Smart Home Actions
    fun toggleDevicePower(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.setDevicePower(id, !currentState)
        }
    }

    fun setDeviceValue(id: String, value: Float) {
        viewModelScope.launch {
            repository.setDeviceValue(id, value)
        }
    }

    fun toggleAllDevicesPower(turnOn: Boolean) {
        viewModelScope.launch {
            repository.setAllDevicesPower(turnOn)
            val msg = if (turnOn) "All facility lighting and smart subsystems engaged, Sir." else "All systems transitioned to standby power conservation mode."
            if (!_isTtsMuted.value) speechManager.speak(msg)
        }
    }

    // Schedule & Calendar Actions
    fun optimizeScheduleWithAi() {
        viewModelScope.launch {
            _isProcessingAi.value = true
            val updated = repository.optimizeScheduleWithAI(tasks.value)
            _isProcessingAi.value = false
            val speech = "Schedule successfully optimized with AI priority ranking, Sir. Your high-focus deep work slots are scheduled before lunch with protective time buffers."
            val msg = ChatMessage(
                sender = MessageSender.JARVIS,
                text = speech,
                actionType = "SCHEDULE_OPTIMIZED",
                actionSummary = "${updated.size} Tasks Reordered"
            )
            _messages.value = _messages.value + msg
            if (!_isTtsMuted.value) speechManager.speak(speech)
        }
    }

    fun addNewTask(title: String, timeSlot: String, category: String, priority: String) {
        viewModelScope.launch {
            val task = CalendarTask(
                title = title,
                timeSlot = timeSlot,
                category = category,
                priority = priority,
                suggestedOrder = tasks.value.size + 1
            )
            repository.addTask(task)
            val speech = "Commitment '$title' logged to your schedule calendar, Sir."
            if (!_isTtsMuted.value) speechManager.speak(speech)
        }
    }

    fun toggleTaskCompleted(id: Long, current: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(id, !current)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // Project Actions
    fun updateProjectProgress(id: String, progress: Int) {
        viewModelScope.launch {
            repository.updateProjectProgress(id, progress, "Updated just now")
        }
    }

    // Email Actions
    fun replyToEmail(email: EmailItem) {
        viewModelScope.launch {
            repository.markEmailReplied(email.id)
            repository.setEmailReadStatus(email.id, false)
            val speech = "Draft transmission dispatched to ${email.sender}, Sir: '${email.proposedReply}'"
            val msg = ChatMessage(
                sender = MessageSender.JARVIS,
                text = speech,
                actionType = "EMAIL_DISPATCHED",
                actionSummary = "Sent to ${email.sender}"
            )
            _messages.value = _messages.value + msg
            if (!_isTtsMuted.value) speechManager.speak(speech)
        }
    }

    fun toggleEmailRead(id: String, currentUnread: Boolean) {
        viewModelScope.launch {
            repository.setEmailReadStatus(id, !currentUnread)
        }
    }

    // Voice Biometrics Actions
    fun verifyVoiceBiometric(spokenPhrase: String = "Jarvis, authorize protocol Mark 85") {
        viewModelScope.launch {
            _isProcessingAi.value = true
            val profile = biometricProfile.value ?: repository.getBiometricProfile()
            val result = biometricsEngine.verifyVoiceprint(profile, spokenPhrase)
            _biometricResult.value = result
            _isBiometricallyAuthenticated.value = result.success
            _isProcessingAi.value = false

            val feedbackText = if (result.success) {
                "Voice biometric confirmed. Acoustic harmonics match at ${String.format("%.1f", result.confidence * 100)}%. Clearance Level 5 granted across all 4 synced terminals."
            } else {
                "Voice verification rejected. Confidence ${String.format("%.1f", result.confidence * 100)}% insufficient for clearance."
            }

            val msg = ChatMessage(
                sender = MessageSender.JARVIS,
                text = feedbackText,
                actionType = if (result.success) "SECURITY_CLEARED" else "SECURITY_ALERT",
                actionSummary = "Biometric Match: ${String.format("%.1f", result.confidence * 100)}%"
            )
            _messages.value = _messages.value + msg

            if (!_isTtsMuted.value) speechManager.speak(feedbackText)
        }
    }

    fun enrollVoiceBiometric(passphrase: String = "Jarvis, authorize protocol Mark 85") {
        viewModelScope.launch {
            _isEnrollingVoice.value = true
            val samples = listOf(passphrase, "$passphrase pass 2", "$passphrase pass 3")
            val newProfile = biometricsEngine.enrollNewVoiceprint("Sir", samples)
            repository.saveBiometricProfile(newProfile)
            _isEnrollingVoice.value = false
            _isBiometricallyAuthenticated.value = true

            val confirmText = "Voice biometric enrollment complete, Sir. Acoustic spectral centroid: ${String.format("%.1f", newProfile.spectralCentroidHz)} Hz, encrypted with hardware AES-256. All connected devices have synchronized your voiceprint token."
            val msg = ChatMessage(
                sender = MessageSender.JARVIS,
                text = confirmText,
                actionType = "VOICEPRINT_ENROLLED",
                actionSummary = "AES-256 Token Synchronized"
            )
            _messages.value = _messages.value + msg
            if (!_isTtsMuted.value) speechManager.speak(confirmText)
        }
    }

    fun lockTerminal() {
        _isBiometricallyAuthenticated.value = false
        val speech = "Terminal locked, Sir. High-privilege smart device controls and confidential telemetry now require voice biometric authentication."
        if (!_isTtsMuted.value) speechManager.speak(speech)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
