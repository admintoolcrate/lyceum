package cc.toolcrate.lyceum.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Stores a PIN only as a salted PBKDF2-SHA256 hash, written as "v1:iterations:salt:hash" (Base64).
 * The PIN itself is never saved.
 */
object PinHasher {
    const val ITERATIONS = 120_000
    private val random = SecureRandom()

    fun hash(pin: String, iterations: Int = ITERATIONS, salt: ByteArray = ByteArray(16).also(random::nextBytes)): String {
        val b64 = Base64.getEncoder()
        return "v1:$iterations:${b64.encodeToString(salt)}:${b64.encodeToString(derive(pin, salt, iterations))}"
    }

    fun verify(pin: String, record: String): Boolean {
        val parts = record.split(':')
        if (parts.size != 4 || parts[0] != "v1") return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val b64 = Base64.getDecoder()
        val salt = runCatching { b64.decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { b64.decode(parts[3]) }.getOrNull() ?: return false
        return MessageDigest.isEqual(derive(pin, salt, iterations), expected)
    }

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, 256)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
