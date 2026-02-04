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
import com.shjprofessionals.careride1.domain.model.*
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditBasicInfoScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditBasicInfoViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditBasicInfoContent(
            state = state,
            onBackClick = { navigator.pop() },
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPhoneChange = viewModel::onPhoneChange,
            onDateOfBirthChange = viewModel::onDateOfBirthChange,
            onGenderChange = viewModel::onGenderChange,
            onSave = viewModel::save
        )
    }
}

data class EditBasicInfoState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
) {
    val hasChanges: Boolean get() = true // Simplified for now
}

class EditBasicInfoViewModel(
    private val authRepository: AuthRepository
) : ScreenModel {
    private val profileStore = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(EditBasicInfoState())
    val state: StateFlow<EditBasicInfoState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    name = profile.name,
                    email = profile.email,
                    phone = profile.phone,
                    dateOfBirth = profile.dateOfBirth,
                    gender = profile.gender,
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

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onPhoneChange(value: String) {
        _state.update { it.copy(phone = value) }
    }

    fun onDateOfBirthChange(value: String) {
        _state.update { it.copy(dateOfBirth = value) }
    }

    fun onGenderChange(value: Gender) {
        _state.update { it.copy(gender = value) }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val current = _state.value

            // Update auth
            val result = authRepository.updateUserInfo(
                name = current.name.trim(),
                email = current.email.trim()
            )

            when (result) {
                is AuthResult.Success -> {
                    // Update profile store
                    profileStore.updateBasicInfo(
                        name = current.name.trim(),
                        email = current.email.trim(),
                        phone = current.phone,
                        dateOfBirth = current.dateOfBirth,
                        gender = current.gender
                    )
                    _state.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                is AuthResult.Error -> {
                    _state.update { it.copy(isSaving = false, error = result.message) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBasicInfoContent(
    state: EditBasicInfoState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onSave: () -> Unit
) {
    var genderExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basic Information") },
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
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    placeholder = { Text("(555) 123-4567") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.dateOfBirth,
                    onValueChange = onDateOfBirthChange,
                    label = { Text("Date of Birth") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    placeholder = { Text("MM/DD/YYYY") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                // Gender dropdown
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.gender.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        Gender.entries.forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender.displayName) },
                                onClick = {
                                    onGenderChange(gender)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}