package com.shjprofessionals.careride1.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.DoctorProfile
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import com.shjprofessionals.careride1.feature.doctor.profile.EditDoctorBasicInfoScreen
import com.shjprofessionals.careride1.feature.doctor.profile.EditDoctorBioScreen
import com.shjprofessionals.careride1.feature.doctor.profile.EditDoctorEducationScreen
import com.shjprofessionals.careride1.feature.doctor.profile.EditDoctorPracticeScreen
import com.shjprofessionals.careride1.feature.doctor.profile.EditDoctorProfessionalScreen
import org.koin.compose.koinInject

/**
 * Account onboarding (Doctor): guides the doctor through key profile sections.
 * After completion, routes to DoctorMainScreen.
 */
class DoctorProfileSetupScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository: AuthRepository = koinInject()
        val profileStore = FakeBackend.doctorProfileStore

        val profile by profileStore.profileFlow.collectAsState(initial = null)

        // Ensure profile exists (for brand-new accounts)
        LaunchedEffect(profile) {
            if (profile == null) {
                authRepository.getCurrentUser()?.let { profileStore.syncWithAuthUser(it) }
            }
        }

        val d = profile
        val basicComplete = d?.phone?.isNotBlank() == true
        val professionalComplete = d?.licenseNumber?.isNotBlank() == true && (d.yearsOfExperience > 0)
        val bioComplete = d?.bio?.isNotBlank() == true
        val educationComplete = d?.education?.isNotEmpty() == true
        val practiceComplete = d?.practiceName?.isNotBlank() == true && d.practiceAddress.isComplete && d.officePhone.isNotBlank()
        val allComplete = basicComplete && professionalComplete && bioComplete && educationComplete && practiceComplete

        // Auto-continue when all steps are complete
        LaunchedEffect(allComplete) {
            if (allComplete) {
                navigator.replaceAll(DoctorMainScreen())
            }
        }

        DoctorProfileSetupContent(
            profile = d,
            basicComplete = basicComplete,
            professionalComplete = professionalComplete,
            bioComplete = bioComplete,
            educationComplete = educationComplete,
            practiceComplete = practiceComplete,
            onSkip = { navigator.replaceAll(DoctorMainScreen()) },
            onStepClick = { step ->
                when (step) {
                    DoctorSetupStep.Basic -> navigator.push(EditDoctorBasicInfoScreen())
                    DoctorSetupStep.Professional -> navigator.push(EditDoctorProfessionalScreen())
                    DoctorSetupStep.Bio -> navigator.push(EditDoctorBioScreen())
                    DoctorSetupStep.Education -> navigator.push(EditDoctorEducationScreen())
                    DoctorSetupStep.Practice -> navigator.push(EditDoctorPracticeScreen())
                }
            },
            onContinue = {
                when {
                    !basicComplete -> navigator.push(EditDoctorBasicInfoScreen())
                    !professionalComplete -> navigator.push(EditDoctorProfessionalScreen())
                    !bioComplete -> navigator.push(EditDoctorBioScreen())
                    !educationComplete -> navigator.push(EditDoctorEducationScreen())
                    !practiceComplete -> navigator.push(EditDoctorPracticeScreen())
                    else -> navigator.replaceAll(DoctorMainScreen())
                }
            }
        )
    }
}

private enum class DoctorSetupStep { Basic, Professional, Bio, Education, Practice }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorProfileSetupContent(
    profile: DoctorProfile?,
    basicComplete: Boolean,
    professionalComplete: Boolean,
    bioComplete: Boolean,
    educationComplete: Boolean,
    practiceComplete: Boolean,
    onSkip: () -> Unit,
    onStepClick: (DoctorSetupStep) -> Unit,
    onContinue: () -> Unit
) {
    val completed = listOf(basicComplete, professionalComplete, bioComplete, educationComplete, practiceComplete).count { it }
    val progress = completed / 5f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set up your profile") },
                actions = {
                    TextButton(onClick = onSkip) { Text("Skip") }
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
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CareRideTheme.spacing.md)
                ) {
                    Text(if (completed == 5) "Finish" else "Continue")
                }
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
                text = "A complete profile helps patients trust you and increases bookings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(CareRideTheme.spacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completed/5",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                    Text(
                        text = profile?.displayName?.takeIf { it.isNotBlank() }?.let { "Welcome, $it" } ?: "Welcome",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SetupStepCard(
                icon = Icons.Default.Person,
                title = "Basic info",
                subtitle = if (basicComplete) "Added" else "Add phone, title, contact details",
                isComplete = basicComplete,
                onClick = { onStepClick(DoctorSetupStep.Basic) }
            )

            SetupStepCard(
                icon = Icons.Default.Badge,
                title = "Professional",
                subtitle = if (professionalComplete) "Added" else "Add specialty, license, NPI, experience",
                isComplete = professionalComplete,
                onClick = { onStepClick(DoctorSetupStep.Professional) }
            )

            SetupStepCard(
                icon = Icons.Default.EditNote,
                title = "Bio",
                subtitle = if (bioComplete) "Added" else "Write your bio and expertise",
                isComplete = bioComplete,
                onClick = { onStepClick(DoctorSetupStep.Bio) }
            )

            SetupStepCard(
                icon = Icons.Default.School,
                title = "Education",
                subtitle = if (educationComplete) "Added" else "Add education, certifications, awards",
                isComplete = educationComplete,
                onClick = { onStepClick(DoctorSetupStep.Education) }
            )

            SetupStepCard(
                icon = Icons.Default.Business,
                title = "Practice",
                subtitle = profile?.practiceName?.takeIf { it.isNotBlank() } ?: "Add practice name, address, fees",
                isComplete = practiceComplete,
                onClick = { onStepClick(DoctorSetupStep.Practice) }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            Text(
                text = "You can update these anytime in Profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
        }
    }
}

@Composable
private fun SetupStepCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isComplete: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CareRideTheme.spacing.xs)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isComplete) {
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
