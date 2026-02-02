package com.careride.domain.repository

import com.careride.domain.model.Conversation
import com.careride.domain.model.Message
import com.careride.domain.model.QuickReply
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    // ============ Patient Methods ============

    /**
     * Get all conversations for the current patient
     */
    fun getPatientConversations(): Flow<List<Conversation>>

    /**
     * Get messages for a specific conversation
     */
    fun getMessages(conversationId: String): Flow<List<Message>>

    /**
     * Send a message in a conversation (as patient)
     */
    suspend fun sendPatientMessage(conversationId: String, content: String): Result<Message>

    /**
     * Start a new conversation with a doctor
     */
    suspend fun startConversation(doctorId: String): Result<Conversation>

    /**
     * Get or create a conversation with a doctor
     */
    suspend fun getOrCreateConversation(doctorId: String): Result<Conversation>

    /**
     * Mark messages as read (patient perspective)
     */
    suspend fun markAsReadByPatient(conversationId: String): Result<Unit>

    /**
     * Get unread message count for patient
     */
    fun getPatientUnreadCount(): Flow<Int>

    // ============ Doctor Methods ============

    /**
     * Get all conversations for the current doctor
     */
    fun getDoctorConversations(): Flow<List<Conversation>>

    /**
     * Send a message in a conversation (as doctor)
     */
    suspend fun sendDoctorMessage(conversationId: String, content: String): Result<Message>

    /**
     * Mark messages as read (doctor perspective)
     */
    suspend fun markAsReadByDoctor(conversationId: String): Result<Unit>

    /**
     * Get unread message count for doctor
     */
    fun getDoctorUnreadCount(): Flow<Int>

    /**
     * Get available quick replies
     */
    fun getQuickReplies(): List<QuickReply>

    // ============ Legacy Methods (for backward compatibility) ============

    @Deprecated("Use getPatientConversations instead")
    fun getConversations(): Flow<List<Conversation>> = getPatientConversations()

    @Deprecated("Use sendPatientMessage instead")
    suspend fun sendMessage(conversationId: String, content: String): Result<Message> =
        sendPatientMessage(conversationId, content)

    @Deprecated("Use markAsReadByPatient instead")
    suspend fun markAsRead(conversationId: String): Result<Unit> =
        markAsReadByPatient(conversationId)

    @Deprecated("Use getPatientUnreadCount instead")
    fun getUnreadCount(): Flow<Int> = getPatientUnreadCount()
}