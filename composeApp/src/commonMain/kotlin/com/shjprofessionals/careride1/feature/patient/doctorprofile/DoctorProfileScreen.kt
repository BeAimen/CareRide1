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

        LaunchedEffect(state.navigateToChat) {
            state.navigateToChat?.let { conversation ->
                viewModel.onNavigatedToChat()
                navigator.push(PatientChatScreen(conversationId = conversation.id))
            }
        }

        val doctor = state.doctor

        DoctorProfileContent(
            state = state,
            onBackClick = { navigator.pop() },
            onMessageClick = viewModel::onMessageDoctorClick,
            onRetry = viewModel::onRetry
        )

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

                        if (!state.subscriptionStatus.canMessage()) {
                            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                            Text(
                                text = "Messaging is available with a subscription",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
                state.isLoading -> DoctorProfileSkeleton()
                state.error != null -> ErrorState(
                    message = state.error?.userMessage ?: "Unknown error",
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize()
                )
                doctor != null -> DoctorProfileBody(doctor = doctor)
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

        InfoRow(icon = Icons.Default.MedicalServices, label = "Specialty", value = doctor.specialty.displayName)
        InfoRow(icon = Icons.Default.LocationOn, label = "Location", value = doctor.location)
        InfoRow(icon = Icons.Default.Star, label = "Rating", value = "${doctor.rating} (${doctor.reviewCount} reviews)")
        InfoRow(icon = Icons.Default.Work, label = "Experience", value = "${doctor.yearsOfExperience} years")

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        SectionHeader(title = "Availability")

        AvailabilityCard(doctor = doctor)

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))
    }
}

@Composable
private fun DoctorProfileHeader(doctor: Doctor) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = doctor.name.split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .take(2)
                        .joinToString(""),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (doctor.isBoosted) {
                    Surface(
                        color = CareRideLightColors.SponsoredContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Sponsored",
                            style = MaterialTheme.typography.labelSmall,
                            color = CareRideLightColors.Sponsored,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            Text(
                text = doctor.specialty.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = doctor.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AvailabilityCard(doctor: Doctor) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CareRideTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (doctor.isAvailableToday) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (doctor.isAvailableToday) CareRideLightColors.Secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

                Text(
                    text = if (doctor.isAvailableToday) "Available today" else "Next available soon",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            Text(
                text = if (doctor.acceptingNewPatients) "Accepting new patients" else "Not accepting new patients",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CareRideTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DoctorProfileSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Spacer(
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth(0.7f)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.4f)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Spacer(
            modifier = Modifier
                .height(20.dp)
                .width(180.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        Spacer(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
