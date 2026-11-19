package cc.toolcrate.lyceum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/** How the app chooses between light and dark. */
enum class ThemeMode { System, Light, Dark }

private fun Paper.scheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent, onPrimary = page,
        primaryContainer = accentSoft, onPrimaryContainer = ink,
        secondary = muted, onSecondary = page,
        secondaryContainer = accentSoft, onSecondaryContainer = ink,
        tertiary = accent, onTertiary = page,
        background = page, onBackground = ink,
        surface = page, onSurface = ink,
        surfaceVariant = card, onSurfaceVariant = muted,
        surfaceContainerLowest = page, surfaceContainerLow = card,
        surfaceContainer = card, surfaceContainerHigh = card, surfaceContainerHighest = accentSoft,
        surfaceTint = page,
        outline = hairline, outlineVariant = hairline,
        inverseSurface = ink, inverseOnSurface = page,
    )
}

private val LyceumShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun LyceumTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val paper = if (dark) PaperDark else PaperLight
    CompositionLocalProvider(LocalPaper provides paper) {
        MaterialTheme(
            colorScheme = paper.scheme(),
            typography = LyceumTypography,
            shapes = LyceumShapes,
        ) {
            // Icons and text drawn outside a Surface use ink too, not Material's default black.
            CompositionLocalProvider(LocalContentColor provides paper.ink, content = content)
        }
    }
}

object LyceumTheme {
    val paper: Paper @Composable get() = LocalPaper.current
}
