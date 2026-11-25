package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AtelierLightColorScheme = lightColorScheme(
  primary = ObsidianBlack,
  onPrimary = PureIvory,
  primaryContainer = WarmBeige,
  onPrimaryContainer = ObsidianBlack,
  secondary = DeepAntiqueGold,
  onSecondary = PureIvory,
  secondaryContainer = SoftGoldTint,
  onSecondaryContainer = WarmCharcoal,
  tertiary = ChampagneGold,
  onTertiary = ObsidianBlack,
  background = AlabasterWhite,
  onBackground = ObsidianBlack,
  surface = PureIvory,
  onSurface = ObsidianBlack,
  surfaceVariant = WarmBeige,
  onSurfaceVariant = MutedTaupe,
  surfaceContainerLowest = PureIvory,
  surfaceContainerLow = AlabasterWhite,
  surfaceContainer = WarmBeige,
  surfaceContainerHigh = SandstoneBeige,
  outline = SubtleOutline,
  outlineVariant = SandstoneBeige,
  error = CrimsonAlert,
  onError = PureIvory,
  errorContainer = CrimsonSurface,
  onErrorContainer = CrimsonAlert,
)

private val AtelierDarkColorScheme = darkColorScheme(
  primary = ChampagneGold,
  onPrimary = ObsidianBlack,
  primaryContainer = WarmCharcoal,
  onPrimaryContainer = SoftGoldTint,
  secondary = SandstoneBeige,
  onSecondary = ObsidianBlack,
  secondaryContainer = WarmCharcoal,
  onSecondaryContainer = SandstoneBeige,
  background = ObsidianBlack,
  onBackground = AlabasterWhite,
  surface = WarmCharcoal,
  onSurface = AlabasterWhite,
  surfaceVariant = WarmCharcoal,
  onSurfaceVariant = SandstoneBeige,
  outline = MutedTaupe,
  error = CrimsonAlert,
  onError = PureIvory,
)

val AtelierShapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(14.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun AtelierOpticTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) AtelierDarkColorScheme else AtelierLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = AtelierTypography,
    shapes = AtelierShapes,
    content = content,
  )
}
