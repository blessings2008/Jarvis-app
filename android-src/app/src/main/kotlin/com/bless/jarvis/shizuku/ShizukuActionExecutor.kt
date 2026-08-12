package com.bless.jarvis.shizuku

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Entry point for actions that require Shizuku's elevated shell access.
 * Keep privileged operations isolated here rather than allowing arbitrary
 * commands from the AI/backend — only these four named actions are ever run.
 */
object ShizukuActionExecutor {

    fun handles(actionName: String): Boolean =
        actionName in setOf("reboot", "volume_up", "volume_down", "screenshot")

    fun execute(actionName: String, parameters: Map<String, Any>?): Result<String> {
        if (!ShizukuManager.isAvailable()) {
            return Result.failure(IllegalStateException("Shizuku is not running. Open the Shizuku app and start the service."))
        }
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission()
            return Result.failure(SecurityException("JARVIS just requested Shizuku permission — grant it in the dialog, then try again."))
        }

        return try {
            when (actionName) {
                "reboot" -> reboot()
                "volume_up" -> volume(up = true, parameters)
                "volume_down" -> volume(up = false, parameters)
                "screenshot" -> screenshot()
                else -> Result.failure(UnsupportedOperationException("Shizuku action '$actionName' is not implemented."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun reboot(): Result<String> {
        val result = ShizukuShell.run(arrayOf("reboot"))
        return if (result.exitCode == 0) Result.success("Rebooting now.")
        else Result.failure(IllegalStateException("Reboot command failed: ${result.output.ifBlank { "exit code ${result.exitCode}" }}"))
    }

    private fun volume(up: Boolean, parameters: Map<String, Any>?): Result<String> {
        val steps = (parameters?.get("steps") as? Number)?.toInt()?.coerceIn(1, 15) ?: 1
        val keyEvent = if (up) "24" else "25" // KEYCODE_VOLUME_UP / KEYCODE_VOLUME_DOWN
        repeat(steps) {
            val result = ShizukuShell.run(arrayOf("input", "keyevent", keyEvent))
            if (result.exitCode != 0) {
                return Result.failure(IllegalStateException("Volume command failed: ${result.output.ifBlank { "exit code ${result.exitCode}" }}"))
            }
        }
        return Result.success("Volume ${if (up) "up" else "down"} by $steps step(s).")
    }

    private fun screenshot(): Result<String> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val path = "/sdcard/Pictures/jarvis_$timestamp.png"
        ShizukuShell.run(arrayOf("mkdir", "-p", "/sdcard/Pictures"))
        val result = ShizukuShell.run(arrayOf("screencap", "-p", path))
        return if (result.exitCode == 0) Result.success("Screenshot saved to $path")
        else Result.failure(IllegalStateException("Screenshot failed: ${result.output.ifBlank { "exit code ${result.exitCode}" }}"))
    }
}
