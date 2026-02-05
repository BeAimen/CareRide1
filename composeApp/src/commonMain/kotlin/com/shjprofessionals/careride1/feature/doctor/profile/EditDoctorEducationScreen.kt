package com.shjprofessionals.careride1.feature.doctor.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.Education
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditDoctorEducationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditDoctorEducationViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditDoctorEducationContent(
            state = state,
            onBackClick = { navigator.pop() },
            onAddEducation = viewModel::showAddDialog,
            onRemoveEducation = viewModel::removeEducation,
            onCertificationsChange = viewModel::onCertificationsChange,
            onAffiliationsChange = viewModel::onAffiliationsChange,
            onAwardsChange = viewModel::onAwardsChange,
            onDismissDialog = viewModel::hideAddDialog,
            onConfirmAddEducation = viewModel::addEducation,
            onSave = viewModel::save
        )
    }
}

data class EditDoctorEducationState(
    val education: List<Education> = emptyList(),
    val certifications: String = "",
    val affiliations: String = "",
    val awards: String = "",
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditDoctorEducationViewModel : ScreenModel {
    private val profileStore = FakeBackend.doctorProfileStore

    private val _state = MutableStateFlow(EditDoctorEducationState())
    val state: StateFlow<EditDoctorEducationState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    education = profile.education,
                    certifications = profile.boardCertifications.joinToString("\n"),
                    affiliations = profile.hospitalAffiliations.joinToString("\n"),
                    awards = profile.awards.joinToString("\n"),
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun showAddDialog() {
        _state.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }

    fun addEducation(degree: String, institution: String, year: Int, honors: String) {
        val education = Education(
            degree = degree.trim(),
            institution = institution.trim(),
            year = year,
            honors = honors.trim()
        )
        _state.update {
            it.copy(
                education = it.education + education,
                showAddDialog = false
            )
        }
    }

    fun removeEducation(index: Int) {
        _state.update {
            it.copy(education = it.education.filterIndexed { i, _ -> i != index })
        }
    }

    fun onCertificationsChange(value: String) {
        _state.update { it.copy(certifications = value) }
    }

    fun onAffiliationsChange(value: String) {
        _state.update { it.copy(affiliations = value) }
    }

    fun onAwardsChange(value: String) {
        _state.update { it.copy(awards = value) }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val current = _state.value

            profileStore.updateEducation(current.education)
            profileStore.updateCredentials(
                boardCertifications = current.certifications.lines().map { it.trim() }.filter { it.isNotEmpty() },
                hospitalAffiliations = current.affiliations.lines().map { it.trim() }.filter { it.isNotEmpty() },
                awards = current.awards.lines().map { it.trim() }.filter { it.isNotEmpty() }
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDoctorEducationContent(
    state: EditDoctorEducationState,
    onBackClick: () -> Unit,
    onAddEducation: () -> Unit,
    onRemoveEducation: (Int) -> Unit,
    onCertificationsChange: (String) -> Unit,
    onAffiliationsChange: (String) -> Unit,
    onAwardsChange: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAddEducation: (String, String, Int, String) -> Unit,
    onSave: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Education & Credentials") },
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
                // Education section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Education")
                    TextButton(onClick = onAddEducation) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                        Text("Add")
                    }
                }

                if (state.education.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No education entries yet. Tap Add to include your degrees.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(CareRideTheme.spacing.md)
                        )
                    }
                } else {
                    state.education.forEachIndexed { index, edu ->
                        EducationCard(
                            education = edu,
                            onRemove = { onRemoveEducation(index) }
                        )
                        if (index < state.education.lastIndex) {
                            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Board Certifications
                SectionHeader(title = "Board Certifications")

                OutlinedTextField(
                    value = state.certifications,
                    onValueChange = onCertificationsChange,
                    label = { Text("Certifications") },
                    placeholder = { Text("American Board of Internal Medicine\nCardiovascular Disease") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Hospital Affiliations
                SectionHeader(title = "Hospital Affiliations")

                OutlinedTextField(
                    value = state.affiliations,
                    onValueChange = onAffiliationsChange,
                    label = { Text("Affiliations") },
                    placeholder = { Text("Stanford Medical Center\nUCSF Health") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Awards
                SectionHeader(title = "Awards & Recognition")

                OutlinedTextField(
                    value = state.awards,
                    onValueChange = onAwardsChange,
                    label = { Text("Awards") },
                    placeholder = { Text("Top Doctor 2023\nPatient's Choice Award") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }

    // Add Education Dialog
    if (state.showAddDialog) {
        AddEducationDialog(
            onDismiss = onDismissDialog,
            onConfirm = onConfirmAddEducation
        )
    }
}

@Composable
private fun EducationCard(
    education: Education,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${education.degree} - ${education.institution}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = education.year.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (education.honors.isNotBlank()) {
                    Text(
                        text = education.honors,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddEducationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var degree by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var honors by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Education") },
        text = {
            Column {
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree *") },
                    placeholder = { Text("MD, DO, PhD, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Institution *") },
                    placeholder = { Text("Harvard Medical School") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            year = it
                        }
                    },
                    label = { Text("Year *") },
                    placeholder = { Text("2015") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                OutlinedTextField(
                    value = honors,
                    onValueChange = { honors = it },
                    label = { Text("Honors (Optional)") },
                    placeholder = { Text("Magna Cum Laude") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val yearInt = year.toIntOrNull() ?: 2000
                    onConfirm(degree, institution, yearInt, honors)
                },
                enabled = degree.isNotBlank() && institution.isNotBlank() && year.length == 4
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}