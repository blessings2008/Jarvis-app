package com.bless.jarvis.execution
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.BatteryManager
import android.media.AudioManager
import com.bless.jarvis.core.network.ToolCall
class ActionExecutor(private val context: Context) {
 suspend fun execute(call: ToolCall): Map<String, Any?> = try { when(call.name) {
 "open_app" -> { val pkg=call.arguments["package"]?.toString() ?: call.arguments["app"]?.toString(); val intent=pkg?.let{context.packageManager.getLaunchIntentForPackage(it)}; if(intent==null) mapOf("completed" to false,"error" to "App not found: $pkg") else {intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(intent);mapOf("completed" to true)} }
 "launch_url" -> { val url=call.arguments["url"]?.toString() ?: ""; context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));mapOf("completed" to true) }
 "device_info" -> mapOf("completed" to true,"manufacturer" to android.os.Build.MANUFACTURER,"model" to android.os.Build.MODEL,"android" to android.os.Build.VERSION.RELEASE)
 "get_battery" -> { val bm=context.getSystemService(BatteryManager::class.java);mapOf("completed" to true,"percent" to bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) }
 "get_volume" -> { val am=context.getSystemService(AudioManager::class.java);mapOf("completed" to true,"music" to am.getStreamVolume(AudioManager.STREAM_MUSIC),"max" to am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) }
 "set_volume" -> { val am=context.getSystemService(AudioManager::class.java);val v=(call.arguments["volume"] as? Number)?.toInt() ?: return mapOf("completed" to false,"error" to "volume required");am.setStreamVolume(AudioManager.STREAM_MUSIC,v.coerceIn(0,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),0);mapOf("completed" to true,"volume" to v) }
 "open_settings" -> {context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));mapOf("completed" to true)}
 else -> mapOf("completed" to false,"unsupported" to true,"error" to "Android body does not implement ${call.name} yet")
} } catch(e:Exception){mapOf("completed" to false,"error" to (e.message ?: "execution failed"))}
}