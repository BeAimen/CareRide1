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
import com.shjprofessionals.careride1.feature.doctor.profile.*

class DoctorProfileSetupScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val profile by FakeBackend.doctorProfileStore.profileFlow.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Doctor Setup") },
                    actions = {
                        TextButton(onClick = { navigator.replace(DoctorMainScreen()) }) {
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
                        onClick = { navigator.replace(DoctorMainScreen()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CareRideTheme.spacing.md),
                        accessibilityLabel = "Continue to doctor home"
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

                SetupRow(
                    icon = Icons.Default.Badge,
                    title = "Basic information",
                    subtitle = "Name, contact, DOB, gender",
                    isFilled = (profile?.phone?.isNotBlank() == true) ||
                            (profile?.dateOfBirth?.isNotBlank() == true) ||
                            (profile?.gender?.name == "MALE" || profile?.gender?.name == "FEMALE"),
                    onClick = { navigator.push(EditDoctorBasicInfoScreen()) }
                )

                SetupRow(
                    icon = Icons.Default.Work,
                    title = "Professional",
                    subtitle = "Specialty, license, NPI, experience",
                    isFilled = (profile?.licenseNumber?.isNotBlank() == true) ||
                            (profile?.npiNumber?.isNotBlank() == true) ||
                            ((profile?.yearsOfExperience ?: 0) > 0),
                    onClick = { navigator.push(EditDoctorProfessionalScreen()) }
                )

                SetupRow(
                    icon = Icons.Default.Description,
                    title = "Bio",
                    subtitle = "Bio, philosophy, expertise & conditions",
                    isFilled = (profile?.bio?.isNotBlank() == true) ||
                            (profile?.areasOfExpertise?.isNotEmpty() == true) ||
                            (profile?.conditionsTreated?.isNotEmpty() == true),
                    onClick = { navigator.push(EditDoctorBioScreen()) }
                )

                SetupRow(
                    icon = Icons.Default.School,
                    title = "Education",
                    subtitle = "Education entries, certifications, awards",
                    isFilled = (profile?.education?.isNotEmpty() == true) ||
                            (profile?.boardCertifications?.isNotEmpty() == true) ||
                            (profile?.awards?.isNotEmpty() == true),
                    onClick = { navigator.push(EditDoctorEducationScreen()) }
                )

                SetupRow(
                    icon = Icons.Default.Place,
                    title = "Practice",
                    subtitle = "Practice name, location, fees, insurance",
                    isFilled = (profile?.practiceName?.isNotBlank() == true) ||
                            (profile?.practiceAddress?.street?.isNotBlank() == true) ||
                            (profile?.acceptedInsurance?.isNotEmpty() == true),
                    onClick = { navigator.push(EditDoctorPracticeScreen()) }
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}

@Composable
private fun SetupRow(
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
