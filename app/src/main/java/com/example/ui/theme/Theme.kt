package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkGold,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3B2A0F),
    onPrimaryContainer = Color(0xFFFFE7BA),
    secondary = DarkPrimary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0F3B59),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = DarkEmerald,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = Color(0xFFF87171),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = BrandNavy,
    secondary = BrandGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3D6),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = TextSecondaryLight,
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to retain signature AET fintech branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
