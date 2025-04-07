package com.example.taskmaster.ui.theme

import android.app.Activity
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
    primary = Color(0xFF8BC34A),   // Verde lima
    secondary = Color(0xFF689F38), // Verde oliva
    tertiary = Color(0xFF33691E),  // Verde bosque oscuro
    background = Color(0xFF1B5E20),  // Fondo verde muy oscuro
    surface = Color(0xFF2E7D32),   // Superficie verde oscura
    onPrimary = Color.Black,      // Texto sobre primario (contraste)
    onSecondary = Color.White,    // Texto sobre secundario
    onTertiary = Color.White,     // Texto sobre terciario
    onBackground = Color.White,    // Texto sobre el fondo
    onSurface = Color.White       // Texto sobre la superficie
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xAEE7FA9F),   // Verde claro
    secondary = Color(0xFF81C784), // Verde pastel
    tertiary = Color(0xFF4CAF50),  // Verde esmeralda
    background = Color(0xFFE8F5E9),  // Fondo verde muy claro
    surface = Color(0xFFF1F8E9),   // Superficie verde clara
    onPrimary = Color.Black,      // Texto sobre primario (contraste)
    onSecondary = Color.Black,    // Texto sobre secundario
    onTertiary = Color.White,     // Texto sobre terciario
    onBackground = Color.Black,    // Texto sobre el fondo
    onSurface = Color.Black       // Texto sobre la superficie
)

@Composable
fun TaskMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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