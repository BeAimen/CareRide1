package com.shjprofessionals.careride1.feature.patient.doctorprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.*
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideLightColors
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.feature.patient.messages.PatientChatScreen
import com.shjprofessionals.careride1.feature.patient.subscription.PaywallScreen
import org.koin.core.parameter.parametersOf

data class DoctorProfileScreen(
    val doctorId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<DoctorProfileViewModel> { parametersOf(doctorId) }
        val state by viewModel.state.collectAsState()

        // Handle navigation to chat
        LaunchedEffect(state.navigateToChat) {
            state.navigateToChat?.let { conversation ->
                viewModel.onNavigatedToChat()
                navigator.push(PatientChatScreen(conversationId = conversation.id))
            }
        }

        // Capture doctor for smart cast
        val doctor = state.doctor

        DoctorProfileContent(
            state = state,
            onBackClick = { navigator.pop() },
            onMessageClick = viewModel::onMessageDoctorClick,
            onRetry = viewModel::onRetry
        )

        // Gating sheet
        if (state.showGatingSheet && doctor != null) {
            MessageGatingSheet(
                doctorName = doctor.name,
                onDismiss = viewModel::dismissGatingSheet,
                onViewPlans = {
                    viewModel.dismissGatingSheet()
                    navigator.push(PaywallScreen())
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorProfileContent(
    state: DoctorProfileState,
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onRetry: () -> Unit
) {
    val doctor = state.doctor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (doctor != null) {
                Surface(
                    shadowElevation = CareRideTheme.elevation.lg,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CareRideTheme.spacing.md)
                    ) {
                        CareRidePrimaryButton(
                            text = "Message ${doctor.name.split(" ").first()}",
                            onClick = onMessageClick,
                            modifier = Modifier.fillMaxWidth(),
                            accessibilityLabel = "Send a message to ${doctor.name}"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    DoctorProfileSkeleton()
                }

                state.error != null -> {
                    ErrorState(
                        message = state.error?.userMessage ?: "Unknown error",
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                doctor != null -> {
                    DoctorProfileBody(doctor = doctor)
                }
            }
        }
    }
}

@Composable
private fun DoctorProfileBody(doctor: Doctor) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CareRideTheme.spacing.md)
    ) {
        DoctorProfileHeader(doctor = doctor)

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        SectionHeader(title = "About")

        Text(
            text = doctor.bio,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        SectionHeader(title = "Details")

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.Star,
            label = "Experience",
            value = "${doctor.yearsOfExperience} years"
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.Place,
            label = "Location",
            value = doctor.location
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.DateRange,
            label = "Languages",
            value = doctor.languages.joinToString(", ")
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.Person,
            label = "New patients",
            value = if (doctor.acceptingNewPatients) "Accepting" else "Not accepting",
            valueColor = if (doctor.acceptingNewPatients) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.CheckCircle,
            label = "Availability",
            value = if (doctor.isAvailableToday) "Available today" else "Next available soon",
            valueColor = if (doctor.isAvailableToday) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
    }
}

@Composable
private fun DoctorProfileHeader(doctor: Doctor) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .semantics { contentDescription = "Profile photo of ${doctor.name}" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = doctor.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (doctor.isBoosted) {
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                SponsoredBadge()
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

        Text(
            text = doctor.specialty.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    val filled = index < doctor.rating.toInt()
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (filled) {
                            CareRideLightColors.Sponsored
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

            Text(
                text = "${doctor.rating}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = " (${doctor.reviewCount} reviews)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DoctorProfileSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        Spacer(
            modifier = Modifier
                .height(28.dp)
                .width(180.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Spacer(
            modifier = Modifier
                .height(20.dp)
                .width(120.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
