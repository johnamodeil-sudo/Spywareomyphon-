package com.spywareonmyphon
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
val BgDark = Color(0xFF3A0808)
val BgDeep = Color(0xFF4A0A0A)
val Red = Color(0xFFE24B4A)
val RedLight = Color(0xFFF09595)
val Amber = Color(0xFFEF9F27)
val White = Color(0xFFFFFFFF)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuManager.init()
        setContent { SpywareApp() }
    }
    override fun onDestroy() { super.onDestroy(); ShizukuManager.destroy() }
    @Composable
    fun SpywareApp() {
        var shizukuReady by remember { mutableStateOf(ShizukuManager.isAvailable() && ShizukuManager.hasPermission()) }
        var killedPackages by remember { mutableStateOf(setOf<String>()) }
        var isNuking by remember { mutableStateOf(false) }
        val entries = SpywareDatabase.getAll()
        LaunchedEffect(Unit) {
            if (ShizukuManager.isAvailable() && !ShizukuManager.hasPermission()) ShizukuManager.requestPermission(42)
            shizukuReady = ShizukuManager.isAvailable() && ShizukuManager.hasPermission()
        }
        Column(modifier = Modifier.fillMaxSize().background(BgDeep)) {
            Box(modifier = Modifier.fillMaxWidth().background(BgDeep).padding(16.dp)) {
                Column {
                    Text("SpywareOnMyPhon?!", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(if (shizukuReady) "● shizuku connected" else "○ shizuku not ready", color = if (shizukuReady) Red else Amber, fontSize = 11.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("${entries.size - killedPackages.size}", "threats", Modifier.weight(1f))
                StatCard("${killedPackages.size}", "eliminated", Modifier.weight(1f))
            }
            Button(
                onClick = {
                    if (!shizukuReady) { Toast.makeText(this@MainActivity, "Shizuku not ready!", Toast.LENGTH_SHORT).show(); return@Button }
                    isNuking = true
                    lifecycleScope.launch {
                        PackageKiller.nukeAllSpyware { if (it.success) killedPackages = killedPackages + it.packageName }
                        isNuking = false
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                shape = RoundedCornerShape(8.dp), enabled = !isNuking
            ) {
                if (isNuking) { CircularProgressIndicator(color = White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (isNuking) "NUKING..." else "☠ NUKE ALL SPYWARE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { Text("🔴 Critical", color = RedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                items(SpywareDatabase.getByThreatLevel(ThreatLevel.CRITICAL)) { entry ->
                    if (entry.packageName !in killedPackages) SpyCard(entry) {
                        lifecycleScope.launch { val r = PackageKiller.kill(entry.packageName); if (r.success) killedPackages = killedPackages + entry.packageName else Toast.makeText(this@MainActivity, "Failed: ${r.error}", Toast.LENGTH_SHORT).show() }
                    }
                }
                item { Text("🟡 High", color = RedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                items(SpywareDatabase.getByThreatLevel(ThreatLevel.HIGH)) { entry ->
                    if (entry.packageName !in killedPackages) SpyCard(entry) {
                        lifecycleScope.launch { val r = PackageKiller.kill(entry.packageName); if (r.success) killedPackages = killedPackages + entry.packageName else Toast.makeText(this@MainActivity, "Failed: ${r.error}", Toast.LENGTH_SHORT).show() }
                    }
                }
                item { Text("⚪ Medium", color = RedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                items(SpywareDatabase.getByThreatLevel(ThreatLevel.MEDIUM)) { entry ->
                    if (entry.packageName !in killedPackages) SpyCard(entry) {
                        if (!entry.safeToKill) return@SpyCard
                        lifecycleScope.launch { val r = PackageKiller.kill(entry.packageName); if (r.success) killedPackages = killedPackages + entry.packageName else Toast.makeText(this@MainActivity, "Failed: ${r.error}", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }
    @Composable
    fun StatCard(number: String, label: String, modifier: Modifier) {
        Box(modifier = modifier.background(BgDark, RoundedCornerShape(8.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(number, color = Red, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(label, color = RedLight, fontSize = 10.sp)
            }
        }
    }
    @Composable
    fun SpyCard(entry: SpywareEntry, onKill: () -> Unit) {
        val tc = when(entry.threatLevel) { ThreatLevel.CRITICAL -> Red; ThreatLevel.HIGH -> Amber; ThreatLevel.MEDIUM -> Color(0xFF888780) }
        Box(modifier = Modifier.fillMaxWidth().background(BgDark, RoundedCornerShape(8.dp)).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(tc, RoundedCornerShape(4.dp)))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.displayName, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(entry.packageName, color = RedLight, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(entry.reason, color = Color(0xFFB4B2A9), fontSize = 10.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onKill, colors = ButtonDefaults.buttonColors(containerColor = if (entry.safeToKill) Red else Color(0xFF5F5E5A)), shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), modifier = Modifier.height(30.dp), enabled = entry.safeToKill) {
                    Text(if (entry.safeToKill) "KILL" else "BLOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
