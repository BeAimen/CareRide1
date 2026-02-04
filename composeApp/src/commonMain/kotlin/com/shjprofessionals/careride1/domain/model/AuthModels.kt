package com.shjprofessionals.careride1.domain.model

/**
 * Represents an authenticated user
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole?,
    val createdAt: Long
) {
    val needsRoleSelection: Boolean get() = role == null

    val firstName: String get() = name.split(" ").firstOrNull() ?: name
}

/**
 * Authentication state machine
 */
sealed class AuthState {
    /** User is not authenticated */
    data object SignedOut : AuthState()

    /** Authentication in progress */
    data object Loading : AuthState()

    /** User signed in but hasn't selected a role yet */
    data class SignedInNoRole(val user: User) : AuthState()

    /** User signed in as Patient */
    data class SignedInPatient(val user: User) : AuthState()

    /** User signed in as Doctor */
    data class SignedInDoctor(val user: User) : AuthState()

    fun isAuthenticated(): Boolean = this !is SignedOut && this !is Loading

    fun currentUser(): User? = when (this) {
        is SignedInNoRole -> user
        is SignedInPatient -> user
        is SignedInDoctor -> user
        else -> null
    }
}

/**
 * Credentials for sign-in
 */
data class SignInCredentials(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

/**
 * Credentials for sign-up
 */
data class SignUpCredentials(
    val name: String,
    val email: String,
    val password: String
)

/**
 * Auth operation result
 */
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Password strength levels
 */
enum class PasswordStrength(val label: String, val minScore: Int) {
    WEAK("Weak", 0),
    FAIR("Fair", 2),
    GOOD("Good", 3),
    STRONG("Strong", 4);

    companion object {
        fun calculate(password: String): PasswordStrength {
            var score = 0

            if (password.length >= 8) score++
            if (password.length >= 12) score++
            if (password.any { it.isUpperCase() }) score++
            if (password.any { it.isLowerCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++

            return entries.lastOrNull { score >= it.minScore } ?: WEAK
        }
    }
}