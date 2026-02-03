package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.accessibility.AccessibilityDefaults
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.QuickReply

@Composable
fun QuickReplyChip(
    quickReply: QuickReply,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(
                minHeight = AccessibilityDefaults.MinTouchTargetDp.dp
            )
            .semantics {
                role = Role.Button
                contentDescription = "Quick reply: ${quickReply.label}. Tap to insert: ${quickReply.message.take(50)}"
            },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = quickReply.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                horizontal = CareRideTheme.spacing.sm,
                vertical = CareRideTheme.spacing.xs
            )
        )
    }
}