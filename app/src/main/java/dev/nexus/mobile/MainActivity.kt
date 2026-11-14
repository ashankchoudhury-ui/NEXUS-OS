package dev.nexus.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF21191A)
private val Paper = Color(0xFFF1E9DC)
private val Wine = Color(0xFF6D2638)
private val Muted = Color(0xFF9B8D84)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NexusApp() }
    }
}

@Composable
private fun NexusApp() {
    var dark by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("HOME") }
    val bg = if (dark) Ink else Paper
    val fg = if (dark) Paper else Ink
    val panel = if (dark) Color(0xFF302628) else Color(0xFFE5D9CA)
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(bg).padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("N / X", color = Wine, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 26.sp)
                    Text("NEXUS OPERATING LAYER // BUILD 001", color = fg, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                }
                Text(if (dark) "DARK ■" else "LIGHT □", color = fg, fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                    modifier = Modifier.clickable { dark = !dark }.padding(8.dp))
            }
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("HOME", "SPACES", "PROCESSES", "TERMINAL", "SYSTEM").forEach { tab ->
                    val active = selected == tab
                    Text(tab, modifier = Modifier.weight(1f).background(if (active) Wine else panel, RoundedCornerShape(3.dp))
                        .clickable { selected = tab }.padding(vertical = 9.dp),
                        color = if (active) Paper else fg, fontSize = 7.sp, fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(22.dp))
            when (selected) {
                "HOME" -> HomeScreen(fg, panel)
                "SPACES" -> InfoScreen("WORKSPACES", "USER_SPACE", "AGENT_SPACE", "TEST_SPACE", fg, panel)
                "PROCESSES" -> InfoScreen("RESOURCE CONTROL", "APP STATE MONITOR", "IDLE DETECTION", "POLICY ENGINE", fg, panel)
                "TERMINAL" -> InfoScreen("NEXUS SHELL", "TERMUX BRIDGE // PENDING", "SSH / VPS // PENDING", "PYTHON + GIT // PENDING", fg, panel)
                else -> InfoScreen("SYSTEM STATUS", "DEVICE: LAVA Z35", "ANDROID: 14 / API 34", "PRIVILEGED ACCESS: NOT CONNECTED", fg, panel)
            }
            Spacer(Modifier.weight(1f))
            Text("UI PROTOTYPE • SYSTEM CONTROL NOT CONNECTED", color = Muted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun HomeScreen(fg: Color, panel: Color) {
    Text("CONTROL DECK", color = fg, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Metric("SPACES", "03", fg, panel, Modifier.weight(1f))
        Metric("AGENTS", "00", fg, panel, Modifier.weight(1f))
        Metric("STATUS", "IDLE", fg, panel, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Panel("ACTIVE WORKSPACE", "USER_SPACE", fg, panel)
    Panel("SYSTEM NOTE", "Workspace isolation and process controls are not connected in Build 001.", fg, panel)
    Panel("NEXT MILESTONE", "Verify Android work-profile support, then implement real workspace switching.", fg, panel)
}

@Composable
private fun Metric(label: String, value: String, fg: Color, panel: Color, modifier: Modifier = Modifier) {
    Column(modifier.background(panel, RoundedCornerShape(3.dp)).padding(10.dp)) {
        Text(label, color = Muted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = fg, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun Panel(title: String, body: String, fg: Color, panel: Color) {
    Column(Modifier.fillMaxWidth().padding(bottom = 9.dp).background(panel, RoundedCornerShape(3.dp)).padding(13.dp)) {
        Text(title, color = Wine, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(6.dp))
        Text(body, color = fg, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun InfoScreen(title: String, a: String, b: String, c: String, fg: Color, panel: Color) {
    Text(title, color = fg, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Panel("01 / MODULE", a, fg, panel)
    Panel("02 / MODULE", b, fg, panel)
    Panel("03 / MODULE", c, fg, panel)
}
