package cc.toolcrate.lyceum.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cc.toolcrate.lyceum.R

/** EB Garamond: headings, dates, prompts and quotations. */
val Garamond = FontFamily(
    Font(R.font.eb_garamond_regular, FontWeight.Normal),
    Font(R.font.eb_garamond_medium, FontWeight.Medium),
    Font(R.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Inter: body text, labels and controls. */
val Inter = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
)

private fun serif(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontFamily = Garamond, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp)

private fun sans(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Double = 0.0) =
    TextStyle(fontFamily = Inter, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.sp)

val LyceumTypography = Typography(
    displayLarge = serif(52, 60),
    displayMedium = serif(42, 50),
    displaySmall = serif(34, 42),
    headlineLarge = serif(32, 40),
    headlineMedium = serif(28, 36),
    headlineSmall = serif(24, 32),
    titleLarge = serif(22, 28, FontWeight.Medium),
    titleMedium = sans(16, 24, FontWeight.Medium),
    titleSmall = sans(14, 20, FontWeight.Medium),
    bodyLarge = sans(16, 26),
    bodyMedium = sans(14, 22),
    bodySmall = sans(12, 18),
    labelLarge = sans(14, 20, FontWeight.Medium),
    labelMedium = sans(12, 16, FontWeight.Medium, 0.2),
    labelSmall = sans(11, 16, FontWeight.Medium, 0.4),
)

/** A quotation or prompt: Garamond italic. */
val QuoteStyle = TextStyle(fontFamily = Garamond, fontStyle = FontStyle.Italic, fontSize = 21.sp, lineHeight = 30.sp)
