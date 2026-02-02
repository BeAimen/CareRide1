package com.shjprofessionals.careride1.data.repository

import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.Specialty
import com.shjprofessionals.careride1.domain.repository.DoctorRepository
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
