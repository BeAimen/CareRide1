package com.shjprofessionals.careride1.data.repository

import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.model.Message
import com.shjprofessionals.careride1.domain.model.QuickReply
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MessageRepositoryImpl : MessageRepository {

    private val store = FakeBackend.messageStore

    private suspend fun simulateNetworkDelay() {
        delay(300)
    }

    // ============ Patient Methods ============

    override fun getPatientConversations(): Flow<List<Conversation>> = flow {
        simulateNetworkDelay()
        emit(store.getConversationsForPatient())

        store.conversations.collect { _ ->
            emit(store.getConversationsForPatient())
        }
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> = flow {
        simulateNetworkDelay()
        emit(store.getMessagesForConversation(conversationId))

        store.messages.map { it[conversationId] ?: emptyList() }
            .collect { messages ->
                emit(messages.sortedBy { it.timestamp })
            }
    }

    override suspend fun sendPatientMessage(conversationId: String, content: String): Result<Message> {
        simulateNetworkDelay()
        return try {
            val message = store.sendPatientMessage(conversationId, content)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startConversation(doctorId: String): Result<Conversation> {
        simulateNetworkDelay()
        val doctor = FakeBackend.getDoctorById(doctorId)
            ?: return Result.failure(IllegalArgumentException("Doctor not found"))

        return try {
            val conversation = store.startConversation(doctor)
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrCreateConversation(doctorId: String): Result<Conversation> {
        simulateNetworkDelay()
        val doctor = FakeBackend.getDoctorById(doctorId)
            ?: return Result.failure(IllegalArgumentException("Doctor not found"))

        return try {
            val conversation = store.getOrCreateConversation(doctor)
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsReadByPatient(conversationId: String): Result<Unit> {
        simulateNetworkDelay()
        return try {
            store.markAsReadByPatient(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPatientUnreadCount(): Flow<Int> = flow {
        emit(store.getPatientUnreadCount())
        store.conversations.collect { _ ->
            emit(store.getPatientUnreadCount())
        }
    }

    // ============ Doctor Methods ============

    override fun getDoctorConversations(): Flow<List<Conversation>> = flow {
        simulateNetworkDelay()
        emit(store.getConversationsForDoctor())

        store.conversations.collect { _ ->
            emit(store.getConversationsForDoctor())
        }
    }

    override suspend fun sendDoctorMessage(conversationId: String, content: String): Result<Message> {
        simulateNetworkDelay()
        return try {
            val message = store.sendDoctorMessage(conversationId, content)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsReadByDoctor(conversationId: String): Result<Unit> {
        simulateNetworkDelay()
        return try {
            store.markAsReadByDoctor(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDoctorUnreadCount(): Flow<Int> = flow {
        emit(store.getDoctorUnreadCount())
        store.conversations.collect { _ ->
            emit(store.getDoctorUnreadCount())
        }
    }

    override fun getQuickReplies(): List<QuickReply> {
        return store.getQuickReplies()
    }
}
