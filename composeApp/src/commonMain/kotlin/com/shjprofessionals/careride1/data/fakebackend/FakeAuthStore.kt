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
        // Seed with test accounts
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
                createdAt = now() - 86400000
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

        // Test account without role
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

        if (users.containsKey(email)) {
            return AuthResult.Error("An account with this email already exists")
        }

        val userId = "user_${now()}"
        val user = User(
            id = userId,
            name = credentials.name.trim(),
            email = email,
            role = null,
            createdAt = now()
        )

        users[email] = UserRecord(
            user = user,
            passwordHash = credentials.password.hashCode()
        )

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

        rememberedEmail = if (credentials.rememberMe) email else null

        val user = record.user
        val newState = when (user.role) {
            UserRole.PATIENT -> AuthState.SignedInPatient(user)
            UserRole.DOCTOR -> AuthState.SignedInDoctor(user)
            null -> AuthState.SignedInNoRole(user)
        }
        _authState.value = newState

        // Sync profile stores
        syncProfileStores(user)

        return AuthResult.Success(user)
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        _authState.value = AuthState.SignedOut
        // Clear profile stores
        FakeBackend.patientProfileStore.clear()
    }

    /**
     * Request password reset
     */
    fun requestPasswordReset(email: String): Boolean {
        val normalizedEmail = email.lowercase().trim()
        return users.containsKey(normalizedEmail)
    }

    /**
     * Set user's role
     */
    fun setRole(role: UserRole): AuthResult {
        val currentState = _authState.value
        val currentUser = currentState.currentUser()
            ?: return AuthResult.Error("No user is signed in")

        val email = currentUser.email
        val record = users[email] ?: return AuthResult.Error("User not found")

        val updatedUser = currentUser.copy(role = role)
        users[email] = record.copy(user = updatedUser)

        val newState = when (role) {
            UserRole.PATIENT -> AuthState.SignedInPatient(updatedUser)
            UserRole.DOCTOR -> AuthState.SignedInDoctor(updatedUser)
        }
        _authState.value = newState

        // Sync profile stores
        syncProfileStores(updatedUser)

        return AuthResult.Success(updatedUser)
    }

    /**
     * Update user's basic info (name, email)
     */
    fun updateUserInfo(name: String? = null, email: String? = null): AuthResult {
        val currentUser = _authState.value.currentUser()
            ?: return AuthResult.Error("No user is signed in")

        val currentEmail = currentUser.email
        val record = users[currentEmail] ?: return AuthResult.Error("User not found")

        val newEmail = email?.lowercase()?.trim() ?: currentEmail

        // Check if new email is already taken (by another user)
        if (newEmail != currentEmail && users.containsKey(newEmail)) {
            return AuthResult.Error("Email is already in use")
        }

        val updatedUser = currentUser.copy(
            name = name?.trim() ?: currentUser.name,
            email = newEmail
        )

        // Remove old email entry if email changed
        if (newEmail != currentEmail) {
            users.remove(currentEmail)
        }

        users[newEmail] = record.copy(user = updatedUser)

        // Update auth state
        _authState.value = when (updatedUser.role) {
            UserRole.PATIENT -> AuthState.SignedInPatient(updatedUser)
            UserRole.DOCTOR -> AuthState.SignedInDoctor(updatedUser)
            null -> AuthState.SignedInNoRole(updatedUser)
        }

        return AuthResult.Success(updatedUser)
    }

    /**
     * Sync profile stores with user data
     */
    private fun syncProfileStores(user: User) {
        when (user.role) {
            UserRole.PATIENT -> {
                FakeBackend.patientProfileStore.syncWithAuthUser(user)
            }
            UserRole.DOCTOR -> {
                FakeBackend.doctorProfileStore.syncWithAuthUser(user)
            }
            null -> {
                // No role yet, nothing to sync
            }
        }
    }

    fun getRememberedEmail(): String? = rememberedEmail

    fun isAuthenticated(): Boolean = _authState.value.isAuthenticated()

    fun getCurrentUser(): User? = _authState.value.currentUser()

    fun setLoading() {
        _authState.value = AuthState.Loading
    }

    fun resetToSignedOut() {
        _authState.value = AuthState.SignedOut
    }

    private data class UserRecord(
        val user: User,
        val passwordHash: Int
    )
}