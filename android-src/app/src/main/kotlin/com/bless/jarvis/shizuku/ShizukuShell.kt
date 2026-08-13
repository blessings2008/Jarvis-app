package com.bless.jarvis.shizuku

/** Small wrapper around JARVIS's authorized Shizuku UserService. */
object ShizukuShell {
    data class Result(val exitCode: Int, val output: String)

    fun run(command: Array<String>): Result {
        val raw = ShizukuManager.executeAllowed(command).getOrElse { throw it }
        val marker = "__JARVIS_EXIT__:"
        val firstLine = raw.lineSequence().firstOrNull().orEmpty()
        if (firstLine.startsWith(marker)) {
            val code = firstLine.removePrefix(marker).trim().toIntOrNull() ?: 1
            val output = raw.substringAfter('\n', "").trim()
            return Result(code, output)
        }
        return Result(0, raw.trim())
    }
}
