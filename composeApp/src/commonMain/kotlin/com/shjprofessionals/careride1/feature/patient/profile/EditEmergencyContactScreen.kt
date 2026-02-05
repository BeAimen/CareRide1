package com.shjprofessionals.careride1.feature.patient.profile

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
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.EmergencyContact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditEmergencyContactScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditEmergencyContactViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditEmergencyContactContent(
            state = state,
            onBackClick = { navigator.pop() },
            onNameChange = viewModel::onNameChange,
            onRelationshipChange = viewModel::onRelationshipChange,
            onPhoneChange = viewModel::onPhoneChange,
            onEmailChange = viewModel::onEmailChange,
            onSave = viewModel::save
        )
    }
}

data class EditEmergencyContactState(
    val name: String = "",
    val relationship: String = "",
    val phone: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditEmergencyContactViewModel : ScreenModel {
    private val profileStore = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(EditEmergencyContactState())
    val state: StateFlow<EditEmergencyContactState> = _state.asStateFlow()

    init {
        loadEmergencyContact()
    }

    private fun loadEmergencyContact() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    name = profile.emergencyContact.name,
                    relationship = profile.emergencyContact.relationship,
                    phone = profile.emergencyContact.phone,
                    email = profile.emergencyContact.email,
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, error = null) }
    }

    fun onRelationshipChange(value: String) {
        _state.update { it.copy(relationship = value) }
    }

    fun onPhoneChange(value: String) {
        _state.update { it.copy(phone = value, error = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value) }
    }

    fun save() {
        val current = _state.value

        // Validation - if any field is filled, name and phone are required
        val hasAnyData = current.name.isNotBlank() || current.phone.isNotBlank() ||
                current.relationship.isNotBlank() || current.email.isNotBlank()

        if (hasAnyData && current.name.isBlank()) {
            _state.update { it.copy(error = "Contact name is required") }
            return
        }

        if (hasAnyData && current.phone.isBlank()) {
            _state.update { it.copy(error = "Phone number is required") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val contact = EmergencyContact(
                name = current.name.trim(),
                relationship = current.relationship.trim(),
                phone = current.phone.trim(),
                email = current.email.trim()
            )

            profileStore.updateEmergencyContact(contact)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEmergencyContactContent(
    state: EditEmergencyContactState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var relationshipExpanded by remember { mutableStateOf(false) }
    val relationships = listOf("Spouse", "Parent", "Child", "Sibling", "Friend", "Other")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contact") },
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
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(CareRideTheme.spacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(
                            text = "This person will be contacted in case of a medical emergency. Make sure they are aware they are listed as your emergency contact.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Contact Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    placeholder = { Text("John Smith") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                ExposedDropdownMenuBox(
                    expanded = relationshipExpanded,
                    onExpandedChange = { relationshipExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.relationship,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenu(
                        expanded = relationshipExpanded,
                        onDismissRequest = { relationshipExpanded = false }
                    ) {
                        relationships.forEach { relationship ->
                            DropdownMenuItem(
                                text = { Text(relationship) },
                                onClick = {
                                    onRelationshipChange(relationship)
                                    relationshipExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    placeholder = { Text("(555) 123-4567") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    placeholder = { Text("john@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}