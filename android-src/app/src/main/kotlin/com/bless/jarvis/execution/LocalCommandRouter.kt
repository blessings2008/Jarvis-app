package com.bless.jarvis.execution

/** Routes unambiguous device-only commands locally so the cloud brain cannot misclassify them as general chat. */
object LocalCommandRouter {
    data class LocalCommand(val action: String, val parameters: Map<String, Any>? = null)

    fun match(input: String): LocalCommand? {
        val text = input.trim().lowercase()
        return when {
            text.contains("shizuku") && (text.contains("connected") || text.contains("running") || text.contains("status") || text.contains("permission")) ->
                LocalCommand("shizuku_check")
            text.matches(Regex("(take|get|capture|make|save).*(screenshot|screen shot)")) || text in setOf("screenshot", "take screenshot", "take a screenshot") ->
                LocalCommand("screenshot")
            text.contains("android version") || text == "what android am i using" || text == "what version of android" ->
                LocalCommand("get_android_version")
            text.contains("installed apps") || text.contains("list installed apps") || text.contains("what apps are installed") ->
                LocalCommand("list_installed_packages")
            text.matches(Regex("(volume|sound).*(up|increase|louder)")) || text == "volume up" ->
                LocalCommand("volume_up")
            text.matches(Regex("(volume|sound).*(down|decrease|lower|quieter)")) || text == "volume down" ->
                LocalCommand("volume_down")
            else -> null
        }
    }
}
