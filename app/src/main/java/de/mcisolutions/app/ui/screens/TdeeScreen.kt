package de.mcisolutions.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.data.model.*
import de.mcisolutions.app.ui.components.*
import de.mcisolutions.app.ui.theme.*
import de.mcisolutions.app.util.FitnessCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdeeScreen(onBack: () -> Unit) {
    var weight by remember { mutableStateOf("75") }
    var height by remember { mutableStateOf("175") }
    var age by remember { mutableStateOf("25") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATELY_ACTIVE) }
    var fitnessGoal by remember { mutableStateOf(FitnessGoal.MAINTAIN) }
    var result by remember { mutableStateOf<TdeeResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalorienbedarf", color = MCIWhite) },
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
            // Info
            MCICard {
                Text(
                    text = "TDEE Rechner",
                    style = MaterialTheme.typography.titleLarge,
                    color = MCIOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Berechnung nach der Mifflin-St Jeor Gleichung – " +
                        "die genaueste Formel für den Grundumsatz (BMR). " +
                        "Der TDEE ergibt sich aus BMR × Aktivitätsfaktor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MCIGrayLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs
            MCICard {
                // Gender Selection
                Text("Geschlecht", style = MaterialTheme.typography.labelLarge, color = MCIGrayLight)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    GenderChip("Männlich", gender == Gender.MALE, Modifier.weight(1f)) {
                        gender = Gender.MALE
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    GenderChip("Weiblich", gender == Gender.FEMALE, Modifier.weight(1f)) {
                        gender = Gender.FEMALE
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MCINumberInput(value = age, onValueChange = { age = it }, label = "Alter", suffix = "Jahre")
                Spacer(modifier = Modifier.height(12.dp))
                MCINumberInput(value = weight, onValueChange = { weight = it }, label = "Gewicht", suffix = "kg")
                Spacer(modifier = Modifier.height(12.dp))
                MCINumberInput(value = height, onValueChange = { height = it }, label = "Größe", suffix = "cm")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Level
            MCICard {
                Text("Aktivitätslevel", style = MaterialTheme.typography.titleMedium, color = MCIWhite)
                Spacer(modifier = Modifier.height(12.dp))
                ActivityLevel.entries.forEach { level ->
                    SelectableRow(
                        text = level.label,
                        subtitle = "Faktor: ${level.factor}",
                        selected = activityLevel == level,
                        onClick = { activityLevel = level }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal
            MCICard {
                Text("Ziel", style = MaterialTheme.typography.titleMedium, color = MCIWhite)
                Spacer(modifier = Modifier.height(12.dp))
                FitnessGoal.entries.forEach { goal ->
                    val subtitle = when {
                        goal.calorieAdjustment < 0 -> "${goal.calorieAdjustment} kcal/Tag"
                        goal.calorieAdjustment > 0 -> "+${goal.calorieAdjustment} kcal/Tag"
                        else -> "±0 kcal/Tag"
                    }
                    SelectableRow(
                        text = goal.label,
                        subtitle = subtitle,
                        selected = fitnessGoal == goal,
                        onClick = { fitnessGoal = goal }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            MCIButton(
                text = "Berechnen",
                onClick = {
                    val w = weight.toDoubleOrNull() ?: return@MCIButton
                    val h = height.toDoubleOrNull() ?: return@MCIButton
                    val a = age.toIntOrNull() ?: return@MCIButton
                    if (w > 0 && h > 0 && a > 0) {
                        result = FitnessCalculator.calculateTdee(
                            UserProfile(
                                weightKg = w, heightCm = h, age = a,
                                gender = gender, activityLevel = activityLevel,
                                fitnessGoal = fitnessGoal
                            )
                        )
                    }
                }
            )

            // Result
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + slideInVertically()
            ) {
                result?.let { tdee ->
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        MCIGradientCard {
                            Text(
                                text = "Dein täglicher Bedarf",
                                fontSize = 16.sp,
                                color = MCIWhite.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${tdee.targetCalories.toInt()} kcal",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MCIWhite
                            )
                            Text(
                                text = "Ziel: ${tdee.goal.label}",
                                fontSize = 14.sp,
                                color = MCIWhite.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatBox(
                                label = "Grundumsatz",
                                value = "${tdee.bmr.toInt()} kcal",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            StatBox(
                                label = "TDEE",
                                value = "${tdee.tdee.toInt()} kcal",
                                color = MCIAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Quelle: Mifflin-St Jeor Equation (1990)",
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
private fun GenderChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MCIOrange else MCIDarkCardElevated)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MCIWhite else MCIGrayLight
        )
    }
}

@Composable
private fun SelectableRow(text: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (selected) Modifier.border(1.dp, MCIOrange, RoundedCornerShape(10.dp))
                else Modifier
            )
            .background(if (selected) MCIOrange.copy(alpha = 0.1f) else MCIDarkCardElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, fontSize = 14.sp, color = if (selected) MCIOrange else MCIWhite)
            Text(text = subtitle, fontSize = 12.sp, color = MCIGrayLight)
        }
        if (selected) {
            Icon(Icons.Default.Check, null, tint = MCIOrange, modifier = Modifier.size(20.dp))
        }
    }
}
