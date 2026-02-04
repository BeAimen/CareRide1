package com.shjprofessionals.careride1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shjprofessionals.careride1.core.designsystem.components.AppErrorSnackbarHost
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.feature.auth.AuthNavigator
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        CareRideTheme {
            val snackbarHostState = remember { SnackbarHostState() }

            Box(modifier = Modifier.fillMaxSize()) {
                AuthNavigator()

                AppErrorSnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}