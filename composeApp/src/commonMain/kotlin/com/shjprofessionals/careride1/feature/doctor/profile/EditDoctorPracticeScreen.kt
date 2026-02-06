package com.shjprofessionals.careride1.feature.doctor.profile

import androidx.compose.foundation.horizontalScroll
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
import com.shjprofessionals.careride1.domain.model.Address
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditDoctorPracticeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditDoctorPracticeViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) navigator.pop()
        }

        EditDoctorPracticeContent(
            state = state,
            onBackClick = { navigator.pop() },
            onPracticeNameChange = viewModel::onPracticeNameChange,
            onStreetChange = viewModel::onStreetChange,
            onCityChange = viewModel::onCityChange,
            onStateChange = viewModel::onStateChange,
            onOfficePhoneChange = viewModel::onOfficePhoneChange,
            onFeeMinChange = viewModel::onFeeMinChange,
            onFeeMaxChange = viewModel::onFeeMaxChange,
            onInsuranceChange = viewModel::onInsuranceChange,
            onAddInsurance = viewModel::addInsurance,
            onToggleTelehealth = viewModel::toggleTelehealth,
            onToggleInPerson = viewModel::toggleInPerson,
            onSave = viewModel::save
        )
    }
}

data class EditDoctorPracticeState(
    val practiceName: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val officePhone: String = "",
    val feeMin: String = "",
    val feeMax: String = "",
    val acceptedInsurance: String = "",
    val offersTelehealth: Boolean = true,
    val offersInPerson: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditDoctorPracticeViewModel : ScreenModel {
    private val profileStore = FakeBackend.doctorProfileStore

    private val _state = MutableStateFlow(EditDoctorPracticeState())
    val state: StateFlow<EditDoctorPracticeState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    practiceName = profile.practiceName,
                    street = profile.practiceAddress.street,
                    city = profile.practiceAddress.city,
                    state = profile.practiceAddress.state,
                    officePhone = profile.officePhone,
                    feeMin = if (profile.consultationFeeMin > 0) (profile.consultationFeeMin / 100).toString() else "",
                    feeMax = if (profile.consultationFeeMax > 0) (profile.consultationFeeMax / 100).toString() else "",
                    acceptedInsurance = profile.acceptedInsurance.joinToString("\n"),
                    offersTelehealth = profile.offersTelehealth,
                    offersInPerson = profile.offersInPerson,
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onPracticeNameChange(value: String) = _state.update { it.copy(practiceName = value) }
    fun onStreetChange(value: String) = _state.update { it.copy(street = value) }
    fun onCityChange(value: String) = _state.update { it.copy(city = value) }
    fun onStateChange(value: String) = _state.update { it.copy(state = value) }
    fun onOfficePhoneChange(value: String) = _state.update { it.copy(officePhone = value) }

    fun onFeeMinChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) _state.update { it.copy(feeMin = value) }
    }

    fun onFeeMaxChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) _state.update { it.copy(feeMax = value) }
    }

    fun onInsuranceChange(value: String) = _state.update { it.copy(acceptedInsurance = value) }

    fun addInsurance(insurance: String) {
        val currentText = _state.value.acceptedInsurance
        val existing = currentText.lines()
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (insurance.trim().lowercase() in existing) return

        val newValue = if (currentText.isBlank()) insurance else "$currentText\n$insurance"
        _state.update { it.copy(acceptedInsurance = newValue) }
    }

    fun toggleTelehealth() = _state.update { it.copy(offersTelehealth = !it.offersTelehealth) }
    fun toggleInPerson() = _state.update { it.copy(offersInPerson = !it.offersInPerson) }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val current = _state.value

            val address = Address(
                street = current.street.trim(),
                city = current.city.trim(),
                state = current.state.trim(),
                zipCode = "" // ZIP removed
            )

            profileStore.updatePracticeInfo(
                practiceName = current.practiceName.trim(),
                practiceAddress = address,
                officePhone = current.officePhone.trim(),
                consultationFeeMin = (current.feeMin.toIntOrNull() ?: 0) * 100,
                consultationFeeMax = (current.feeMax.toIntOrNull() ?: 0) * 100,
                acceptedInsurance = current.acceptedInsurance.lines().map { it.trim() }.filter { it.isNotEmpty() }
            )

            profileStore.updateAvailability(
                offersTelehealth = current.offersTelehealth,
                offersInPerson = current.offersInPerson
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDoctorPracticeContent(
    state: EditDoctorPracticeState,
    onBackClick: () -> Unit,
    onPracticeNameChange: (String) -> Unit,
    onStreetChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onOfficePhoneChange: (String) -> Unit,
    onFeeMinChange: (String) -> Unit,
    onFeeMaxChange: (String) -> Unit,
    onInsuranceChange: (String) -> Unit,
    onAddInsurance: (String) -> Unit,
    onToggleTelehealth: () -> Unit,
    onToggleInPerson: () -> Unit,
    onSave: () -> Unit
) {
    var stateExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val existingInsurance = remember(state.acceptedInsurance) {
        state.acceptedInsurance.lines()
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    val filteredInsuranceSuggestions = remember(existingInsurance) {
        BioGenerator.commonInsuranceProviders
            .filter { it.trim().lowercase() !in existingInsurance }
            .take(6)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice Information") },
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
                SectionHeader(title = "Practice Details")

                OutlinedTextField(
                    value = state.practiceName,
                    onValueChange = onPracticeNameChange,
                    label = { Text("Practice Name") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    placeholder = { Text("Bay Area Cardiology Group") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.officePhone,
                    onValueChange = onOfficePhoneChange,
                    label = { Text("Office Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    placeholder = { Text("(555) 123-4567") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Location")

                OutlinedTextField(
                    value = state.street,
                    onValueChange = onStreetChange,
                    label = { Text("Street Address") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.md)
                ) {
                    OutlinedTextField(
                        value = state.city,
                        onValueChange = onCityChange,
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenuBox(
                        expanded = stateExpanded,
                        onExpandedChange = { stateExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.state,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                            modifier = Modifier.menuAnchor(),
                            enabled = !state.isSaving
                        )

                        ExposedDropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false }
                        ) {
                            BioGenerator.usStates.forEach { stateCode ->
                                DropdownMenuItem(
                                    text = { Text(stateCode) },
                                    onClick = {
                                        onStateChange(stateCode)
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Consultation Fees")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.md)
                ) {
                    OutlinedTextField(
                        value = state.feeMin,
                        onValueChange = onFeeMinChange,
                        label = { Text("Min ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = state.feeMax,
                        onValueChange = onFeeMaxChange,
                        label = { Text("Max ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !state.isSaving
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Visit Types")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.md)
                ) {
                    FilterChip(
                        selected = state.offersTelehealth,
                        onClick = onToggleTelehealth,
                        label = { Text("Telehealth") },
                        leadingIcon = if (state.offersTelehealth) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = state.offersInPerson,
                        onClick = onToggleInPerson,
                        label = { Text("In Person") },
                        leadingIcon = if (state.offersInPerson) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Accepted Insurance")

                if (filteredInsuranceSuggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                    ) {
                        filteredInsuranceSuggestions.forEach { insurance ->
                            SuggestionChip(
                                onClick = { onAddInsurance(insurance) },
                                label = { Text(insurance, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                }

                OutlinedTextField(
                    value = state.acceptedInsurance,
                    onValueChange = onInsuranceChange,
                    label = { Text("Insurance Providers") },
                    placeholder = { Text("Blue Cross Blue Shield\nAetna\nMedicare") },
                    supportingText = { Text("One per line. Suggestions disappear once added.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}
