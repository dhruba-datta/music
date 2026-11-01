@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package echo.music.iad1tya.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import echo.music.iad1tya.R

@OptIn(ExperimentalTextApi::class)
val ManropeFontFamily =
  FontFamily(
    Font(R.font.manrope, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.manrope, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
  )

val SansFlexFontFamily =
  FontFamily(
    Font(
      resId = R.font.sans_flex,
      weight = FontWeight.Normal,
      variationSettings =
        FontVariation.Settings(
          FontVariation.weight(400),
          FontVariation.width(100f),
          FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
      resId = R.font.sans_flex,
      weight = FontWeight.Medium,
      variationSettings =
        FontVariation.Settings(
          FontVariation.weight(500),
          FontVariation.width(100f),
          FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
      resId = R.font.sans_flex,
      weight = FontWeight.Bold,
      variationSettings =
        FontVariation.Settings(
          FontVariation.weight(700),
          FontVariation.width(100f),
          FontVariation.Setting("ROND", 100f)
        )
    )
  )

@OptIn(ExperimentalTextApi::class)
val OutfitFontFamily =
  FontFamily(
    Font(
      resId = R.font.outfit,
      weight = FontWeight.Normal,
      variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
      resId = R.font.outfit,
      weight = FontWeight.Medium,
      variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
      resId = R.font.outfit,
      weight = FontWeight.Bold,
      variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
  )

@OptIn(ExperimentalTextApi::class)
val PlusJakartaSansFontFamily =
  FontFamily(
    Font(
      resId = R.font.plus_jakarta_sans,
      weight = FontWeight.Normal,
      variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
      resId = R.font.plus_jakarta_sans,
      weight = FontWeight.Medium,
      variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
      resId = R.font.plus_jakarta_sans,
      weight = FontWeight.Bold,
      variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
  )

@OptIn(ExperimentalTextApi::class)
fun getTypography(brandFont: FontFamily): Typography =
  Typography(
    displayLarge =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
      ),
    displayMedium =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
      ),
    displaySmall =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
      ),
    headlineLarge =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
      ),
    headlineMedium =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
      ),
    headlineSmall =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
      ),
    titleLarge =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
      ),
    titleMedium =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
      ),
    titleSmall =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
      ),
    bodyLarge =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
      ),
    bodyMedium =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
      ),
    bodySmall =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
      ),
    labelLarge =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
      ),
    labelMedium =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
      ),
    labelSmall =
      TextStyle(
        fontFamily = brandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
      )
  )
