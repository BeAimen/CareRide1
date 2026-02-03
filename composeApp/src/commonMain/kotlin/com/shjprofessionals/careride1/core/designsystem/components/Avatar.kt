package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * Avatar sizes for consistent usage across the app
 */
enum class AvatarSize(
    val sizeDp: Dp,
    val fontSize: TextUnit,
    val iconSize: Dp
) {
    Small(32.dp, 12.sp, 18.dp),
    Medium(40.dp, 14.sp, 22.dp),
    Large(52.dp, 18.sp, 28.dp),
    XLarge(80.dp, 28.sp, 44.dp),
    XXLarge(100.dp, 36.sp, 56.dp)
}

/**
 * Avatar component that displays initials with a deterministic background color.
 * Falls back to an icon if no name is provided.
 *
 * @param name The full name to generate initials from
 * @param size The size preset for the avatar
 * @param modifier Optional modifier
 * @param contentDescription Accessibility description (auto-generated if null)
 */
@Composable
fun Avatar(
    name: String?,
    size: AvatarSize = AvatarSize.Medium,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val initials = remember(name) { getInitials(name) }
    val backgroundColor = remember(name) { getAvatarColor(name) }
    val textColor = remember(backgroundColor) { getContrastColor(backgroundColor) }
    val accessibilityDesc = contentDescription ?: name?.let { "Avatar for $it" } ?: "User avatar"

    Box(
        modifier = modifier
            .size(size.sizeDp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics { this.contentDescription = accessibilityDesc },
        contentAlignment = Alignment.Center
    ) {
        if (initials != null) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = size.fontSize,
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null, // Parent has description
                modifier = Modifier.size(size.iconSize),
                tint = textColor
            )
        }
    }
}

/**
 * Doctor-specific avatar with primary color scheme
 */
@Composable
fun DoctorAvatar(
    name: String?,
    size: AvatarSize = AvatarSize.Medium,
    modifier: Modifier = Modifier
) {
    Avatar(
        name = name,
        size = size,
        modifier = modifier,
        contentDescription = name?.let { "Doctor $it" } ?: "Doctor avatar"
    )
}

/**
 * Patient-specific avatar with secondary color scheme
 */
@Composable
fun PatientAvatar(
    name: String?,
    size: AvatarSize = AvatarSize.Medium,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) { getInitials(name) }
    val backgroundColor = remember(name) { getPatientAvatarColor(name) }
    val textColor = remember(backgroundColor) { getContrastColor(backgroundColor) }
    val accessibilityDesc = name?.let { "Patient $it" } ?: "Patient avatar"

    Box(
        modifier = modifier
            .size(size.sizeDp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics { contentDescription = accessibilityDesc },
        contentAlignment = Alignment.Center
    ) {
        if (initials != null) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = size.fontSize,
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(size.iconSize),
                tint = textColor
            )
        }
    }
}

/**
 * Extract initials from a name.
 * "Dr. John Smith" -> "JS"
 * "John" -> "J"
 * "" or null -> null
 */
private fun getInitials(name: String?): String? {
    if (name.isNullOrBlank()) return null

    val cleanName = name
        .replace(Regex("^(Dr\\.?|Mr\\.?|Mrs\\.?|Ms\\.?)\\s*", RegexOption.IGNORE_CASE), "")
        .trim()

    if (cleanName.isEmpty()) return null

    val parts = cleanName.split(Regex("\\s+")).filter { it.isNotEmpty() }

    return when {
        parts.isEmpty() -> null
        parts.size == 1 -> parts[0].first().uppercaseChar().toString()
        else -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
    }
}

/**
 * Generate a deterministic color based on the name.
 * Same name always produces the same color.
 */
private fun getAvatarColor(name: String?): Color {
    if (name.isNullOrBlank()) return AvatarColors.DEFAULT

    val hash = name.lowercase().hashCode().absoluteValue
    val index = hash % AvatarColors.PALETTE.size
    return AvatarColors.PALETTE[index]
}

/**
 * Generate patient-specific colors (warmer tones)
 */
private fun getPatientAvatarColor(name: String?): Color {
    if (name.isNullOrBlank()) return AvatarColors.PATIENT_DEFAULT

    val hash = name.lowercase().hashCode().absoluteValue
    val index = hash % AvatarColors.PATIENT_PALETTE.size
    return AvatarColors.PATIENT_PALETTE[index]
}

/**
 * Determine if text should be light or dark based on background
 */
private fun getContrastColor(backgroundColor: Color): Color {
    // Simple luminance calculation
    val luminance = (0.299 * backgroundColor.red +
            0.587 * backgroundColor.green +
            0.114 * backgroundColor.blue)
    return if (luminance > 0.5f) Color(0xFF1F2937) else Color.White
}

/**
 * Predefined color palettes for avatars
 */
private object AvatarColors {
    // Default fallback
    val DEFAULT = Color(0xFF6366F1) // Indigo

    // Palette for doctors/general use (cool professional tones)
    val PALETTE = listOf(
        Color(0xFF2563EB), // Blue 600
        Color(0xFF7C3AED), // Violet 600
        Color(0xFF0891B2), // Cyan 600
        Color(0xFF0D9488), // Teal 600
        Color(0xFF059669), // Emerald 600
        Color(0xFF4F46E5), // Indigo 600
        Color(0xFF7C3AED), // Purple 600
        Color(0xFF2563EB), // Blue 600
        Color(0xFF0E7490), // Cyan 700
        Color(0xFF047857), // Emerald 700
        Color(0xFF6D28D9), // Violet 700
        Color(0xFF1D4ED8), // Blue 700
    )

    // Patient-specific default
    val PATIENT_DEFAULT = Color(0xFFF59E0B) // Amber

    // Palette for patients (warmer tones)
    val PATIENT_PALETTE = listOf(
        Color(0xFFEA580C), // Orange 600
        Color(0xFFD97706), // Amber 600
        Color(0xFFCA8A04), // Yellow 600
        Color(0xFF16A34A), // Green 600
        Color(0xFF0D9488), // Teal 600
        Color(0xFFDC2626), // Red 600
        Color(0xFFDB2777), // Pink 600
        Color(0xFF9333EA), // Purple 600
        Color(0xFFC2410C), // Orange 700
        Color(0xFFB45309), // Amber 700
        Color(0xFF15803D), // Green 700
        Color(0xFFBE185D), // Pink 700
    )
}