package com.shjprofessionals.careride1.feature.patient.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.BloodType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditMedicalInfoScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditMedicalInfoViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditMedicalInfoContent(
            state = state,
            onBackClick = { navigator.pop() },
            onBloodTypeChange = viewModel::onBloodTypeChange,
            onAllergiesChange = viewModel::onAllergiesChange,
            onMedicationsChange = viewModel::onMedicationsChange,
            onConditionsChange = viewModel::onConditionsChange,
            onPrimaryPhysicianChange = viewModel::onPrimaryPhysicianChange,
            onSave = viewModel::save
        )
    }
}

data class EditMedicalInfoState(
    val bloodType: BloodType = BloodType.UNKNOWN,
    val allergies: String = "",
    val medications: String = "",
    val conditions: String = "",
    val primaryPhysician: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditMedicalInfoViewModel : ScreenModel {
    private val profileStore = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(EditMedicalInfoState())
    val state: StateFlow<EditMedicalInfoState> = _state.asStateFlow()

    init {
        loadMedicalInfo()
    }

    private fun loadMedicalInfo() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    bloodType = profile.bloodType,
                    allergies = profile.allergies.joinToString("\n"),
                    medications = profile.medications.joinToString("\n"),
                    conditions = profile.medicalConditions.joinToString("\n"),
                    primaryPhysician = profile.primaryPhysician,
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onBloodTypeChange(value: BloodType) {
        _state.update { it.copy(bloodType = value) }
    }

    fun onAllergiesChange(value: String) {
        _state.update { it.copy(allergies = value) }
    }

    fun onMedicationsChange(value: String) {
        _state.update { it.copy(medications = value) }
    }

    fun onConditionsChange(value: String) {
        _state.update { it.copy(conditions = value) }
    }

    fun onPrimaryPhysicianChange(value: String) {
        _state.update { it.copy(primaryPhysician = value) }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val current = _state.value

            profileStore.updateMedicalInfo(
                bloodType = current.bloodType,
                allergies = current.allergies.lines().map { it.trim() }.filter { it.isNotEmpty() },
                medications = current.medications.lines().map { it.trim() }.filter { it.isNotEmpty() },
                medicalConditions = current.conditions.lines().map { it.trim() }.filter { it.isNotEmpty() },
                primaryPhysician = current.primaryPhysician.trim()
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMedicalInfoContent(
    state: EditMedicalInfoState,
    onBackClick: () -> Unit,
    onBloodTypeChange: (BloodType) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onMedicationsChange: (String) -> Unit,
    onConditionsChange: (String) -> Unit,
    onPrimaryPhysicianChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var bloodTypeExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Information") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                shadowElevation = CareRideTheme.elevation.lg,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CareRideTheme.spacing.md)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(CareRideTheme.spacing.md)
            ) {
                // Important notice
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(CareRideTheme.spacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(
                            text = "This information may be shared with healthcare providers to ensure your safety. Please keep it accurate and up-to-date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Blood Type
                SectionHeader(title = "Blood Type")

                ExposedDropdownMenuBox(
                    expanded = bloodTypeExpanded,
                    onExpandedChange = { bloodTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.bloodType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenu(
                        expanded = bloodTypeExpanded,
                        onDismissRequest = { bloodTypeExpanded = false }
                    ) {
                        BloodType.entries.forEach { bloodType ->
                            DropdownMenuItem(
                                text = { Text(bloodType.displayName) },
                                onClick = {
                                    onBloodTypeChange(bloodType)
                                    bloodTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Allergies
                SectionHeader(title = "Allergies")

                OutlinedTextField(
                    value = state.allergies,
                    onValueChange = onAllergiesChange,
                    label = { Text("Known Allergies") },
                    placeholder = { Text("Penicillin\nPeanuts\nLatex") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Medications
                SectionHeader(title = "Current Medications")

                OutlinedTextField(
                    value = state.medications,
                    onValueChange = onMedicationsChange,
                    label = { Text("Medications") },
                    placeholder = { Text("Lisinopril 10mg\nMetformin 500mg") },
                    supportingText = { Text("Include dosage if known, one per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Medical Conditions
                SectionHeader(title = "Medical Conditions")

                OutlinedTextField(
                    value = state.conditions,
                    onValueChange = onConditionsChange,
                    label = { Text("Conditions") },
                    placeholder = { Text("Type 2 Diabetes\nHypertension") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Primary Physician
                SectionHeader(title = "Primary Care Physician")

                OutlinedTextField(
                    value = state.primaryPhysician,
                    onValueChange = onPrimaryPhysicianChange,
                    label = { Text("Physician Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    placeholder = { Text("Dr. Jane Smith") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}