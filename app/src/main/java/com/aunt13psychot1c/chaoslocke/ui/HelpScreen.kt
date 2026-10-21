package com.aunt13psychot1c.chaoslocke.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The for-dummies manual. Written to pass "the Aaron test": someone who isn't a techie should
 * be able to send a secret message without asking anyone. Plain words, one action per step,
 * exact button names. Opens by itself the first time, and any time from the Help tab.
 */
private class Section(val title: String, val lines: List<String>)

private val SECTIONS = listOf(
    Section("🔐 What is Chaos Locke?", listOf(
        "It turns a message into a string of tough-guy emoji like 🔐🐗🦏🚂🐉🔥💀…",
        "Anybody can see the emoji. Only someone who knows your secret key can turn them back into words.",
        "Think of it like a diary lock for texts. You and your friend share the key, and nobody else can read it."
    )),
    Section("✉️ Send a secret (step by step)", listOf(
        "1. Tap 🔐 Lock at the bottom.",
        "2. In Secret key, type a key. Make it something only you and your friend know, like oldtruck1987.",
        "3. In Message, type your secret.",
        "4. Tap 🔒 Lock. Your message turns into emoji.",
        "5. Tap 📤 Send and pick the app you want: Messenger, Discord, texts, anything. Or tap 📋 Copy and paste it yourself.",
        "6. Tell your friend the key some other way: in person, on the phone, or in a different app. Never in the same message!"
    )),
    Section("📬 Read a secret someone sent you", listOf(
        "1. Copy the whole emoji message. Hold your finger on it in your chat app and tap Copy.",
        "2. Open Chaos Locke, tap 🔐 Lock, then tap 📋 Paste.",
        "3. Type the key your friend gave you.",
        "4. Tap 🔓 Unlock. The secret shows up underneath.",
        "Shortcut: in most apps, hold your finger on the emoji message, tap Share (or the ⋮ menu), and pick Chaos Locke. It lands right in the Message box."
    )),
    Section("😬 It says something went wrong", listOf(
        "\"Wrong key, or the message got changed.\" Check the key, every letter counts, and capitals matter. Then try again.",
        "\"The message got cut off.\" You didn't copy all of it. Go back and copy the whole thing, from the 🔐 at the start to the very last emoji.",
        "\"There's a 🍓 in there that isn't part of the code.\" Something extra got pasted in, like a word or a different emoji. Copy just the code.",
        "Spaces and line breaks don't matter. Chaos Locke ignores them."
    )),
    Section("🎭 What's a tripcode?", listOf(
        "On imageboards (like 4chan) there are no accounts, so anybody can type any name. A tripcode proves it's really you.",
        "1. Tap 🎭 Tripcode.",
        "2. Type your name, then #, then a secret key. Example: Davi#mysecret",
        "3. Your code shows up, like !X/EFwDtXFE. Same key, same code, every time.",
        "On the board itself you type the same Name#key in the name box, and the site does the math for you. This tab lets you see your code ahead of time, or check someone else's.",
        "Leave \"Match 4chan\" on unless you know your board doesn't change & < > \"."
    )),
    Section("🛡️ How safe is it?", listOf(
        "Chaos Locke never uses the internet. It doesn't even have permission to. Nothing you type leaves your phone.",
        "Every lock comes out different, even the same message with the same key, so nobody can spot repeats.",
        "It's diary-lock strong: great for private notes between friends. It uses DES, a lock from the 1970s, so a serious expert with big computers could break it. Don't use it for bank info, passwords, or anything dangerous.",
        "Longer, weirder keys are better. \"cat\" is easy to guess. \"purpleGRAVY-sloth42\" is not.",
        "Tripcodes only use the first 8 characters of the key, and short keys can be guessed. Never use a real password as a tripcode key."
    )),
    Section("🌐 Works with the web page too", listOf(
        "Chaos Locke speaks the same language as the Tripcode Forge web page. A message locked on one unlocks on the other, and tripcodes match."
    ))
)

@Composable
fun HelpScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Heading("📖 Chaos Locke for Dummies")
        Text("No tech talk. Just what to tap.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        SECTIONS.forEach { s ->
            Card {
                Text(s.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                s.lines.forEach { line ->
                    Text(line, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("🔐 Got it, let's lock something", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
