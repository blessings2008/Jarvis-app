package com.bless.jarvis.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import rikka.shizuku.Shizuku

/** Central lifecycle/permission gate for JARVIS's Shizuku integration. */
object ShizukuManager {
    const val REQUEST_CODE = 7001

    @Volatile private var shellService: IJarvisShellService? = null
    @Volatile private var connection: ServiceConnection? = null
    private val serviceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName("com.bless.jarvis", JarvisShellService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("shizuku")
            .debuggable(false)
            .version(1)
    }

    fun isAvailable(): Boolean = try { Shizuku.pingBinder() } catch (_: Exception) { false }

    fun hasPermission(): Boolean = isAvailable() && try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Exception) { false }

    fun requestPermission() {
        if (isAvailable() && !hasPermission()) Shizuku.requestPermission(REQUEST_CODE)
    }

    fun isConnected(): Boolean = shellService != null && try {
        shellService?.asBinder()?.pingBinder() == true
    } catch (_: Exception) { false }

    /**
     * Bind the UserService. Binding is asynchronous, so callers that need to
     * execute immediately should use executeAllowed(), which waits for the
     * binder instead of racing the connection callback.
     */
    fun connect(onChanged: (() -> Unit)? = null): Boolean {
        if (!isAvailable() || !hasPermission()) return false
        if (isConnected()) return true

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                shellService = binder?.let { IJarvisShellService.Stub.asInterface(it) }
                onChanged?.invoke()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                shellService = null
                onChanged?.invoke()
            }
        }
        connection = serviceConnection
        return try {
            Shizuku.bindUserService(serviceArgs, serviceConnection)
            true
        } catch (_: Exception) {
            connection = null
            false
        }
    }

    fun disconnect() {
        val current = connection ?: return
        try { Shizuku.unbindUserService(serviceArgs, current, true) } catch (_: Exception) { }
        shellService = null
        connection = null
    }

    /** Executes only commands selected by the local allow-list in ShizukuActionExecutor. */
    fun executeAllowed(command: Array<String>): Result<String> {
        if (!isAvailable()) return Result.failure(IllegalStateException("Shizuku is not running."))
        if (!hasPermission()) return Result.failure(SecurityException("JARVIS does not have Shizuku permission."))

        if (!isConnected()) {
            val connected = CountDownLatch(1)
            if (!connect { connected.countDown() }) {
                return Result.failure(IllegalStateException("JARVIS could not bind to the Shizuku service."))
            }

            // bindUserService() returns before onServiceConnected(). Wait briefly
            // so the first device action doesn't race the asynchronous binder setup.
            if (!connected.await(5, TimeUnit.SECONDS) || !isConnected()) {
                return Result.failure(IllegalStateException("JARVIS connected to Shizuku, but the JARVIS UserService did not become ready in time."))
            }
        }

        return try {
            val service = shellService
                ?: return Result.failure(IllegalStateException("JARVIS Shizuku service is unavailable."))
            Result.success(service.execute(command))
        } catch (e: Exception) {
            shellService = null
            Result.failure(e)
        }
    }
}
