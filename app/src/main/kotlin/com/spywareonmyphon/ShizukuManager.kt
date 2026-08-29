package com.spywareonmyphon

import android.content.Context
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object ShizukuManager {

    private var isConnected = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        isConnected = true
    }
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        isConnected = false
    }

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
    }

    fun isAvailable() = isConnected && Shizuku.pingBinder()

    fun hasPermission() = try {
        Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) { false }

    fun requestPermission(code: Int) = Shizuku.requestPermission(code)

    fun uninstallForUser(packageName: String, userId: Int = 0): Result<Unit> {
        return try {
            val binder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
            val pmClass = Class.forName("android.content.pm.IPackageManager\$Stub")
            val pm = pmClass.getMethod("asInterface", android.os.IBinder::class.java).invoke(null, binder)

            val deleteMethod = pm.javaClass.getMethod(
                "deletePackageAsUser",
                String::class.java,
                Int::class.java,
                Class.forName("android.content.pm.IPackageDeleteObserver2"),
                Int::class.java,
                Int::class.java
            )
            deleteMethod.invoke(pm, packageName, -1, null, 0, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
