package com.example.chessgame.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.chessgame.ui.theme.backgroundDark
import com.example.chessgame.ui.theme.backgroundDarkHighContrast
import com.example.chessgame.ui.theme.backgroundDarkMediumContrast
import com.example.chessgame.ui.theme.backgroundLight
import com.example.chessgame.ui.theme.backgroundLightHighContrast
import com.example.chessgame.ui.theme.backgroundLightMediumContrast
import com.example.chessgame.ui.theme.errorContainerDark
import com.example.chessgame.ui.theme.errorContainerDarkHighContrast
import com.example.chessgame.ui.theme.errorContainerDarkMediumContrast
import com.example.chessgame.ui.theme.errorContainerLight
import com.example.chessgame.ui.theme.errorContainerLightHighContrast
import com.example.chessgame.ui.theme.errorContainerLightMediumContrast
import com.example.chessgame.ui.theme.errorDark
import com.example.chessgame.ui.theme.errorDarkHighContrast
import com.example.chessgame.ui.theme.errorDarkMediumContrast
import com.example.chessgame.ui.theme.errorLight
import com.example.chessgame.ui.theme.errorLightHighContrast
import com.example.chessgame.ui.theme.errorLightMediumContrast
import com.example.chessgame.ui.theme.inverseOnSurfaceDark
import com.example.chessgame.ui.theme.inverseOnSurfaceDarkHighContrast
import com.example.chessgame.ui.theme.inverseOnSurfaceDarkMediumContrast
import com.example.chessgame.ui.theme.inverseOnSurfaceLight
import com.example.chessgame.ui.theme.inverseOnSurfaceLightHighContrast
import com.example.chessgame.ui.theme.inverseOnSurfaceLightMediumContrast
import com.example.chessgame.ui.theme.inversePrimaryDark
import com.example.chessgame.ui.theme.inversePrimaryDarkHighContrast
import com.example.chessgame.ui.theme.inversePrimaryDarkMediumContrast
import com.example.chessgame.ui.theme.inversePrimaryLight
import com.example.chessgame.ui.theme.inversePrimaryLightHighContrast
import com.example.chessgame.ui.theme.inversePrimaryLightMediumContrast
import com.example.chessgame.ui.theme.inverseSurfaceDark
import com.example.chessgame.ui.theme.inverseSurfaceDarkHighContrast
import com.example.chessgame.ui.theme.inverseSurfaceDarkMediumContrast
import com.example.chessgame.ui.theme.inverseSurfaceLight
import com.example.chessgame.ui.theme.inverseSurfaceLightHighContrast
import com.example.chessgame.ui.theme.inverseSurfaceLightMediumContrast
import com.example.chessgame.ui.theme.onBackgroundDark
import com.example.chessgame.ui.theme.onBackgroundDarkHighContrast
import com.example.chessgame.ui.theme.onBackgroundDarkMediumContrast
import com.example.chessgame.ui.theme.onBackgroundLight
import com.example.chessgame.ui.theme.onBackgroundLightHighContrast
import com.example.chessgame.ui.theme.onBackgroundLightMediumContrast
import com.example.chessgame.ui.theme.onErrorContainerDark
import com.example.chessgame.ui.theme.onErrorContainerDarkHighContrast
import com.example.chessgame.ui.theme.onErrorContainerDarkMediumContrast
import com.example.chessgame.ui.theme.onErrorContainerLight
import com.example.chessgame.ui.theme.onErrorContainerLightHighContrast
import com.example.chessgame.ui.theme.onErrorContainerLightMediumContrast
import com.example.chessgame.ui.theme.onErrorDark
import com.example.chessgame.ui.theme.onErrorDarkHighContrast
import com.example.chessgame.ui.theme.onErrorDarkMediumContrast
import com.example.chessgame.ui.theme.onErrorLight
import com.example.chessgame.ui.theme.onErrorLightHighContrast
import com.example.chessgame.ui.theme.onErrorLightMediumContrast
import com.example.chessgame.ui.theme.onPrimaryContainerDark
import com.example.chessgame.ui.theme.onPrimaryContainerDarkHighContrast
import com.example.chessgame.ui.theme.onPrimaryContainerDarkMediumContrast
import com.example.chessgame.ui.theme.onPrimaryContainerLight
import com.example.chessgame.ui.theme.onPrimaryContainerLightHighContrast
import com.example.chessgame.ui.theme.onPrimaryContainerLightMediumContrast
import com.example.chessgame.ui.theme.onPrimaryDark
import com.example.chessgame.ui.theme.onPrimaryDarkHighContrast
import com.example.chessgame.ui.theme.onPrimaryDarkMediumContrast
import com.example.chessgame.ui.theme.onPrimaryLight
import com.example.chessgame.ui.theme.onPrimaryLightHighContrast
import com.example.chessgame.ui.theme.onPrimaryLightMediumContrast
import com.example.chessgame.ui.theme.onSecondaryContainerDark
import com.example.chessgame.ui.theme.onSecondaryContainerDarkHighContrast
import com.example.chessgame.ui.theme.onSecondaryContainerDarkMediumContrast
import com.example.chessgame.ui.theme.onSecondaryContainerLight
import com.example.chessgame.ui.theme.onSecondaryContainerLightHighContrast
import com.example.chessgame.ui.theme.onSecondaryContainerLightMediumContrast
import com.example.chessgame.ui.theme.onSecondaryDark
import com.example.chessgame.ui.theme.onSecondaryDarkHighContrast
import com.example.chessgame.ui.theme.onSecondaryDarkMediumContrast
import com.example.chessgame.ui.theme.onSecondaryLight
import com.example.chessgame.ui.theme.onSecondaryLightHighContrast
import com.example.chessgame.ui.theme.onSecondaryLightMediumContrast
import com.example.chessgame.ui.theme.onSurfaceDark
import com.example.chessgame.ui.theme.onSurfaceDarkHighContrast
import com.example.chessgame.ui.theme.onSurfaceDarkMediumContrast
import com.example.chessgame.ui.theme.onSurfaceLight
import com.example.chessgame.ui.theme.onSurfaceLightHighContrast
import com.example.chessgame.ui.theme.onSurfaceLightMediumContrast
import com.example.chessgame.ui.theme.onSurfaceVariantDark
import com.example.chessgame.ui.theme.onSurfaceVariantDarkHighContrast
import com.example.chessgame.ui.theme.onSurfaceVariantDarkMediumContrast
import com.example.chessgame.ui.theme.onSurfaceVariantLight
import com.example.chessgame.ui.theme.onSurfaceVariantLightHighContrast
import com.example.chessgame.ui.theme.onSurfaceVariantLightMediumContrast
import com.example.chessgame.ui.theme.onTertiaryContainerDark
import com.example.chessgame.ui.theme.onTertiaryContainerDarkHighContrast
import com.example.chessgame.ui.theme.onTertiaryContainerDarkMediumContrast
import com.example.chessgame.ui.theme.onTertiaryContainerLight
import com.example.chessgame.ui.theme.onTertiaryContainerLightHighContrast
import com.example.chessgame.ui.theme.onTertiaryContainerLightMediumContrast
import com.example.chessgame.ui.theme.onTertiaryDark
import com.example.chessgame.ui.theme.onTertiaryDarkHighContrast
import com.example.chessgame.ui.theme.onTertiaryDarkMediumContrast
import com.example.chessgame.ui.theme.onTertiaryLight
import com.example.chessgame.ui.theme.onTertiaryLightHighContrast
import com.example.chessgame.ui.theme.onTertiaryLightMediumContrast
import com.example.chessgame.ui.theme.outlineDark
import com.example.chessgame.ui.theme.outlineDarkHighContrast
import com.example.chessgame.ui.theme.outlineDarkMediumContrast
import com.example.chessgame.ui.theme.outlineLight
import com.example.chessgame.ui.theme.outlineLightHighContrast
import com.example.chessgame.ui.theme.outlineLightMediumContrast
import com.example.chessgame.ui.theme.outlineVariantDark
import com.example.chessgame.ui.theme.outlineVariantDarkHighContrast
import com.example.chessgame.ui.theme.outlineVariantDarkMediumContrast
import com.example.chessgame.ui.theme.outlineVariantLight
import com.example.chessgame.ui.theme.outlineVariantLightHighContrast
import com.example.chessgame.ui.theme.outlineVariantLightMediumContrast
import com.example.chessgame.ui.theme.primaryContainerDark
import com.example.chessgame.ui.theme.primaryContainerDarkHighContrast
import com.example.chessgame.ui.theme.primaryContainerDarkMediumContrast
import com.example.chessgame.ui.theme.primaryContainerLight
import com.example.chessgame.ui.theme.primaryContainerLightHighContrast
import com.example.chessgame.ui.theme.primaryContainerLightMediumContrast
import com.example.chessgame.ui.theme.primaryDark
import com.example.chessgame.ui.theme.primaryDarkHighContrast
import com.example.chessgame.ui.theme.primaryDarkMediumContrast
import com.example.chessgame.ui.theme.primaryLight
import com.example.chessgame.ui.theme.primaryLightHighContrast
import com.example.chessgame.ui.theme.primaryLightMediumContrast
import com.example.chessgame.ui.theme.scrimDark
import com.example.chessgame.ui.theme.scrimDarkHighContrast
import com.example.chessgame.ui.theme.scrimDarkMediumContrast
import com.example.chessgame.ui.theme.scrimLight
import com.example.chessgame.ui.theme.scrimLightHighContrast
import com.example.chessgame.ui.theme.scrimLightMediumContrast
import com.example.chessgame.ui.theme.secondaryContainerDark
import com.example.chessgame.ui.theme.secondaryContainerDarkHighContrast
import com.example.chessgame.ui.theme.secondaryContainerDarkMediumContrast
import com.example.chessgame.ui.theme.secondaryContainerLight
import com.example.chessgame.ui.theme.secondaryContainerLightHighContrast
import com.example.chessgame.ui.theme.secondaryContainerLightMediumContrast
import com.example.chessgame.ui.theme.secondaryDark
import com.example.chessgame.ui.theme.secondaryDarkHighContrast
import com.example.chessgame.ui.theme.secondaryDarkMediumContrast
import com.example.chessgame.ui.theme.secondaryLight
import com.example.chessgame.ui.theme.secondaryLightHighContrast
import com.example.chessgame.ui.theme.secondaryLightMediumContrast
import com.example.chessgame.ui.theme.surfaceDark
import com.example.chessgame.ui.theme.surfaceDarkHighContrast
import com.example.chessgame.ui.theme.surfaceDarkMediumContrast
import com.example.chessgame.ui.theme.surfaceLight
import com.example.chessgame.ui.theme.surfaceLightHighContrast
import com.example.chessgame.ui.theme.surfaceLightMediumContrast
import com.example.chessgame.ui.theme.surfaceVariantDark
import com.example.chessgame.ui.theme.surfaceVariantDarkHighContrast
import com.example.chessgame.ui.theme.surfaceVariantDarkMediumContrast
import com.example.chessgame.ui.theme.surfaceVariantLight
import com.example.chessgame.ui.theme.surfaceVariantLightHighContrast
import com.example.chessgame.ui.theme.surfaceVariantLightMediumContrast
import com.example.chessgame.ui.theme.tertiaryContainerDark
import com.example.chessgame.ui.theme.tertiaryContainerDarkHighContrast
import com.example.chessgame.ui.theme.tertiaryContainerDarkMediumContrast
import com.example.chessgame.ui.theme.tertiaryContainerLight
import com.example.chessgame.ui.theme.tertiaryContainerLightHighContrast
import com.example.chessgame.ui.theme.tertiaryContainerLightMediumContrast
import com.example.chessgame.ui.theme.tertiaryDark
import com.example.chessgame.ui.theme.tertiaryDarkHighContrast
import com.example.chessgame.ui.theme.tertiaryDarkMediumContrast
import com.example.chessgame.ui.theme.tertiaryLight
import com.example.chessgame.ui.theme.tertiaryLightHighContrast
import com.example.chessgame.ui.theme.tertiaryLightMediumContrast

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    //surfaceDim = surfaceDimLight,
    //surfaceBright = surfaceBrightLight,
    //surfaceContainerLowest = surfaceContainerLowestLight,
    //surfaceContainerLow = surfaceContainerLowLight,
    //surfaceContainer = surfaceContainerLight,
    //surfaceContainerHigh = surfaceContainerHighLight,
    //surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    /*surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,*/
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    /*surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,*/
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    /*surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,*/
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
/*    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,*/
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
/*    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,*/
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun ChessGameTheme(
    //darkTheme: Boolean = isSystemInDarkTheme(),
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      
      darkTheme -> darkScheme
      else -> lightScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    //typography = AppTypography,
    content = content
  )
}

