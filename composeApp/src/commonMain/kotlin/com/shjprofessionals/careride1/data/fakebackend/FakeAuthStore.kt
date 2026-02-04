package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

/**
 * In-memory authentication store for mock backend.
 * Simulates user registration, login, and session management.
 */
class FakeAuthStore {

    // Registered users (email -> UserRecord)
    private val users = mutableMapOf<String, UserRecord>()

    // Current auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Remembered email for "Remember me" feature
    private var rememberedEmail: String? = null

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    init {
        // Seed with a test account for easy testing
        seedTestAccounts()
    }

    private fun seedTestAccounts() {
        // Test patient account
        val patientId = "user_patient_001"
        users["patient@test.com"] = UserRecord(
            user = User(
                id = patientId,
                name = "Test Patient",
                email = "patient@test.com",
                role = UserRole.PATIENT,
                createdAt = now() - 86400000 // 1 day ago
            ),
            passwordHash = "password123".hashCode()
        )

        // Test doctor account
        val doctorId = "user_doctor_001"
        users["doctor@test.com"] = UserRecord(
            user = User(
                id = doctorId,
                name = "Test Doctor",
                email = "doctor@test.com",
                role = UserRole.DOCTOR,
                createdAt = now() - 86400000
            ),
            passwordHash = "password123".hashCode()
        )

        // Test account without role (for testing role selection)
        val newUserId = "user_new_001"
        users["new@test.com"] = UserRecord(
            user = User(
                id = newUserId,
                name = "New User",
                email = "new@test.com",
                role = null,
                createdAt = now()
            ),
            passwordHash = "password123".hashCode()
        )
    }

    /**
     * Register a new user
     */
    fun register(credentials: SignUpCredentials): AuthResult {
        val email = credentials.email.lowercase().trim()

        // Check if email already exists
        if (users.containsKey(email)) {
            return AuthResult.Error("An account with this email already exists")
        }

        // Create new user
        val userId = "user_${now()}"
        val user = User(
            id = userId,
            name = credentials.name.trim(),
            email = email,
            role = null, // Role will be selected after signup
            createdAt = now()
        )

        users[email] = UserRecord(
            user = user,
            passwordHash = credentials.password.hashCode()
        )

        // Auto-login after registration
        _authState.value = AuthState.SignedInNoRole(user)

        return AuthResult.Success(user)
    }

    /**
     * Sign in with email and password
     */
    fun signIn(credentials: SignInCredentials): AuthResult {
        val email = credentials.email.lowercase().trim()

        val record = users[email]
            ?: return AuthResult.Error("No account found with this email")

        if (record.passwordHash != credentials.password.hashCode()) {
            return AuthResult.Error("Incorrect password")
        }

        // Remember email if requested
        rememberedEmail = if (credentials.rememberMe) email else null

        // Set auth state based on user's role
        val user = record.user
        _authState.value = when (user.role) {
            UserRole.PATIENT -> AuthState.SignedInPatient(user)
            UserRole.DOCTOR -> AuthState.SignedInDoctor(user)
            null -> AuthState.SignedInNoRole(user)
        }

        return AuthResult.Success(user)
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        _authState.value = AuthState.SignedOut
    }

    /**
     * Request password reset (mock - just validates email exists)
     */
    fun requestPasswordReset(email: String): Boolean {
        val normalizedEmail = email.lowercase().trim()
        return users.containsKey(normalizedEmail)
    }

    /**
     * Set user's role (after signup or role change)
     */
    fun setRole(role: UserRole): AuthResult {
        val currentState = _authState.value
        val currentUser = currentState.currentUser()
            ?: return AuthResult.Error("No user is signed in")

        // Update user record
        val email = currentUser.email
        val record = users[email] ?: return AuthResult.Error("User not found")

        val updatedUser = currentUser.copy(role = role)
        users[email] = record.copy(user = updatedUser)

        // Update auth state
        _authState.value = when (role) {
            UserRole.PATIENT -> AuthState.SignedInPatient(updatedUser)
            UserRole.DOCTOR -> AuthState.SignedInDoctor(updatedUser)
        }

        return AuthResult.Success(updatedUser)
    }

    /**
     * Get remembered email (for "Remember me" feature)
     */
    fun getRememberedEmail(): String? = rememberedEmail

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean = _authState.value.isAuthenticated()

    /**
     * Get current user
     */
    fun getCurrentUser(): User? = _authState.value.currentUser()

    /**
     * Set loading state
     */
    fun setLoading() {
        _authState.value = AuthState.Loading
    }

    /**
     * Reset to signed out (for error recovery)
     */
    fun resetToSignedOut() {
        _authState.value = AuthState.SignedOut
    }

    /**
     * Internal user record with password hash
     */
    private data class UserRecord(
        val user: User,
        val passwordHash: Int
    )
}