package cc.toolcrate.lyceum.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.security.AppLock
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.lock.PinPad
import cc.toolcrate.lyceum.ui.theme.LyceumTheme

private enum class PinStep { None, NewFirst, NewConfirm, Disable }

@Composable
fun LockSection(
    settings: Settings,
    onEnable: (pin: String, biometric: Boolean) -> Unit,
    onDisable: suspend (pin: String) -> Boolean,
    onBiometric: (Boolean) -> Unit,
    launch: (suspend () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val biometricAvailable = remember { AppLock.biometricAvailable(context) }
    var step by remember { mutableStateOf(PinStep.None) }
    var first by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var note by remember { mutableStateOf<String?>(null) }

    SettingSwitch(
        title = "App lock",
        detail = "Ask for a PIN when Lyceum opens, and after half a minute away.",
        checked = settings.lockEnabled,
        onChange = { on -> pin = ""; note = null; step = if (on) PinStep.NewFirst else PinStep.Disable },
    )
    if (settings.lockEnabled) {
        SettingRow("Change PIN", onClick = { pin = ""; note = null; step = PinStep.NewFirst })
        if (biometricAvailable) {
            SettingSwitch("Fingerprint or face", "Unlock without the PIN on this phone.", settings.biometricEnabled, onBiometric)
        }
    }

    if (step != PinStep.None) {
        val paper = LyceumTheme.paper
        AlertDialog(
            onDismissRequest = { step = PinStep.None },
            title = {
                Text(
                    when (step) {
                        PinStep.NewFirst -> "Choose a PIN"
                        PinStep.NewConfirm -> "Enter it again"
                        else -> "Enter your PIN to turn the lock off"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        note ?: "4 to 8 digits. If you forget it, Lyceum can't recover it; keep an export somewhere safe.",
                        style = MaterialTheme.typography.bodySmall, color = paper.muted,
                    )
                    Gap(20)
                    PinPad(pin = pin, onPin = { pin = it }, onSubmit = {
                        val entered = pin
                        pin = ""
                        when (step) {
                            PinStep.NewFirst -> { first = entered; note = null; step = PinStep.NewConfirm }
                            PinStep.NewConfirm -> if (entered == first) {
                                onEnable(entered, settings.biometricEnabled || (!settings.lockEnabled && biometricAvailable))
                                step = PinStep.None
                            } else {
                                note = "Those didn't match. Choose a PIN again."
                                step = PinStep.NewFirst
                            }
                            PinStep.Disable -> launch {
                                if (onDisable(entered)) step = PinStep.None else note = "That isn't it."
                            }
                            PinStep.None -> Unit
                        }
                    })
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { step = PinStep.None }) { Text("Cancel") } },
            containerColor = paper.card,
        )
    }
}
