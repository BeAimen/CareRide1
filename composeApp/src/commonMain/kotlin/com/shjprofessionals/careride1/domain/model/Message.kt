package com.shjprofessionals.careride1.domain.model

import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderType: MessageSenderType,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
) {
    val formattedTime: String
        get() {
            val now = Clock.System.now().toEpochMilliseconds()
            val diff = now - timestamp

            val minutes = diff / (1000 * 60)
            val hours = diff / (1000 * 60 * 60)
            val days = diff / (1000 * 60 * 60 * 24)

            return when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days < 7 -> "${days}d ago"
                else -> {
                    val instant = Instant.fromEpochMilliseconds(timestamp)
                    val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                    "${localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${localDateTime.dayOfMonth}"
                }
            }
        }
}

enum class MessageSenderType {
    PATIENT,
    DOCTOR
}
