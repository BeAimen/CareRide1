package com.shjprofessionals.careride1.feature.doctor.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.*
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.Conversation

class DoctorInboxTab : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<DoctorInboxViewModel>()
        val state by viewModel.state.collectAsState()

        DoctorInboxContent(
            state = state,
            onConversationClick = { conversation ->
                navigator.push(DoctorChatScreen(conversationId = conversation.id))
            },
            onRetry = viewModel::refresh,
            onRefresh = viewModel::refresh,
            onFilterChange = viewModel::setFilter,
            onMarkRead = viewModel::markConversationRead,
            onArchive = viewModel::archiveConversation
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorInboxContent(
    state: DoctorInboxState,
    onConversationClick: (Conversation) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (DoctorInboxFilter) -> Unit,
    onMarkRead: (String) -> Unit,
    onArchive: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Inbox", style = MaterialTheme.typography.headlineSmall)
                        if (state.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) { Text(text = state.unreadCount.toString()) }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            InboxFilters(
                selected = state.filter,
                needsReplyCount = state.needsReplyCount,
                onChange = onFilterChange
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LoadingContent(
                    isLoading = state.isLoading && state.conversations.isEmpty(),
                    isEmpty = state.conversations.isEmpty(),
                    data = state.conversations,
                    error = state.error,
                    loadingContent = { ScreenLoading(message = "Loading messages...") },
                    emptyContent = {
                        EmptyState(
                            icon = Icons.Default.MailOutline,
                            title = if (state.filter == DoctorInboxFilter.NEEDS_REPLY) {
                                "No conversations need a reply"
                            } else {
                                "No messages yet"
                            },
                            subtitle = if (state.filter == DoctorInboxFilter.NEEDS_REPLY) {
                                "You're all caught up"
                            } else {
                                "When patients message you, their conversations will appear here"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    errorContent = {
                        ErrorState(
                            message = state.error?.userMessage ?: "Unknown error",
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                ) { conversations ->
                    RefreshableContent(
                        isRefreshing = state.isLoading && state.conversations.isNotEmpty(),
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = conversations, key = { it.id }) { conversation ->
                                ConversationRow(
                                    conversation = conversation,
                                    onOpen = { onConversationClick(conversation) },
                                    onMarkRead = { onMarkRead(conversation.id) },
                                    onArchive = { onArchive(conversation.id) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxFilters(
    selected: DoctorInboxFilter,
    needsReplyCount: Int,
    onChange: (DoctorInboxFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CareRideTheme.spacing.md, vertical = CareRideTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
    ) {
        FilterChip(
            selected = selected == DoctorInboxFilter.ALL,
            onClick = { onChange(DoctorInboxFilter.ALL) },
            label = { Text("All") }
        )

        FilterChip(
            selected = selected == DoctorInboxFilter.NEEDS_REPLY,
            onClick = { onChange(DoctorInboxFilter.NEEDS_REPLY) },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Needs reply")
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = { onChange(DoctorInboxFilter.NEEDS_REPLY) },
                        label = { Text(needsReplyCount.toString()) }
                    )
                }
            }
        )
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onOpen: () -> Unit,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit
) {
    val needsReply = conversation.isLastMessageFromPatient
    val isUnread = conversation.unreadCount > 0

    val desc = buildString {
        append("Conversation with ${conversation.patientName}. ")
        append("Last message: ${conversation.lastMessagePreview}. ")
        if (isUnread) append("Unread messages. ")
        if (needsReply) append("Needs reply. ")
        append("Tap to open.")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .semantics {
                role = Role.Button
                contentDescription = desc
            }
            .padding(horizontal = CareRideTheme.spacing.md, vertical = CareRideTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PatientAvatar(name = conversation.patientName, size = AvatarSize.Large)

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.patientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (needsReply) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

                Text(
                    text = conversation.lastMessageTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

            val prefix = if (!conversation.isLastMessageFromPatient) "You: " else ""
            Text(
                text = prefix + conversation.lastMessagePreview,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (needsReply) FontWeight.Medium else FontWeight.Normal,
                color = if (needsReply) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isUnread) {
                FilledTonalIconButton(
                    onClick = onMarkRead
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "Mark as read")
                }
            }

            FilledTonalIconButton(
                onClick = onArchive
            ) {
                Icon(imageVector = Icons.Default.Archive, contentDescription = "Archive conversation")
            }
        }
    }
}
