package cc.toolcrate.lyceum.backup

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * For weekly backups only: keeps the derived backup key on this phone, itself encrypted by an Android
 * Keystore key that never leaves the device. The passphrase is never stored. With manual backups this
 * stays empty.
 */
@Singleton
class KeyVault @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("vault", Context.MODE_PRIVATE)
    private val b64 = Base64.getEncoder()
    private val unb64 = Base64.getDecoder()

    fun load(): BackupKey? = runCatching {
        val wrapped = unb64.decode(prefs.getString("key", null) ?: return null)
        val iv = unb64.decode(prefs.getString("iv", null))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey(), GCMParameterSpec(128, iv))
        BackupKey(cipher.doFinal(wrapped), unb64.decode(prefs.getString("salt", null)), prefs.getInt("iterations", 0))
    }.getOrNull()

    fun save(key: BackupKey) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey())
        prefs.edit()
            .putString("key", b64.encodeToString(cipher.doFinal(key.bytes)))
            .putString("iv", b64.encodeToString(cipher.iv))
            .putString("salt", b64.encodeToString(key.salt))
            .putInt("iterations", key.iterations)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun wrappingKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ALIAS = "lyceum-backup-wrap"
    }
}
