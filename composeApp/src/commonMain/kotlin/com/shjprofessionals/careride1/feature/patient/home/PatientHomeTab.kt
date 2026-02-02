package com.careride.feature.patient.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.careride.core.designsystem.components.*
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.domain.model.Doctor
import com.careride.feature.patient.doctorprofile.DoctorProfileScreen

class PatientHomeTab : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<PatientHomeViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        PatientHomeContent(
            state = state,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onDoctorClick = { doctor ->
                navigator.push(DoctorProfileScreen(doctor.id))
            },
            onWhyThisClick = viewModel::showWhyThisDoctor,
            onDismissWhyThis = viewModel::hideWhyThisDoctor,
            onRetry = viewModel::onRetry
        )

        // "Why am I seeing this?" bottom sheet
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
    onDoctorClick: (Doctor) -> Unit,
    onWhyThisClick: (Doctor) -> Unit,
    onDismissWhyThis: () -> Unit,
    onRetry: () -> Unit
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
                .padding(horizontal = CareRideTheme.spacing.md)
        ) {
            // Search bar
            CareRideSearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search by name, specialty, location..."
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Content based on state
            when {
                state.isLoading -> {
                    DoctorListSkeleton(
                        itemCount = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.error != null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = onRetry,
                        modifier = Modifier.weight(1f)
                    )
                }

                state.doctors.isEmpty() -> {
                    EmptyState(
                        title = "No doctors found",
                        subtitle = if (state.searchQuery.isNotEmpty()) {
                            "Try adjusting your search terms"
                        } else {
                            "No doctors available at the moment"
                        },
                        modifier = Modifier.weight(1f),
                        action = if (state.searchQuery.isNotEmpty()) {
                            {
                                CareRideSecondaryButton(
                                    text = "Clear Search",
                                    onClick = { onSearchQueryChange("") }
                                )
                            }
                        } else null
                    )
                }

                else -> {
                    // Results count
                    Text(
                        text = "${state.doctors.size} doctor${if (state.doctors.size != 1) "s" else ""} found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                    // Doctor list
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm),
                        contentPadding = PaddingValues(bottom = CareRideTheme.spacing.lg)
                    ) {
                        items(
                            items = state.doctors,
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