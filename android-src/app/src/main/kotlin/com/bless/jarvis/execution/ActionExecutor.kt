package com.bless.jarvis.execution

import android.content.Context
import android.content.Intent

data class ExecutionResult(
    val status: String,
    val details: String
)

object ActionExecutor {
    private val KNOWN_APPS = mapOf(
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome",
        "whatsapp" to "com.whatsapp",
        "instagram" to "com.instagram.android",
        "gmail" to "com.google.android.gm",
        "spotify" to "com.spotify.music",
        "settings" to "com.android.settings",
        "camera" to "com.android.camera",
        "maps" to "com.google.android.apps.maps",
        "play store" to "com.android.vending",
        "phone" to "com.android.dialer",
        "messages" to "com.google.android.apps.messaging"
    )

    fun execute(context: Context, actionName: String, parameters: Map<String, Any>?): ExecutionResult {
        return when (actionName) {
            "open_app" -> openApp(context, parameters)
            else -> ExecutionResult("failure", "\"$actionName\" isn't wired up on the Android side yet.")
        }
    }

    private fun openApp(context: Context, parameters: Map<String, Any>?): ExecutionResult {
        val requestedName = (parameters?.get("app") as? String)?.trim()?.lowercase()
            ?: return ExecutionResult("failure", "No app name was provided.")

        val packageName = KNOWN_APPS[requestedName]
            ?: KNOWN_APPS.entries.firstOrNull { requestedName.contains(it.key) || it.key.contains(requestedName) }?.value

        if (packageName == null) {
            return ExecutionResult("failure", "\"$requestedName\" isn't in the known app list yet.")
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return ExecutionResult("failure", "$packageName isn't installed on this device.")

        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ExecutionResult("success", "Opened $requestedName")
        } catch (e: Exception) {
            ExecutionResult("failure", "Failed to open $requestedName: ${e.message}")
        }
    }
}
