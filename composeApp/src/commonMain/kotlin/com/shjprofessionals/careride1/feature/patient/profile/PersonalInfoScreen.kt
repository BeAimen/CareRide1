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
import com.shjprofessionals.careride1.core.designsystem.components.PatientAvatar
import com.shjprofessionals.careride1.core.designsystem.components.AvatarSize
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.PatientProfile
import kotlinx.coroutines.delay
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
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPhoneChange = viewModel::onPhoneChange,
            onDateOfBirthChange = viewModel::onDateOfBirthChange,
            onEmergencyContactChange = viewModel::onEmergencyContactChange,
            onEmergencyPhoneChange = viewModel::onEmergencyPhoneChange,
            onSave = viewModel::save,
            onDismissMessage = viewModel::clearMessage
        )
    }
}

data class PersonalInfoState(
    val profile: PatientProfile? = null,
    val editName: String = "",
    val editEmail: String = "",
    val editPhone: String = "",
    val editDateOfBirth: String = "",
    val editEmergencyContact: String = "",
    val editEmergencyPhone: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null
) {
    val hasChanges: Boolean
        get() = profile?.let {
            editName != it.name ||
                    editEmail != it.email ||
                    editPhone != it.phone ||
                    editDateOfBirth != it.dateOfBirth ||
                    editEmergencyContact != it.emergencyContact ||
                    editEmergencyPhone != it.emergencyPhone
        } ?: false
}

class PersonalInfoViewModel : ScreenModel {
    private val store = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(PersonalInfoState())
    val state: StateFlow<PersonalInfoState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        screenModelScope.launch {
            store.profile.collect { profile ->
                _state.update {
                    it.copy(
                        profile = profile,
                        editName = profile.name,
                        editEmail = profile.email,
                        editPhone = profile.phone,
                        editDateOfBirth = profile.dateOfBirth,
                        editEmergencyContact = profile.emergencyContact,
                        editEmergencyPhone = profile.emergencyPhone,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(editName = value) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(editEmail = value) }
    }

    fun onPhoneChange(value: String) {
        _state.update { it.copy(editPhone = value) }
    }

    fun onDateOfBirthChange(value: String) {
        _state.update { it.copy(editDateOfBirth = value) }
    }

    fun onEmergencyContactChange(value: String) {
        _state.update { it.copy(editEmergencyContact = value) }
    }

    fun onEmergencyPhoneChange(value: String) {
        _state.update { it.copy(editEmergencyPhone = value) }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            delay(500) // Simulate network

            val current = _state.value
            store.updateProfile(
                name = current.editName,
                email = current.editEmail,
                phone = current.editPhone,
                dateOfBirth = current.editDateOfBirth,
                emergencyContact = current.editEmergencyContact,
                emergencyPhone = current.editEmergencyPhone
            )

            _state.update {
                it.copy(
                    isSaving = false,
                    message = "Profile updated successfully"
                )
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoContent(
    state: PersonalInfoState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onEmergencyContactChange: (String) -> Unit,
    onEmergencyPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onDismissMessage()
        }
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.hasChanges) {
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
                            Text("Save Changes")
                        }
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
                // Avatar
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PatientAvatar(
                        name = state.editName,
                        size = AvatarSize.XXLarge
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Basic Information")

                OutlinedTextField(
                    value = state.editName,
                    onValueChange = onNameChange,
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                OutlinedTextField(
                    value = state.editEmail,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                OutlinedTextField(
                    value = state.editPhone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                OutlinedTextField(
                    value = state.editDateOfBirth,
                    onValueChange = onDateOfBirthChange,
                    label = { Text("Date of Birth") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    placeholder = { Text("MM/DD/YYYY") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Emergency Contact")

                OutlinedTextField(
                    value = state.editEmergencyContact,
                    onValueChange = onEmergencyContactChange,
                    label = { Text("Contact Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                OutlinedTextField(
                    value = state.editEmergencyPhone,
                    onValueChange = onEmergencyPhoneChange,
                    label = { Text("Contact Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}