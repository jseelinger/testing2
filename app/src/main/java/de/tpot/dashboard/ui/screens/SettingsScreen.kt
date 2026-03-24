package de.tpot.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.tpot.dashboard.data.ConnectionStore
import de.tpot.dashboard.data.TpotConnection
import de.tpot.dashboard.ui.components.SectionHeader
import de.tpot.dashboard.ui.components.TpotButton
import de.tpot.dashboard.ui.components.TpotCard
import de.tpot.dashboard.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    connectionStore: ConnectionStore,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentConnection by connectionStore.connection.collectAsState(initial = TpotConnection())

    var serverUrl by remember(currentConnection) { mutableStateOf(currentConnection.serverUrl) }
    var username by remember(currentConnection) { mutableStateOf(currentConnection.username) }
    var password by remember(currentConnection) { mutableStateOf(currentConnection.password) }
    var acceptSelfSigned by remember(currentConnection) { mutableStateOf(currentConnection.acceptSelfSigned) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TpotBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "> Einstellungen",
                        style = MaterialTheme.typography.titleLarge,
                        color = TpotGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurueck",
                            tint = TpotTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TpotSurface
                )
            )
        },
        snackbarHost = {
            if (showSavedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = TpotGreenDark,
                    contentColor = TpotBackground
                ) {
                    Text("Verbindung gespeichert!")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = "Server-Verbindung")

            TpotCard(modifier = Modifier.fillMaxWidth()) {
                // Server URL
                Text(
                    text = "Server URL",
                    style = MaterialTheme.typography.labelMedium,
                    color = TpotTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "https://mein-tpot-server.de",
                            color = TpotTextTertiary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    leadingIcon = {
                        Icon(Icons.Default.Dns, null, tint = TpotGreen)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TpotGreen,
                        unfocusedBorderColor = TpotBorder,
                        focusedTextColor = TpotTextPrimary,
                        unfocusedTextColor = TpotTextPrimary,
                        cursorColor = TpotGreen,
                        focusedContainerColor = TpotSurfaceVariant,
                        unfocusedContainerColor = TpotSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username
                Text(
                    text = "Benutzername",
                    style = MaterialTheme.typography.labelMedium,
                    color = TpotTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("admin", color = TpotTextTertiary)
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, null, tint = TpotGreen)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TpotGreen,
                        unfocusedBorderColor = TpotBorder,
                        focusedTextColor = TpotTextPrimary,
                        unfocusedTextColor = TpotTextPrimary,
                        cursorColor = TpotGreen,
                        focusedContainerColor = TpotSurfaceVariant,
                        unfocusedContainerColor = TpotSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                Text(
                    text = "Passwort",
                    style = MaterialTheme.typography.labelMedium,
                    color = TpotTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("********", color = TpotTextTertiary)
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = TpotGreen)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Passwort anzeigen",
                                tint = TpotTextSecondary
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TpotGreen,
                        unfocusedBorderColor = TpotBorder,
                        focusedTextColor = TpotTextPrimary,
                        unfocusedTextColor = TpotTextPrimary,
                        cursorColor = TpotGreen,
                        focusedContainerColor = TpotSurfaceVariant,
                        unfocusedContainerColor = TpotSurfaceVariant
                    )
                )
            }

            SectionHeader(title = "Sicherheit")

            TpotCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Selbstsignierte Zertifikate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TpotTextPrimary
                        )
                        Text(
                            text = "Erlaubt Verbindungen zu Servern mit selbstsignierten SSL-Zertifikaten",
                            style = MaterialTheme.typography.bodySmall,
                            color = TpotTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = acceptSelfSigned,
                        onCheckedChange = { acceptSelfSigned = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TpotGreen,
                            checkedTrackColor = TpotGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TpotTextSecondary,
                            uncheckedTrackColor = TpotSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            TpotButton(
                text = "Verbindung speichern",
                onClick = {
                    scope.launch {
                        connectionStore.saveConnection(
                            TpotConnection(
                                serverUrl = serverUrl.trimEnd('/'),
                                username = username,
                                password = password,
                                acceptSelfSigned = acceptSelfSigned
                            )
                        )
                        showSavedSnackbar = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = serverUrl.isNotBlank() && username.isNotBlank(),
                icon = Icons.Default.Save
            )

            // Test connection button
            OutlinedButton(
                onClick = {
                    // Connection test would go here
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TpotGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TpotGreen.copy(alpha = 0.5f))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Verbindung testen",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Danger zone
            SectionHeader(title = "Gefahrenzone")

            OutlinedButton(
                onClick = {
                    scope.launch {
                        connectionStore.clearConnection()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TpotRed
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TpotRed.copy(alpha = 0.5f))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Verbindungsdaten loeschen",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info
            Text(
                text = "T-Pot ist ein Multi-Honeypot-Plattform von T-Mobile/Deutsche Telekom. " +
                        "Diese App zeigt die Web-Dashboards deines T-Pot Servers an.",
                style = MaterialTheme.typography.bodySmall,
                color = TpotTextTertiary
            )
        }
    }
}
