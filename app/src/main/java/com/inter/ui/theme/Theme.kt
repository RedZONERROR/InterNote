package com.inter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = HighDensityDarkPrimary,
    onPrimary = HighDensityDarkOnPrimary,
    primaryContainer = HighDensityDarkPrimaryContainer,
    onPrimaryContainer = HighDensityDarkOnPrimaryContainer,
    secondary = HighDensityDarkPrimary,
    onSecondary = HighDensityDarkOnPrimary,
    secondaryContainer = HighDensityDarkSurfaceVariant,
    onSecondaryContainer = HighDensityDarkOnSurfaceVariant,
    background = HighDensityDarkBackground,
    onBackground = HighDensityDarkOnBackground,
    surface = HighDensityDarkSurface,
    onSurface = HighDensityDarkOnSurface,
    surfaceVariant = HighDensityDarkSurfaceVariant,
    onSurfaceVariant = HighDensityDarkOnSurfaceVariant,
    outline = HighDensityDarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondary = HighDensityPrimary,
    onSecondary = HighDensityOnPrimary,
    secondaryContainer = HighDensityOutline,
    onSecondaryContainer = HighDensityOnSurfaceVariant,
    background = HighDensityBackground,
    onBackground = HighDensityOnBackground,
    surface = HighDensitySurface,
    onSurface = HighDensityOnSurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityOnSurfaceVariant,
    outline = HighDensityOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to preserve the rich custom High Density brand color theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
