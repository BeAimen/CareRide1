package com.shjprofessionals.careride1.core.designsystem.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Modifier that announces content changes to screen readers.
 * Use for loading states, errors, success messages.
 */
fun Modifier.announceForAccessibility(
    message: String,
    mode: LiveRegionMode = LiveRegionMode.Polite
): Modifier = this.semantics {
    liveRegion = mode
    contentDescription = message
}

/**
 * Modifier for assertive announcements (interrupts current speech).
 * Use sparingly - for errors and critical state changes.
 */
fun Modifier.announceAssertive(message: String): Modifier =
    announceForAccessibility(message, LiveRegionMode.Assertive)

/**
 * Modifier to mark content as a heading for screen reader navigation.
 */
fun Modifier.asHeading(): Modifier = this.semantics {
    heading()
}

/**
 * Modifier for clickable cards that should be announced as buttons.
 */
fun Modifier.clickableCard(
    label: String,
    stateDesc: String? = null
): Modifier = this.semantics {
    role = Role.Button
    contentDescription = label
    if (stateDesc != null) {
        stateDescription = stateDesc
    }
}

/**
 * Modifier for list items with position info.
 */
fun Modifier.listItem(
    label: String,
    index: Int,
    total: Int
): Modifier = this.semantics {
    contentDescription = "$label, item ${index + 1} of $total"
}

/**
 * Minimum touch target size per WCAG guidelines.
 */
object AccessibilityDefaults {
    const val MinTouchTargetDp = 48
}