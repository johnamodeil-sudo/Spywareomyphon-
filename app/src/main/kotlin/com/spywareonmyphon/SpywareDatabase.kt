package com.spywareonmyphon
data class SpywareEntry(val packageName: String, val displayName: String, val reason: String, val threatLevel: ThreatLevel, val safeToKill: Boolean = true)
enum class ThreatLevel { CRITICAL, HIGH, MEDIUM }
object SpywareDatabase {
    val entries = listOf(
        SpywareEntry("com.glance.lockscreenM","Glance lockscreen","InMobi ad platform on lockscreen",ThreatLevel.CRITICAL),
        SpywareEntry("com.inmobi.weather","InMobi weather","Tracking company disguised as weather",ThreatLevel.CRITICAL),
        SpywareEntry("com.aura.oobe.motorola","Aura setup","Data monetization company",ThreatLevel.CRITICAL),
        SpywareEntry("com.facebook.appmanager","Facebook App Manager","Background tracking",ThreatLevel.CRITICAL),
        SpywareEntry("com.facebook.services","Facebook Services","Background data harvesting",ThreatLevel.CRITICAL),
        SpywareEntry("com.facebook.system","Facebook System","System-level Facebook hooks",ThreatLevel.CRITICAL),
        SpywareEntry("com.motorola.brapps","Hello Feed","Tracks reading habits",ThreatLevel.HIGH),
        SpywareEntry("com.tinno.productInfo","Tinno device info","Phones home device identifiers",ThreatLevel.HIGH),
        SpywareEntry("com.motorola.motocare","Moto Care","Telemetry disguised as support",ThreatLevel.HIGH),
        SpywareEntry("com.motorola.help","Moto Help","Tracks device info on launch",ThreatLevel.HIGH),
        SpywareEntry("com.motorola.genie","Moto Genie","Unnecessary background service",ThreatLevel.MEDIUM),
        SpywareEntry("com.motorola.paks","Moto Paks","Bloatware delivery system",ThreatLevel.MEDIUM),
        SpywareEntry("com.motorola.paks.notification","Moto Paks notifs","Push notifs for bloatware",ThreatLevel.MEDIUM),
        SpywareEntry("com.motorola.demo","Moto Demo","Store demo mode",ThreatLevel.MEDIUM),
        SpywareEntry("com.motorola.motocit","Moto factory test","Factory test app",ThreatLevel.MEDIUM),
        SpywareEntry("com.king.candycrushsaga","Candy Crush (protected?!)","Microsoft paid for system protection lol",ThreatLevel.MEDIUM,false),
    )
    fun getByThreatLevel(level: ThreatLevel) = entries.filter { it.threatLevel == level }
    fun getAll() = entries
}
