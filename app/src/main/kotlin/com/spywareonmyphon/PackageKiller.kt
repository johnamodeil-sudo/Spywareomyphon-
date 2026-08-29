package com.spywareonmyphon
import kotlinx.coroutines.*
data class KillResult(val packageName: String, val success: Boolean, val error: String? = null, val timeMs: Long = 0)
object PackageKiller {
    suspend fun kill(packageName: String, userId: Int = 0): KillResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val result = ShizukuManager.uninstallForUser(packageName, userId)
        KillResult(packageName, result.isSuccess, result.exceptionOrNull()?.message, System.currentTimeMillis() - start)
    }
    suspend fun killAll(packages: List<String>, userId: Int = 0, onProgress: (KillResult) -> Unit = {}): List<KillResult> = coroutineScope {
        packages.map { pkg -> async(Dispatchers.IO) { kill(pkg, userId).also { onProgress(it) } } }.awaitAll()
    }
    suspend fun nukeAllSpyware(onProgress: (KillResult) -> Unit = {}) =
        killAll(SpywareDatabase.getAll().filter { it.safeToKill }.map { it.packageName }, onProgress = onProgress)
}
