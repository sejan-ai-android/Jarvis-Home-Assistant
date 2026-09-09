package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CalendarTask
import com.example.data.model.EmailItem
import com.example.data.model.ProjectItem
import com.example.data.model.SmartDevice
import com.example.data.model.VoiceBiometricProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {

    // Smart Devices
    @Query("SELECT * FROM smart_devices ORDER BY room ASC, name ASC")
    fun getAllDevices(): Flow<List<SmartDevice>>

    @Query("SELECT * FROM smart_devices WHERE id = :id")
    suspend fun getDeviceById(id: String): SmartDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<SmartDevice>)

    @Update
    suspend fun updateDevice(device: SmartDevice)

    @Query("UPDATE smart_devices SET isOn = :isOn, lastUpdated = :now WHERE id = :id")
    suspend fun setDevicePower(id: String, isOn: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET value = :value, lastUpdated = :now WHERE id = :id")
    suspend fun setDeviceValue(id: String, value: Float, now: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET isOn = :isOn, lastUpdated = :now")
    suspend fun setAllDevicesPower(isOn: Boolean, now: Long = System.currentTimeMillis())

    // Calendar Tasks
    @Query("SELECT * FROM calendar_tasks ORDER BY suggestedOrder ASC, id ASC")
    fun getAllTasks(): Flow<List<CalendarTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<CalendarTask>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CalendarTask): Long

    @Update
    suspend fun updateTask(task: CalendarTask)

    @Query("UPDATE calendar_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM calendar_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // Personal Projects
    @Query("SELECT * FROM personal_projects ORDER BY progressPercent DESC")
    fun getAllProjects(): Flow<List<ProjectItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectItem>)

    @Update
    suspend fun updateProject(project: ProjectItem)

    @Query("UPDATE personal_projects SET progressPercent = :progress, lastTelemetryUpdate = :telemetry WHERE id = :id")
    suspend fun updateProjectProgress(id: String, progress: Int, telemetry: String)

    // Emails
    @Query("SELECT * FROM email_items ORDER BY isUrgent DESC, isUnread DESC")
    fun getAllEmails(): Flow<List<EmailItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailItem>)

    @Query("UPDATE email_items SET isUnread = :isUnread WHERE id = :id")
    suspend fun setEmailReadStatus(id: String, isUnread: Boolean)

    @Query("UPDATE email_items SET replied = 1 WHERE id = :id")
    suspend fun markEmailReplied(id: String)

    // Voice Biometrics Profile
    @Query("SELECT * FROM voice_biometrics WHERE id = :id LIMIT 1")
    fun getBiometricProfileFlow(id: String = "primary_user"): Flow<VoiceBiometricProfile?>

    @Query("SELECT * FROM voice_biometrics WHERE id = :id LIMIT 1")
    suspend fun getBiometricProfile(id: String = "primary_user"): VoiceBiometricProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBiometricProfile(profile: VoiceBiometricProfile)
}
