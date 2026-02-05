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
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.InsuranceInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditInsuranceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EditInsuranceViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(state.saveSuccess) {
            if (state.saveSuccess) {
                navigator.pop()
            }
        }

        EditInsuranceContent(
            state = state,
            onBackClick = { navigator.pop() },
            onProviderChange = viewModel::onProviderChange,
            onPlanNameChange = viewModel::onPlanNameChange,
            onPolicyNumberChange = viewModel::onPolicyNumberChange,
            onGroupNumberChange = viewModel::onGroupNumberChange,
            onSubscriberNameChange = viewModel::onSubscriberNameChange,
            onRelationshipChange = viewModel::onRelationshipChange,
            onSave = viewModel::save
        )
    }
}

data class EditInsuranceState(
    val provider: String = "",
    val planName: String = "",
    val policyNumber: String = "",
    val groupNumber: String = "",
    val subscriberName: String = "",
    val subscriberRelationship: String = "Self",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditInsuranceViewModel : ScreenModel {
    private val profileStore = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(EditInsuranceState())
    val state: StateFlow<EditInsuranceState> = _state.asStateFlow()

    init {
        loadInsurance()
    }

    private fun loadInsurance() {
        val profile = profileStore.getProfile()
        if (profile != null) {
            _state.update {
                it.copy(
                    provider = profile.insurance.provider,
                    planName = profile.insurance.planName,
                    policyNumber = profile.insurance.policyNumber,
                    groupNumber = profile.insurance.groupNumber,
                    subscriberName = profile.insurance.subscriberName,
                    subscriberRelationship = profile.insurance.subscriberRelationship,
                    isLoading = false
                )
            }
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onProviderChange(value: String) {
        _state.update { it.copy(provider = value, error = null) }
    }

    fun onPlanNameChange(value: String) {
        _state.update { it.copy(planName = value) }
    }

    fun onPolicyNumberChange(value: String) {
        _state.update { it.copy(policyNumber = value, error = null) }
    }

    fun onGroupNumberChange(value: String) {
        _state.update { it.copy(groupNumber = value) }
    }

    fun onSubscriberNameChange(value: String) {
        _state.update { it.copy(subscriberName = value) }
    }

    fun onRelationshipChange(value: String) {
        _state.update { it.copy(subscriberRelationship = value) }
    }

    fun save() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            delay(300)

            val current = _state.value

            val insurance = InsuranceInfo(
                provider = current.provider.trim(),
                planName = current.planName.trim(),
                policyNumber = current.policyNumber.trim(),
                groupNumber = current.groupNumber.trim(),
                subscriberName = current.subscriberName.trim(),
                subscriberRelationship = current.subscriberRelationship
            )

            profileStore.updateInsurance(insurance)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInsuranceContent(
    state: EditInsuranceState,
    onBackClick: () -> Unit,
    onProviderChange: (String) -> Unit,
    onPlanNameChange: (String) -> Unit,
    onPolicyNumberChange: (String) -> Unit,
    onGroupNumberChange: (String) -> Unit,
    onSubscriberNameChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var relationshipExpanded by remember { mutableStateOf(false) }
    val relationships = listOf("Self", "Spouse", "Child", "Parent", "Other")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insurance Information") },
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
                SectionHeader(title = "Insurance Provider")

                OutlinedTextField(
                    value = state.provider,
                    onValueChange = onProviderChange,
                    label = { Text("Insurance Company") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    placeholder = { Text("Blue Cross Blue Shield") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.planName,
                    onValueChange = onPlanNameChange,
                    label = { Text("Plan Name") },
                    placeholder = { Text("PPO Gold") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Policy Details")

                OutlinedTextField(
                    value = state.policyNumber,
                    onValueChange = onPolicyNumberChange,
                    label = { Text("Policy/Member ID") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    placeholder = { Text("ABC123456789") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                OutlinedTextField(
                    value = state.groupNumber,
                    onValueChange = onGroupNumberChange,
                    label = { Text("Group Number") },
                    placeholder = { Text("GRP001") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Subscriber Information")

                OutlinedTextField(
                    value = state.subscriberName,
                    onValueChange = onSubscriberNameChange,
                    label = { Text("Subscriber Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    placeholder = { Text("Primary policyholder name") },
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
                        value = state.subscriberRelationship,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship to Subscriber") },
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
                            text = "You can find this information on your insurance card. Keeping this up-to-date helps streamline your appointments.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
            }
        }
    }
}