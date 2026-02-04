package com.shjprofessionals.careride1.data.repository

import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.*
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl : AuthRepository {

    private val store = FakeBackend.authStore

    private suspend fun simulateNetworkDelay() {
        delay(800) // Simulate network latency
    }

    override fun observeAuthState(): Flow<AuthState> = store.authState

    override fun getAuthState(): AuthState = store.authState.value

    override suspend fun register(credentials: SignUpCredentials): AuthResult {
        store.setLoading()
        simulateNetworkDelay()
        return store.register(credentials)
    }

    override suspend fun signIn(credentials: SignInCredentials): AuthResult {
        store.setLoading()
        simulateNetworkDelay()
        return store.signIn(credentials)
    }

    override suspend fun signOut() {
        simulateNetworkDelay()
        store.signOut()
    }

    override suspend fun requestPasswordReset(email: String): Boolean {
        simulateNetworkDelay()
        return store.requestPasswordReset(email)
    }

    override suspend fun setRole(role: UserRole): AuthResult {
        store.setLoading()
        delay(400)
        return store.setRole(role)
    }

    override fun getRememberedEmail(): String? = store.getRememberedEmail()
}