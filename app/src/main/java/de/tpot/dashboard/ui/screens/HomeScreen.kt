package de.tpot.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tpot.dashboard.data.ConnectionStore
import de.tpot.dashboard.data.TpotConnection
import de.tpot.dashboard.data.TpotDashboards
import de.tpot.dashboard.ui.components.*
import de.tpot.dashboard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    connectionStore: ConnectionStore,
    onDashboardClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val connection by connectionStore.connection.collectAsState(initial = TpotConnection())
    val isConfigured = connection.serverUrl.isNotBlank()
    val dashboardsByCategory = TpotDashboards.getByCategory()

    Scaffold(
        containerColor = TpotBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TpotGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "T-Pot",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TpotGreen
                        )
                    }
                },
                actions = {
                    StatusIndicator(isConnected = isConfigured)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Einstellungen",
                            tint = TpotTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TpotSurface
                )
            )
        }
    ) { padding ->
        if (!isConfigured) {
            // No connection configured - show setup prompt
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = TpotGreen.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Willkommen bei T-Pot Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TpotTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Konfiguriere zuerst die Verbindung zu deinem T-Pot Server.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TpotTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TpotButton(
                        text = "Server konfigurieren",
                        onClick = onSettingsClick,
                        icon = Icons.Default.Settings
                    )
                }
            }
        } else {
            // Dashboard list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Server info card
                item {
                    TpotCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "> server_status",
                            style = MaterialTheme.typography.labelMedium,
                            color = TpotGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Host: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = TpotTextSecondary
                            )
                            Text(
                                text = connection.serverUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = TpotTextPrimary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "User: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = TpotTextSecondary
                            )
                            Text(
                                text = connection.username,
                                style = MaterialTheme.typography.bodySmall,
                                color = TpotTextPrimary
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Dashboards by category
                dashboardsByCategory.forEach { (category, dashboards) ->
                    item {
                        SectionHeader(title = category.label)
                    }

                    items(dashboards) { dashboard ->
                        DashboardCard(
                            name = dashboard.name,
                            description = dashboard.description,
                            icon = dashboard.icon,
                            onClick = { onDashboardClick(dashboard.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }

                // Footer
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "T-Pot Dashboard v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = TpotTextTertiary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
