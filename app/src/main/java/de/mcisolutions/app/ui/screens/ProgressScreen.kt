package de.mcisolutions.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.data.model.ProgressEntry
import de.mcisolutions.app.ui.components.*
import de.mcisolutions.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(onBack: () -> Unit) {
    var entries by remember {
        mutableStateOf(
            listOf(
                ProgressEntry("2024-01-01", 80.0, "Start"),
                ProgressEntry("2024-01-15", 79.2),
                ProgressEntry("2024-02-01", 78.5),
                ProgressEntry("2024-02-15", 77.8),
                ProgressEntry("2024-03-01", 77.0, "Zwischenziel erreicht!")
            )
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var newWeight by remember { mutableStateOf("") }
    var newNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fortschritt", color = MCIWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Zurück", tint = MCIWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MCIBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MCIOrange,
                contentColor = MCIWhite
            ) {
                Icon(Icons.Default.Add, "Eintrag hinzufügen")
            }
        },
        containerColor = MCIBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Stats overview
            if (entries.size >= 2) {
                val first = entries.first().weightKg
                val last = entries.last().weightKg
                val diff = last - first

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatBox(
                        label = "Start",
                        value = "${first}kg",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatBox(
                        label = "Aktuell",
                        value = "${last}kg",
                        color = MCIAmber,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatBox(
                        label = "Differenz",
                        value = "${if (diff > 0) "+" else ""}${"%.1f".format(diff)}kg",
                        color = if (diff <= 0) MCIGreen else MCIRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chart
                MCICard {
                    Text("Gewichtsverlauf", style = MaterialTheme.typography.titleMedium, color = MCIWhite)
                    Spacer(modifier = Modifier.height(12.dp))
                    WeightChart(
                        entries = entries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Entries list
            Text("Einträge", style = MaterialTheme.typography.titleLarge, color = MCIWhite)
            Spacer(modifier = Modifier.height(12.dp))

            entries.reversed().forEach { entry ->
                MCICard(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.date,
                                fontSize = 12.sp,
                                color = MCIGrayLight
                            )
                            Text(
                                text = "${entry.weightKg} kg",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MCIOrange
                            )
                            if (entry.note.isNotEmpty()) {
                                Text(
                                    text = entry.note,
                                    fontSize = 13.sp,
                                    color = MCIGrayLight
                                )
                            }
                        }
                        IconButton(onClick = {
                            entries = entries.filter { it != entry }
                        }) {
                            Icon(Icons.Default.Delete, "Löschen", tint = MCIGrayMedium)
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Noch keine Einträge. Tippe auf + um zu starten.",
                        color = MCIGrayMedium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Neuer Eintrag", color = MCIWhite) },
            text = {
                Column {
                    MCINumberInput(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = "Gewicht",
                        suffix = "kg"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newNote,
                        onValueChange = { newNote = it },
                        label = { Text("Notiz (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MCIOrange,
                            unfocusedBorderColor = MCIGrayMedium,
                            focusedLabelColor = MCIOrange,
                            unfocusedLabelColor = MCIGrayLight,
                            cursorColor = MCIOrange,
                            focusedTextColor = MCIWhite,
                            unfocusedTextColor = MCIWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = newWeight.toDoubleOrNull()
                    if (w != null && w > 0) {
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        entries = entries + ProgressEntry(today, w, newNote)
                        newWeight = ""
                        newNote = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Speichern", color = MCIOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Abbrechen", color = MCIGrayLight)
                }
            },
            containerColor = MCIDarkCard
        )
    }
}

@Composable
private fun WeightChart(entries: List<ProgressEntry>, modifier: Modifier = Modifier) {
    if (entries.size < 2) return

    val weights = entries.map { it.weightKg.toFloat() }
    val minW = weights.min() - 1f
    val maxW = weights.max() + 1f
    val range = (maxW - minW).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val padding = 8.dp.toPx()
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val path = Path()
        val points = weights.mapIndexed { index, weight ->
            val x = padding + (index.toFloat() / (weights.size - 1)) * chartWidth
            val y = padding + (1f - (weight - minW) / range) * chartHeight
            Offset(x, y)
        }

        // Draw line
        points.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }

        drawPath(
            path = path,
            color = MCIOrange,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dots
        points.forEach { point ->
            drawCircle(
                color = MCIOrange,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = MCIDarkCard,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}
