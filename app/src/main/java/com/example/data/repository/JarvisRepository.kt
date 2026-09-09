package com.example.data.repository

import com.example.data.local.JarvisDao
import com.example.data.model.CalendarTask
import com.example.data.model.EmailItem
import com.example.data.model.ProjectItem
import com.example.data.model.SmartDevice
import com.example.data.model.VoiceBiometricProfile
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val jarvisDao: JarvisDao) {

    // Smart Devices
    val allDevices: Flow<List<SmartDevice>> = jarvisDao.getAllDevices()

    suspend fun setDevicePower(id: String, isOn: Boolean) {
        jarvisDao.setDevicePower(id, isOn)
    }

    suspend fun setDeviceValue(id: String, value: Float) {
        jarvisDao.setDeviceValue(id, value)
    }

    suspend fun setAllDevicesPower(isOn: Boolean) {
        jarvisDao.setAllDevicesPower(isOn)
    }

    suspend fun updateDevice(device: SmartDevice) {
        jarvisDao.updateDevice(device)
    }

    // Calendar Tasks
    val allTasks: Flow<List<CalendarTask>> = jarvisDao.getAllTasks()

    suspend fun addTask(task: CalendarTask): Long {
        return jarvisDao.insertTask(task)
    }

    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean) {
        jarvisDao.setTaskCompleted(id, isCompleted)
    }

    suspend fun deleteTask(id: Long) {
        jarvisDao.deleteTask(id)
    }

    suspend fun optimizeScheduleWithAI(currentTasks: List<CalendarTask>): List<CalendarTask> {
        // Reorder tasks by priority: CRITICAL first, then HIGH, then NORMAL
        // and adjust suggested focus blocks
        val prioritized = currentTasks.sortedWith(
            compareBy(
                {
                    when (it.priority) {
                        "CRITICAL" -> 0
                        "HIGH" -> 1
                        else -> 2
                    }
                },
                { it.suggestedOrder }
            )
        )
        val optimized = prioritized.mapIndexed { index, task ->
            val note = when (task.priority) {
                "CRITICAL" -> "⚡ AI Optimized: Morning prime focus slot. Minimized interruptions."
                "HIGH" -> "✦ AI Optimized: Scheduled with 15-min decompression buffer."
                else -> "✓ AI Optimized: Grouped into collaborative late afternoon block."
            }
            task.copy(suggestedOrder = index + 1, aiScheduleNote = note)
        }
        jarvisDao.insertTasks(optimized)
        return optimized
    }

    // Personal Projects
    val allProjects: Flow<List<ProjectItem>> = jarvisDao.getAllProjects()

    suspend fun updateProject(project: ProjectItem) {
        jarvisDao.updateProject(project)
    }

    suspend fun updateProjectProgress(id: String, progress: Int, telemetry: String) {
        jarvisDao.updateProjectProgress(id, progress, telemetry)
    }

    // Emails
    val allEmails: Flow<List<EmailItem>> = jarvisDao.getAllEmails()

    suspend fun markEmailReplied(id: String) {
        jarvisDao.markEmailReplied(id)
    }

    suspend fun setEmailReadStatus(id: String, isUnread: Boolean) {
        jarvisDao.setEmailReadStatus(id, isUnread)
    }

    // Voice Biometrics
    val biometricProfileFlow: Flow<VoiceBiometricProfile?> = jarvisDao.getBiometricProfileFlow()

    suspend fun getBiometricProfile(): VoiceBiometricProfile? {
        return jarvisDao.getBiometricProfile()
    }

    suspend fun saveBiometricProfile(profile: VoiceBiometricProfile) {
        jarvisDao.saveBiometricProfile(profile)
    }
}
