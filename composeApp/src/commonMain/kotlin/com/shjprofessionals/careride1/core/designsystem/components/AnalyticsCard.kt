package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    change: String,
    isPositiveChange: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val changeDirection = if (isPositiveChange) "increased" else "decreased"
    val fullDescription = "$title: $value, $changeDirection by $change"

    Card(
        modifier = modifier.semantics {
            contentDescription = fullDescription
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(CareRideTheme.spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Decorative, card has full description
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                ChangeIndicator(
                    change = change,
                    isPositive = isPositiveChange
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChangeIndicator(
    change: String,
    isPositive: Boolean
) {
    val color = if (isPositive) {
        Color(0xFF10B981) // Green
    } else {
        MaterialTheme.colorScheme.error
    }

    val icon = if (isPositive) {
        Icons.Default.KeyboardArrowUp
    } else {
        Icons.Default.KeyboardArrowDown
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Parent has full description
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = change,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}