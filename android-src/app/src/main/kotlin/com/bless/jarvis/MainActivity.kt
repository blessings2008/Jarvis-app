package com.bless.jarvis

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bless.jarvis.shizuku.ShizukuManager
import com.bless.jarvis.ui.JarvisApp
import com.bless.jarvis.ui.theme.JarvisTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.REQUEST_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
            ShizukuManager.connect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        setContent {
            JarvisTheme {
                JarvisApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ShizukuManager.isAvailable()) {
            if (ShizukuManager.hasPermission()) {
                ShizukuManager.connect()
            } else {
                ShizukuManager.requestPermission()
            }
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        ShizukuManager.disconnect()
        super.onDestroy()
    }
}
