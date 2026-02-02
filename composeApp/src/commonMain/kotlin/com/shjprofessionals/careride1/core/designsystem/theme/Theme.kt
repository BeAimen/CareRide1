package com.shjprofessionals.careride1.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme = lightColorScheme(
    primary = CareRideLightColors.Primary,
    onPrimary = CareRideLightColors.OnPrimary,
    primaryContainer = CareRideLightColors.PrimaryContainer,
    onPrimaryContainer = CareRideLightColors.OnPrimaryContainer,
    secondary = CareRideLightColors.Secondary,
    onSecondary = CareRideLightColors.OnSecondary,
    secondaryContainer = CareRideLightColors.SecondaryContainer,
    onSecondaryContainer = CareRideLightColors.OnSecondaryContainer,
    surface = CareRideLightColors.Surface,
    onSurface = CareRideLightColors.OnSurface,
    surfaceVariant = CareRideLightColors.SurfaceVariant,
    onSurfaceVariant = CareRideLightColors.OnSurfaceVariant,
    background = CareRideLightColors.Background,
    onBackground = CareRideLightColors.OnBackground,
    error = CareRideLightColors.Error,
    onError = CareRideLightColors.OnError,
    outline = CareRideLightColors.Outline,
    outlineVariant = CareRideLightColors.OutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = CareRideDarkColors.Primary,
    onPrimary = CareRideDarkColors.OnPrimary,
    primaryContainer = CareRideDarkColors.PrimaryContainer,
    onPrimaryContainer = CareRideDarkColors.OnPrimaryContainer,
    secondary = CareRideDarkColors.Secondary,
    onSecondary = CareRideDarkColors.OnSecondary,
    secondaryContainer = CareRideDarkColors.SecondaryContainer,
    onSecondaryContainer = CareRideDarkColors.OnSecondaryContainer,
    surface = CareRideDarkColors.Surface,
    onSurface = CareRideDarkColors.OnSurface,
    surfaceVariant = CareRideDarkColors.SurfaceVariant,
    onSurfaceVariant = CareRideDarkColors.OnSurfaceVariant,
    background = CareRideDarkColors.Background,
    onBackground = CareRideDarkColors.OnBackground,
    error = CareRideDarkColors.Error,
    onError = CareRideDarkColors.OnError,
    outline = CareRideDarkColors.Outline,
    outlineVariant = CareRideDarkColors.OutlineVariant
)

@Composable
fun CareRideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalCareRideSpacing provides CareRideSpacing(),
        LocalCareRideRadii provides CareRideRadii(),
        LocalCareRideElevation provides CareRideElevation()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CareRideTypography,
            content = content
        )
    }
}

// Extension properties for easy access
object CareRideTheme {
    val spacing: CareRideSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalCareRideSpacing.current

    val radii: CareRideRadii
        @Composable
        @ReadOnlyComposable
        get() = LocalCareRideRadii.current

    val elevation: CareRideElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalCareRideElevation.current
}
