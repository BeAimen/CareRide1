package com.shjprofessionals.careride1.feature.patient.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.CareRidePrimaryButton
import com.shjprofessionals.careride1.core.designsystem.components.CareRideSecondaryButton
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import org.koin.core.parameter.parametersOf

data class MockCheckoutScreen(
    val planId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<MockCheckoutViewModel> { parametersOf(planId) }
        val state by viewModel.state.collectAsState()

        MockCheckoutContent(
            state = state,
            onBackClick = { navigator.pop() },
            onPayClick = viewModel::processPayment,
            onSimulateFailure = viewModel::simulateFailure,
            onCancel = {
                viewModel.cancelCheckout()
                navigator.pop()
            },
            onRetry = viewModel::retry,
            onDone = {
                // Pop back to where we started (past paywall)
                navigator.popUntil { screen ->
                    screen !is MockCheckoutScreen && screen !is PaywallScreen
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockCheckoutContent(
    state: MockCheckoutState,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit,
    onSimulateFailure: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = {
            if (state.step == CheckoutStep.REVIEW) {
                TopAppBar(
                    title = { Text("Checkout") },
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
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.step) {
                CheckoutStep.REVIEW -> {
                    ReviewStep(
                        state = state,
                        onPayClick = onPayClick,
                        onSimulateFailure = onSimulateFailure,
                        onCancel = onCancel
                    )
                }

                CheckoutStep.PROCESSING -> {
                    ProcessingStep()
                }

                CheckoutStep.SUCCESS -> {
                    SuccessStep(
                        status = state.subscriptionStatus,
                        onDone = onDone
                    )
                }

                CheckoutStep.FAILURE -> {
                    FailureStep(
                        error = state.error?.userMessage ?: "Payment failed",
                        onRetry = onRetry,
                        onCancel = onCancel
                    )
                }

                CheckoutStep.CANCELLED -> {
                    // Will be popped immediately
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(
    state: MockCheckoutState,
    onPayClick: () -> Unit,
    onSimulateFailure: () -> Unit,
    onCancel: () -> Unit
) {
    val plan = state.plan ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CareRideTheme.spacing.md)
    ) {
        // Mock payment disclaimer
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧪",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Text(
                    text = "This is a mock checkout. No real payment will be processed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        // Order summary
        Text(
            text = "Order Summary",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        // Plan details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(CareRideTheme.spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CareRide ${plan.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = plan.displayPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

                Text(
                    text = "Billed ${plan.billingDescription}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total due today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = plan.displayPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        // Renewal info
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md)
            ) {
                Text(
                    text = "📅 Renewal Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))
                Text(
                    text = "Your subscription will automatically renew ${plan.billingDescription} at ${plan.displayPrice}. " +
                            "You can cancel anytime from your profile settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        CareRidePrimaryButton(
            text = "Pay ${plan.displayPrice}",
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        // Test buttons (for demo purposes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
        ) {
            OutlinedButton(
                onClick = onSimulateFailure,
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Failure", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
    }
}

@Composable
private fun ProcessingStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "Processing Payment...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = "Please wait while we confirm your subscription",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessStep(
    status: SubscriptionStatus?,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = "Your subscription is now active. You can message any doctor directly.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (status is SubscriptionStatus.Active) {
            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(CareRideTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Next renewal: ${status.renewalDateFormatted}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

        CareRidePrimaryButton(
            text = "Start Messaging",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FailureStep(
    error: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "Payment Failed",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

        CareRidePrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        CareRideSecondaryButton(
            text = "Cancel",
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
