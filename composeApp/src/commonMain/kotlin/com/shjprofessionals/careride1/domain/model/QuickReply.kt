package com.shjprofessionals.careride1.domain.model

/**
 * Predefined quick reply templates for doctors.
 */
data class QuickReply(
    val id: String,
    val label: String,
    val message: String,
    val category: QuickReplyCategory
)

enum class QuickReplyCategory {
    GREETING,
    SCHEDULING,
    FOLLOW_UP,
    GENERAL
}

/**
 * Predefined quick replies for doctors
 */
object QuickReplies {

    val ALL = listOf(
        QuickReply(
            id = "qr_greeting_1",
            label = "Thank you",
            message = "Thank you for reaching out. I'm happy to help.",
            category = QuickReplyCategory.GREETING
        ),
        QuickReply(
            id = "qr_greeting_2",
            label = "Hello",
            message = "Hello! Thank you for your message. How can I assist you today?",
            category = QuickReplyCategory.GREETING
        ),
        QuickReply(
            id = "qr_schedule_1",
            label = "Schedule visit",
            message = "Based on what you've described, I'd recommend scheduling an in-person visit. Please call our office to book an appointment.",
            category = QuickReplyCategory.SCHEDULING
        ),
        QuickReply(
            id = "qr_schedule_2",
            label = "Availability",
            message = "I have availability this week. Would you like to schedule a consultation?",
            category = QuickReplyCategory.SCHEDULING
        ),
        QuickReply(
            id = "qr_followup_1",
            label = "Need more info",
            message = "Could you please provide more details about your symptoms? This will help me give you better guidance.",
            category = QuickReplyCategory.FOLLOW_UP
        ),
        QuickReply(
            id = "qr_followup_2",
            label = "How are you feeling?",
            message = "How are you feeling today? Any changes since we last spoke?",
            category = QuickReplyCategory.FOLLOW_UP
        ),
        QuickReply(
            id = "qr_general_1",
            label = "Emergency",
            message = "If you're experiencing a medical emergency, please call 911 or go to your nearest emergency room immediately.",
            category = QuickReplyCategory.GENERAL
        ),
        QuickReply(
            id = "qr_general_2",
            label = "Take care",
            message = "Take care and don't hesitate to reach out if you have any more questions.",
            category = QuickReplyCategory.GENERAL
        )
    )

    fun getByCategory(category: QuickReplyCategory): List<QuickReply> {
        return ALL.filter { it.category == category }
    }
}
