package com.shjprofessionals.careride1.feature.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.core.util.AuthValidators
import com.shjprofessionals.careride1.core.util.ValidationResult
import com.shjprofessionals.careride1.domain.model.*
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SignUpScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<SignUpViewModel>()
        val state by viewModel.state.collectAsState()

        // Handle successful sign up
        LaunchedEffect(state.signUpSuccess) {
            if (state.signUpSuccess) {
                viewModel.onNavigated()
                // Navigation will be handled by AuthNavigator - goes to role selection
            }
        }

        SignUpContent(
            state = state,
            onBackClick = { navigator.pop() },
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
            onConfirmPasswordVisibilityToggle = viewModel::toggleConfirmPasswordVisibility,
            onAcceptTermsChange = viewModel::onAcceptTermsChange,
            onSignUpClick = viewModel::signUp,
            onSignInClick = { navigator.replace(SignInScreen()) }
        )
    }
}

data class SignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val signUpSuccess: Boolean = false
) {
    val passwordStrength: PasswordStrength
        get() = PasswordStrength.calculate(password)

    val isFormValid: Boolean
        get() = name.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                acceptedTerms &&
                nameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null
}

class SignUpViewModel(
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null, generalError = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update {
            it.copy(
                password = value,
                passwordError = null,
                confirmPasswordError = null,
                generalError = null
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
        }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onAcceptTermsChange(value: Boolean) {
        _state.update { it.copy(acceptedTerms = value) }
    }

    fun signUp() {
        val currentState = _state.value
        var hasError = false

        // Validate all fields
        val nameValidation = AuthValidators.validateName(currentState.name)
        if (nameValidation is ValidationResult.Invalid) {
            _state.update { it.copy(nameError = nameValidation.reason) }
            hasError = true
        }

        val emailValidation = AuthValidators.validateEmail(currentState.email)
        if (emailValidation is ValidationResult.Invalid) {
            _state.update { it.copy(emailError = emailValidation.reason) }
            hasError = true
        }

        val passwordValidation = AuthValidators.validatePassword(currentState.password)
        if (passwordValidation is ValidationResult.Invalid) {
            _state.update { it.copy(passwordError = passwordValidation.reason) }
            hasError = true
        }

        val confirmValidation = AuthValidators.validatePasswordMatch(
            currentState.password,
            currentState.confirmPassword
        )
        if (confirmValidation is ValidationResult.Invalid) {
            _state.update { it.copy(confirmPasswordError = confirmValidation.reason) }
            hasError = true
        }

        if (!currentState.acceptedTerms) {
            _state.update { it.copy(generalError = "Please accept the Terms of Service") }
            hasError = true
        }

        if (hasError) return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, generalError = null) }

            val credentials = SignUpCredentials(
                name = currentState.name.trim(),
                email = currentState.email.trim(),
                password = currentState.password
            )

            when (val result = authRepository.register(credentials)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false, signUpSuccess = true) }
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, generalError = result.message)
                    }
                }
            }
        }
    }

    fun onNavigated() {
        _state.update { it.copy(signUpSuccess = false) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpContent(
    state: SignUpState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onAcceptTermsChange: (Boolean) -> Unit,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(CareRideTheme.spacing.lg)
        ) {
            // Header
            Text(
                text = "Join CareRide",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            Text(
                text = "Create your account to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            // General error
            if (state.generalError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(CareRideTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(
                            text = state.generalError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
            }

            // Name field
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Email field
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Password field
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (state.isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !state.isLoading
            )

            // Password strength indicator
            if (state.password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))
                PasswordStrengthIndicator(strength = state.passwordStrength)
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Confirm password field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (state.isConfirmPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (state.isConfirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (state.isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                isError = state.confirmPasswordError != null,
                supportingText = state.confirmPasswordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Terms checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    contentDescription = "Accept terms checkbox, ${if (state.acceptedTerms) "checked" else "unchecked"}"
                }
            ) {
                Checkbox(
                    checked = state.acceptedTerms,
                    onCheckedChange = onAcceptTermsChange,
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

            // Sign up button
            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
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
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign in link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onSignInClick,
                    enabled = !state.isLoading
                ) {
                    Text("Sign In")
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val progress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> 0.25f
            PasswordStrength.FAIR -> 0.5f
            PasswordStrength.GOOD -> 0.75f
            PasswordStrength.STRONG -> 1f
        },
        label = "strength"
    )

    val color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> Color(0xFFE53935) // Red
            PasswordStrength.FAIR -> Color(0xFFFB8C00) // Orange
            PasswordStrength.GOOD -> Color(0xFFFDD835) // Yellow
            PasswordStrength.STRONG -> Color(0xFF43A047) // Green
        },
        label = "color"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

        Text(
            text = "Password strength: ${strength.label}",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}