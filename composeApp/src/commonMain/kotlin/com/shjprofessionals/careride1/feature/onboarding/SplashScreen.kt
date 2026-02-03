package com.shjprofessionals.careride1.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import kotlinx.coroutines.delay

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Navigate after splash duration
        LaunchedEffect(Unit) {
            delay(2000) // 2 second splash
            navigator.replace(RoleSelectionScreen())
        }

        SplashContent()
    }
}

@Composable
private fun SplashContent() {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Pulsing animation for the icon
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Fade in animation
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "fade"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "CareRide is loading, please wait"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .padding(CareRideTheme.spacing.xl)
        ) {
            // App Icon
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

            // App Name
            Text(
                text = "CareRide",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Tagline
            Text(
                text = "Healthcare at your fingertips",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        }

        // Version at bottom
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CareRideTheme.spacing.xl)
        )
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)