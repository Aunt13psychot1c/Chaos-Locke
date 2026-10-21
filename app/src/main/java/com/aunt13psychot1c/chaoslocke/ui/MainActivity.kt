package com.aunt13psychot1c.chaoslocke.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {

    /** Text handed over by another app (Share, or the highlight menu), waiting for the Lock tab. */
    private val incoming = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) take(intent)
        val prefs = getSharedPreferences("lock", MODE_PRIVATE)
        val firstRun = !prefs.getBoolean("help_seen", false)
        setContent {
            ChaosLockeTheme {
                LockApp(
                    incoming = incoming.value,
                    onIncomingUsed = { incoming.value = null },
                    startOnHelp = firstRun && incoming.value == null,
                    onHelpSeen = { prefs.edit().putBoolean("help_seen", true).apply() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        take(intent)
    }

    private fun take(intent: Intent?) {
        val text = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            else -> null
        }
        if (!text.isNullOrBlank()) incoming.value = text
    }
}
