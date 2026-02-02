package com.shjprofessionals.careride1.domain.model

/**
 * Represents a conversation between a patient and doctor.
 * Can be viewed from either perspective.
 */
data class Conversation(
    val id: String,
    val patientId: String,
    val patientName: String, // Added for doctor view
    val doctorId: String,
    val doctor: Doctor,
    val lastMessage: Message?,
    val unreadCount: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    val lastMessagePreview: String
        get() = lastMessage?.content?.take(50)?.let {
            if (it.length == 50) "$it..." else it
        } ?: "No messages yet"

    val lastMessageTime: String
        get() = lastMessage?.formattedTime ?: ""

    /**
     * Check if the last message was from the patient
     */
    val isLastMessageFromPatient: Boolean
        get() = lastMessage?.senderType == MessageSenderType.PATIENT
}
