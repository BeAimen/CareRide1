package com.shjprofessionals.careride1.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.AuthResult
import com.shjprofessionals.careride1.domain.model.User
import com.shjprofessionals.careride1.domain.model.UserRole
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthRoleSelectionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AuthRoleSelectionViewModel>()
        val state by viewModel.state.collectAsState()

        AuthRoleSelectionContent(
            state = state,
            onRoleSelect = viewModel::selectRole,
            onContinueClick = viewModel::confirmRole
        )
    }
}

data class AuthRoleSelectionState(
    val user: User? = null,
    val selectedRole: UserRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthRoleSelectionViewModel(
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _state = MutableStateFlow(AuthRoleSelectionState())
    val state: StateFlow<AuthRoleSelectionState> = _state.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        screenModelScope.launch {
            authRepository.observeAuthState().collect { authState ->
                _state.update { it.copy(user = authState.currentUser()) }
            }
        }
    }

    fun selectRole(role: UserRole) {
        _state.update { it.copy(selectedRole = role, error = null) }
    }

    fun confirmRole() {
        val selectedRole = _state.value.selectedRole ?: return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.setRole(selectedRole)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    // Navigation will be handled by AuthNavigator
                }
                is AuthResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}

@Composable
private fun AuthRoleSelectionContent(
    state: AuthRoleSelectionState,
    onRoleSelect: (UserRole) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))

        // Greeting
        Text(
            text = "Welcome, ${state.user?.firstName ?: "there"}!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = "How will you use CareRide?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

        // Error message
        if (state.error != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(CareRideTheme.spacing.md),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
        }

        // Role options
        RoleOptionCard(
            icon = Icons.Default.Person,
            title = "I'm a Patient",
            description = "Find doctors, book appointments, and manage your health",
            isSelected = state.selectedRole == UserRole.PATIENT,
            onClick = { onRoleSelect(UserRole.PATIENT) },
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        RoleOptionCard(
            icon = Icons.Default.LocalHospital,
            title = "I'm a Doctor",
            description = "Connect with patients, manage your practice, grow your reach",
            isSelected = state.selectedRole == UserRole.DOCTOR,
            onClick = { onRoleSelect(UserRole.DOCTOR) },
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.selectedRole != null && !state.isLoading,
            shape = MaterialTheme.shapes.large
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))
    }
}

@Composable
private fun RoleOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "$title, $description. ${if (isSelected) "Selected" else "Not selected"}"
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}