package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.accessibility.AccessibilityDefaults
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme

@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    onSubscribeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shadowElevation = CareRideTheme.elevation.md,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        if (enabled) {
            EnabledMessageInput(
                value = value,
                onValueChange = onValueChange,
                onSend = onSend
            )
        } else {
            DisabledMessageInput(
                onSubscribeClick = onSubscribeClick
            )
        }
    }
}

@Composable
private fun EnabledMessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CareRideTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Type a message...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = RoundedCornerShape(CareRideTheme.radii.xl),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = false,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() })
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))

        FilledIconButton(
            onClick = onSend,
            enabled = value.isNotBlank(),
            modifier = Modifier
                .size(AccessibilityDefaults.MinTouchTargetDp.dp)
                .semantics {
                    contentDescription = if (value.isNotBlank()) {
                        "Send message"
                    } else {
                        "Send message, disabled until you type a message"
                    }
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun DisabledMessageInput(
    onSubscribeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CareRideTheme.spacing.sm)
            .semantics {
                contentDescription = "Messaging is locked. View plans to unlock messaging."
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

        Text(
            text = "Messaging locked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

        TextButton(
            onClick = onSubscribeClick,
            modifier = Modifier.semantics {
                contentDescription = "View plans to unlock messaging"
            }
        ) {
            Text("View plans")
        }
    }
}
