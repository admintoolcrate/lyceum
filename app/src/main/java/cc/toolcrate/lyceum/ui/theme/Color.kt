package cc.toolcrate.lyceum.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The palette, as Mono: the ToolCrate family look (docs/family-look.md), shared with Stagira,
 * Telos and Kairos. Black on white, true black at night; `accent` is ink, `accentSoft` the soft
 * tint. (The class keeps the name Paper to keep the change small.)
 */
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
    page = Color(0xFFFFFFFF),
    card = Color(0xFFF7F7F6),
    ink = Color(0xFF111111),
    muted = Color(0xFF6E6E6E),
    hairline = Color(0xFFEAEAE8),
    accent = Color(0xFF111111),
    accentSoft = Color(0xFFECECEA),
    isDark = false,
)

val PaperDark = Paper(
    page = Color(0xFF000000),
    card = Color(0xFF0E0E0E),
    ink = Color(0xFFFFFFFF),
    muted = Color(0xFF9A9A9A),
    hairline = Color(0xFF1C1C1C),
    accent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFF262626),
    isDark = true,
)

val LocalPaper = staticCompositionLocalOf { PaperLight }
