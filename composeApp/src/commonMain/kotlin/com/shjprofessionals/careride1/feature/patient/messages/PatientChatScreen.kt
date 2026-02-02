package com.shjprofessionals.careride1.feature.patient.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.ChatBubble
import com.shjprofessionals.careride1.core.designsystem.components.EmergencyDisclaimer
import com.shjprofessionals.careride1.core.designsystem.components.MessageInput
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.feature.patient.subscription.PaywallScreen
import org.koin.core.parameter.parametersOf

data class PatientChatScreen(
    val conversationId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<PatientChatViewModel> { parametersOf(conversationId) }
        val state by viewModel.state.collectAsState()

        PatientChatContent(
            state = state,
            onBackClick = { navigator.pop() },
            onMessageInputChange = viewModel::onMessageInputChange,
            onSend = viewModel::sendMessage,
            onSubscribeClick = { navigator.push(PaywallScreen()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientChatContent(
    state: PatientChatState,
    onBackClick: () -> Unit,
    onMessageInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSubscribeClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val doctor = state.doctor

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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

                        Column {
                            Text(
                                text = doctor?.name ?: "Doctor",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (doctor != null) {
                                Text(
                                    text = doctor.specialty.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                MessageInput(
                    value = state.messageInput,
                    onValueChange = onMessageInputChange,
                    onSend = onSend,
                    enabled = state.isInputEnabled,
                    onSubscribeClick = onSubscribeClick
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Emergency disclaimer at top
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
                        text = if (state.isInputEnabled) {
                            "Send a message to start the conversation"
                        } else {
                            "Subscribe to start messaging this doctor"
                        },
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

                    // Sending indicator
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
