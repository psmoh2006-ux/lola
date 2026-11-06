package com.mahmoud.lolita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import android.content.Context

private val Wine = Color(0xFF54243F)
private val Rose = Color(0xFFE6A8BA)
private val Cream = Color(0xFFFFF7F0)
private val Gold = Color(0xFFD7B36A)
private val Ink = Color(0xFF382431)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LolitaApp(this) }
    }
}

@Composable
fun LolitaApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("lolita_world", Context.MODE_PRIVATE) }
    var unlocked by remember { mutableStateOf(prefs.getBoolean("unlocked", false)) }
    var password by remember { mutableStateOf("") }
    var customPassword by remember { mutableStateOf(prefs.getString("password", "بحبك ي بابا") ?: "بحبك ي بابا") }
    var selectedWorld by remember { mutableStateOf<Int?>(null) }
    var selectedSurprise by remember { mutableStateOf<Surprise?>(null) }
    var completed by remember { mutableStateOf(prefs.getStringSet("completed", emptySet())?.toMutableSet() ?: mutableSetOf()) }
    var showSettings by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    MaterialTheme(colorScheme = lightColorScheme(
        primary = Wine, secondary = Rose, background = Cream, surface = Color.White,
        onPrimary = Color.White, onBackground = Ink, onSurface = Ink
    )) {
        Surface(Modifier.fillMaxSize(), color = Cream) {
            when {
                !unlocked -> LoginScreen(password, { password = it }, {
                    if (password == customPassword) {
                        unlocked = true
                        prefs.edit().putBoolean("unlocked", true).apply()
                    }
                })
                selectedSurprise != null -> SurpriseScreen(
                    surprise = selectedSurprise!!,
                    done = completed.contains(selectedSurprise!!.id.toString()),
                    onBack = { selectedSurprise = null },
                    onCopy = { clipboard.setText(AnnotatedString(selectedSurprise!!.body)) },
                    onDone = {
                        completed = (completed + selectedSurprise!!.id.toString()).toMutableSet()
                        prefs.edit().putStringSet("completed", completed.toSet()).apply()
                    }
                )
                selectedWorld != null -> WorldScreen(
                    worldId = selectedWorld!!,
                    completed = completed,
                    onBack = { selectedWorld = null },
                    onSelect = { selectedSurprise = it }
                )
                showSettings -> SettingsScreen(
                    newPassword = newPassword,
                    onChange = { newPassword = it },
                    onSave = {
                        if (newPassword.length >= 6) {
                            customPassword = newPassword
                            prefs.edit().putString("password", newPassword).apply()
                            newPassword = ""
                            showSettings = false
                        }
                    },
                    onBack = { showSettings = false },
                    onLock = {
                        unlocked = false
                        prefs.edit().putBoolean("unlocked", false).apply()
                        showSettings = false
                    }
                )
                else -> HomeScreen(
                    completedCount = completed.size,
                    onWorld = { selectedWorld = it },
                    onSettings = { showSettings = true }
                )
            }
        }
    }
}

