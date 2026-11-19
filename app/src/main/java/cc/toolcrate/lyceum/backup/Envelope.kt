package cc.toolcrate.lyceum.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** A key derived from the backup passphrase, with the salt and work factor it was derived with. */
class BackupKey(val bytes: ByteArray, val salt: ByteArray, val iterations: Int)

/**
 * The file kept in Google Drive: the journal's JSON export sealed with AES-256-GCM under a key derived
 * from the user's passphrase (PBKDF2-HMAC-SHA256). Neither Google nor anyone else can read it without
 * the passphrase, and Lyceum never uploads anything unsealed.
 */
object Envelope {
    const val FORMAT = "lyceum-sealed"
    const val VERSION = 1
    const val ITERATIONS = 210_000
    const val MIN_PASSPHRASE = 8

    /** Work factors outside this range are refused, so a damaged file can't hang the app. */
    val ACCEPTED_ITERATIONS = 10_000..5_000_000

    private const val TRANSFORM = "AES/GCM/NoPadding"
    private val AAD = "$FORMAT/$VERSION".toByteArray()
    private val random = SecureRandom()
    private val b64 = Base64.getEncoder()
    private val unb64 = Base64.getDecoder()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Serializable
    data class Sealed(
        val format: String = FORMAT,
        val version: Int = VERSION,
        val cipher: String = "AES-256-GCM",
        val kdf: String = "PBKDF2-HMAC-SHA256",
        val iterations: Int,
        val salt: String,
        val iv: String,
        val data: String,
    ) {
        val saltBytes: ByteArray get() = unb64.decode(salt)
    }

    class WrongPassphrase : Exception("That passphrase doesn't open this backup.")
    class Unreadable(message: String) : Exception(message)

    fun deriveKey(passphrase: CharArray, salt: ByteArray = randomBytes(16), iterations: Int = ITERATIONS): BackupKey {
        val spec = PBEKeySpec(passphrase, salt, iterations, 256)
        try {
            return BackupKey(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, salt, iterations)
        } finally {
            spec.clearPassword()
        }
    }

    fun seal(plain: String, key: BackupKey): String {
        val iv = randomBytes(12)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.bytes, "AES"), GCMParameterSpec(128, iv))
        cipher.updateAAD(AAD)
        val data = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return json.encodeToString(
            Sealed.serializer(),
            Sealed(iterations = key.iterations, salt = b64.encodeToString(key.salt), iv = b64.encodeToString(iv), data = b64.encodeToString(data)),
        )
    }

    /** Reads the outer file without opening it. Throws [Unreadable] for anything that isn't a Lyceum backup. */
    fun read(text: String): Sealed {
        val sealed = runCatching { json.decodeFromString(Sealed.serializer(), text) }.getOrNull()
            ?: throw Unreadable("This isn't a Lyceum backup.")
        if (sealed.format != FORMAT) throw Unreadable("This isn't a Lyceum backup.")
        if (sealed.version > VERSION) throw Unreadable("This backup comes from a newer Lyceum. Update the app, then restore.")
        if (sealed.iterations !in ACCEPTED_ITERATIONS) throw Unreadable("This backup looks damaged.")
        return sealed
    }

    /** Opens [sealed] with a key derived from [passphrase] and the file's own salt. */
    fun open(sealed: Sealed, passphrase: CharArray): String =
        open(sealed, deriveKey(passphrase, sealed.saltBytes, sealed.iterations))

    fun open(sealed: Sealed, key: BackupKey): String {
        val cipher = Cipher.getInstance(TRANSFORM)
        try {
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.bytes, "AES"), GCMParameterSpec(128, unb64.decode(sealed.iv)))
            cipher.updateAAD(AAD)
            return cipher.doFinal(unb64.decode(sealed.data)).toString(Charsets.UTF_8)
        } catch (e: AEADBadTagException) {
            throw WrongPassphrase()
        } catch (e: IllegalArgumentException) {
            throw Unreadable("This backup looks damaged.")
        }
    }

    private fun randomBytes(n: Int) = ByteArray(n).also(random::nextBytes)
}
