package com.bless.jarvis.shizuku

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.concurrent.TimeUnit

/** Runs inside the Shizuku user-service process. */
class JarvisShellService : Service() {
    private val binder = object : IJarvisShellService.Stub() {
        override fun execute(command: Array<String>): String {
            return try {
                val process = ProcessBuilder(*command)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().use { it.readText().trim() }
                process.waitFor(10, TimeUnit.SECONDS)
                if (process.isAlive) process.destroyForcibly()
                "__JARVIS_EXIT__:${process.exitValue()}\n$output"
            } catch (e: Exception) {
                "__JARVIS_EXIT__:1\n${e.message.orEmpty()}"
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder
}
