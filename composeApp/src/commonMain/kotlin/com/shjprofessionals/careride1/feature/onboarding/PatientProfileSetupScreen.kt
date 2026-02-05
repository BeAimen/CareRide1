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
import com.shjprofessionals.careride1.domain.model.PatientProfile
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import com.shjprofessionals.careride1.feature.patient.profile.EditAddressScreen
import com.shjprofessionals.careride1.feature.patient.profile.EditEmergencyContactScreen
import com.shjprofessionals.careride1.feature.patient.profile.EditInsuranceScreen
import com.shjprofessionals.careride1.feature.patient.profile.EditMedicalInfoScreen
import org.koin.compose.koinInject

/**
 * Account onboarding (Patient): guides the user to fill the required profile sections.
 * After completion, routes to PatientMainScreen.
 */
class PatientProfileSetupScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository: AuthRepository = koinInject()
        val profileStore = FakeBackend.patientProfileStore

        val profile by profileStore.profile.collectAsState(initial = null)

        // Ensure profile exists (for brand-new accounts)
        LaunchedEffect(profile) {
            if (profile == null) {
                authRepository.getCurrentUser()?.let { profileStore.syncWithAuthUser(it) }
            }
        }

        val p = profile
        val addressComplete = p?.address?.isComplete == true
        val medicalComplete = p?.hasMedicalInfo == true
        val insuranceComplete = p?.hasInsurance == true
        val emergencyComplete = p?.hasEmergencyContact == true
        val allComplete = addressComplete && medicalComplete && insuranceComplete && emergencyComplete

        // Auto-continue when all steps are complete
        LaunchedEffect(allComplete) {
            if (allComplete) {
                navigator.replaceAll(PatientMainScreen())
            }
        }

        PatientProfileSetupContent(
            profile = p,
            addressComplete = addressComplete,
            medicalComplete = medicalComplete,
            insuranceComplete = insuranceComplete,
            emergencyComplete = emergencyComplete,
            onSkip = { navigator.replaceAll(PatientMainScreen()) },
            onStepClick = { step ->
                when (step) {
                    PatientSetupStep.Address -> navigator.push(EditAddressScreen())
                    PatientSetupStep.Medical -> navigator.push(EditMedicalInfoScreen())
                    PatientSetupStep.Insurance -> navigator.push(EditInsuranceScreen())
                    PatientSetupStep.Emergency -> navigator.push(EditEmergencyContactScreen())
                }
            },
            onContinue = {
                when {
                    !addressComplete -> navigator.push(EditAddressScreen())
                    !medicalComplete -> navigator.push(EditMedicalInfoScreen())
                    !insuranceComplete -> navigator.push(EditInsuranceScreen())
                    !emergencyComplete -> navigator.push(EditEmergencyContactScreen())
                    else -> navigator.replaceAll(PatientMainScreen())
                }
            }
        )
    }
}

private enum class PatientSetupStep { Address, Medical, Insurance, Emergency }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientProfileSetupContent(
    profile: PatientProfile?,
    addressComplete: Boolean,
    medicalComplete: Boolean,
    insuranceComplete: Boolean,
    emergencyComplete: Boolean,
    onSkip: () -> Unit,
    onStepClick: (PatientSetupStep) -> Unit,
    onContinue: () -> Unit
) {
    val completed = listOf(addressComplete, medicalComplete, insuranceComplete, emergencyComplete).count { it }
    val progress = completed / 4f

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
                    Text(if (completed == 4) "Finish" else "Continue")
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
                text = "A few details help us personalize your experience and keep you safe.",
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
                            text = "$completed/4",
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
                        text = profile?.name?.takeIf { it.isNotBlank() }?.let { "Welcome, $it" } ?: "Welcome",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SetupStepCard(
                icon = Icons.Default.Place,
                title = "Address",
                subtitle = profile?.address?.formatted?.ifBlank { "Add your address" } ?: "Add your address",
                isComplete = addressComplete,
                onClick = { onStepClick(PatientSetupStep.Address) }
            )

            SetupStepCard(
                icon = Icons.Default.LocalHospital,
                title = "Medical info",
                subtitle = if (medicalComplete) "Added" else "Add blood type, allergies, medications",
                isComplete = medicalComplete,
                onClick = { onStepClick(PatientSetupStep.Medical) }
            )

            SetupStepCard(
                icon = Icons.Default.CreditCard,
                title = "Insurance",
                subtitle = profile?.insurance?.provider?.takeIf { it.isNotBlank() } ?: "Add your insurance details",
                isComplete = insuranceComplete,
                onClick = { onStepClick(PatientSetupStep.Insurance) }
            )

            SetupStepCard(
                icon = Icons.Default.Warning,
                title = "Emergency contact",
                subtitle = profile?.emergencyContact?.name?.takeIf { it.isNotBlank() } ?: "Add a trusted contact",
                isComplete = emergencyComplete,
                onClick = { onStepClick(PatientSetupStep.Emergency) }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            Text(
                text = "You can update these anytime in Profile → Personal Information.",
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
