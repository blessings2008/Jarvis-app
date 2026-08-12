package com.bless.jarvis.shizuku

import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Runs a shell command at Shizuku's privilege level (same tier as
 * `adb shell` — not root, but enough for input events, screencap,
 * and on most devices, reboot).
 *
 * NOTE: Shizuku.newProcess() is marked @Deprecated in newer shizuku-api
 * versions in favor of a full AIDL-based "user service." It's still
 * present and functional, and far simpler to wire up for a first
 * working version. If you outgrow this (e.g. need something that
 * survives Shizuku restarts, or two-way binder calls), migrating to
 * a user service is the documented next step — not needed yet.
 */
object ShizukuShell {
    data class ShellResult(val exitCode: Int, val output: String)

    @Suppress("DEPRECATION")
    fun run(command: Array<String>): ShellResult {
        val process = Shizuku.newProcess(command, null, null)
        val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText().trim()
        val exitCode = process.waitFor()
        return ShizukuShell.ShellResult(exitCode, if (stdout.isNotBlank()) stdout else stderr)
    }
}
