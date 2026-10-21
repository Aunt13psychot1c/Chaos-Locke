package com.aunt13psychot1c.chaoslocke.crypto

import java.security.SecureRandom
import java.util.Base64

/**
 * The imageboard tripcode recipe and the secret-message lock built from the same parts.
 *
 * Tripcodes are the classic Unix DES crypt(3): a salt picked from the key twists the DES
 * expansion table, 25 rounds scramble a block of zeros, and the last 10 characters come out.
 * Secret messages reuse that salt-twisted DES on the message itself (CBC mode, random start
 * block) so the same key can run it backwards.
 *
 * Kept byte-for-byte compatible with the Tripcode Forge web page, so a message locked on one
 * opens on the other.
 */
object ChaosCipher {

    /** Every step of the tripcode recipe, so the screen can show its work. */
    data class Trip(
        val key: String,
        val padded: String,
        val picked: String,
        val cleaned: String,
        val salt: String,
        val full: String,
        val trip: String,
        val used: String,
        val ignored: String
    )

    class UnlockException(message: String) : Exception(message)

    fun trip(rawKey: String, escape: Boolean = true): Trip {
        val key = if (escape) escapeKey(rawKey) else rawKey
        val bytes = utf8(key)
        val latin = String(CharArray(bytes.size) { Char(bytes[it]) })
        val padded = latin + "H.."
        val picked = padded.substring(1, 3)
        val cleaned = picked.map { if (it in '.'..'z') it else '.' }.joinToString("")
        val salt = cleaned.map { c -> FROM.indexOf(c).let { if (it < 0) c else TO[it] } }.joinToString("")
        val full = crypt(bytes, salt)
        return Trip(
            key = key, padded = padded, picked = picked, cleaned = cleaned, salt = salt,
            full = full, trip = full.takeLast(10),
            used = latin.take(8), ignored = latin.drop(8)
        )
    }

    /** Traditional crypt(3). Only the low 7 bits of the first 8 bytes of [key] count. */
    fun crypt(key: IntArray, salt: String): String {
        val keys = subkeys(keyBits(key))
        val e = saltedE(salt)
        var block = IntArray(64)
        repeat(25) { block = feistel(block, keys, e) }
        val out = StringBuilder(salt)
        for (c in 0 until 11) {
            var v = 0
            for (q in 0 until 6) {
                val idx = c * 6 + q
                v = (v shl 1) or (if (idx < 64) block[idx] else 0)
            }
            out.append(ALPHA[v])
        }
        return out.toString()
    }

    fun lock(message: String, rawKey: String, random: SecureRandom = SecureRandom()): String {
        val c = setup(rawKey)
        val data = utf8(message).toMutableList()
        val pad = 8 - data.size % 8
        repeat(pad) { data.add(pad) }
        val iv = ByteArray(8).also(random::nextBytes).map { it.toInt() and 0xFF }.toIntArray()
        var prev = iv
        val out = iv.toMutableList()
        for (i in data.indices step 8) {
            val blk = IntArray(8) { data[i + it] xor prev[it] }
            prev = toBytes(feistel(toBits(blk), c.keys, c.e))
            out.addAll(prev.toList())
        }
        return toEmoji(out.toIntArray())
    }

