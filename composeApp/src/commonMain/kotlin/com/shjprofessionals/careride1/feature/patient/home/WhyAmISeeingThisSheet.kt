package com.careride.feature.patient.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.careride.core.designsystem.theme.CareRideLightColors
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.domain.model.Doctor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhyAmISeeingThisSheet(
    doctor: Doctor,
    searchQuery: String,
    onDismiss: () -> Unit
) {
    val rankingReason = doctor.getRankingReason(searchQuery)
    val reasons = rankingReason.toDisplayList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CareRideTheme.spacing.lg)
                .padding(bottom = CareRideTheme.spacing.xl)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Text(
                    text = "Why am I seeing this?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Doctor name
            Text(
                text = doctor.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = doctor.specialty.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            // Divider
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            // Reasons list
            Text(
                text = "This listing appears because:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            if (reasons.isEmpty()) {
                Text(
                    text = "This doctor is part of our general directory.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                reasons.forEach { reason ->
                    ReasonItem(
                        reason = reason,
                        isSponsored = reason.contains("Sponsored", ignoreCase = true)
                    )
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))
                }
            }

            // Sponsored disclosure
            if (doctor.isBoosted) {
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                Surface(
                    color = CareRideLightColors.SponsoredContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CareRideTheme.spacing.sm),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "ℹ️",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                        Text(
                            text = "This is a paid placement. Featured doctors pay to appear higher in search results. This does not affect their qualifications or ratings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            // Close button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Got it")
            }
        }
    }
}

@Composable
private fun ReasonItem(
    reason: String,
    isSponsored: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isSponsored) {
                CareRideLightColors.Sponsored
            } else {
                MaterialTheme.colorScheme.secondary
            }
        )
        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}