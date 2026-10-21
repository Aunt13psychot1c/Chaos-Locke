package com.aunt13psychot1c.chaoslocke.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aunt13psychot1c.chaoslocke.crypto.ChaosCipher

@Composable
fun TripScreen() {
    val context = LocalContext.current
    var field by rememberSaveable { mutableStateOf("Anonymous#mysecret") }
    var escape by rememberSaveable { mutableStateOf(true) }

    val hash = field.indexOf('#')
    val name = (if (hash < 0) field else field.substring(0, hash)).trim().ifEmpty { "Anonymous" }
    val key = if (hash < 0) "" else field.substring(hash + 1)
    val r = if (key.isEmpty()) null else ChaosCipher.trip(key, escape)
    val trip = r?.let { "!" + it.trip }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Heading("🎭 Tripcodes")
        Text(
            "Prove it's really you on an imageboard, no account needed. Same key, same code, every time.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card {
            OutlinedTextField(
                value = field, onValueChange = { field = it },
                label = { Text("Name#key") }, singleLine = true, textStyle = CodeStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                Text("Match 4chan (& < > \" become web codes)", fontSize = 16.sp, modifier = Modifier.weight(1f))
                Switch(checked = escape, onCheckedChange = { escape = it })
            }
        }
        Card {
            if (trip == null) {
                Text("Add # and a key after the name, like Anonymous#mysecret", fontWeight = FontWeight.Bold)
            } else {
                SelectionContainer {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(trip, style = CodeStyle.copy(fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    OutlinedButton(onClick = { copy(context, trip) }, modifier = Modifier.weight(1f)) { Text("📋 Copy code") }
                    OutlinedButton(onClick = { copy(context, "$name $trip") }, modifier = Modifier.weight(1f)) { Text("📋 Name + code") }
                }
            }
        }
        if (r != null) {
            Card {
                Text("How it got made", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Step(1, "Your key", r.key)
                Step(2, "Add H.. and take characters 2 and 3", "${r.padded}  →  ${r.picked}")
                Step(3, "Anything outside . to z becomes a dot", "${r.picked}  →  ${r.cleaned}")
                Step(4, "Swap symbols for letters (${ChaosCipher.FROM} → ${ChaosCipher.TO}). That's the salt.", "${r.cleaned}  →  ${r.salt}")
                Step(5, "Scramble the first 8 characters 25 times with DES", (if (r.ignored.isEmpty()) r.used else "${r.used}  (ignored: ${r.ignored})") + "  →  ${r.full}")
                Step(6, "Keep the last 10, add !", trip!!)
            }
        }
    }
}

@Composable
private fun Step(n: Int, what: String, value: String) {
    Column(Modifier.padding(top = 12.dp)) {
        Text("$n. $what", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SelectionContainer { Text(value, style = CodeStyle) }
    }
}