    fun unlock(text: String, rawKey: String): String {
        val t = text.trim()
        if (t.isEmpty()) throw UnlockException("Paste a locked message into the box first.")
        val c = setup(rawKey)
        // Early web-page messages came out as chaos:base64, so those still open.
        val raw = if (t.startsWith("chaos:")) {
            try {
                Base64.getDecoder().decode(t.removePrefix("chaos:").filterNot { it.isWhitespace() })
                    .map { it.toInt() and 0xFF }.toIntArray()
            } catch (_: IllegalArgumentException) {
                throw UnlockException(CUT_OFF)
            }
        } else {
            fromEmoji(t)
        }
        if (raw.size < 16 || raw.size % 8 != 0) throw UnlockException(CUT_OFF)
        var prev = raw.copyOfRange(0, 8)
        val data = ArrayList<Int>(raw.size)
        for (i in 8 until raw.size step 8) {
            val blk = raw.copyOfRange(i, i + 8)
            val plain = toBytes(feistel(toBits(blk), c.back, c.e))
            for (k in 0 until 8) data.add(plain[k] xor prev[k])
            prev = blk
        }
        val pad = data.last()
        val padOk = pad in 1..8 && data.takeLast(pad).all { it == pad }
        if (!padOk) throw UnlockException(WRONG)
        val bytes = ByteArray(data.size - pad) { data[it].toByte() }
        return try {
            Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (_: java.nio.charset.CharacterCodingException) {
            throw UnlockException(WRONG)
        }
    }

    // ---- emoji armor: 64 tough-guy emoji, one per 6 bits ----

    private const val MARK = 0x1F510 // 🔐
    private val EMOJI: IntArray =
        "🔥💀👊🐺🦅🐍🦂🐉🦈🐊🦍🐗🦬🦏🐂🦁🐯🐻🦇🦖🦑🦀💣🔪🪓🔨🔧🔩🪚🪛🧨💥⚡🎯🎸🥁🏈🏆🥊🚀🚂🚜🚒🪖🎮🍺🥃🥩🌋🔗💎🪙💰🎲🗿🦴📡⚓🦾🤘💪🧠🐲🐎"
            .codePoints().toArray()

    val alphabet: List<String> get() = EMOJI.map { String(Character.toChars(it)) }

    private fun toEmoji(bytes: IntArray): String {
        val out = StringBuilder().appendCodePoint(MARK)
        var acc = 0
        var n = 0
        for (b in bytes) {
            acc = (acc shl 8) or b; n += 8
            while (n >= 6) { n -= 6; out.appendCodePoint(EMOJI[(acc shr n) and 63]) }
            acc = acc and ((1 shl n) - 1)
        }
        if (n > 0) out.appendCodePoint(EMOJI[(acc shl (6 - n)) and 63])
        return out.toString()
    }

    private fun fromEmoji(text: String): IntArray {
        val out = ArrayList<Int>()
        var acc = 0
        var n = 0
        text.codePoints().forEach { cp ->
            if (cp == MARK || cp == 0xFE0E || cp == 0xFE0F || cp == 0x200D || Character.isWhitespace(cp)) return@forEach
            val v = EMOJI.indexOf(cp)
            if (v < 0) {
                throw UnlockException(
                    "There's a ${String(Character.toChars(cp))} in there that isn't part of the code. Copy the whole message again."
                )
            }
            acc = (acc shl 6) or v; n += 6
            if (n >= 8) { n -= 8; out.add((acc shr n) and 255); acc = acc and ((1 shl n) - 1) }
        }
        return out.toIntArray()
    }

    // ---- DES ----

    private class Setup(val e: IntArray, val keys: Array<IntArray>, val back: Array<IntArray>)

    // Every character of a long key counts: bytes past the 8th fold back over the first 8.
    private fun setup(rawKey: String): Setup {
        val salt = trip(rawKey, escape = false).salt
        val folded = IntArray(8)
        utf8(rawKey).forEachIndexed { i, b -> folded[i % 8] = folded[i % 8] xor b }
        val keys = subkeys(keyBits(folded))
        return Setup(saltedE(salt), keys, keys.reversedArray())
    }

    private fun feistel(block: IntArray, keys: Array<IntArray>, e: IntArray): IntArray {
        val b = permute(block, IP)
        var l = b.copyOfRange(0, 32)
        var r = b.copyOfRange(32, 64)
        for (round in 0 until 16) {
            val x = permute(r, e)
            val k = keys[round]
            for (i in 0 until 48) x[i] = x[i] xor k[i]
            val f = IntArray(32)
            for (s in 0 until 8) {
                val o = s * 6
                val row = (x[o] shl 1) or x[o + 5]
                val col = (x[o + 1] shl 3) or (x[o + 2] shl 2) or (x[o + 3] shl 1) or x[o + 4]
                val v = S[s][row * 16 + col]
                f[s * 4] = (v shr 3) and 1; f[s * 4 + 1] = (v shr 2) and 1
                f[s * 4 + 2] = (v shr 1) and 1; f[s * 4 + 3] = v and 1
            }
            val p = permute(f, P)
            val nr = IntArray(32) { l[it] xor p[it] }
            l = r; r = nr
        }
        return permute(r + l, FP)
    }

    private fun permute(bits: IntArray, table: IntArray) = IntArray(table.size) { bits[table[it] - 1] }

    private fun keyBits(bytes: IntArray): IntArray {
        val bits = IntArray(64)
        for (i in 0 until 8) {
            val c = if (i < bytes.size) bytes[i] else 0
            for (j in 0 until 7) bits[i * 8 + j] = (c shr (6 - j)) and 1
        }
        return bits
    }

    private fun subkeys(bits: IntArray): Array<IntArray> {
        val cd = permute(bits, PC1)
        val c = cd.copyOfRange(0, 28).toMutableList()
        val d = cd.copyOfRange(28, 56).toMutableList()
        return Array(16) { round ->
            repeat(SHIFTS[round]) { c.add(c.removeAt(0)); d.add(d.removeAt(0)) }
            permute((c + d).toIntArray(), PC2)
        }
    }

    // The salt's 12 bits each swap one pair of entries in the expansion table.
    private fun saltedE(salt: String): IntArray {
        val e = E.copyOf()
        val value = ALPHA.indexOf(salt[0]).coerceAtLeast(0) or (ALPHA.indexOf(salt[1]).coerceAtLeast(0) shl 6)
        for (i in 0 until 12) {
            if ((value shr i) and 1 == 1) { val t = e[i]; e[i] = e[i + 24]; e[i + 24] = t }
        }
        return e
    }

    private fun toBits(bytes: IntArray) = IntArray(64) { (bytes[it / 8] shr (7 - it % 8)) and 1 }

    private fun toBytes(bits: IntArray) = IntArray(8) { i ->
        var v = 0
        for (j in 0 until 8) v = (v shl 1) or bits[i * 8 + j]
        v
    }

    private fun utf8(s: String) = s.toByteArray(Charsets.UTF_8).map { it.toInt() and 0xFF }.toIntArray()

    private fun escapeKey(key: String) =
        key.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    /** For tests: one plain DES block, salt ".." meaning no twist. */
    internal fun desBlock(key7: IntArray, block: IntArray, salt: String = ".."): IntArray =
        toBytes(feistel(toBits(block), subkeys(keyBits(key7)), saltedE(salt)))

    const val FROM = ":;<=>?@[\\]^_`"
    const val TO = "ABCDEFGabcdef"
    private const val ALPHA = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private const val WRONG = "Wrong key, or the message got changed."
    private const val CUT_OFF = "The message got cut off or changed. Copy the whole thing again."

    private val PC1 = intArrayOf(57,49,41,33,25,17,9,1,58,50,42,34,26,18,10,2,59,51,43,35,27,19,11,3,60,52,44,36,
        63,55,47,39,31,23,15,7,62,54,46,38,30,22,14,6,61,53,45,37,29,21,13,5,28,20,12,4)
    private val PC2 = intArrayOf(14,17,11,24,1,5,3,28,15,6,21,10,23,19,12,4,26,8,16,7,27,20,13,2,
        41,52,31,37,47,55,30,40,51,45,33,48,44,49,39,56,34,53,46,42,50,36,29,32)
    private val SHIFTS = intArrayOf(1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1)
    private val IP = intArrayOf(58,50,42,34,26,18,10,2,60,52,44,36,28,20,12,4,62,54,46,38,30,22,14,6,64,56,48,40,32,24,16,8,
        57,49,41,33,25,17,9,1,59,51,43,35,27,19,11,3,61,53,45,37,29,21,13,5,63,55,47,39,31,23,15,7)
    private val FP = intArrayOf(40,8,48,16,56,24,64,32,39,7,47,15,55,23,63,31,38,6,46,14,54,22,62,30,37,5,45,13,53,21,61,29,
        36,4,44,12,52,20,60,28,35,3,43,11,51,19,59,27,34,2,42,10,50,18,58,26,33,1,41,9,49,17,57,25)
    private val E = intArrayOf(32,1,2,3,4,5,4,5,6,7,8,9,8,9,10,11,12,13,12,13,14,15,16,17,
        16,17,18,19,20,21,20,21,22,23,24,25,24,25,26,27,28,29,28,29,30,31,32,1)
    private val P = intArrayOf(16,7,20,21,29,12,28,17,1,15,23,26,5,18,31,10,2,8,24,14,32,27,3,9,19,13,30,6,22,11,4,25)
    private val S = arrayOf(
        intArrayOf(14,4,13,1,2,15,11,8,3,10,6,12,5,9,0,7,0,15,7,4,14,2,13,1,10,6,12,11,9,5,3,8,4,1,14,8,13,6,2,11,15,12,9,7,3,10,5,0,15,12,8,2,4,9,1,7,5,11,3,14,10,0,6,13),
        intArrayOf(15,1,8,14,6,11,3,4,9,7,2,13,12,0,5,10,3,13,4,7,15,2,8,14,12,0,1,10,6,9,11,5,0,14,7,11,10,4,13,1,5,8,12,6,9,3,2,15,13,8,10,1,3,15,4,2,11,6,7,12,0,5,14,9),
        intArrayOf(10,0,9,14,6,3,15,5,1,13,12,7,11,4,2,8,13,7,0,9,3,4,6,10,2,8,5,14,12,11,15,1,13,6,4,9,8,15,3,0,11,1,2,12,5,10,14,7,1,10,13,0,6,9,8,7,4,15,14,3,11,5,2,12),
        intArrayOf(7,13,14,3,0,6,9,10,1,2,8,5,11,12,4,15,13,8,11,5,6,15,0,3,4,7,2,12,1,10,14,9,10,6,9,0,12,11,7,13,15,1,3,14,5,2,8,4,3,15,0,6,10,1,13,8,9,4,5,11,12,7,2,14),
        intArrayOf(2,12,4,1,7,10,11,6,8,5,3,15,13,0,14,9,14,11,2,12,4,7,13,1,5,0,15,10,3,9,8,6,4,2,1,11,10,13,7,8,15,9,12,5,6,3,0,14,11,8,12,7,1,14,2,13,6,15,0,9,10,4,5,3),
        intArrayOf(12,1,10,15,9,2,6,8,0,13,3,4,14,7,5,11,10,15,4,2,7,12,9,5,6,1,13,14,0,11,3,8,9,14,15,5,2,8,12,3,7,0,4,10,1,13,11,6,4,3,2,12,9,5,15,10,11,14,1,7,6,0,8,13),
        intArrayOf(4,11,2,14,15,0,8,13,3,12,9,7,5,10,6,1,13,0,11,7,4,9,1,10,14,3,5,12,2,15,8,6,1,4,11,13,12,3,7,14,10,15,6,8,0,5,9,2,6,11,13,8,1,4,10,7,9,5,0,15,14,2,3,12),
        intArrayOf(13,2,8,4,6,15,11,1,10,9,3,14,5,0,12,7,1,15,13,8,10,3,7,4,12,5,6,11,0,14,9,2,7,11,4,1,9,12,14,2,0,6,10,13,15,3,5,8,2,1,14,7,4,10,8,13,15,12,9,0,3,5,6,11)
    )
}
