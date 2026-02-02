package com.shjprofessionals.careride1

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.shjprofessionals.careride1.core.designsystem.components.AppErrorSnackbarHost
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.feature.onboarding.RoleSelectionScreen
import org.koin.compose.KoinContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App() {
    KoinContext {
        CareRideTheme {
            val snackbarHostState = remember { SnackbarHostState() }

            Box(modifier = Modifier.fillMaxSize()) {
                Navigator(RoleSelectionScreen()) { navigator ->
                    SlideTransition(navigator)
                }

                AppErrorSnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
