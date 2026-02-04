package com.shjprofessionals.careride1.feature.patient.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.*
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.feature.patient.doctorprofile.DoctorProfileScreen
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
            onDoctorClick = { doctorId ->
                navigator.push(DoctorProfileScreen(doctorId = doctorId))
            },
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
    onDoctorClick: (String) -> Unit,
    onMessageInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSubscribeClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val doctor = state.doctor

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .then(
                                if (doctor != null) {
                                    Modifier
                                        .clickable { onDoctorClick(doctor.id) }
                                        .semantics {
                                            contentDescription = "View ${doctor.name}'s profile"
                                        }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(vertical = CareRideTheme.spacing.xs) // Increase touch target
                    ) {
                        // Doctor avatar with initials
                        DoctorAvatar(
                            name = doctor?.name,
                            size = AvatarSize.Medium
                        )

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
            EmergencyDisclaimer(
                modifier = Modifier.padding(CareRideTheme.spacing.sm)
            )

            LoadingContent(
                isLoading = state.isLoading,
                isEmpty = state.messages.isEmpty(),
                data = state.messages,
                loadingContent = {
                    ScreenLoading(message = "Loading messages...")
                },
                emptyContent = {
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
                    items(
                        items = messages,
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