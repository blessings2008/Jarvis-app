package com.bless.jarvis.shizuku

import java.util.concurrent.TimeUnit

/**
 * Shizuku UserService.
 *
 * IMPORTANT: A Shizuku UserService is not an Android Service. Shizuku creates
 * this class directly in its privileged user-service process, so the class
 * itself must be an IBinder (our generated AIDL Stub).
 */
class JarvisShellService : IJarvisShellService.Stub() {

    override fun execute(command: Array<String>): String {
        if (command.isEmpty()) {
            return "__JARVIS_EXIT__:2\nNo command supplied."
        }

        return try {
            val process = ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }.trim()

            val finished = process.waitFor(10, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                process.waitFor(1, TimeUnit.SECONDS)
                return "__JARVIS_EXIT__:124\nCommand timed out after 10 seconds."
            }

            "__JARVIS_EXIT__:${process.exitValue()}\n$output"
        } catch (e: Exception) {
            "__JARVIS_EXIT__:1\n${e.javaClass.simpleName}: ${e.message.orEmpty()}"
        }
    }
}
