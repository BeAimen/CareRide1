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
            if (state.saveSuccess) navigator.pop()
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

    private var allSuggestedExpertise: List<String> = emptyList()
    private var allSuggestedConditions: List<String> = emptyList()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = profileStore.getProfile()
        allSuggestedExpertise = profileStore.getSuggestedExpertise()
        allSuggestedConditions = profileStore.getSuggestedConditions()

        if (profile != null) {
            val expertiseText = profile.areasOfExpertise.joinToString("\n")
            val conditionsText = profile.conditionsTreated.joinToString("\n")

            _state.update {
                it.copy(
                    bio = profile.bio,
                    philosophy = profile.treatmentPhilosophy,
                    expertise = expertiseText,
                    conditions = conditionsText,
                    suggestedExpertise = filterSuggestions(allSuggestedExpertise, expertiseText),
                    suggestedConditions = filterSuggestions(allSuggestedConditions, conditionsText),
                    isLoading = false
                )
            }
        } else {
            _state.update {
                it.copy(
                    suggestedExpertise = filterSuggestions(allSuggestedExpertise, ""),
                    suggestedConditions = filterSuggestions(allSuggestedConditions, ""),
                    isLoading = false
                )
            }
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
        _state.update {
            it.copy(
                expertise = value,
                suggestedExpertise = filterSuggestions(allSuggestedExpertise, value)
            )
        }
    }

    fun onConditionsChange(value: String) {
        _state.update {
            it.copy(
                conditions = value,
                suggestedConditions = filterSuggestions(allSuggestedConditions, value)
            )
        }
    }

    fun addSuggestedExpertise(item: String) {
        val current = _state.value.expertise
        val existing = normalizedLineSet(current)
        if (normalizeKey(item) in existing) return

        val newValue = if (current.isBlank()) item else "$current\n$item"
        _state.update {
            it.copy(
                expertise = newValue,
                suggestedExpertise = filterSuggestions(allSuggestedExpertise, newValue)
            )
        }
    }

    fun addSuggestedCondition(item: String) {
        val current = _state.value.conditions
        val existing = normalizedLineSet(current)
        if (normalizeKey(item) in existing) return

        val newValue = if (current.isBlank()) item else "$current\n$item"
        _state.update {
            it.copy(
                conditions = newValue,
                suggestedConditions = filterSuggestions(allSuggestedConditions, newValue)
            )
        }
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

    private fun filterSuggestions(all: List<String>, text: String): List<String> {
        val existing = normalizedLineSet(text)
        return all.filter { normalizeKey(it) !in existing }
    }

    private fun normalizedLineSet(text: String): Set<String> =
        text.lines()
            .map { normalizeKey(it) }
            .filter { it.isNotEmpty() }
            .toSet()

    private fun normalizeKey(value: String): String =
        value.trim()
            .lowercase()
            .replace(Regex("""\s+"""), " ")
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
                // Bio section with generator
                SectionHeader(title = "Bio")

                OutlinedTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    label = { Text("Bio") },
                    placeholder = { Text("Tell patients about your background and approach...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    enabled = !state.isSaving && !state.isGenerating
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onGenerateBio,
                        enabled = !state.isSaving && !state.isGenerating
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate")
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Treatment Philosophy")

                OutlinedTextField(
                    value = state.philosophy,
                    onValueChange = onPhilosophyChange,
                    label = { Text("Philosophy") },
                    placeholder = { Text("Your care philosophy...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !state.isSaving && !state.isGenerating
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onGeneratePhilosophy,
                        enabled = !state.isSaving && !state.isGenerating
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Suggest")
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Expertise")

                if (state.suggestedExpertise.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                    ) {
                        state.suggestedExpertise.take(10).forEach { item ->
                            SuggestionChip(
                                onClick = { onAddSuggestedExpertise(item) },
                                label = { Text(item, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                }

                OutlinedTextField(
                    value = state.expertise,
                    onValueChange = onExpertiseChange,
                    label = { Text("Areas of Expertise") },
                    placeholder = { Text("One per line") },
                    supportingText = { Text("Suggestions disappear once added.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    enabled = !state.isSaving && !state.isGenerating
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Conditions Treated")

                if (state.suggestedConditions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                    ) {
                        state.suggestedConditions.take(10).forEach { item ->
                            SuggestionChip(
                                onClick = { onAddSuggestedCondition(item) },
                                label = { Text(item, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                }

                OutlinedTextField(
                    value = state.conditions,
                    onValueChange = onConditionsChange,
                    label = { Text("Conditions") },
                    placeholder = { Text("One per line") },
                    supportingText = { Text("Suggestions disappear once added.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    enabled = !state.isSaving && !state.isGenerating
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}
