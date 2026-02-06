package com.shjprofessionals.careride1.feature.doctor.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.shjprofessionals.careride1.domain.model.DoctorTitle
import com.shjprofessionals.careride1.domain.model.Gender
import com.shjprofessionals.careride1.feature.onboarding.DoctorMainScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditDoctorBasicInfoScreen(
    val isOnboarding: Boolean = false
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditDoctorBasicInfoViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                if (isOnboarding) {
                    navigator.replaceAll(DoctorMainScreen())
                } else {
                    navigator.pop()
                }
            }
        }

        EditDoctorBasicInfoContent(
            state = state,
            isOnboarding = isOnboarding,
            onBackClick = { navigator.pop() },
            onSkip = { navigator.replaceAll(DoctorMainScreen()) },
            onTitleChange = viewModel::onTitleChange,
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPhoneChange = viewModel::onPhoneChange,
            onDateOfBirthChange = viewModel::onDateOfBirthChange,
            onGenderChange = viewModel::onGenderChange,
            onSave = viewModel::save
        )
    }
}

data class EditDoctorBasicInfoState(
    val title: DoctorTitle = DoctorTitle.MD,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditDoctorBasicInfoViewModel : ScreenModel {
    private val profileStore = FakeBackend.doctorProfileStore

    private val _state = MutableStateFlow(EditDoctorBasicInfoState())
    val state: StateFlow<EditDoctorBasicInfoState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    title = profile.title,
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

    fun onTitleChange(value: DoctorTitle) {
        _state.update { it.copy(title = value) }
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
        val current = _state.value

        if (current.name.isBlank()) {
            _state.update { it.copy(error = "Name is required") }
            return
        }

        if (current.email.isBlank()) {
            _state.update { it.copy(error = "Email is required") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            profileStore.updateBasicInfo(
                name = current.name.trim(),
                email = current.email.trim(),
                phone = current.phone.trim(),
                dateOfBirth = current.dateOfBirth.trim(),
                gender = current.gender,
                title = current.title
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDoctorBasicInfoContent(
    state: EditDoctorBasicInfoState,
    isOnboarding: Boolean,
    onBackClick: () -> Unit,
    onSkip: () -> Unit,
    onTitleChange: (DoctorTitle) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onSave: () -> Unit
) {
    var titleExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isOnboarding) "Setup Profile" else "Basic Information") },
                navigationIcon = {
                    if (!isOnboarding) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                        }
                    }
                },
                actions = {
                    if (isOnboarding) {
                        TextButton(onClick = onSkip) {
                            Text("Skip")
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
                        Text(if (isOnboarding) "Complete Setup" else "Save")
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
                ExposedDropdownMenuBox(
                    expanded = titleExpanded,
                    onExpandedChange = { titleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.title.fullTitle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Title") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !state.isSaving
                    )

                    ExposedDropdownMenu(
                        expanded = titleExpanded,
                        onDismissRequest = { titleExpanded = false }
                    ) {
                        DoctorTitle.entries.forEach { title ->
                            DropdownMenuItem(
                                text = { Text("${title.prefix.trim()} - ${title.fullTitle}") },
                                onClick = {
                                    onTitleChange(title)
                                    titleExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

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

                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.gender.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
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