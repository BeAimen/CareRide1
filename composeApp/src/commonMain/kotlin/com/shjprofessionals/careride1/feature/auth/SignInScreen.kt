package com.shjprofessionals.careride1.feature.auth

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

class SignInScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<SignInViewModel>()
        val state by viewModel.state.collectAsState()

        // Handle successful sign in
        LaunchedEffect(state.signInSuccess) {
            if (state.signInSuccess) {
                viewModel.onNavigated()
                // Navigation will be handled by AuthNavigator observing auth state
            }
        }

        SignInContent(
            state = state,
            onBackClick = { navigator.pop() },
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRememberMeChange = viewModel::onRememberMeChange,
            onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
            onSignInClick = viewModel::signIn,
            onForgotPasswordClick = { navigator.push(ForgotPasswordScreen()) },
            onCreateAccountClick = { navigator.replace(SignUpScreen()) }
        )
    }
}

data class SignInState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val signInSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank() &&
                emailError == null && passwordError == null
}

class SignInViewModel(
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    init {
        // Pre-fill remembered email
        authRepository.getRememberedEmail()?.let { email ->
            _state.update { it.copy(email = email, rememberMe = true) }
        }
    }

    fun onEmailChange(value: String) {
        _state.update {
            it.copy(
                email = value,
                emailError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _state.update {
            it.copy(
                password = value,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun onRememberMeChange(value: Boolean) {
        _state.update { it.copy(rememberMe = value) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun signIn() {
        val currentState = _state.value

        // Validate
        var hasError = false

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

        if (hasError) return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, generalError = null) }

            val credentials = SignInCredentials(
                email = currentState.email,
                password = currentState.password,
                rememberMe = currentState.rememberMe
            )

            when (val result = authRepository.signIn(credentials)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false, signInSuccess = true) }
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
        _state.update { it.copy(signInSuccess = false) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignInContent(
    state: SignInState,
    onBackClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign In") },
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
                .padding(CareRideTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome text
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

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

            // Email field
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Email input field" },
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

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Password field
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = onPasswordVisibilityToggle,
                        modifier = Modifier.semantics {
                            contentDescription = if (state.isPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = null
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
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Password input field" },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (state.isFormValid) onSignInClick()
                    }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Remember me + Forgot password row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics {
                        contentDescription = "Remember me checkbox, ${if (state.rememberMe) "checked" else "unchecked"}"
                    }
                ) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = onRememberMeChange,
                        enabled = !state.isLoading
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onForgotPasswordClick,
                    enabled = !state.isLoading
                ) {
                    Text("Forgot password?")
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

            // Sign in button
            Button(
                onClick = onSignInClick,
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
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create account link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onCreateAccountClick,
                    enabled = !state.isLoading
                ) {
                    Text("Create one")
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
        }
    }
}