package cc.toolcrate.lyceum

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import cc.toolcrate.lyceum.ui.nav.LyceumApp
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import dagger.hilt.android.AndroidEntryPoint

/** A FragmentActivity so BiometricPrompt can attach to it. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LyceumTheme {
                LyceumApp()
            }
        }
    }
}
