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
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.core.util.BioGenerator
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.Address
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditAddressScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditAddressViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditAddressContent(
            state = state,
            onBackClick = { navigator.pop() },
            onStreetChange = viewModel::onStreetChange,
            onApartmentChange = viewModel::onApartmentChange,
            onCityChange = viewModel::onCityChange,
            onStateChange = viewModel::onStateChange,
            onZipCodeChange = viewModel::onZipCodeChange,
            onSave = viewModel::save
        )
    }
}

data class EditAddressState(
    val street: String = "",
    val apartment: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditAddressViewModel : ScreenModel {
    private val profileStore = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(EditAddressState())
    val state: StateFlow<EditAddressState> = _state.asStateFlow()

    init {
        loadAddress()
    }

    private fun loadAddress() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    street = profile.address.street,
                    apartment = profile.address.apartment,
                    city = profile.address.city,
                    state = profile.address.state,
                    zipCode = profile.address.zipCode,
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onStreetChange(value: String) {
        _state.update { it.copy(street = value, error = null) }
    }

    fun onApartmentChange(value: String) {
        _state.update { it.copy(apartment = value) }
    }

    fun onCityChange(value: String) {
        _state.update { it.copy(city = value, error = null) }
    }

    fun onStateChange(value: String) {
        _state.update { it.copy(state = value, error = null) }
    }

    fun onZipCodeChange(value: String) {
        // Only allow digits and limit to 5 characters
        if (value.length <= 5 && value.all { it.isDigit() }) {
            _state.update { it.copy(zipCode = value, error = null) }
        }
    }

    fun save() {
        val current = _state.value

        // Validation
        if (current.street.isNotBlank() && current.city.isBlank()) {
            _state.update { it.copy(error = "City is required") }
            return
        }
        if (current.city.isNotBlank() && current.state.isBlank()) {
            _state.update { it.copy(error = "State is required") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val address = Address(
                street = current.street.trim(),
                apartment = current.apartment.trim(),
                city = current.city.trim(),
                state = current.state.trim(),
                zipCode = current.zipCode.trim()
            )

            profileStore.updateAddress(address)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAddressContent(
    state: EditAddressState,
    onBackClick: () -> Unit,
    onStreetChange: (String) -> Unit,
    onApartmentChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onZipCodeChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var stateExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Address") },
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
                OutlinedTextField(
                    value = state.street,
                    onValueChange = onStreetChange,
                    label = { Text("Street Address") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    placeholder = { Text("123 Main Street") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.apartment,
                    onValueChange = onApartmentChange,
                    label = { Text("Apt, Suite, Unit (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    placeholder = { Text("Apt 4B") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.city,
                    onValueChange = onCityChange,
                    label = { Text("City") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.md)
                ) {
                    // State dropdown
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

                    // ZIP Code
                    OutlinedTextField(
                        value = state.zipCode,
                        onValueChange = onZipCodeChange,
                        label = { Text("ZIP Code") },
                        placeholder = { Text("12345") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !state.isSaving
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Info card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(CareRideTheme.spacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(
                            text = "Your address is used to help find doctors near you and for appointment scheduling.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}