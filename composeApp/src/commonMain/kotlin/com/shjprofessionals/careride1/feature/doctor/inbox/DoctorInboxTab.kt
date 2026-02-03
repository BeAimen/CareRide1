package com.shjprofessionals.careride1.feature.doctor.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
            onRefresh = viewModel::refresh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorInboxContent(
    state: DoctorInboxState,
    onConversationClick: (Conversation) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inbox",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (state.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(text = state.unreadCount.toString())
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LoadingContent(
                isLoading = state.isLoading && state.conversations.isEmpty(),
                isEmpty = state.conversations.isEmpty(),
                data = state.conversations,
                error = state.error,
                loadingContent = {
                    ScreenLoading(message = "Loading messages...")
                },
                emptyContent = {
                    EmptyState(
                        icon = Icons.Default.MailOutline,
                        title = "No messages yet",
                        subtitle = "When patients message you, their conversations will appear here",
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = conversations,
                            key = { it.id }
                        ) { conversation ->
                            DoctorConversationCard(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation) }
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

@Composable
private fun DoctorConversationCard(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val hasUnread = conversation.isLastMessageFromPatient
    val unreadInfo = if (hasUnread) ", new message from patient" else ""

    val fullDescription = buildString {
        append("Conversation with ${conversation.patientName}")
        append(". Last message: ${conversation.lastMessagePreview}")
        append(unreadInfo)
        append(". Tap to open.")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = fullDescription
            }
            .padding(
                horizontal = CareRideTheme.spacing.md,
                vertical = CareRideTheme.spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Patient avatar with initials
        PatientAvatar(
            name = conversation.patientName,
            size = AvatarSize.Large
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.patientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (conversation.isLastMessageFromPatient) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = conversation.lastMessageTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (conversation.isLastMessageFromPatient) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val prefix = if (!conversation.isLastMessageFromPatient) "You: " else ""
                Text(
                    text = prefix + conversation.lastMessagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (conversation.isLastMessageFromPatient) {
                        FontWeight.Medium
                    } else {
                        FontWeight.Normal
                    },
                    color = if (conversation.isLastMessageFromPatient) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (conversation.isLastMessageFromPatient) {
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    ) {}
                }
            }
        }
    }
}