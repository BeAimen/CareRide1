package com.careride.data.repository

import com.careride.data.fakebackend.FakeBackend
import com.careride.domain.model.Doctor
import com.careride.domain.model.Specialty
import com.careride.domain.repository.DoctorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DoctorRepositoryImpl : DoctorRepository {

    // Simulate network delay for realistic UX testing
    private suspend fun simulateNetworkDelay() {
        delay(500)
    }

    override fun getAllDoctors(): Flow<List<Doctor>> = flow {
        simulateNetworkDelay()
        emit(FakeBackend.searchDoctors(""))
    }

    override fun searchDoctors(query: String): Flow<List<Doctor>> = flow {
        simulateNetworkDelay()
        emit(FakeBackend.searchDoctors(query))
    }

    override fun getDoctorsBySpecialty(specialty: Specialty): Flow<List<Doctor>> = flow {
        simulateNetworkDelay()
        emit(FakeBackend.getDoctorsBySpecialty(specialty))
    }

    override suspend fun getDoctorById(id: String): Doctor? {
        simulateNetworkDelay()
        return FakeBackend.getDoctorById(id)
    }
}