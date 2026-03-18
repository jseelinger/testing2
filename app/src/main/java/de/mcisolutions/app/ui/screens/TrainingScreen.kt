package de.mcisolutions.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.data.model.FitnessGoal
import de.mcisolutions.app.data.model.TrainingDay
import de.mcisolutions.app.ui.components.MCICard
import de.mcisolutions.app.ui.theme.*
import de.mcisolutions.app.util.FitnessCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(onBack: () -> Unit) {
    var selectedGoal by remember { mutableStateOf(FitnessGoal.BUILD_MUSCLE) }
    val plan = remember(selectedGoal) { FitnessCalculator.generateTrainingPlan(selectedGoal) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trainingsplan", color = MCIWhite) },
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
            // Goal selector
            MCICard {
                Text("Trainingsziel", style = MaterialTheme.typography.titleMedium, color = MCIWhite)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FitnessGoal.entries.forEachIndexed { index, goal ->
                        if (index > 0) Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedGoal == goal) MCIOrange else MCIDarkCardElevated)
                                .clickable { selectedGoal = goal }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = goal.label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedGoal == goal) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedGoal == goal) MCIWhite else MCIGrayLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Training days
            plan.forEach { day ->
                TrainingDayCard(day)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quelle: Empfehlungen basierend auf ACSM Guidelines for Exercise Testing",
                fontSize = 11.sp,
                color = MCIGrayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TrainingDayCard(day: TrainingDay) {
    var expanded by remember { mutableStateOf(false) }

    MCICard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.dayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MCIOrange
                )
                Text(
                    text = day.muscleGroup,
                    fontSize = 14.sp,
                    color = MCIGrayLight
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MCIGrayMedium
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MCIGrayMedium.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            day.exercises.forEach { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            fontSize = 14.sp,
                            color = MCIWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${exercise.sets} Sätze × ${exercise.reps}",
                                fontSize = 12.sp,
                                color = MCIGrayLight
                            )
                            if (exercise.restSeconds > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Timer,
                                    null,
                                    tint = MCIGrayMedium,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${exercise.restSeconds}s Pause",
                                    fontSize = 12.sp,
                                    color = MCIGrayMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
