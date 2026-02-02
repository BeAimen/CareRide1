package com.shjprofessionals.careride1.feature.doctor.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.ChatBubble
import com.shjprofessionals.careride1.core.designsystem.components.EmergencyDisclaimer
import com.shjprofessionals.careride1.core.designsystem.components.QuickReplyChip
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.QuickReply
import com.shjprofessionals.careride1.domain.model.QuickReplyCategory
import org.koin.core.parameter.parametersOf

data class DoctorChatScreen(
    val conversationId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<DoctorChatViewModel> { parametersOf(conversationId) }
        val state by viewModel.state.collectAsState()

        DoctorChatContent(
            state = state,
            onBackClick = { navigator.pop() },
            onMessageInputChange = viewModel::onMessageInputChange,
            onSend = viewModel::sendMessage,
            onToggleQuickReplies = viewModel::toggleQuickReplies,
            onQuickReplyClick = viewModel::useQuickReply,
            onDismissQuickReplies = viewModel::hideQuickReplies
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorChatContent(
    state: DoctorChatState,
    onBackClick: () -> Unit,
    onMessageInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleQuickReplies: () -> Unit,
    onQuickReplyClick: (QuickReply) -> Unit,
    onDismissQuickReplies: () -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Patient avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.patientName.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

                        Text(
                            text = state.patientName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                // Quick replies panel
                AnimatedVisibility(
                    visible = state.showQuickReplies,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    QuickRepliesPanel(
                        quickReplies = state.quickReplies,
                        onQuickReplyClick = onQuickReplyClick,
                        onDismiss = onDismissQuickReplies
                    )
                }

                // Message input
                DoctorMessageInput(
                    value = state.messageInput,
                    onValueChange = onMessageInputChange,
                    onSend = onSend,
                    onQuickReplyClick = onToggleQuickReplies,
                    showQuickRepliesActive = state.showQuickReplies,
                    isSending = state.isSending
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Emergency disclaimer
            EmergencyDisclaimer(
                modifier = Modifier.padding(CareRideTheme.spacing.sm)
            )

            // Messages
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(CareRideTheme.spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = CareRideTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm),
                    contentPadding = PaddingValues(vertical = CareRideTheme.spacing.sm)
                ) {
                    items(
                        items = state.messages,
                        key = { it.id }
                    ) { message ->
                        ChatBubble(message = message)
                    }

                    if (state.isSending) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                                Text(
                                    text = "Sending...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun DoctorMessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onQuickReplyClick: () -> Unit,
    showQuickRepliesActive: Boolean,
    isSending: Boolean
) {
    Surface(
        shadowElevation = CareRideTheme.elevation.md,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick reply button
            IconButton(
                onClick = onQuickReplyClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (showQuickRepliesActive) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Quick replies",
                    tint = if (showQuickRepliesActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Type a reply...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                shape = RoundedCornerShape(CareRideTheme.radii.xl),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() })
            )

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))

            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSending,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

@Composable
private fun QuickRepliesPanel(
    quickReplies: List<QuickReply>,
    onQuickReplyClick: (QuickReply) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = CareRideTheme.elevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Replies",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            // Group by category
            QuickReplyCategory.entries.forEach { category ->
                val categoryReplies = quickReplies.filter { it.category == category }
                if (categoryReplies.isNotEmpty()) {
                    Text(
                        text = category.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = CareRideTheme.spacing.xxs)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                    ) {
                        items(categoryReplies) { quickReply ->
                            QuickReplyChip(
                                quickReply = quickReply,
                                onClick = { onQuickReplyClick(quickReply) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))
                }
            }
        }
    }
}
