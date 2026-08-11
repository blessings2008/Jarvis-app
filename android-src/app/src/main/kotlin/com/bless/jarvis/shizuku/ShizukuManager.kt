package com.bless.jarvis.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Small, centralized gate for JARVIS's Shizuku capabilities.
 * Shizuku must be running and the user must explicitly grant JARVIS access.
 */
object ShizukuManager {
    const val REQUEST_CODE = 7001

    fun isAvailable(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: Exception) {
        false
    }

    fun hasPermission(): Boolean = isAvailable() && try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Exception) {
        false
    }

    fun requestPermission() {
        if (isAvailable() && !hasPermission()) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }
}
