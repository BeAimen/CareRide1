package com.shjprofessionals.careride1.domain.repository

import com.shjprofessionals.careride1.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun observeAuthState(): Flow<AuthState>

    fun getAuthState(): AuthState

    suspend fun register(credentials: SignUpCredentials): AuthResult

    suspend fun signIn(credentials: SignInCredentials): AuthResult

    suspend fun signOut()

    suspend fun requestPasswordReset(email: String): Boolean

    suspend fun setRole(role: UserRole): AuthResult

    /**
     * Update user's basic info (name, email)
     */
    suspend fun updateUserInfo(name: String? = null, email: String? = null): AuthResult

    fun getRememberedEmail(): String?

    /**
     * Get current user synchronously
     */
    fun getCurrentUser(): User?
}