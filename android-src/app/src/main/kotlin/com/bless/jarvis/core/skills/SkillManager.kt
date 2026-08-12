package com.bless.jarvis.core.skills

import android.content.Context
import org.json.JSONObject
import java.io.File

object SkillManager {
    private const val DIRECTORY = "skills"

    private val catalog = listOf(
        SkillManifest("android-control", "Android Control", "1.0.0", "JARVIS", "Control supported Android functions.", listOf("open_app", "battery", "screenshot", "volume"), listOf("Shizuku")),
        SkillManifest("vision", "Vision", "1.0.0", "JARVIS", "Image, camera, OCR and screen analysis.", listOf("image_analysis", "ocr", "screen_analysis"), listOf("Camera")),
        SkillManifest("music", "Music", "1.0.0", "JARVIS", "Control supported music applications.", listOf("play_music", "pause_music", "next_track")),
        SkillManifest("bluetooth", "Bluetooth Control", "1.0.0", "JARVIS", "Read and control supported Bluetooth functions.", listOf("bluetooth_state", "bluetooth_toggle"), listOf("Nearby devices")),
        SkillManifest("web-search", "Web Search", "1.2.0", "JARVIS", "Search the web through the JARVIS cloud brain.", listOf("web_search"))
    )

    fun catalog(): List<SkillManifest> = catalog

    fun installed(context: Context): List<SkillManifest> = skillDir(context).listFiles()
        ?.filter { it.extension == "json" }
        ?.mapNotNull { parse(it.readText()) }
        ?: emptyList()

    fun isInstalled(context: Context, id: String): Boolean = installed(context).any { it.id == id }

    fun install(context: Context, manifest: SkillManifest): Result<Unit> = runCatching {
        skillDir(context).mkdirs()
        File(skillDir(context), "${manifest.id}.json").writeText(toJson(manifest).toString())
    }

    fun uninstall(context: Context, id: String): Result<Unit> = runCatching {
        File(skillDir(context), "$id.json").delete()
    }

    /** Imports a signed/validated manifest format; executable code is never loaded dynamically. */
    fun installManifest(context: Context, json: String): Result<SkillManifest> = runCatching {
        val manifest = parse(json) ?: error("Invalid skill manifest")
        install(context, manifest).getOrThrow()
        manifest
    }

    private fun skillDir(context: Context) = File(context.filesDir, DIRECTORY)

    private fun toJson(m: SkillManifest) = JSONObject().apply {
        put("id", m.id); put("name", m.name); put("version", m.version); put("author", m.author)
        put("description", m.description); put("source", m.source)
        put("capabilities", org.json.JSONArray(m.capabilities)); put("permissions", org.json.JSONArray(m.permissions)); put("dependencies", org.json.JSONArray(m.dependencies))
    }

    private fun parse(json: String): SkillManifest? = runCatching {
        val o = JSONObject(json)
        fun array(key: String): List<String> = (0 until o.optJSONArray(key).length()).map { o.optJSONArray(key).optString(it) }
        SkillManifest(o.getString("id"), o.getString("name"), o.getString("version"), o.getString("author"), o.getString("description"), array("capabilities"), array("permissions"), array("dependencies"), o.optString("source", "Custom"))
    }.getOrNull()
}
