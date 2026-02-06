package com.shjprofessionals.careride1.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.CareRidePrimaryButton
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.feature.patient.profile.*

class PatientProfileSetupScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val profile by FakeBackend.patientProfileStore.profile.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Patient Setup") },
                    actions = {
                        TextButton(onClick = { navigator.replace(PatientMainScreen()) }) {
                            Text("Skip")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                Surface(
                    shadowElevation = CareRideTheme.elevation.lg,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CareRidePrimaryButton(
                        text = "Continue to App",
                        onClick = { navigator.replace(PatientMainScreen()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CareRideTheme.spacing.md),
                        accessibilityLabel = "Continue to patient home"
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(CareRideTheme.spacing.md)
            ) {
                Text(
                    text = "You can fill these now or later.\nNothing is required to proceed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))
                SectionHeader(title = "Profile Sections")

                PatientSetupRow(
                    icon = Icons.Default.Home,
                    title = "Address",
                    subtitle = "Full address + state dropdown",
                    isFilled = (profile?.address?.street?.isNotBlank() == true) &&
                            (profile?.address?.city?.isNotBlank() == true),
                    onClick = { navigator.push(EditAddressScreen()) }
                )

                PatientSetupRow(
                    icon = Icons.Default.Favorite,
                    title = "Medical info",
                    subtitle = "Blood type, allergies, medications, conditions",
                    isFilled = (profile?.allergies?.isNotEmpty() == true) ||
                            (profile?.medications?.isNotEmpty() == true) ||
                            (profile?.medicalConditions?.isNotEmpty() == true),
                    onClick = { navigator.push(EditMedicalInfoScreen()) }
                )

                PatientSetupRow(
                    icon = Icons.Default.Shield,
                    title = "Insurance",
                    subtitle = "Provider + policy details",
                    isFilled = (profile?.insurance?.provider?.isNotBlank() == true),
                    onClick = { navigator.push(EditInsuranceScreen()) }
                )

                PatientSetupRow(
                    icon = Icons.Default.Call,
                    title = "Emergency contact",
                    subtitle = "Contact + relationship",
                    isFilled = (profile?.emergencyContact?.name?.isNotBlank() == true) &&
                            (profile?.emergencyContact?.phone?.isNotBlank() == true),
                    onClick = { navigator.push(EditEmergencyContactScreen()) }
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}

@Composable
private fun PatientSetupRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isFilled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CareRideTheme.spacing.xs)
            .clickable(onClick = onClick)
    ) {
        ListItem(
            leadingContent = { Icon(icon, contentDescription = null) },
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            trailingContent = {
                if (isFilled) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Filled")
                } else {
                    Text("Optional", style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}
