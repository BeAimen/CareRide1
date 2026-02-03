package com.shjprofessionals.careride1.feature.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for splash screen.
 * Currently handles timing, but can be extended for:
 * - Auth state checking
 * - Initial data loading
 * - Deep link handling
 */

sealed class SplashDestination {
    data object RoleSelection : SplashDestination()
    data object PatientHome : SplashDestination() // Future: if already logged in as patient
    data object DoctorHome : SplashDestination() // Future: if already logged in as doctor
}

data class SplashState(
    val isLoading: Boolean = true,
    val destination: SplashDestination? = null,
    val minimumTimeElapsed: Boolean = false
)

class SplashViewModel : ScreenModel {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    private val minimumSplashDuration = 1500L // 1.5 seconds minimum

    init {
        initialize()
    }

    private fun initialize() {
        screenModelScope.launch {
            // Start minimum time countdown
            launch {
                delay(minimumSplashDuration)
                _state.value = _state.value.copy(minimumTimeElapsed = true)
                checkReadyToNavigate()
            }

            // Perform any initialization tasks here
            // For now, we just simulate initialization
            performInitialization()
        }
    }

    private suspend fun performInitialization() {
        // Future implementations:
        // - Check if user is logged in
        // - Load cached user preferences
        // - Initialize analytics
        // - Handle deep links

        // Simulate initialization work
        delay(500)

        // For now, always go to role selection
        _state.value = _state.value.copy(
            isLoading = false,
            destination = SplashDestination.RoleSelection
        )
        checkReadyToNavigate()
    }

    private fun checkReadyToNavigate() {
        val currentState = _state.value
        if (currentState.minimumTimeElapsed && currentState.destination != null) {
            // Ready to navigate - state already has destination
        }
    }

    fun isReadyToNavigate(): Boolean {
        val currentState = _state.value
        return currentState.minimumTimeElapsed && currentState.destination != null
    }
}