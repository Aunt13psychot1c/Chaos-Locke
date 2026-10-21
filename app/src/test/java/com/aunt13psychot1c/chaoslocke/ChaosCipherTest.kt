package com.aunt13psychot1c.chaoslocke

import com.aunt13psychot1c.chaoslocke.crypto.ChaosCipher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ChaosCipherTest {

    private fun bytes(s: String) = s.toByteArray(Charsets.UTF_8).map { it.toInt() and 0xFF }.toIntArray()

    @Test fun desMatchesTheTextbook() {
        // FIPS worked example: key 133457799BBCDFF1, plaintext 0123456789ABCDEF.
        val key = intArrayOf(0x13, 0x34, 0x57, 0x79, 0x9B, 0xBC, 0xDF, 0xF1).map { it shr 1 }.toIntArray()
        val out = ChaosCipher.desBlock(key, intArrayOf(0x01, 0x23, 0x45, 0x67, 0x89, 0xAB, 0xCD, 0xEF))
        assertEquals("85E813540F0AB405", out.joinToString("") { "%02X".format(it) })
    }

    @Test fun cryptMatchesUnix() {
        // Made with the system's own crypt(3).
        assertEquals("hapX/EFwDtXFE", ChaosCipher.crypt(bytes("chaosqueen"), "ha"))
        assertEquals("H.6ZnBI2EKkq.", ChaosCipher.crypt(bytes("a"), "H."))
        assertEquals("as1ozOtJW9BFA", ChaosCipher.crypt(bytes("password"), "as"))
    }

    @Test fun tripcodesMatchTheWebPage() {
        assertEquals("X/EFwDtXFE", ChaosCipher.trip("chaosqueen").trip)
        assertEquals("SeEGQz6EDI", ChaosCipher.trip("tea<&>", escape = true).trip)
        assertEquals("0OS1UzWNl6", ChaosCipher.trip("tea<&>", escape = false).trip)
        assertEquals("ZnBI2EKkq.", ChaosCipher.trip("a").trip)
        val odd = ChaosCipher.trip("#!x")
        assertEquals(".x", odd.salt)
        assertEquals("KXeaUV03bA", odd.trip)
    }

    @Test fun webPageMessagesOpenHere() {
        assertEquals("Erin the Slew knows too much 🦥",
            ChaosCipher.unlock("🔐🐗🦏🚂🐉🐊⚓🦁🦖🐊🪓🥩🤘🦾🦀🌋🌋🪓📡🦁🤘🚒🎮🪓🐂💥💀🚀🦁🔩⚡🐉🪙🍺🎸🐎🚂🦑🦬🪓🔗📡🐲🔗🔨🦖💀🚂🪓🤘🐎🥃💎🦅🦖💀🔪🚂🐯💥🏆🔨🔩🌋🐗", "chaosqueen"))
        assertEquals("Made on the web page 🔐",
            ChaosCipher.unlock("🔐⚓🐂🦈🏈🦖⚓⚓🦁⚡💀🥃🦖💎🚒🦏🐎🦍⚡🚒🦍🪖🏈💰🐎🪛🐍🪚🚜🥁🐉🧠🦏🚜🪛🥃🐉🦏🔗🌋🪚🪙🦀🧨🦂🦖💰🔥🐻🪖🦾🐲🎯👊⚡", "another key!"))
    }

    @Test fun messagesRoundTrip() {
        val keys = listOf("chaosqueen", "x", "a much longer key with spaces!", "ñandú 👑")
        val messages = listOf("", "a", "exactly8", "Erin knows too much 🦥", "日本語とemoji 👑✨ ".repeat(20))
        for (k in keys) for (m in messages) {
            val locked = ChaosCipher.lock(m, k)
            assertTrue(locked.startsWith("🔐"))
            assertEquals(m, ChaosCipher.unlock(locked, k))
            // Spaces and line breaks picked up while pasting don't matter.
            val spaced = locked.codePoints().toArray().toList().chunked(3)
                .joinToString(" \n") { group -> group.joinToString("") { String(Character.toChars(it)) } }
            assertEquals(m, ChaosCipher.unlock(spaced, k))
        }
    }

    @Test fun sameMessageLocksDifferentlyEachTime() {
        assertNotEquals(ChaosCipher.lock("hi", "k"), ChaosCipher.lock("hi", "k"))
    }

    @Test fun wrongKeysAndDamageAreCaught() {
        val locked = ChaosCipher.lock("secret", "chaosqueen")
        var slipped = 0
        for (i in 0 until 200) {
            try { ChaosCipher.unlock(locked, "nope$i"); slipped++ } catch (_: ChaosCipher.UnlockException) { }
        }
        assertEquals(0, slipped)
        expectError(locked + "🍓", "isn't part of the code")
        expectError(locked.dropLast(4), "cut off")
        expectError("   ", "Paste")
        expectError("This reminds me of the Earthquake", "regular message")
    }

    @Test fun alphabetHas64DistinctEmoji() {
        assertEquals(64, ChaosCipher.alphabet.toSet().size)
    }

    private fun expectError(text: String, contains: String) {
        try {
            ChaosCipher.unlock(text, "chaosqueen")
            fail("expected an error for $text")
        } catch (e: ChaosCipher.UnlockException) {
            assertTrue(e.message, e.message!!.contains(contains))
        }
    }
}