@Composable
fun Background(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Cream, Color(0xFFF7E5EA), Cream)))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun LoginScreen(password: String, onPassword: (String)->Unit, onEnter: ()->Unit) {
    Background {
        Spacer(Modifier.height(42.dp))
        Text("♡", color = Rose, fontSize = 64.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("عالم لوليتا", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Wine,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("عالم صغير معمول مخصوص علشانك", color = Ink.copy(alpha=.7f),
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("أهلًا بصاحبة العالم ❤️", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Wine)
                Text("الباب ده ليكي وحدك. اكتبي كلمة السر علشان تدخلي.", color = Ink)
                OutlinedTextField(
                    value = password, onValueChange = onPassword, label = { Text("كلمة السر") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Button(onClick = onEnter, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Wine)) {
                    Text("افتحي عالمك")
                }
                Text("تلميح: كلمة السر فيها كلمة بحبك ❤️", color = Color.Gray, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("صُنع بكل الحب — محمود ❤️", color = Wine.copy(alpha=.75f),
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
fun HomeScreen(completedCount: Int, onWorld: (Int)->Unit, onSettings: ()->Unit) {
    Background {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Alaa's World", color = Wine, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "الإعدادات", tint = Wine) }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Wine), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("أهلًا يا لوليتا ❤️", color = Color(0xFFFFE8EF), fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("كل باب هنا وراه حكاية صغيرة من محمود ليكي", color = Color.White)
                Text("$completedCount من 100 مفاجأة اتفتحت", color = Rose, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { completedCount / 100f }, modifier = Modifier.fillMaxWidth(),
                    color = Rose, trackColor = Color.White.copy(alpha=.2f))
            }
        }
        Text("اختاري عالمك", color = Wine, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(worlds) { world ->
                val count = surprises.count { it.worldId == world.id && completed.contains(it.id.toString()) }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onWorld(world.id) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(world.emoji, fontSize = 30.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(world.name, color = Wine, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(world.subtitle, color = Ink.copy(alpha=.65f), fontSize = 12.sp)
                        }
                        Text("$count/10", color = Wine, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Rose)
                    }
                }
            }
        }
    }
}

@Composable
fun WorldScreen(worldId: Int, completed: Set<String>, onBack: ()->Unit, onSelect: (Surprise)->Unit) {
    val world = worlds.first { it.id == worldId }
    Background {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Wine) }
            Text("${world.emoji} ${world.name}", color = Wine, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
        Text(world.subtitle, color = Ink.copy(alpha=.7f))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(surprises.filter { it.worldId == worldId }) { item ->
                Card(
                    Modifier.fillMaxWidth().clickable { onSelect(item) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (completed.contains(item.id.toString())) Icons.Default.Favorite else Icons.Default.MailOutline,
                            contentDescription = null, tint = Rose)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("%03d · %s".format(item.id, item.title), color = Wine, fontWeight = FontWeight.SemiBold)
                            Text(if (completed.contains(item.id.toString())) "اتفتحت ❤️" else "اضغطي لفتح المفاجأة",
                                color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Rose)
                    }
                }
            }
        }
    }
}

@Composable
fun SurpriseScreen(surprise: Surprise, done: Boolean, onBack: ()->Unit, onCopy: ()->Unit, onDone: ()->Unit) {
    Background {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Wine) }
            Text("المفاجأة رقم %03d".format(surprise.id), color = Wine, fontWeight = FontWeight.Bold)
        }
        Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("♡", color = Rose, fontSize = 44.sp)
                Text(surprise.title, color = Wine, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text(surprise.body, color = Ink, fontSize = 18.sp, lineHeight = 30.sp)
                Spacer(Modifier.weight(1f))
                Text("صُنع بكل الحب — محمود ❤️", color = Rose, fontSize = 13.sp)
            }
        }
        val clipboard = LocalClipboardManager.current
        OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("انسخي الرسالة")
        }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Wine), enabled = !done) {
            Text(if (done) "اتفتحت واتحفظت ❤️" else "علّميها كمفاجأة مفتوحة")
        }
    }
}

@Composable
fun SettingsScreen(newPassword: String, onChange: (String)->Unit, onSave: ()->Unit, onBack: ()->Unit, onLock: ()->Unit) {
    Background {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Wine) }
            Text("الإعدادات", color = Wine, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تغيير كلمة السر", color = Wine, fontWeight = FontWeight.Bold)
                OutlinedTextField(newPassword, onChange, label = { Text("كلمة السر الجديدة (6 أحرف على الأقل)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = onSave, enabled = newPassword.length >= 6,
                    colors = ButtonDefaults.buttonColors(containerColor = Wine), modifier = Modifier.fillMaxWidth()) {
                    Text("حفظ كلمة السر")
                }
            }
        }
        OutlinedButton(onClick = onLock, modifier = Modifier.fillMaxWidth()) { Text("قفل العالم") }
        Text("ملاحظة: كلمة المرور قفل محلي بسيط داخل التطبيق وليست تشفيرًا لمحتوى التطبيق.", color = Color.Gray, fontSize = 12.sp)
    }
}
