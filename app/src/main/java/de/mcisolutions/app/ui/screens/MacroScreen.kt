package de.mcisolutions.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.data.model.FitnessGoal
import de.mcisolutions.app.data.model.MacroResult
import de.mcisolutions.app.ui.components.*
import de.mcisolutions.app.ui.theme.*
import de.mcisolutions.app.util.FitnessCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroScreen(onBack: () -> Unit) {
    var calories by remember { mutableStateOf("2200") }
    var weight by remember { mutableStateOf("75") }
    var selectedGoal by remember { mutableStateOf(FitnessGoal.MAINTAIN) }
    var result by remember { mutableStateOf<MacroResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Makronährstoffe", color = MCIWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Zurück", tint = MCIWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MCIBlack)
            )
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
            MCICard {
                Text(
                    text = "Makro-Verteilung",
                    style = MaterialTheme.typography.titleLarge,
                    color = MCIOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Optimale Verteilung von Protein, Kohlenhydraten und Fett " +
                        "basierend auf deinem Kalorienbedarf und Trainingsziel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MCIGrayLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            MCICard {
                MCINumberInput(
                    value = calories,
                    onValueChange = { calories = it },
                    label = "Tägliche Kalorien",
                    suffix = "kcal"
                )
                Spacer(modifier = Modifier.height(12.dp))
                MCINumberInput(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Körpergewicht",
                    suffix = "kg"
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Ziel", style = MaterialTheme.typography.labelLarge, color = MCIGrayLight)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FitnessGoal.entries.forEachIndexed { index, goal ->
                        if (index > 0) Spacer(modifier = Modifier.width(8.dp))
                        GoalChip(
                            text = goal.label,
                            selected = selectedGoal == goal,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedGoal = goal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                MCIButton(
                    text = "Berechnen",
                    onClick = {
                        val cal = calories.toDoubleOrNull() ?: return@MCIButton
                        val w = weight.toDoubleOrNull() ?: return@MCIButton
                        if (cal > 0 && w > 0) {
                            result = FitnessCalculator.calculateMacros(cal, w, selectedGoal)
                        }
                    }
                )
            }

            // Result
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + slideInVertically()
            ) {
                result?.let { macros ->
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Donut Chart
                        MCICard {
                            Text(
                                text = "Verteilung",
                                style = MaterialTheme.typography.titleMedium,
                                color = MCIWhite
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MacroDonutChart(macros)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${macros.calories.toInt()}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MCIWhite
                                    )
                                    Text(text = "kcal", fontSize = 12.sp, color = MCIGrayLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Legend
                            MacroRow("Protein", "${macros.proteinGrams}g", "${macros.proteinPercent}%", MCIOrange)
                            MacroRow("Kohlenhydrate", "${macros.carbsGrams}g", "${macros.carbsPercent}%", MCIAmber)
                            MacroRow("Fett", "${macros.fatGrams}g", "${macros.fatPercent}%", MCIGreen)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Quellen: ISSN Position Stand (Jäger et al., 2017), DGE",
                            fontSize = 11.sp,
                            color = MCIGrayMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MacroDonutChart(macros: MacroResult) {
    val proteinAngle = macros.proteinPercent * 3.6f
    val carbsAngle = macros.carbsPercent * 3.6f
    val fatAngle = macros.fatPercent * 3.6f

    Canvas(modifier = Modifier.size(160.dp)) {
        val strokeWidth = 28.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - 2 * radius - strokeWidth) / 2 + strokeWidth / 2,
            (size.height - 2 * radius - strokeWidth) / 2 + strokeWidth / 2
        )
        val arcSize = Size(radius * 2, radius * 2)

        var startAngle = -90f
        // Protein
        drawArc(
            color = Color(0xFFFF6E40),
            startAngle = startAngle,
            sweepAngle = proteinAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += proteinAngle
        // Carbs
        drawArc(
            color = Color(0xFFFFAA00),
            startAngle = startAngle,
            sweepAngle = carbsAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += carbsAngle
        // Fat
        drawArc(
            color = Color(0xFF66BB6A),
            startAngle = startAngle,
            sweepAngle = fatAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MacroRow(name: String, grams: String, percent: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = name, fontSize = 14.sp, color = MCIWhite, modifier = Modifier.weight(1f))
        Text(text = grams, fontSize = 14.sp, color = MCIGrayLight)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = percent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun GoalChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MCIOrange else MCIDarkCardElevated)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MCIWhite else MCIGrayLight
        )
    }
}
