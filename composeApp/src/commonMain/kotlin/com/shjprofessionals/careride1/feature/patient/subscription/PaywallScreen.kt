package com.careride.feature.patient.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.careride.core.designsystem.components.CareRidePrimaryButton
import com.careride.core.designsystem.components.PlanCard
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.domain.model.SubscriptionStatus

class PaywallScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<PaywallViewModel>()
        val state by viewModel.state.collectAsState()

        // Navigate away if already subscribed
        LaunchedEffect(state.subscriptionStatus) {
            if (state.subscriptionStatus.canMessage()) {
                navigator.pop()
            }
        }

        PaywallContent(
            state = state,
            onBackClick = { navigator.pop() },
            onPlanSelect = viewModel::selectPlan,
            onContinue = {
                state.selectedPlan?.let { plan ->
                    navigator.push(MockCheckoutScreen(planId = plan.id))
                }
            },
            onRestorePurchases = viewModel::restorePurchases,
            onDismissMessage = viewModel::clearRestoreMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaywallContent(
    state: PaywallState,
    onBackClick: () -> Unit,
    onPlanSelect: (com.careride.domain.model.SubscriptionPlan) -> Unit,
    onContinue: () -> Unit,
    onRestorePurchases: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show restore message
    LaunchedEffect(state.restoreMessage) {
        state.restoreMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Plan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(CareRideTheme.spacing.md)
                ) {
                    // Header
                    Text(
                        text = "Unlock Direct Doctor Access",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

                    Text(
                        text = "Message any doctor, get responses within 24 hours",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                    // Plan cards
                    state.plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            isSelected = state.selectedPlan?.id == plan.id,
                            onSelect = { onPlanSelect(plan) }
                        )
                        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                    // Restore purchases
                    TextButton(
                        onClick = onRestorePurchases,
                        enabled = !state.isRestoring,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        if (state.isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                        }
                        Text("Restore Purchases")
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                    // Terms
                    Text(
                        text = "By subscribing, you agree to our Terms of Service and Privacy Policy. " +
                                "Subscriptions auto-renew unless cancelled at least 24 hours before the renewal date. " +
                                "You can cancel anytime from your profile settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom CTA
                Surface(
                    shadowElevation = CareRideTheme.elevation.lg,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CareRideTheme.spacing.md)
                    ) {
                        state.selectedPlan?.let { plan ->
                            Text(
                                text = "Total: ${plan.displayPrice} ${plan.billingDescription}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                        }

                        CareRidePrimaryButton(
                            text = "Continue to Checkout",
                            onClick = onContinue,
                            enabled = state.selectedPlan != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}