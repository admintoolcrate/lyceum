package cc.toolcrate.lyceum

import android.content.Intent
import android.os.Bundle
import cc.toolcrate.lyceum.stagira.StagiraContract
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.ui.lock.LockScreen
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
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) { main.onForeground() }
            override fun onStop(owner: LifecycleOwner) { main.onBackground() }
        })
        handle(intent)
        setContent {
            val settings by main.settings.collectAsStateWithLifecycle()
            val locked by main.locked.collectAsStateWithLifecycle()
            val s = settings
            val lockOn = s?.lockEnabled == true

            // With the lock on, pages stay out of screenshots and the recent-apps preview.
            LaunchedEffect(lockOn) {
                if (lockOn) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            LyceumTheme(s?.theme ?: ThemeMode.System) {
                when {
                    // Settings not read yet: show only the page colour, never a flash of the journal.
                    s == null -> Box(Modifier.fillMaxSize().background(LyceumTheme.paper.page))
                    lockOn && locked -> LockScreen(main.lock, biometric = s.biometricEnabled)
                    else -> LyceumApp(openToday = main.openToday, onOpenedToday = main::openedToday, today = main.today)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** A goal sent by Stagira waits for today's page; the page itself decides nothing until the user saves. */
    private fun handle(intent: Intent?) {
        if (intent?.action != StagiraContract.ACTION_NEW_ENTRY) return
        main.offerGoal(StagiraContract.goalFrom(intent))
    }
}
