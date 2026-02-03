package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.Specialty

/**
 * Horizontal scrolling filter chips for doctor specialties.
 * Includes an "All" chip to clear the filter.
 */
@Composable
fun SpecialtyFilterChips(
    selectedSpecialty: Specialty?,
    onSpecialtySelected: (Specialty?) -> Unit,
    modifier: Modifier = Modifier,
    showAllChip: Boolean = true
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))

        // "All" chip to clear filter
        if (showAllChip) {
            SpecialtyChip(
                label = "All",
                isSelected = selectedSpecialty == null,
                onClick = { onSpecialtySelected(null) }
            )
        }

        // Individual specialty chips
        Specialty.entries.forEach { specialty ->
            SpecialtyChip(
                label = specialty.displayName,
                isSelected = selectedSpecialty == specialty,
                onClick = {
                    // Toggle: if already selected, deselect (show all)
                    if (selectedSpecialty == specialty) {
                        onSpecialtySelected(null)
                    } else {
                        onSpecialtySelected(specialty)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
    }
}

@Composable
private fun SpecialtyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectionState = if (isSelected) "selected" else "not selected"

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier
            .height(36.dp)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "$label filter, $selectionState"
            },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * Compact version with just icons for common specialties
 */
@Composable
fun SpecialtyFilterChipsCompact(
    selectedSpecialty: Specialty?,
    onSpecialtySelected: (Specialty?) -> Unit,
    modifier: Modifier = Modifier,
    visibleSpecialties: List<Specialty> = Specialty.entries.take(5)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))

        // All chip
        FilterChip(
            selected = selectedSpecialty == null,
            onClick = { onSpecialtySelected(null) },
            label = { Text("All", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.height(32.dp)
        )

        visibleSpecialties.forEach { specialty ->
            FilterChip(
                selected = selectedSpecialty == specialty,
                onClick = {
                    if (selectedSpecialty == specialty) {
                        onSpecialtySelected(null)
                    } else {
                        onSpecialtySelected(specialty)
                    }
                },
                label = {
                    Text(
                        text = specialty.displayName.take(12) + if (specialty.displayName.length > 12) "…" else "",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.height(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
    }
}

/**
 * Active filter indicator with clear button
 */
@Composable
fun ActiveSpecialtyFilter(
    specialty: Specialty,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(
                start = CareRideTheme.spacing.sm,
                end = CareRideTheme.spacing.xs,
                top = CareRideTheme.spacing.xs,
                bottom = CareRideTheme.spacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
        ) {
            Text(
                text = "Showing: ${specialty.displayName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear ${specialty.displayName} filter",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}