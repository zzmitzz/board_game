package com.boardgame.deepdeck.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.boardgame.deepdeck.R

/** Google Play Services downloadable-font provider (certs in res/values/font_certs.xml). */
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val frauncesFont = GoogleFont("Fraunces")
private val jakartaFont = GoogleFont("Plus Jakarta Sans")

/**
 * Fraunces (editorial serif) for question cards and hero titles.
 * Falls back to the device serif if the download is unavailable.
 */
val Fraunces = FontFamily(
    Font(googleFont = frauncesFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = frauncesFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = frauncesFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = frauncesFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(DeviceFontFamilyName("serif"), weight = FontWeight.Normal),
    Font(DeviceFontFamilyName("serif"), weight = FontWeight.SemiBold),
    Font(DeviceFontFamilyName("serif"), weight = FontWeight.Bold),
)

/**
 * Plus Jakarta Sans: real weights via downloadable fonts, bundled file
 * (res/font/plus_jakarta_sans.ttf) as offline fallback.
 */
val Jakarta = FontFamily(
    Font(googleFont = jakartaFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = jakartaFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = jakartaFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = jakartaFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = jakartaFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold),
)

/** Type roles from plan §2.2 that Material's scale doesn't name directly. */
@Immutable
data class DeepTalkTypography(
    /** Question card: Fraunces 30/38 SemiBold. */
    val display: TextStyle = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.2).sp
    ),
    /** Hero title: Fraunces 36/42 SemiBold. */
    val hero: TextStyle = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.4).sp
    ),
    /** Smaller serif for compact cards (daily card, sample cards). */
    val displaySmall: TextStyle = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 30.sp
    ),
    /** Headline: Jakarta 22/28 Bold. */
    val headline: TextStyle = TextStyle(
        fontFamily = Jakarta, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    /** Title: Jakarta 17/24 SemiBold. */
    val title: TextStyle = TextStyle(
        fontFamily = Jakarta, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp
    ),
    /** Body: Jakarta 15/22 Regular. */
    val body: TextStyle = TextStyle(
        fontFamily = Jakarta, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    ),
    /** Label: Jakarta 13/16 SemiBold, +0.4 tracking. */
    val label: TextStyle = TextStyle(
        fontFamily = Jakarta, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    /** Overline / badge: Jakarta 11/14 Bold, UPPERCASE (caller uppercases), +1.2 tracking. */
    val overline: TextStyle = TextStyle(
        fontFamily = Jakarta, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.2.sp
    ),
)

private val base = DeepTalkTypography()

/** Material typography mapped onto the DeepTalk scale so stock M3 components match. */
val Typography = Typography(
    displayLarge = base.hero.copy(fontSize = 44.sp, lineHeight = 50.sp),
    displayMedium = base.hero,
    displaySmall = base.display,
    headlineLarge = base.headline.copy(fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = base.headline.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = base.headline,
    titleLarge = base.title.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = base.title,
    titleSmall = base.title.copy(fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = base.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = base.body,
    bodySmall = base.body.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = base.label.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    labelMedium = base.label,
    labelSmall = base.overline.copy(letterSpacing = 0.08.em),
)
