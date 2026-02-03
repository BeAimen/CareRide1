package com.shjprofessionals.careride1.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.shjprofessionals.careride1.feature.onboarding.SplashScreen

/**
 * Custom transition that uses fade for splash screen,
 * slide for everything else.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CareRideTransition(navigator: Navigator) {
    AnimatedContent(
        targetState = navigator.lastItem,
        transitionSpec = {
            val isSplashTransition = initialState is SplashScreen || targetState is SplashScreen

            if (isSplashTransition) {
                // Fade transition for splash
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            } else {
                // Slide transition for other screens
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                )
            }
        },
        label = "screen_transition"
    ) { screen ->
        navigator.saveableState("transition", screen) {
            screen.Content()
        }
    }
}