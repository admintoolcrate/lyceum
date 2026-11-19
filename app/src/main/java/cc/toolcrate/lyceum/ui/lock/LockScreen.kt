package cc.toolcrate.lyceum.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import cc.toolcrate.lyceum.security.AppLock
import cc.toolcrate.lyceum.security.PinResult
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Covers the whole app until the PIN or a fingerprint opens it. */
@Composable
fun LockScreen(lock: AppLock, biometric: Boolean) {
    val paper = LyceumTheme.paper
    val activity = LocalContext.current as? FragmentActivity
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var waitUntil by remember { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(biometric) {
        if (biometric && activity != null && AppLock.biometricAvailable(activity)) lock.promptBiometric(activity) {}
    }
    LaunchedEffect(waitUntil) {
        while (System.currentTimeMillis() < waitUntil) {
            now = System.currentTimeMillis()
            delay(500)
        }
        now = System.currentTimeMillis()
    }
    val waiting = now < waitUntil

    Column(
        Modifier.fillMaxSize().background(paper.page).safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Lyceum", style = MaterialTheme.typography.displaySmall, color = paper.ink)
        Gap(8)
        Text(
            when {
                waiting -> "Try again in ${((waitUntil - now) / 1000) + 1} s."
                message != null -> message!!
                else -> "Enter your PIN"
            },
            style = MaterialTheme.typography.bodyMedium, color = paper.muted,
        )
        Gap(32)
        PinPad(
            pin = pin,
            onPin = { pin = it; message = null },
            enabled = !waiting,
            onSubmit = {
                val attempt = pin
                pin = ""
                scope.launch {
                    when (val r = lock.tryPin(attempt)) {
                        PinResult.Ok -> Unit
                        PinResult.Wrong -> message = "That isn't it. Try again."
                        is PinResult.Wait -> waitUntil = System.currentTimeMillis() + r.millis
                    }
                }
            },
            extraKey = if (biometric && activity != null && AppLock.biometricAvailable(activity)) {
                { TextButton(onClick = { lock.promptBiometric(activity) {} }) { Text("Use fingerprint or face", color = paper.accent) } }
            } else null,
        )
    }
}
