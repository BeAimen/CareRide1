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
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.DoctorTitle
import com.shjprofessionals.careride1.domain.model.Gender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditDoctorBasicInfoScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditDoctorBasicInfoViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) navigator.pop()
        }

        EditDoctorBasicInfoContent(
            state = state,
            onBackClick = { navigator.pop() },
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
    val gender: Gender? = null, // Only Male/Female allowed here; null means not set
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
                    gender = profile.gender.takeIf { g -> g == Gender.MALE || g == Gender.FEMALE },
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onTitleChange(value: DoctorTitle) = _state.update { it.copy(title = value) }
    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value) }

    fun onDateOfBirthChange(value: String) {
        // Allow only digits and '/' and max length 10 (MM/DD/YYYY)
        val filtered = value.filter { it.isDigit() || it == '/' }
        if (filtered.length <= 10) {
            _state.update { it.copy(dateOfBirth = filtered, error = null) }
        }
    }

    fun onGenderChange(value: Gender?) = _state.update { it.copy(gender = value) }

    fun save() {
        val current = _state.value

        val dobRaw = current.dateOfBirth.trim()
        val dobNormalized = if (dobRaw.isBlank()) null else normalizeDobOrNull(dobRaw)

        if (dobRaw.isNotBlank() && dobNormalized == null) {
            _state.update { it.copy(error = "Invalid date of birth. Use MM/DD/YYYY (e.g., 01/25/1999).") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(250)

            profileStore.updateBasicInfo(
                name = current.name.trim().takeIf { it.isNotBlank() },
                email = current.email.trim().takeIf { it.isNotBlank() },
                phone = current.phone.trim().takeIf { it.isNotBlank() },
                dateOfBirth = dobNormalized,
                gender = current.gender, // only Male/Female or null
                title = current.title
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    private fun normalizeDobOrNull(input: String): String? {
        val s = input.trim()

        // MM/DD/YYYY
        val mmdd = Regex("""^(\d{1,2})/(\d{1,2})/(\d{4})$""").matchEntire(s)
        if (mmdd != null) {
            val m = mmdd.groupValues[1].toIntOrNull() ?: return null
            val d = mmdd.groupValues[2].toIntOrNull() ?: return null
            val y = mmdd.groupValues[3].toIntOrNull() ?: return null
            if (!isValidDate(y, m, d)) return null
            return formatDob(m, d, y)
        }

        // YYYY-MM-DD (optional acceptance)
        val ymd = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})$""").matchEntire(s)
        if (ymd != null) {
            val y = ymd.groupValues[1].toIntOrNull() ?: return null
            val m = ymd.groupValues[2].toIntOrNull() ?: return null
            val d = ymd.groupValues[3].toIntOrNull() ?: return null
            if (!isValidDate(y, m, d)) return null
            return formatDob(m, d, y)
        }

        return null
    }

    private fun formatDob(month: Int, day: Int, year: Int): String {
        val mm = month.toString().padStart(2, '0')
        val dd = day.toString().padStart(2, '0')
        val yyyy = year.toString().padStart(4, '0')
        return "$mm/$dd/$yyyy"
    }

    private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        if (year !in 1900..2100) return false
        if (month !in 1..12) return false
        val maxDay = daysInMonth(year, month)
        return day in 1..maxDay
    }

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 0
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDoctorBasicInfoContent(
    state: EditDoctorBasicInfoState,
    onBackClick: () -> Unit,
    onTitleChange: (DoctorTitle) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (Gender?) -> Unit,
    onSave: () -> Unit
) {
    var titleExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val allowedGenders = remember { listOf(Gender.MALE, Gender.FEMALE) }
    val genderDisplay = state.gender?.displayName ?: "Select"

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                // Title dropdown
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
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
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
                    supportingText = { Text("Optional. If filled, must be a valid date.") },
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
                        value = genderDisplay,
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
                        allowedGenders.forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender.displayName) },
                                onClick = {
                                    onGenderChange(gender)
                                    genderExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Clear") },
                            onClick = {
                                onGenderChange(null)
                                genderExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}
