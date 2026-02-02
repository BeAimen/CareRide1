package com.shjprofessionals.careride1.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CareRideSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

@Immutable
data class CareRideRadii(
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 12.dp,
    val xl: Dp = 16.dp,
    val full: Dp = 9999.dp
)

@Immutable
data class CareRideElevation(
    val none: Dp = 0.dp,
    val sm: Dp = 2.dp,
    val md: Dp = 4.dp,
    val lg: Dp = 8.dp
)

val LocalCareRideSpacing = staticCompositionLocalOf { CareRideSpacing() }
val LocalCareRideRadii = staticCompositionLocalOf { CareRideRadii() }
val LocalCareRideElevation = staticCompositionLocalOf { CareRideElevation() }
