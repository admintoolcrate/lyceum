package cc.toolcrate.lyceum.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EnvelopeTest {
    private val pass = "correct horse".toCharArray()
    // A low work factor keeps the tests quick; the app uses Envelope.ITERATIONS.
    private fun key(p: CharArray = pass) = Envelope.deriveKey(p, iterations = 10_000)

    @Test
    fun sealsAndOpensWithThePassphrase() {
        val plain = """{"format":"lyceum","entries":[{"body":"ünïcode ✓"}]}"""
        val text = Envelope.seal(plain, key())
        assertFalse(text.contains("ünïcode"))
        assertFalse(text.contains("entries"))
        assertEquals(plain, Envelope.open(Envelope.read(text), pass))
    }

    @Test
    fun theWrongPassphraseIsRefused() {
        val sealed = Envelope.read(Envelope.seal("secret", key()))
        assertThrows(Envelope.WrongPassphrase::class.java) { Envelope.open(sealed, "wrong horse".toCharArray()) }
    }

    @Test
    fun tamperingIsDetected() {
        val sealed = Envelope.read(Envelope.seal("secret page", key()))
        val bytes = java.util.Base64.getDecoder().decode(sealed.data)
        bytes[0] = (bytes[0].toInt() xor 1).toByte()
        val altered = sealed.copy(data = java.util.Base64.getEncoder().encodeToString(bytes))
        assertThrows(Envelope.WrongPassphrase::class.java) { Envelope.open(altered, pass) }
    }

    @Test
    fun eachSealIsFresh() {
        val k = key()
        val a = Envelope.read(Envelope.seal("same", k))
        val b = Envelope.read(Envelope.seal("same", k))
        assertNotEquals(a.iv, b.iv)
        assertNotEquals(a.data, b.data)
        assertNotEquals(key().salt.toList(), key().salt.toList())
    }

    @Test
    fun refusesOtherFilesNewerVersionsAndAbsurdWorkFactors() {
        assertThrows(Envelope.Unreadable::class.java) { Envelope.read("{}") }
        assertThrows(Envelope.Unreadable::class.java) { Envelope.read("plain text") }
        val good = Envelope.read(Envelope.seal("x", key()))
        val json = kotlinx.serialization.json.Json
        assertThrows(Envelope.Unreadable::class.java) {
            Envelope.read(json.encodeToString(Envelope.Sealed.serializer(), good.copy(version = 2)))
        }
        assertThrows(Envelope.Unreadable::class.java) {
            Envelope.read(json.encodeToString(Envelope.Sealed.serializer(), good.copy(iterations = 999_999_999)))
        }
    }
}
