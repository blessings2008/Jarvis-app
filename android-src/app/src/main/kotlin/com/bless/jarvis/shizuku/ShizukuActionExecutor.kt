package com.bless.jarvis.shizuku

/**
 * Entry point for actions that require Shizuku.
 * Keep privileged operations isolated here rather than allowing arbitrary
 * commands from the AI/backend.
 */
object ShizukuActionExecutor {
    fun execute(actionName: String, parameters: Map<String, Any>?): Result {
        if (!ShizukuManager.isAvailable()) {
            return Result.failure(IllegalStateException("Shizuku is not running."))
        }
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission()
            return Result.failure(SecurityException("JARVIS does not have Shizuku permission yet."))
        }

        return when (actionName) {
            else -> Result.failure(UnsupportedOperationException("Shizuku action '$actionName' is not implemented yet."))
        }
    }
}
