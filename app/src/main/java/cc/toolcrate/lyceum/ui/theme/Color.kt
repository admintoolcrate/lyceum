package cc.toolcrate.lyceum.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The Paper palette: warm cream pages and brown-black ink, or its evening counterpart. */
@Immutable
data class Paper(
    val page: Color,
    val card: Color,
    val ink: Color,
    val muted: Color,
    val hairline: Color,
    val accent: Color,
    val accentSoft: Color,
    val isDark: Boolean,
)

val PaperLight = Paper(
    page = Color(0xFFF5EFE3),
    card = Color(0xFFFBF7EE),
    ink = Color(0xFF2A211B),
    muted = Color(0xFF6E6259),
    hairline = Color(0xFFDDD3C2),
    accent = Color(0xFF7A5A3A),
    accentSoft = Color(0xFFEADFCB),
    isDark = false,
)

val PaperDark = Paper(
    page = Color(0xFF1B1713),
    card = Color(0xFF24201B),
    ink = Color(0xFFEDE4D3),
    muted = Color(0xFFA89B8A),
    hairline = Color(0xFF3A332B),
    accent = Color(0xFFC9A578),
    accentSoft = Color(0xFF3A3022),
    isDark = true,
)

val LocalPaper = staticCompositionLocalOf { PaperLight }
