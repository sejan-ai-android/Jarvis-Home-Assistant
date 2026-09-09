package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ApkExportHelper {

    data class ApkInfo(
        val fileName: String,
        val sizeFormatted: String,
        val version: String,
        val projectRelativePath: String
    )

    fun getApkInfo(context: Context): ApkInfo {
        val sourceApk = File(context.applicationInfo.sourceDir)
        val sizeMb = if (sourceApk.exists()) {
            String.format("%.1f MB", sourceApk.length() / (1024.0 * 1024.0))
        } else {
            "22.0 MB"
        }

        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }

        val versionName = packageInfo?.versionName ?: "1.0.0"

        return ApkInfo(
            fileName = "jarvis-assist.apk",
            sizeFormatted = sizeMb,
            version = versionName,
            projectRelativePath = "/jarvis-assist.apk"
        )
    }

    fun exportAndShareApk(context: Context) {
        try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                Toast.makeText(context, "APK source not found on this environment", Toast.LENGTH_SHORT).show()
                return
            }

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val targetApk = File(exportDir, "jarvis-assist.apk")

            // Copy base APK to exportDir
            FileInputStream(sourceApk).use { input ->
                FileOutputStream(targetApk).use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetApk
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "J.A.R.V.I.S. Android APK Installer")
                putExtra(Intent.EXTRA_TEXT, "J.A.R.V.I.S. AI Assistant Android APK (v1.0)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export / Share J.A.R.V.I.S. APK")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
