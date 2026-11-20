package cc.toolcrate.lyceum.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cc.toolcrate.lyceum.R

/** EB Garamond: display moments only (the name, dates, prompts and quotations). */
val Garamond = FontFamily(
    Font(R.font.eb_garamond_regular, FontWeight.Normal),
    Font(R.font.eb_garamond_medium, FontWeight.Medium),
    Font(R.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic),
)

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight) = Font(
    R.font.inter,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Inter at the family's weights, from its variable axis (as in Stagira). */
val Inter = FontFamily(inter(FontWeight.Light), inter(FontWeight.Normal), inter(FontWeight.Medium), inter(FontWeight.SemiBold))

/** The family type scale (docs/family-look.md): Garamond for display moments, Inter for the rest. */
val LyceumTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = Garamond, fontWeight = FontWeight.Normal, letterSpacing = (-1).sp, fontFeatureSettings = "tnum"),
        displayMedium = base.displayMedium.copy(fontFamily = Garamond, fontWeight = FontWeight.Normal, letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum"),
        displaySmall = base.displaySmall.copy(fontFamily = Garamond, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, fontFeatureSettings = "tnum"),
        headlineLarge = base.headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Light, letterSpacing = (-0.6).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Light, letterSpacing = (-0.4).sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Normal, letterSpacing = (-0.3).sp),
        titleLarge = base.titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
        titleMedium = base.titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp),
        titleSmall = base.titleSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = Inter, letterSpacing = 0.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = Inter, letterSpacing = 0.sp),
        bodySmall = base.bodySmall.copy(fontFamily = Inter, letterSpacing = 0.1.sp),
        labelLarge = base.labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp),
        labelSmall = base.labelSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp),
    )
}

/** A screen's title, in Garamond, as Stagira sets its top bars (docs/family-look.md). */
val ScreenTitle = TextStyle(fontFamily = Garamond, fontSize = 30.sp, lineHeight = 34.sp)

/** A quotation or prompt: Garamond italic. */
val QuoteStyle = TextStyle(fontFamily = Garamond, fontStyle = FontStyle.Italic, fontSize = 21.sp, lineHeight = 30.sp)
