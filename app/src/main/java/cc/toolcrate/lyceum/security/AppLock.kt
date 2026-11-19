package cc.toolcrate.lyceum.security

import android.content.Context
import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.domain.PinRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

sealed interface PinResult {
    data object Ok : PinResult
    data object Wrong : PinResult
    data class Wait(val millis: Long) : PinResult
}

/**
 * Whether the journal is locked. It locks when the app starts and after [RELOCK_AFTER_MS] in the
 * background, unless the app sent the user somewhere on purpose (a file picker, Stagira, Google).
 */
@Singleton
class AppLock @Inject constructor(
    private val settings: SettingsStore,
    private val clock: Clock,
) {
    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var backgroundAt = 0L
    private var expectingReturn = false

    /** Called once settings are known at start: an app without a lock is simply open. */
    suspend fun start() {
        _locked.value = settings.current().lockEnabled
    }

    fun onBackground() {
        backgroundAt = SystemClock.elapsedRealtime()
    }

    suspend fun onForeground() {
        if (!settings.current().lockEnabled) {
            _locked.value = false
            return
        }
        if (expectingReturn) {
            expectingReturn = false
            return
        }
        if (backgroundAt != 0L && SystemClock.elapsedRealtime() - backgroundAt > RELOCK_AFTER_MS) _locked.value = true
    }

    /** The next trip out of the app is one we started (a picker, sign-in); don't lock on the way back. */
    fun expectReturn() {
        expectingReturn = true
    }

    fun unlock() {
        _locked.value = false
    }

    suspend fun tryPin(pin: String): PinResult {
        val (failures, lastAt) = settings.pinFailures()
        val now = clock.millis()
        val wait = PinRules.waitRemaining(failures, lastAt, now)
        if (wait > 0) return PinResult.Wait(wait)
        val record = settings.pinRecord() ?: return PinResult.Wrong
        return if (PinHasher.verify(pin, record)) {
            settings.setPinFailures(0, 0)
            unlock()
            PinResult.Ok
        } else {
            val count = failures + 1
            settings.setPinFailures(count, now)
            val next = PinRules.backoffMillis(count)
            if (next > 0) PinResult.Wait(next) else PinResult.Wrong
        }
    }

    /** Checks a PIN without unlocking or counting, e.g. before turning the lock off. Wrong guesses still count. */
    suspend fun checkPin(pin: String): Boolean {
        val record = settings.pinRecord() ?: return false
        val ok = PinHasher.verify(pin, record)
        if (!ok) settings.pinFailures().let { (n, _) -> settings.setPinFailures(n + 1, clock.millis()) }
        return ok
    }

    suspend fun enable(pin: String, biometric: Boolean) {
        require(PinRules.isValid(pin))
        settings.setPinRecord(PinHasher.hash(pin))
        settings.setPinFailures(0, 0)
        settings.setLock(true, biometric)
        _locked.value = false
    }

    suspend fun disable() {
        settings.setLock(false, false)
        settings.setPinRecord(null)
        settings.setPinFailures(0, 0)
        _locked.value = false
    }

    fun promptBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
        expectReturn()
        val prompt = BiometricPrompt(
            activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    unlock()
                    onSuccess()
                }
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Lyceum")
                .setNegativeButtonText("Use PIN")
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build(),
        )
    }

    companion object {
        const val RELOCK_AFTER_MS = 30_000L
        private const val AUTHENTICATORS = BIOMETRIC_STRONG or BIOMETRIC_WEAK

        fun biometricAvailable(context: Context): Boolean =
            BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
