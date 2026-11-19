package cc.toolcrate.lyceum

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.ui.nav.LyceumApp
import cc.toolcrate.lyceum.ui.nav.MainViewModel
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

/** A FragmentActivity so BiometricPrompt can attach to it. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val main: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settings by main.settings.collectAsStateWithLifecycle()
            LyceumTheme(settings?.theme ?: ThemeMode.System) {
                LyceumApp()
            }
        }
    }
}
