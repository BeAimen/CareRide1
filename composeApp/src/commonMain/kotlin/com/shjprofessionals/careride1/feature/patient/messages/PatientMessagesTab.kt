package com.shjprofessionals.careride1.feature.patient.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.*
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.feature.patient.subscription.PaywallScreen

class PatientMessagesTab : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<PatientMessagesViewModel>()
        val state by viewModel.state.collectAsState()

        PatientMessagesContent(
            state = state,
            onConversationClick = { conversation ->
                navigator.push(PatientChatScreen(conversationId = conversation.id))
            },
            onRetry = viewModel::refresh,
            onRefresh = viewModel::refresh,
            onSubscribe = { navigator.push(PaywallScreen()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientMessagesContent(
    state: PatientMessagesState,
    onConversationClick: (Conversation) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSubscribe: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineSmall
                    )
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
            EmergencyDisclaimer(
                modifier = Modifier.padding(
                    horizontal = CareRideTheme.spacing.md,
                    vertical = CareRideTheme.spacing.sm
                )
            )

            LoadingContent(
                isLoading = state.isLoading && state.conversations.isEmpty(),
                isEmpty = state.conversations.isEmpty(),
                data = state.conversations,
                error = state.error,
                loadingContent = {
                    ScreenLoading(message = "Loading conversations...")
                },
                emptyContent = {
                    EmptyState(
                        icon = Icons.Default.MailOutline,
                        title = "No conversations yet",
                        subtitle = if (state.subscriptionStatus.canMessage()) {
                            "Start a conversation by messaging a doctor from their profile"
                        } else {
                            "Subscribe to start messaging doctors"
                        },
                        modifier = Modifier.weight(1f),
                        action = if (!state.subscriptionStatus.canMessage()) {
                            {
                                CareRidePrimaryButton(
                                    text = "Subscribe Now",
                                    onClick = onSubscribe
                                )
                            }
                        } else null
                    )
                },
                errorContent = {
                    ErrorState(
                        message = state.error?.userMessage ?: "Unknown error",
                        onRetry = onRetry,
                        modifier = Modifier.weight(1f)
                    )
                }
            ) { conversations ->
                RefreshableContent(
                    isRefreshing = state.isLoading && state.conversations.isNotEmpty(),
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = conversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationCard(
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