package com.shjprofessionals.careride1.domain.repository

import com.shjprofessionals.careride1.domain.model.AuthResult
import com.shjprofessionals.careride1.domain.model.AuthState
import com.shjprofessionals.careride1.domain.model.SignInCredentials
import com.shjprofessionals.careride1.domain.model.SignUpCredentials
import com.shjprofessionals.careride1.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Get current auth state
     */
    fun getAuthState(): AuthState

    /**
     * Register a new user
     */
    suspend fun register(credentials: SignUpCredentials): AuthResult

    /**
     * Sign in with credentials
     */
    suspend fun signIn(credentials: SignInCredentials): AuthResult

    /**
     * Sign out current user
     */
    suspend fun signOut()

    /**
     * Request password reset email
     */
    suspend fun requestPasswordReset(email: String): Boolean

    /**
     * Set user's role
     */
    suspend fun setRole(role: UserRole): AuthResult

    /**
     * Get remembered email for "Remember me" feature
     */
    fun getRememberedEmail(): String?
}