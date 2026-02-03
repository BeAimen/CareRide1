package com.shjprofessionals.careride1.feature.patient.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.*
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.Specialty
import com.shjprofessionals.careride1.feature.patient.doctorprofile.DoctorProfileScreen

class PatientHomeTab : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<PatientHomeViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        PatientHomeContent(
            state = state,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onSpecialtySelected = viewModel::onSpecialtySelected,
            onClearFilters = viewModel::clearFilters,
            onDoctorClick = { doctor ->
                navigator.push(DoctorProfileScreen(doctor.id))
            },
            onWhyThisClick = viewModel::showWhyThisDoctor,
            onDismissWhyThis = viewModel::hideWhyThisDoctor,
            onRetry = viewModel::onRetry,
            onRefresh = viewModel::onRetry
        )

        state.selectedDoctorForInfo?.let { doctor ->
            WhyAmISeeingThisSheet(
                doctor = doctor,
                searchQuery = state.searchQuery,
                onDismiss = viewModel::hideWhyThisDoctor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientHomeContent(
    state: PatientHomeState,
    onSearchQueryChange: (String) -> Unit,
    onSpecialtySelected: (Specialty?) -> Unit,
    onClearFilters: () -> Unit,
    onDoctorClick: (Doctor) -> Unit,
    onWhyThisClick: (Doctor) -> Unit,
    onDismissWhyThis: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Find a Doctor",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            CareRideSearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search by name, specialty, location...",
                modifier = Modifier.padding(horizontal = CareRideTheme.spacing.md)
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Specialty filter chips
            SpecialtyFilterChips(
                selectedSpecialty = state.selectedSpecialty,
                onSpecialtySelected = onSpecialtySelected
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Active filters indicator + results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CareRideTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${state.doctors.size} doctor${if (state.doctors.size != 1) "s" else ""} found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (state.hasActiveFilters) {
                        Text(
                            text = state.filterDescription,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.xs)
                ) {
                    // Clear filters button
                    if (state.hasActiveFilters) {
                        TextButton(
                            onClick = onClearFilters,
                            contentPadding = PaddingValues(horizontal = CareRideTheme.spacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    // Loading indicator
                    if (state.isLoading && state.doctors.isNotEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Content
            LoadingContent(
                isLoading = state.isLoading && state.doctors.isEmpty(),
                isEmpty = state.doctors.isEmpty(),
                data = state.doctors,
                error = state.error,
                loadingContent = {
                    DoctorListSkeleton(
                        itemCount = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = CareRideTheme.spacing.md)
                    )
                },
                emptyContent = {
                    EmptyState(
                        title = if (state.hasActiveFilters) "No matches found" else "No doctors found",
                        subtitle = when {
                            state.selectedSpecialty != null && state.searchQuery.isNotEmpty() ->
                                "Try removing some filters or changing your search"
                            state.selectedSpecialty != null ->
                                "No ${state.selectedSpecialty.displayName} doctors available"
                            state.searchQuery.isNotEmpty() ->
                                "Try a different search term"
                            else ->
                                "No doctors available at the moment"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = CareRideTheme.spacing.md),
                        action = if (state.hasActiveFilters) {
                            {
                                CareRideSecondaryButton(
                                    text = "Clear Filters",
                                    onClick = onClearFilters
                                )
                            }
                        } else null
                    )
                },
                errorContent = {
                    ErrorState(
                        message = state.error?.userMessage ?: "Unknown error",
                        onRetry = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = CareRideTheme.spacing.md)
                    )
                }
            ) { doctors ->
                RefreshableContent(
                    isRefreshing = state.isLoading && state.doctors.isNotEmpty(),
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = CareRideTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm),
                        contentPadding = PaddingValues(bottom = CareRideTheme.spacing.lg)
                    ) {
                        items(
                            items = doctors,
                            key = { it.id }
                        ) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                onClick = { onDoctorClick(doctor) },
                                onWhyThisClick = { onWhyThisClick(doctor) }
                            )
                        }
                    }
                }
            }
        }
    }
}