package com.aunt13psychot1c.chaoslocke.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aunt13psychot1c.chaoslocke.crypto.ChaosCipher

@Composable
fun LockScreen(incoming: String?, onIncomingUsed: () -> Unit) {
    val context = LocalContext.current
    var key by rememberSaveable { mutableStateOf("") }
    var showKey by rememberSaveable { mutableStateOf(true) }
    var message by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var resultIsLocked by rememberSaveable { mutableStateOf(false) }
    var problem by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(incoming) {
        if (incoming != null) { message = incoming; result = ""; problem = null; onIncomingUsed() }
    }

    fun run(locking: Boolean) {
        problem = null
        if (key.isEmpty()) { problem = "Type a secret key first."; return }
        try {
            result = if (locking) ChaosCipher.lock(message, key) else ChaosCipher.unlock(message, key)
            resultIsLocked = locking
        } catch (e: ChaosCipher.UnlockException) {
            result = ""; problem = e.message
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Heading("🔐 Secret messages")
        Text(
            "Lock a message with a key. It turns into tough-guy emoji. Only someone with the same key can unlock it.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card {
            OutlinedTextField(
                value = key, onValueChange = { key = it },
                label = { Text("Secret key") },
                singleLine = true,
                textStyle = CodeStyle,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "🙈 Hide key" else "👀 Show key") }
            Text(
                "Tell your friend the key in person or some other way. Never send it with the message.",
                fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card {
            OutlinedTextField(
                value = message, onValueChange = { message = it; problem = null },
                label = { Text("Message") },
                placeholder = { Text("Type a secret, or paste a locked one") },
                textStyle = CodeStyle,
                modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { paste(context)?.let { message = it; problem = null } }) { Text("📋 Paste") }
                TextButton(onClick = { message = ""; result = ""; problem = null }) { Text("🧹 Clear") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { run(true) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Text("🔒 Lock", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { run(false) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Text("🔓 Unlock", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
            }
            problem?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (result.isNotEmpty()) {
            Card {
                Text(
                    if (resultIsLocked) "Locked! Send this:" else "Unlocked ✓ The secret says:",
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary
                )
                SelectionContainer {
                    Text(result, style = if (resultIsLocked) CodeStyle.copy(fontSize = 24.sp, lineHeight = 34.sp) else CodeStyle,
                        modifier = Modifier.padding(vertical = 10.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { copy(context, result) }, modifier = Modifier.weight(1f)) { Text("📋 Copy") }
                    if (resultIsLocked) {
                        OutlinedButton(onClick = { share(context, result) }, modifier = Modifier.weight(1f)) { Text("📤 Send") }
                    }
                }
            }
        }
    }
}
