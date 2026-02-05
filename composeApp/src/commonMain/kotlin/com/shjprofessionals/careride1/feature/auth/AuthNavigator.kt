package com.shjprofessionals.careride1.feature.auth

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.shjprofessionals.careride1.core.navigation.CareRideTransition
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.AuthState
import com.shjprofessionals.careride1.domain.model.DoctorProfile
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import com.shjprofessionals.careride1.feature.onboarding.DoctorProfileSetupScreen
import com.shjprofessionals.careride1.feature.onboarding.DoctorMainScreen
import com.shjprofessionals.careride1.feature.onboarding.PatientProfileSetupScreen
import com.shjprofessionals.careride1.feature.onboarding.PatientMainScreen
import org.koin.compose.koinInject

/**
 * Root navigator that handles auth state and routes accordingly.
 */
@Composable
fun AuthNavigator() {
    val authRepository: AuthRepository = koinInject()
    val authState by authRepository.observeAuthState().collectAsState(initial = AuthState.SignedOut)

    // Observe editable profiles (used as onboarding gates)
    val patientProfile by FakeBackend.patientProfileStore.profile.collectAsState(initial = null)
    val doctorProfile by FakeBackend.doctorProfileStore.profileFlow.collectAsState(initial = null)

    val destination: AuthDestination = remember(authState, patientProfile, doctorProfile) {
        when (authState) {
            is AuthState.SignedOut, is AuthState.Loading -> AuthDestination.Welcome
            is AuthState.SignedInNoRole -> AuthDestination.RoleSelection
            is AuthState.SignedInPatient -> {
                val p = patientProfile
                val needsSetup = p == null ||
                        !p.address.isComplete ||
                        !p.hasMedicalInfo ||
                        !p.hasInsurance ||
                        !p.hasEmergencyContact
                if (needsSetup) AuthDestination.PatientSetup else AuthDestination.PatientMain
            }
            is AuthState.SignedInDoctor -> {
                val d = doctorProfile
                val needsSetup = d == null || !d.isOnboardingComplete()
                if (needsSetup) AuthDestination.DoctorSetup else AuthDestination.DoctorMain
            }
        }
    }

    // Determine the appropriate screen based on auth state
    val targetScreen: Screen = remember(destination) {
        when (destination) {
            AuthDestination.Welcome -> WelcomeAuthScreen()
            AuthDestination.RoleSelection -> AuthRoleSelectionScreen()
            AuthDestination.PatientSetup -> PatientProfileSetupScreen()
            AuthDestination.DoctorSetup -> DoctorProfileSetupScreen()
            AuthDestination.PatientMain -> PatientMainScreen()
            AuthDestination.DoctorMain -> DoctorMainScreen()
        }
    }

    Navigator(targetScreen) { navigator ->
        // Replace the current stack whenever the destination changes
        LaunchedEffect(destination) {
            val currentScreen = navigator.lastItem
            val shouldReplace = when (destination) {
                AuthDestination.Welcome -> currentScreen !is WelcomeAuthScreen
                AuthDestination.RoleSelection -> currentScreen !is AuthRoleSelectionScreen
                AuthDestination.PatientSetup -> currentScreen !is PatientProfileSetupScreen
                AuthDestination.DoctorSetup -> currentScreen !is DoctorProfileSetupScreen
                AuthDestination.PatientMain -> currentScreen !is PatientMainScreen
                AuthDestination.DoctorMain -> currentScreen !is DoctorMainScreen
            }

            if (shouldReplace) navigator.replaceAll(targetScreen)
        }

        CareRideTransition(navigator)
    }
}

private enum class AuthDestination {
    Welcome,
    RoleSelection,
    PatientSetup,
    DoctorSetup,
    PatientMain,
    DoctorMain
}

private fun DoctorProfile.isOnboardingComplete(): Boolean {
    val basic = phone.isNotBlank()
    val professional = licenseNumber.isNotBlank() && yearsOfExperience > 0
    val bioDone = bio.isNotBlank()
    val educationDone = education.isNotEmpty()
    val practiceDone = practiceName.isNotBlank() && practiceAddress.isComplete && officePhone.isNotBlank()
    return basic && professional && bioDone && educationDone && practiceDone
}