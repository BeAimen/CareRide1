package com.shjprofessionals.careride1.feature.auth

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.shjprofessionals.careride1.core.navigation.CareRideTransition
import com.shjprofessionals.careride1.domain.model.AuthState
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import com.shjprofessionals.careride1.feature.onboarding.DoctorMainScreen
import com.shjprofessionals.careride1.feature.onboarding.PatientMainScreen
import org.koin.compose.koinInject

/**
 * Root navigator that handles auth state and routes accordingly.
 */
@Composable
fun AuthNavigator() {
    val authRepository: AuthRepository = koinInject()
    val authState by authRepository.observeAuthState().collectAsState(initial = AuthState.SignedOut)

    // Determine the appropriate screen based on auth state
    val targetScreen: Screen = remember(authState) {
        when (authState) {
            is AuthState.SignedOut -> WelcomeAuthScreen()
            is AuthState.Loading -> WelcomeAuthScreen() // Show welcome while loading
            is AuthState.SignedInNoRole -> AuthRoleSelectionScreen()
            is AuthState.SignedInPatient -> PatientMainScreen()
            is AuthState.SignedInDoctor -> DoctorMainScreen()
        }
    }

    Navigator(targetScreen) { navigator ->
        // When auth state changes, replace the current screen
        LaunchedEffect(authState) {
            val currentScreen = navigator.lastItem
            val shouldReplace = when (authState) {
                is AuthState.SignedOut -> currentScreen !is WelcomeAuthScreen
                is AuthState.Loading -> false
                is AuthState.SignedInNoRole -> currentScreen !is AuthRoleSelectionScreen
                is AuthState.SignedInPatient -> currentScreen !is PatientMainScreen
                is AuthState.SignedInDoctor -> currentScreen !is DoctorMainScreen
            }

            if (shouldReplace) {
                navigator.replaceAll(targetScreen)
            }
        }

        CareRideTransition(navigator)
    }
}