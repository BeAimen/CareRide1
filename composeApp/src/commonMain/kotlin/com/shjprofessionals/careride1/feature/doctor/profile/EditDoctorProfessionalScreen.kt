package com.shjprofessionals.careride1.feature.doctor.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.core.util.BioGenerator
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.Specialty
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditDoctorProfessionalScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditDoctorProfessionalViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditDoctorProfessionalContent(
            state = state,
            onBackClick = { navigator.pop() },
            onSpecialtyChange = viewModel::onSpecialtyChange,
            onSubSpecialtiesChange = viewModel::onSubSpecialtiesChange,
            onLicenseNumberChange = viewModel::onLicenseNumberChange,
            onLicenseStateChange = viewModel::onLicenseStateChange,
            onNpiNumberChange = viewModel::onNpiNumberChange,
            onYearsExperienceChange = viewModel::onYearsExperienceChange,
            onSave = viewModel::save
        )
    }
}

data class EditDoctorProfessionalState(
    val specialty: Specialty = Specialty.GENERAL_PRACTICE,
    val subSpecialties: String = "",
    val licenseNumber: String = "",
    val licenseState: String = "",
    val npiNumber: String = "",
    val yearsExperience: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditDoctorProfessionalViewModel : ScreenModel {
    private val profileStore = FakeBackend.doctorProfileStore

    private val _state = MutableStateFlow(EditDoctorProfessionalState())
    val state: StateFlow<EditDoctorProfessionalState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    specialty = profile.specialty,
                    subSpecialties = profile.subSpecialties.joinToString(", "),
                    licenseNumber = profile.licenseNumber,
                    licenseState = profile.licenseState,
                    npiNumber = profile.npiNumber,
                    yearsExperience = if (profile.yearsOfExperience > 0) profile.yearsOfExperience.toString() else "",
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onSpecialtyChange(value: Specialty) {
        _state.update { it.copy(specialty = value) }
    }

    fun onSubSpecialtiesChange(value: String) {
        _state.update { it.copy(subSpecialties = value) }
    }

    fun onLicenseNumberChange(value: String) {
        _state.update { it.copy(licenseNumber = value) }
    }

    fun onLicenseStateChange(value: String) {
        _state.update { it.copy(licenseState = value) }
    }

    fun onNpiNumberChange(value: String) {
        // NPI is 10 digits
        if (value.length <= 10 && value.all { it.isDigit() }) {
            _state.update { it.copy(npiNumber = value) }
        }
    }

    fun onYearsExperienceChange(value: String) {
        if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 2)) {
            _state.update { it.copy(yearsExperience = value) }
        }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val current = _state.value

            profileStore.updateProfessionalInfo(
                specialty = current.specialty,
                subSpecialties = current.subSpecialties.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                licenseNumber = current.licenseNumber.trim(),
                licenseState = current.licenseState.trim(),
                npiNumber = current.npiNumber.trim(),
                yearsOfExperience = current.yearsExperience.toIntOrNull() ?: 0
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDoctorProfessionalContent(
    state: EditDoctorProfessionalState,
    onBackClick: () -> Unit,
    onSpecialtyChange: (Specialty) -> Unit,
    onSubSpecialtiesChange: (String) -> Unit,
    onLicenseNumberChange: (String) -> Unit,
    onLicenseStateChange: (String) -> Unit,
    onNpiNumberChange: (String) -> Unit,
    onYearsExperienceChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var specialtyExpanded by remember { mutableStateOf(false) }
    var licenseStateExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Professional Information") },
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
                SectionHeader(title = "Specialty")

                ExposedDropdownMenuBox(
                    expanded = specialtyExpanded,
                    onExpandedChange = { specialtyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.specialty.displayName,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specialtyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenu(
                        expanded = specialtyExpanded,
                        onDismissRequest = { specialtyExpanded = false }
                    ) {
                        Specialty.entries.forEach { specialty ->
                            DropdownMenuItem(
                                text = { Text(specialty.displayName) },
                                onClick = {
                                    onSpecialtyChange(specialty)
                                    specialtyExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.subSpecialties,
                    onValueChange = onSubSpecialtiesChange,
                    label = { Text("Sub-Specialties") },
                    placeholder = { Text("Interventional Cardiology, Heart Failure") },
                    supportingText = { Text("Separate with commas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "License Information")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.md)
                ) {
                    OutlinedTextField(
                        value = state.licenseNumber,
                        onValueChange = onLicenseNumberChange,
                        label = { Text("License Number") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true,
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenuBox(
                        expanded = licenseStateExpanded,
                        onExpandedChange = { licenseStateExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.licenseState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = licenseStateExpanded) },
                            modifier = Modifier.menuAnchor(),
                            enabled = !state.isSaving
                        )

                        ExposedDropdownMenu(
                            expanded = licenseStateExpanded,
                            onDismissRequest = { licenseStateExpanded = false }
                        ) {
                            BioGenerator.usStates.forEach { stateCode ->
                                DropdownMenuItem(
                                    text = { Text(stateCode) },
                                    onClick = {
                                        onLicenseStateChange(stateCode)
                                        licenseStateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.npiNumber,
                    onValueChange = onNpiNumberChange,
                    label = { Text("NPI Number") },
                    placeholder = { Text("10-digit NPI") },
                    supportingText = { Text("National Provider Identifier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Experience")

                OutlinedTextField(
                    value = state.yearsExperience,
                    onValueChange = onYearsExperienceChange,
                    label = { Text("Years of Experience") },
                    leadingIcon = { Icon(Icons.Default.WorkHistory, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}