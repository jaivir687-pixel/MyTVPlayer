package com.example.mytvplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MyTVPlayerTheme(
    isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isInDarkTheme) {
        darkColorScheme(
            primary = AccentBlue,
            secondary = SidebarDark,
            tertiary = RatingYellow,
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF0A0A0A),
            onPrimary = TextPrimary,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
    } else {
        lightColorScheme(
            primary = AccentBlue,
            secondary = SidebarDark,
            tertiary = RatingYellow,
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF0A0A0A),
            onPrimary = TextPrimary,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}