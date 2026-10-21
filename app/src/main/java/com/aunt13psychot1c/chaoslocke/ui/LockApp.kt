package com.aunt13psychot1c.chaoslocke.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

private enum class Tab(val emoji: String, val label: String) {
    LOCK("🔐", "Lock"), TRIP("🎭", "Tripcode"), HELP("📖", "Help")
}

@Composable
fun LockApp(incoming: String?, onIncomingUsed: () -> Unit, startOnHelp: Boolean, onHelpSeen: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(if (startOnHelp) Tab.HELP.ordinal else Tab.LOCK.ordinal) }
    LaunchedEffect(incoming) { if (incoming != null) tab = Tab.LOCK.ordinal }
    LaunchedEffect(tab) { if (tab == Tab.HELP.ordinal) onHelpSeen() }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        // Includes the keyboard, so the box you're typing in never hides behind it.
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Box(Modifier.weight(1f)) {
                when (Tab.entries[tab]) {
                    Tab.LOCK -> LockScreen(incoming, onIncomingUsed)
                    Tab.TRIP -> TripScreen()
                    Tab.HELP -> HelpScreen(onStart = { tab = Tab.LOCK.ordinal })
                }
            }
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, windowInsets = WindowInsets(0)) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t.ordinal,
                        onClick = { tab = t.ordinal },
                        icon = { Text(t.emoji, fontSize = 22.sp) },
                        label = { Text(t.label, fontSize = 15.sp) }
                    )
                }
            }
        }
    }
}
