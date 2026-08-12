package com.bless.jarvis.shizuku

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.concurrent.TimeUnit

/**
 * Runs inside the Shizuku user-service process.
 * The main app never accepts arbitrary shell commands from the cloud; callers
 * must pass through ShizukuActionExecutor's allow-list first.
 */
class JarvisShellService : Service() {
    private val binder = object : IJarvisShellService.Stub() {
        override fun execute(command: Array<String>): String {
            val process = ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            process.waitFor(10, TimeUnit.SECONDS)
            if (process.isAlive) process.destroyForcibly()
            return output
        }
    }

    override fun onBind(intent: Intent): IBinder = binder
}
