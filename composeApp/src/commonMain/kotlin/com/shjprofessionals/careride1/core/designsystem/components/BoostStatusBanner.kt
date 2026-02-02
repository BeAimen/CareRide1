package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideLightColors
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus

@Composable
fun BoostStatusBanner(
    status: DoctorBoostStatus,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (status) {
        is DoctorBoostStatus.Active -> {
            ActiveBoostBanner(
                status = status,
                onManageClick = onManageClick,
                modifier = modifier
            )
        }
        is DoctorBoostStatus.Cancelled -> {
            CancelledBoostBanner(
                status = status,
                onManageClick = onManageClick,
                modifier = modifier
            )
        }
        is DoctorBoostStatus.Expired, DoctorBoostStatus.None -> {
            // No banner for expired/none - show in main content
        }
    }
}

@Composable
private fun ActiveBoostBanner(
    status: DoctorBoostStatus.Active,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CareRideLightColors.SponsoredContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = CareRideLightColors.Sponsored,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CareRideLightColors.Sponsored,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                    Text(
                        text = "Boost Active",
                        style = MaterialTheme.typography.titleMedium,
                        color = CareRideLightColors.Sponsored
                    )
                }
                Text(
                    text = "${status.boost.planName} â€¢ Renews ${status.renewalDateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            TextButton(onClick = onManageClick) {
                Text("Manage")
            }
        }
    }
}

@Composable
private fun CancelledBoostBanner(
    status: DoctorBoostStatus.Cancelled,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Boost Cancelled",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if (status.isStillActive) {
                    Text(
                        text = "Active until ${status.activeUntilFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            TextButton(onClick = onManageClick) {
                Text("Reactivate")
            }
        }
    }
}
