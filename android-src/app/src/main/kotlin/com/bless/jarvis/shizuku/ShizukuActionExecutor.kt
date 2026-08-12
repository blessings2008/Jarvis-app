package com.bless.jarvis.shizuku

/**
 * Safe boundary for privileged JARVIS actions.
 * The cloud/backend can request an action name, but it cannot supply an
 * arbitrary shell command. Only explicitly implemented local actions reach
 * the Shizuku user service.
 */
object ShizukuActionExecutor {
    fun execute(actionName: String, parameters: Map<String, Any>?): Result<String> {
        if (!ShizukuManager.isAvailable()) {
            return Result.failure(IllegalStateException("Shizuku is not running."))
        }
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission()
            return Result.failure(SecurityException("JARVIS does not have Shizuku permission yet."))
        }

        val command = when (actionName) {
            "shizuku_check" -> arrayOf("id")
            "list_installed_packages" -> arrayOf("pm", "list", "packages", "-3")
            "get_android_version" -> arrayOf("getprop", "ro.build.version.release")
            else -> return Result.failure(
                UnsupportedOperationException("Shizuku action '$actionName' is not implemented.")
            )
        }

        return ShizukuManager.executeAllowed(command)
    }
}
