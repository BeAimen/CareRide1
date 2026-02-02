package com.shjprofessionals.careride1.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.CareRidePrimaryButton
import com.shjprofessionals.careride1.core.designsystem.components.CareRideSecondaryButton
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.UserRole

class RoleSelectionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CareRideTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to CareRide",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                Text(
                    text = "Connect with healthcare professionals\nor reach more patients",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))

                CareRidePrimaryButton(
                    text = "I'm a Patient",
                    onClick = { navigator.replace(PatientMainScreen()) },
                    modifier = Modifier.fillMaxWidth(),
                    accessibilityLabel = "Continue as a patient to browse and contact doctors"
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                CareRideSecondaryButton(
                    text = "I'm a Doctor",
                    onClick = { navigator.replace(DoctorMainScreen()) },
                    modifier = Modifier.fillMaxWidth(),
                    accessibilityLabel = "Continue as a doctor to manage your profile and connect with patients"
                )
            }
        }
    }
}
