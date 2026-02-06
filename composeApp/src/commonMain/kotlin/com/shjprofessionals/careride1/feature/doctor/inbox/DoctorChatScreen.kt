package com.shjprofessionals.careride1.feature.doctor.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.accessibility.AccessibilityDefaults
import com.shjprofessionals.careride1.core.designsystem.components.*
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
            onQuickReplyClick = viewModel::onQuickReplySelected,
            onDismissQuickReplies = viewModel::hideQuickReplies,
            onToggleInstantSend = viewModel::toggleInstantQuickReplySend
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
    onDismissQuickReplies: () -> Unit,
    onToggleInstantSend: () -> Unit
) {
    val listState = rememberLazyListState()

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
                        PatientAvatar(name = state.patientName, size = AvatarSize.Medium)
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(text = state.patientName, style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.semantics { contentDescription = "Go back to inbox" }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column {
                if (!state.showQuickReplies && state.suggestedQuickReplies.isNotEmpty()) {
                    SuggestedQuickRepliesRow(
                        quickReplies = state.suggestedQuickReplies,
                        instantSendEnabled = state.instantQuickReplySend,
                        onQuickReplyClick = onQuickReplyClick,
                        onMoreClick = onToggleQuickReplies
                    )
                }

                AnimatedVisibility(
                    visible = state.showQuickReplies,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    QuickRepliesPanel(
                        quickReplies = state.quickReplies,
                        instantSendEnabled = state.instantQuickReplySend,
                        onToggleInstantSend = onToggleInstantSend,
                        onQuickReplyClick = onQuickReplyClick,
                        onDismiss = onDismissQuickReplies
                    )
                }

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
            EmergencyDisclaimer(modifier = Modifier.padding(CareRideTheme.spacing.sm))

            LoadingContent(
                isLoading = state.isLoading,
                isEmpty = state.messages.isEmpty(),
                data = state.messages,
                loadingContent = { ScreenLoading(message = "Loading messages...") },
                emptyContent = {
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
                }
            ) { messages ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = CareRideTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm),
                    contentPadding = PaddingValues(vertical = CareRideTheme.spacing.sm)
                ) {
                    items(items = messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }

                    if (state.isSending) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                InlineLoading(size = 16, contentDescription = "Sending message")
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
private fun SuggestedQuickRepliesRow(
    quickReplies: List<QuickReply>,
    instantSendEnabled: Boolean,
    onQuickReplyClick: (QuickReply) -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = CareRideTheme.elevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CareRideTheme.spacing.sm, vertical = CareRideTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
            ) {
                items(quickReplies) { qr ->
                    QuickReplyChip(quickReply = qr, onClick = { onQuickReplyClick(qr) })
                }
            }

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

            TextButton(
                onClick = onMoreClick,
                modifier = Modifier.semantics { contentDescription = "Open all quick replies" }
            ) {
                Text(if (instantSendEnabled) "More" else "More")
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
    Surface(shadowElevation = CareRideTheme.elevation.md, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onQuickReplyClick,
                modifier = Modifier
                    .size(AccessibilityDefaults.MinTouchTargetDp.dp)
                    .semantics {
                        contentDescription = if (showQuickRepliesActive) "Close quick replies" else "Open quick replies"
                    }
            ) {
                Icon(
                    imageVector = if (showQuickRepliesActive) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (showQuickRepliesActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a reply...", style = MaterialTheme.typography.bodyMedium) },
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
                modifier = Modifier
                    .size(AccessibilityDefaults.MinTouchTargetDp.dp)
                    .semantics {
                        contentDescription =
                            if (isSending) "Sending message"
                            else if (value.isNotBlank()) "Send message"
                            else "Send message, disabled until you type a message"
                    }
            ) {
                if (isSending) InlineLoading(size = 20, contentDescription = "Sending")
                else Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}

@Composable
private fun QuickRepliesPanel(
    quickReplies: List<QuickReply>,
    instantSendEnabled: Boolean,
    onToggleInstantSend: () -> Unit,
    onQuickReplyClick: (QuickReply) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = CareRideTheme.elevation.sm) {
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
                Column {
                    Text(
                        text = "Quick Replies",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (instantSendEnabled) {
                            "Tap sends instantly"
                        } else {
                            "Tap inserts into the message box"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = instantSendEnabled,
                        onCheckedChange = { onToggleInstantSend() }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(AccessibilityDefaults.MinTouchTargetDp.dp)
                            .semantics { contentDescription = "Close quick replies panel" }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            enumValues<QuickReplyCategory>().forEach { category ->
                val categoryReplies = quickReplies.filter { it.category == category }
                if (categoryReplies.isNotEmpty()) {
                    Text(
                        text = category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = CareRideTheme.spacing.xxs)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)) {
                        items(categoryReplies) { qr ->
                            QuickReplyChip(quickReply = qr, onClick = { onQuickReplyClick(qr) })
                        }
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))
                }
            }
        }
    }
}
