package com.careride

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.feature.onboarding.RoleSelectionScreen
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        CareRideTheme {
            Navigator(RoleSelectionScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}