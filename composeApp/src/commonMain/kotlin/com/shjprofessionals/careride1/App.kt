package com.shjprofessionals.careride1

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.feature.onboarding.RoleSelectionScreen
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
