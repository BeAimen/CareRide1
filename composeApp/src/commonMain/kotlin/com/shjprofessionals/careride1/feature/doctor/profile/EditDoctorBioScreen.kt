package com.shjprofessionals.careride1.feature.doctor.profile

import androidx.compose.foundation.horizontalScroll
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditDoctorBioScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditDoctorBioViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditDoctorBioContent(
            state = state,
            onBackClick = { navigator.pop() },
            onBioChange = viewModel::onBioChange,
            onPhilosophyChange = viewModel::onPhilosophyChange,
            onGenerateBio = viewModel::generateBio,
            onGeneratePhilosophy = viewModel::generatePhilosophy,
            onExpertiseChange = viewModel::onExpertiseChange,
            onConditionsChange = viewModel::onConditionsChange,
            onAddSuggestedExpertise = viewModel::addSuggestedExpertise,
            onAddSuggestedCondition = viewModel::addSuggestedCondition,
            onSave = viewModel::save
        )
    }
}

data class EditDoctorBioState(
    val bio: String = "",
    val philosophy: String = "",
    val expertise: String = "",
    val conditions: String = "",
    val suggestedExpertise: List<String> = emptyList(),
    val suggestedConditions: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditDoctorBioViewModel : ScreenModel {
    private val profileStore = FakeBackend.doctorProfileStore

    private val _state = MutableStateFlow(EditDoctorBioState())
    val state: StateFlow<EditDoctorBioState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    bio = profile.bio,
                    philosophy = profile.treatmentPhilosophy,
                    expertise = profile.areasOfExpertise.joinToString("\n"),
                    conditions = profile.conditionsTreated.joinToString("\n"),
                    suggestedExpertise = profileStore.getSuggestedExpertise(),
                    suggestedConditions = profileStore.getSuggestedConditions(),
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onBioChange(value: String) {
        _state.update { it.copy(bio = value) }
    }

    fun onPhilosophyChange(value: String) {
        _state.update { it.copy(philosophy = value) }
    }

    fun generateBio() {
        screenModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            delay(500)
            val generatedBio = profileStore.generateDefaultBio()
            _state.update { it.copy(bio = generatedBio, isGenerating = false) }
        }
    }

    fun generatePhilosophy() {
        screenModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            delay(300)
            val generatedPhilosophy = profileStore.generateDefaultPhilosophy()
            _state.update { it.copy(philosophy = generatedPhilosophy, isGenerating = false) }
        }
    }

    fun onExpertiseChange(value: String) {
        _state.update { it.copy(expertise = value) }
    }

    fun onConditionsChange(value: String) {
        _state.update { it.copy(conditions = value) }
    }

    fun addSuggestedExpertise(item: String) {
        val current = _state.value.expertise
        val newValue = if (current.isBlank()) item else "$current\n$item"
        _state.update { it.copy(expertise = newValue) }
    }

    fun addSuggestedCondition(item: String) {
        val current = _state.value.conditions
        val newValue = if (current.isBlank()) item else "$current\n$item"
        _state.update { it.copy(conditions = newValue) }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val current = _state.value

            profileStore.updateBio(current.bio.trim())
            profileStore.updateTreatmentPhilosophy(current.philosophy.trim())
            profileStore.updateExpertise(
                areasOfExpertise = current.expertise.lines().map { it.trim() }.filter { it.isNotEmpty() },
                conditionsTreated = current.conditions.lines().map { it.trim() }.filter { it.isNotEmpty() }
            )

            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDoctorBioContent(
    state: EditDoctorBioState,
    onBackClick: () -> Unit,
    onBioChange: (String) -> Unit,
    onPhilosophyChange: (String) -> Unit,
    onGenerateBio: () -> Unit,
    onGeneratePhilosophy: () -> Unit,
    onExpertiseChange: (String) -> Unit,
    onConditionsChange: (String) -> Unit,
    onAddSuggestedExpertise: (String) -> Unit,
    onAddSuggestedCondition: (String) -> Unit,
    onSave: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bio & Expertise") },
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
                    enabled = !state.isSaving && !state.isGenerating,
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
                // Bio section with AI generator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Professional Bio")
                    TextButton(
                        onClick = onGenerateBio,
                        enabled = !state.isGenerating
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                        Text("Generate")
                    }
                }

                OutlinedTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    label = { Text("About You") },
                    placeholder = { Text("Tell patients about your background, approach, and what makes your practice unique...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 12,
                    supportingText = { Text("${state.bio.length}/1500 characters") },
                    enabled = !state.isSaving && !state.isGenerating
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Treatment Philosophy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Treatment Philosophy")
                    TextButton(
                        onClick = onGeneratePhilosophy,
                        enabled = !state.isGenerating
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                        Text("Generate")
                    }
                }

                OutlinedTextField(
                    value = state.philosophy,
                    onValueChange = onPhilosophyChange,
                    label = { Text("Your Approach to Care") },
                    placeholder = { Text("Describe your philosophy on patient care...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    enabled = !state.isSaving && !state.isGenerating
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Areas of Expertise
                SectionHeader(title = "Areas of Expertise")

                // Suggested expertise chips
                if (state.suggestedExpertise.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                    ) {
                        state.suggestedExpertise.take(8).forEach { suggestion ->
                            SuggestionChip(
                                onClick = { onAddSuggestedExpertise(suggestion) },
                                label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                }

                OutlinedTextField(
                    value = state.expertise,
                    onValueChange = onExpertiseChange,
                    label = { Text("Expertise") },
                    placeholder = { Text("Heart Disease Prevention\nArrhythmia Management") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Conditions Treated
                SectionHeader(title = "Conditions Treated")

                if (state.suggestedConditions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                    ) {
                        state.suggestedConditions.take(8).forEach { suggestion ->
                            SuggestionChip(
                                onClick = { onAddSuggestedCondition(suggestion) },
                                label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                }

                OutlinedTextField(
                    value = state.conditions,
                    onValueChange = onConditionsChange,
                    label = { Text("Conditions") },
                    placeholder = { Text("Hypertension\nHeart Failure\nAtrial Fibrillation") },
                    supportingText = { Text("One per line") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}