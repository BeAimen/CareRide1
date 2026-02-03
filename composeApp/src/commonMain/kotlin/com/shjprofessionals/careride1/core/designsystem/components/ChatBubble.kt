package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.Message
import com.shjprofessionals.careride1.domain.model.MessageSenderType

@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isPatient = message.senderType == MessageSenderType.PATIENT
    val senderLabel = if (isPatient) "You" else "Doctor"

    val bubbleShape = if (isPatient) {
        RoundedCornerShape(
            topStart = CareRideTheme.radii.lg,
            topEnd = CareRideTheme.radii.lg,
            bottomStart = CareRideTheme.radii.lg,
            bottomEnd = CareRideTheme.radii.sm
        )
    } else {
        RoundedCornerShape(
            topStart = CareRideTheme.radii.lg,
            topEnd = CareRideTheme.radii.lg,
            bottomStart = CareRideTheme.radii.sm,
            bottomEnd = CareRideTheme.radii.lg
        )
    }

    val bubbleColor = if (isPatient) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isPatient) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isPatient) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$senderLabel said: ${message.content}, ${message.formattedTime}"
            },
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(
                    horizontal = CareRideTheme.spacing.sm,
                    vertical = CareRideTheme.spacing.xs
                )
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

        Text(
            text = message.formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}