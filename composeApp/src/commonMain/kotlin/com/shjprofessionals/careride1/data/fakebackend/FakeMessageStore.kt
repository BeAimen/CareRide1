package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.Message
import com.shjprofessionals.careride1.domain.model.MessageSenderType
import com.shjprofessionals.careride1.domain.model.QuickReplies
import com.shjprofessionals.careride1.domain.model.QuickReply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

class FakeMessageStore {

    // Current user IDs (simulated logged-in users)
    private val currentPatientId = "patient_001"
    private val currentPatientName = "John Smith"
    private val currentDoctorId = "doc_001" // Dr. Sarah Chen

    private val _conversations = MutableStateFlow<Map<String, Conversation>>(emptyMap())
    val conversations: StateFlow<Map<String, Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Track unread counts separately for patient and doctor
    private val patientUnreadCounts = mutableMapOf<String, Int>()
    private val doctorUnreadCounts = mutableMapOf<String, Int>()

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    init {
        seedSampleData()
    }

    private fun seedSampleData() {
        val doctor1 = FakeBackend.getDoctorById("doc_002") ?: return
        val doctor2 = FakeBackend.getDoctorById("doc_004") ?: return
        val doctorSelf = FakeBackend.getDoctorById(currentDoctorId) ?: return

        val conv1Id = "conv_001"
        val conv2Id = "conv_002"
        val conv3Id = "conv_003" // Conversation with current doctor

        val messages1 = listOf(
            Message(
                id = "msg_001",
                conversationId = conv1Id,
                senderId = currentPatientId,
                senderType = MessageSenderType.PATIENT,
                content = "Hi Dr. Roberts, I've been having some headaches lately. Should I be concerned?",
                timestamp = now() - (2 * 60 * 60 * 1000),
                isRead = true
            ),
            Message(
                id = "msg_002",
                conversationId = conv1Id,
                senderId = "doc_002",
                senderType = MessageSenderType.DOCTOR,
                content = "Hello! I'd be happy to help. Can you tell me more about the headaches? How often do they occur and where is the pain located?",
                timestamp = now() - (1 * 60 * 60 * 1000),
                isRead = true
            ),
            Message(
                id = "msg_003",
                conversationId = conv1Id,
                senderId = currentPatientId,
                senderType = MessageSenderType.PATIENT,
                content = "They happen about 2-3 times a week, usually in the afternoon. The pain is mostly around my temples.",
                timestamp = now() - (30 * 60 * 1000),
                isRead = true
            )
        )

        val messages2 = listOf(
            Message(
                id = "msg_004",
                conversationId = conv2Id,
                senderId = currentPatientId,
                senderType = MessageSenderType.PATIENT,
                content = "Dr. Park, my child has a fever of 101°F. What should I do?",
                timestamp = now() - (24 * 60 * 60 * 1000),
                isRead = true
            ),
            Message(
                id = "msg_005",
                conversationId = conv2Id,
                senderId = "doc_004",
                senderType = MessageSenderType.DOCTOR,
                content = "For a fever of 101°F, you can give children's acetaminophen or ibuprofen as directed. Make sure they stay hydrated. If the fever persists beyond 3 days or goes above 103°F, please bring them in.",
                timestamp = now() - (23 * 60 * 60 * 1000),
                isRead = false
            )
        )

        // Conversation with current doctor (Dr. Sarah Chen) - visible in doctor inbox
        val messages3 = listOf(
            Message(
                id = "msg_006",
                conversationId = conv3Id,
                senderId = "patient_002",
                senderType = MessageSenderType.PATIENT,
                content = "Hi Dr. Chen, I've been experiencing chest pain occasionally. Is this something I should be worried about?",
                timestamp = now() - (4 * 60 * 60 * 1000),
                isRead = true
            ),
            Message(
                id = "msg_007",
                conversationId = conv3Id,
                senderId = currentDoctorId,
                senderType = MessageSenderType.DOCTOR,
                content = "Thank you for reaching out. Chest pain can have many causes. Can you describe the pain? Is it sharp or dull? Does it occur during physical activity?",
                timestamp = now() - (3 * 60 * 60 * 1000),
                isRead = true
            ),
            Message(
                id = "msg_008",
                conversationId = conv3Id,
                senderId = "patient_002",
                senderType = MessageSenderType.PATIENT,
                content = "It's more of a dull ache. It happens randomly, not really during exercise. Sometimes when I'm stressed.",
                timestamp = now() - (45 * 60 * 1000),
                isRead = false
            )
        )

        val conv1 = Conversation(
            id = conv1Id,
            patientId = currentPatientId,
            patientName = currentPatientName,
            doctorId = "doc_002",
            doctor = doctor1,
            lastMessage = messages1.last(),
            unreadCount = 0,
            createdAt = now() - (3 * 60 * 60 * 1000),
            updatedAt = now() - (30 * 60 * 1000)
        )

        val conv2 = Conversation(
            id = conv2Id,
            patientId = currentPatientId,
            patientName = currentPatientName,
            doctorId = "doc_004",
            doctor = doctor2,
            lastMessage = messages2.last(),
            unreadCount = 1,
            createdAt = now() - (24 * 60 * 60 * 1000),
            updatedAt = now() - (23 * 60 * 60 * 1000)
        )

        val conv3 = Conversation(
            id = conv3Id,
            patientId = "patient_002",
            patientName = "Emily Johnson",
            doctorId = currentDoctorId,
            doctor = doctorSelf,
            lastMessage = messages3.last(),
            unreadCount = 1,
            createdAt = now() - (4 * 60 * 60 * 1000),
            updatedAt = now() - (45 * 60 * 1000)
        )

        _conversations.value = mapOf(
            conv1Id to conv1,
            conv2Id to conv2,
            conv3Id to conv3
        )
        _messages.value = mapOf(
            conv1Id to messages1,
            conv2Id to messages2,
            conv3Id to messages3
        )

        // Set initial unread counts
        patientUnreadCounts[conv2Id] = 1
        doctorUnreadCounts[conv3Id] = 1
    }

