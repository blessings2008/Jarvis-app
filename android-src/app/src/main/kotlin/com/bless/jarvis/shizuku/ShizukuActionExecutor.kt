package com.bless.jarvis.shizuku

/**
 * Entry point for actions that require Shizuku.
 * Privileged operations stay isolated here; the AI/backend cannot execute
 * arbitrary shell commands through this boundary.
 */
object ShizukuActionExecutor {
    fun execute(actionName: String, parameters: Map<String, Any>?): kotlin.Result<Unit> {
        if (!ShizukuManager.isAvailable()) {
            return kotlin.Result.failure(IllegalStateException("Shizuku is not running."))
        }
        if (!ShizukuManager.hasPermission()) {
            ShizukuManager.requestPermission()
            return kotlin.Result.failure(SecurityException("JARVIS does not have Shizuku permission yet."))
        }

        return when (actionName) {
            else -> kotlin.Result.failure(
                UnsupportedOperationException("Shizuku action '$actionName' is not implemented yet.")
            )
        }
    }
}
