package com.spywareonmyphon
import android.content.pm.IPackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
object ShizukuManager {
    private var iPackageManager: IPackageManager? = null
    private var isConnected = false
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        isConnected = true
        bindPackageManager()
    }
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        isConnected = false
        iPackageManager = null
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
    private fun bindPackageManager() {
        try {
            val binder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
            iPackageManager = IPackageManager.Stub.asInterface(binder)
        } catch (e: Exception) { e.printStackTrace() }
    }
    fun uninstallForUser(packageName: String, userId: Int = 0): Result<Unit> {
        return try {
            val pm = iPackageManager ?: return Result.failure(Exception("IPackageManager not bound"))
            val obs = object : android.content.pm.IPackageDeleteObserver2.Stub() {
                override fun onUserActionRequired(intent: android.content.Intent?) {}
                override fun onPackageDeleted(pkg: String?, code: Int, msg: String?) {}
            }
            pm.deletePackageAsUser(packageName, null, obs, 0, userId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