    // ============ Patient Methods ============

    fun getConversationsForPatient(): List<Conversation> {
        return _conversations.value.values
            .filter { it.patientId == currentPatientId }
            .sortedByDescending { it.updatedAt }
    }

    fun getMessagesForConversation(conversationId: String): List<Message> {
        return _messages.value[conversationId]?.sortedBy { it.timestamp } ?: emptyList()
    }

    fun sendPatientMessage(conversationId: String, content: String): Message {
        val messageId = "msg_${now()}"
        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = currentPatientId,
            senderType = MessageSenderType.PATIENT,
            content = content,
            timestamp = now(),
            isRead = true
        )

        addMessage(conversationId, message)

        // Increment doctor's unread count
        val conversation = _conversations.value[conversationId]
        if (conversation != null) {
            doctorUnreadCounts[conversationId] = (doctorUnreadCounts[conversationId] ?: 0) + 1
        }

        return message
    }

    fun markAsReadByPatient(conversationId: String) {
        patientUnreadCounts[conversationId] = 0
        updateConversationUnreadCount(conversationId, 0)
    }

    fun getPatientUnreadCount(): Int {
        return patientUnreadCounts.values.sum()
    }

    // ============ Doctor Methods ============

    fun getConversationsForDoctor(): List<Conversation> {
        return _conversations.value.values
            .filter { it.doctorId == currentDoctorId }
            .sortedByDescending { it.updatedAt }
    }

    fun sendDoctorMessage(conversationId: String, content: String): Message {
        val messageId = "msg_${now()}"
        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = currentDoctorId,
            senderType = MessageSenderType.DOCTOR,
            content = content,
            timestamp = now(),
            isRead = true
        )

        addMessage(conversationId, message)

        // Increment patient's unread count
        val conversation = _conversations.value[conversationId]
        if (conversation != null) {
            patientUnreadCounts[conversationId] = (patientUnreadCounts[conversationId] ?: 0) + 1
        }

        return message
    }

    fun markAsReadByDoctor(conversationId: String) {
        doctorUnreadCounts[conversationId] = 0
    }

    fun getDoctorUnreadCount(): Int {
        return _conversations.value.values
            .filter { it.doctorId == currentDoctorId }
            .sumOf { doctorUnreadCounts[it.id] ?: 0 }
    }

    // ============ Shared Methods ============

    private fun addMessage(conversationId: String, message: Message) {
        val currentMessages = _messages.value[conversationId] ?: emptyList()
        _messages.value = _messages.value + (conversationId to (currentMessages + message))

        val conversation = _conversations.value[conversationId]
        if (conversation != null) {
            val updated = conversation.copy(
                lastMessage = message,
                updatedAt = now()
            )
            _conversations.value = _conversations.value + (conversationId to updated)
        }
    }

    private fun updateConversationUnreadCount(conversationId: String, count: Int) {
        val conversation = _conversations.value[conversationId]
        if (conversation != null) {
            val updated = conversation.copy(unreadCount = count)
            _conversations.value = _conversations.value + (conversationId to updated)
        }
    }

    fun startConversation(doctor: Doctor): Conversation {
        val existing = _conversations.value.values.find {
            it.patientId == currentPatientId && it.doctorId == doctor.id
        }
        if (existing != null) return existing

        val conversationId = "conv_${now()}"
        val conversation = Conversation(
            id = conversationId,
            patientId = currentPatientId,
            patientName = currentPatientName,
            doctorId = doctor.id,
            doctor = doctor,
            lastMessage = null,
            unreadCount = 0,
            createdAt = now(),
            updatedAt = now()
        )

        _conversations.value = _conversations.value + (conversationId to conversation)
        _messages.value = _messages.value + (conversationId to emptyList())

        return conversation
    }

    fun getOrCreateConversation(doctor: Doctor): Conversation {
        val existing = _conversations.value.values.find {
            it.patientId == currentPatientId && it.doctorId == doctor.id
        }
        return existing ?: startConversation(doctor)
    }

    fun getConversationByDoctorId(doctorId: String): Conversation? {
        return _conversations.value.values.find {
            it.patientId == currentPatientId && it.doctorId == doctorId
        }
    }

    fun getQuickReplies(): List<QuickReply> = QuickReplies.ALL
}
