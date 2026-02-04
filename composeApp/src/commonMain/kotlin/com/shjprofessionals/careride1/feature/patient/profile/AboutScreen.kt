package com.shjprofessionals.careride1.feature.patient.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme

class AboutScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        AboutContent(
            onBackClick = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutContent(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About CareRide") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(CareRideTheme.spacing.md)
        ) {
            // App logo/icon area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CareRideTheme.spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                    Text(
                        text = "CareRide",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Version 1.0.0 (Build 1)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SectionHeader(title = "Information")

            AboutMenuItem(
                icon = Icons.Default.Info,
                title = "What's New",
                subtitle = "See the latest features and updates",
                onClick = { /* Open changelog */ }
            )

            AboutMenuItem(
                icon = Icons.Default.Star,
                title = "Rate Us",
                subtitle = "Love CareRide? Leave a review",
                onClick = { /* Open app store */ }
            )

            AboutMenuItem(
                icon = Icons.Default.Share,
                title = "Share CareRide",
                subtitle = "Tell your friends about us",
                onClick = { /* Open share sheet */ }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SectionHeader(title = "Support")

            AboutMenuItem(
                icon = Icons.Default.Email,
                title = "Contact Support",
                subtitle = "support@careride.com",
                onClick = { /* Open email */ }
            )

            AboutMenuItem(
                icon = Icons.Default.Info,
                title = "Help Center",
                subtitle = "FAQs and guides",
                onClick = { /* Open help center */ }
            )

            AboutMenuItem(
                icon = Icons.Default.Warning,
                title = "Report a Problem",
                subtitle = "Let us know about issues",
                onClick = { /* Open bug report */ }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SectionHeader(title = "Legal")

            AboutMenuItem(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                subtitle = "How we protect your data",
                onClick = { /* Open privacy policy */ }
            )

            AboutMenuItem(
                icon = Icons.Default.Info,
                title = "Terms of Service",
                subtitle = "Usage terms and conditions",
                onClick = { /* Open ToS */ }
            )

            AboutMenuItem(
                icon = Icons.Default.Info,
                title = "Open Source Licenses",
                subtitle = "Third-party software used",
                onClick = { /* Open licenses */ }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

            // Footer
            Text(
                text = "© 2024 CareRide Health Inc.\nAll rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            Text(
                text = "Made with ❤️ for better healthcare",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
        }
    }
}

@Composable
private fun AboutMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = CareRideTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}