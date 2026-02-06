package com.shjprofessionals.careride1.feature.doctor.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.shjprofessionals.careride1.core.designsystem.components.*
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideLightColors
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus

class DoctorProfileTab : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<DoctorProfileViewModel>()
        val state by viewModel.state.collectAsState()

        DoctorProfileContent(
            state = state,
            onToggleEditMode = viewModel::toggleEditMode,
            onBioChange = viewModel::onBioChange,
            onLocationChange = viewModel::onLocationChange,
            onYearsExperienceChange = viewModel::onYearsExperienceChange,
            onToggleAvailability = viewModel::toggleAvailability,
            onToggleAcceptingNew = viewModel::toggleAcceptingNewPatients,
            onSave = viewModel::saveChanges,
            onRefresh = viewModel::refresh,
            onDismissMessage = viewModel::clearMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorProfileContent(
    state: DoctorProfileState,
    onToggleEditMode: () -> Unit,
    onBioChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onYearsExperienceChange: (String) -> Unit,
    onToggleAvailability: () -> Unit,
    onToggleAcceptingNew: () -> Unit,
    onSave: () -> Unit,
    onRefresh: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    if (state.isEditMode) {
                        TextButton(
                            onClick = onToggleEditMode,
                            enabled = !state.isSaving
                        ) {
                            Text("Cancel")
                        }
                    } else {
                        IconButton(onClick = onToggleEditMode) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit profile"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.isEditMode) {
                Surface(
                    shadowElevation = CareRideTheme.elevation.lg,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CareRideTheme.spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
                    ) {
                        OutlinedButton(
                            onClick = onToggleEditMode,
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSaving
                        ) {
                            Text("Discard")
                        }

                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            enabled = state.hasUnsavedChanges && !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading && state.doctor == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.doctor != null) {
            RefreshableContent(
                isRefreshing = state.isLoading,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(CareRideTheme.spacing.md)
                ) {
                    // Profile Header
                    ProfileHeader(
                        doctor = state.doctor,
                        boostStatus = state.boostStatus
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                    // Quick Toggles (always visible)
                    QuickTogglesSection(
                        isAvailableToday = state.doctor.isAvailableToday,
                        acceptingNewPatients = state.doctor.acceptingNewPatients,
                        onToggleAvailability = onToggleAvailability,
                        onToggleAcceptingNew = onToggleAcceptingNew,
                        enabled = !state.isEditMode
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                    // Profile Details
                    if (state.isEditMode) {
                        EditProfileSection(
                            bio = state.editBio,
                            location = state.editLocation,
                            yearsExperience = state.editYearsExperience,
                            onBioChange = onBioChange,
                            onLocationChange = onLocationChange,
                            onYearsExperienceChange = onYearsExperienceChange
                        )
                    } else {
                        ViewProfileSection(doctor = state.doctor)
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                    // Performance Summary
                    if (state.analytics != null && !state.isEditMode) {
                        PerformanceSummary(analytics = state.analytics)
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    doctor: com.shjprofessionals.careride1.domain.model.Doctor,
    boostStatus: DoctorBoostStatus
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        DoctorAvatar(
            name = doctor.name,
            size = AvatarSize.XXLarge
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        // Name
        Text(
            text = doctor.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

        // Specialty
        Text(
            text = doctor.specialty.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        // Rating
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = CareRideLightColors.Sponsored
            )
            Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
            Text(
                text = "${doctor.rating} (${doctor.reviewCount} reviews)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Boost status badge
        if (boostStatus.isActive()) {
            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
            Surface(
                color = CareRideLightColors.SponsoredContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = CareRideTheme.spacing.sm,
                        vertical = CareRideTheme.spacing.xxs
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = CareRideLightColors.Sponsored
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                    Text(
                        text = "Boosted Profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = CareRideLightColors.Sponsored
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickTogglesSection(
    isAvailableToday: Boolean,
    acceptingNewPatients: Boolean,
    onToggleAvailability: () -> Unit,
    onToggleAcceptingNew: () -> Unit,
    enabled: Boolean
) {
    Column {
        SectionHeader(title = "Quick Settings")

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        // Availability toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Available Today",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isAvailableToday) "Patients can see you're available" else "Shown as unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAvailableToday,
                    onCheckedChange = { onToggleAvailability() },
                    enabled = enabled
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        // Accepting new patients toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accepting New Patients",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (acceptingNewPatients) "New patients can contact you" else "Not accepting new patients",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = acceptingNewPatients,
                    onCheckedChange = { onToggleAcceptingNew() },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun ViewProfileSection(
    doctor: com.shjprofessionals.careride1.domain.model.Doctor
) {
    Column {
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
            icon = Icons.Default.Place,
            label = "Location",
            value = doctor.location
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.DateRange,
            label = "Experience",
            value = "${doctor.yearsOfExperience} years"
        )
    }
}

@Composable
private fun EditProfileSection(
    bio: String,
    location: String,
    yearsExperience: String,
    onBioChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onYearsExperienceChange: (String) -> Unit
) {
    Column {
        SectionHeader(title = "Edit Profile")

        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            supportingText = { Text("${bio.length}/500 characters") }
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Place, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        OutlinedTextField(
            value = yearsExperience,
            onValueChange = onYearsExperienceChange,
            label = { Text("Years of Experience") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun PerformanceSummary(
    analytics: com.shjprofessionals.careride1.domain.model.BoostAnalytics
) {
    Column {
        SectionHeader(title = "Performance Summary")

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
        ) {
            AnalyticsCard(
                title = "Profile Views",
                value = analytics.profileViews.toString(),
                change = analytics.profileViewsChangePercent,
                isPositiveChange = analytics.profileViewsChange >= 0,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            )

            AnalyticsCard(
                title = "Messages",
                value = analytics.messageRequests.toString(),
                change = analytics.messageRequestsChangePercent,
                isPositiveChange = analytics.messageRequestsChange >= 0,
                icon = Icons.Default.Email,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = "Last 30 days • View full analytics in Boost tab",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}