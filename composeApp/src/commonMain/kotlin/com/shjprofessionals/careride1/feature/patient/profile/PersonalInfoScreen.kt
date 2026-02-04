package com.shjprofessionals.careride1.feature.patient.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.PatientAvatar
import com.shjprofessionals.careride1.core.designsystem.components.AvatarSize
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.*
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PersonalInfoScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<PersonalInfoViewModel>()
        val state by viewModel.state.collectAsState()

        PersonalInfoContent(
            state = state,
            onBackClick = { navigator.pop() },
            onBasicInfoClick = { navigator.push(EditBasicInfoScreen()) },
            onAddressClick = { navigator.push(EditAddressScreen()) },
            onMedicalInfoClick = { navigator.push(EditMedicalInfoScreen()) },
            onInsuranceClick = { navigator.push(EditInsuranceScreen()) },
            onEmergencyContactClick = { navigator.push(EditEmergencyContactScreen()) }
        )
    }
}

data class PersonalInfoState(
    val profile: PatientProfile? = null,
    val isLoading: Boolean = true
)

class PersonalInfoViewModel(
    private val authRepository: AuthRepository
) : ScreenModel {
    private val profileStore = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(PersonalInfoState())
    val state: StateFlow<PersonalInfoState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        screenModelScope.launch {
            profileStore.profile.collect { profile ->
                if (profile != null) {
                    _state.update { it.copy(profile = profile, isLoading = false) }
                } else {
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        profileStore.syncWithAuthUser(user)
                    }
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoContent(
    state: PersonalInfoState,
    onBackClick: () -> Unit,
    onBasicInfoClick: () -> Unit,
    onAddressClick: () -> Unit,
    onMedicalInfoClick: () -> Unit,
    onInsuranceClick: () -> Unit,
    onEmergencyContactClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val profile = state.profile

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(CareRideTheme.spacing.md)
            ) {
                // Profile completion card
                if (profile != null && profile.profileCompletionPercent < 100) {
                    ProfileCompletionCard(
                        percent = profile.profileCompletionPercent
                    )
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))
                }

                // Avatar
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PatientAvatar(
                        name = profile?.name,
                        size = AvatarSize.XXLarge
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Basic Info Section
                ProfileSection(
                    icon = Icons.Default.Person,
                    title = "Basic Information",
                    subtitle = profile?.let {
                        listOfNotNull(
                            it.name.takeIf { n -> n.isNotBlank() },
                            it.phone.takeIf { p -> p.isNotBlank() },
                            it.gender.takeIf { g -> g != Gender.PREFER_NOT_TO_SAY }?.displayName
                        ).joinToString(" • ").ifBlank { "Add your details" }
                    } ?: "Add your details",
                    isComplete = profile?.hasCompleteProfile == true,
                    onClick = onBasicInfoClick
                )

                // Address Section
                ProfileSection(
                    icon = Icons.Default.Place,
                    title = "Address",
                    subtitle = profile?.address?.formatted?.ifBlank { "Add your address" } ?: "Add your address",
                    isComplete = profile?.address?.isComplete == true,
                    onClick = onAddressClick
                )

                // Medical Info Section
                ProfileSection(
                    icon = Icons.Default.LocalHospital,
                    title = "Medical Information",
                    subtitle = profile?.let {
                        buildString {
                            if (it.bloodType != BloodType.UNKNOWN) append("Blood: ${it.bloodType.displayName}")
                            if (it.allergies.isNotEmpty()) {
                                if (isNotEmpty()) append(" • ")
                                append("${it.allergies.size} allergies")
                            }
                            if (it.medications.isNotEmpty()) {
                                if (isNotEmpty()) append(" • ")
                                append("${it.medications.size} medications")
                            }
                        }.ifBlank { "Add medical details" }
                    } ?: "Add medical details",
                    isComplete = profile?.hasMedicalInfo == true,
                    onClick = onMedicalInfoClick
                )

                // Insurance Section
                ProfileSection(
                    icon = Icons.Default.CreditCard,
                    title = "Insurance",
                    subtitle = profile?.insurance?.let {
                        if (it.provider.isNotBlank()) "${it.provider} - ${it.planName}".trim(' ', '-')
                        else "Add insurance information"
                    } ?: "Add insurance information",
                    isComplete = profile?.hasInsurance == true,
                    onClick = onInsuranceClick
                )

                // Emergency Contact Section
                ProfileSection(
                    icon = Icons.Default.Warning,
                    title = "Emergency Contact",
                    subtitle = profile?.emergencyContact?.let {
                        if (it.name.isNotBlank()) "${it.name} (${it.relationship})".trim()
                        else "Add emergency contact"
                    } ?: "Add emergency contact",
                    isComplete = profile?.hasEmergencyContact == true,
                    onClick = onEmergencyContactClick
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}

@Composable
private fun ProfileCompletionCard(percent: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(CareRideTheme.spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Completion",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            Text(
                text = "Complete your profile for better care",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isComplete: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = CareRideTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = if (isComplete) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isComplete) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        if (isComplete) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Complete",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}