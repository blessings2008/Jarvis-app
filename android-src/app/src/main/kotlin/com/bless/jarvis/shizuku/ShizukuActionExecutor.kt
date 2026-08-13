package com.bless.jarvis.shizuku

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShizukuActionExecutor {
    private val allowedActions = setOf(
        "shizuku_check", "get_android_version", "list_installed_packages",
        "reboot", "volume_up", "volume_down", "screenshot"
    )

    fun handles(actionName: String): Boolean = actionName in allowedActions

    fun execute(actionName: String, parameters: Map<String, Any>?): Result<String> {
        if (actionName == "shizuku_check") {
            return if (ShizukuManager.isAvailable()) {
                if (ShizukuManager.hasPermission()) Result.success("Shizuku is running and JARVIS has permission.")
                else {
                    ShizukuManager.requestPermission()
                    Result.failure(SecurityException("Shizuku is running, but JARVIS needs permission. Approve the Shizuku permission prompt."))
                }
            } else Result.failure(IllegalStateException("Shizuku is not running. Start the Shizuku service first."))
        }

        if (!ShizukuManager.isAvailable()) return Result.failure(IllegalStateException("Shizuku is not running. Start the Shizuku service first."))
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission()
            return Result.failure(SecurityException("JARVIS needs Shizuku permission. Approve the permission prompt, then try again."))
        }

        return try {
            when (actionName) {
                "get_android_version" -> command(arrayOf("/system/bin/getprop", "ro.build.version.release"), "Android version")
                "list_installed_packages" -> command(arrayOf("/system/bin/pm", "list", "packages"), "Installed packages")
                "reboot" -> reboot()
                "volume_up" -> volume(true, parameters)
                "volume_down" -> volume(false, parameters)
                "screenshot" -> screenshot()
                else -> Result.failure(UnsupportedOperationException("Shizuku action '$actionName' is not implemented."))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("$actionName failed: ${e.javaClass.simpleName}: ${e.message ?: "no error message"}", e))
        }
    }

    private fun command(command: Array<String>, label: String): Result<String> {
        val result = ShizukuShell.run(command)
        return if (result.exitCode == 0) Result.success("$label:\n${result.output.ifBlank { "No output." }}")
        else Result.failure(IllegalStateException("$label failed (exit ${result.exitCode}): ${result.output.ifBlank { "no command output" }}"))
    }

    private fun reboot(): Result<String> {
        val result = ShizukuShell.run(arrayOf("/system/bin/reboot"))
        return if (result.exitCode == 0) Result.success("Rebooting now.")
        else Result.failure(IllegalStateException("Reboot failed (exit ${result.exitCode}): ${result.output.ifBlank { "no command output" }}"))
    }

    private fun volume(up: Boolean, parameters: Map<String, Any>?): Result<String> {
        val steps = (parameters?.get("steps") as? Number)?.toInt()?.coerceIn(1, 15) ?: 1
        val keyEvent = if (up) "24" else "25"
        repeat(steps) {
            val result = ShizukuShell.run(arrayOf("/system/bin/input", "keyevent", keyEvent))
            if (result.exitCode != 0) {
                return Result.failure(IllegalStateException("Volume command failed (exit ${result.exitCode}): ${result.output.ifBlank { "no command output" }}"))
            }
        }
        return Result.success("Volume ${if (up) "up" else "down"} by $steps step(s).")
    }

    private fun screenshot(): Result<String> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val path = "/sdcard/Pictures/jarvis_$timestamp.png"
        ShizukuShell.run(arrayOf("/system/bin/mkdir", "-p", "/sdcard/Pictures"))
        val result = ShizukuShell.run(arrayOf("/system/bin/screencap", "-p", path))
        return if (result.exitCode == 0) Result.success("Screenshot saved to $path")
        else Result.failure(IllegalStateException("Screenshot failed (exit ${result.exitCode}): ${result.output.ifBlank { "no command output" }}"))
    }
}
