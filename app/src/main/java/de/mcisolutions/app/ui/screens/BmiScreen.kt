package de.mcisolutions.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.data.model.BmiResult
import de.mcisolutions.app.ui.components.*
import de.mcisolutions.app.ui.theme.*
import de.mcisolutions.app.util.FitnessCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(onBack: () -> Unit) {
    var weight by remember { mutableStateOf("75") }
    var height by remember { mutableStateOf("175") }
    var result by remember { mutableStateOf<BmiResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Rechner", color = MCIWhite) },
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
            // Info Card
            MCICard {
                Text(
                    text = "Body Mass Index",
                    style = MaterialTheme.typography.titleLarge,
                    color = MCIOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Der BMI bewertet das Verhältnis von Körpergewicht " +
                        "zur Körpergröße. Formel: BMI = Gewicht(kg) / Größe(m)²",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MCIGrayLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs
            MCICard {
                MCINumberInput(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Gewicht",
                    suffix = "kg"
                )
                Spacer(modifier = Modifier.height(16.dp))
                MCINumberInput(
                    value = height,
                    onValueChange = { height = it },
                    label = "Größe",
                    suffix = "cm"
                )
                Spacer(modifier = Modifier.height(20.dp))
                MCIButton(
                    text = "Berechnen",
                    onClick = {
                        val w = weight.toDoubleOrNull() ?: return@MCIButton
                        val h = height.toDoubleOrNull() ?: return@MCIButton
                        if (w > 0 && h > 0) {
                            result = FitnessCalculator.calculateBmi(w, h)
                        }
                    }
                )
            }

            // Result
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + slideInVertically()
            ) {
                result?.let { bmi ->
                    Spacer(modifier = Modifier.height(20.dp))
                    MCICard {
                        Text(
                            text = "Ergebnis",
                            style = MaterialTheme.typography.titleLarge,
                            color = MCIWhite
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // BMI Value
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${bmi.bmi}",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(bmi.color)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(bmi.color).copy(alpha = 0.15f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = bmi.category,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(bmi.color)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // BMI Scale
                        Text(
                            text = "BMI Klassifikation (WHO)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MCIGrayLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BmiScaleRow("< 18.5", "Untergewicht", MCIYellow)
                        BmiScaleRow("18.5 – 24.9", "Normalgewicht", MCIGreen)
                        BmiScaleRow("25.0 – 29.9", "Übergewicht", MCIYellow)
                        BmiScaleRow("30.0 – 34.9", "Adipositas I", MCIOrange)
                        BmiScaleRow("≥ 35.0", "Adipositas II+", MCIRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Source note
            Text(
                text = "Quelle: WHO Global Database on Body Mass Index",
                fontSize = 11.sp,
                color = MCIGrayMedium
            )
        }
    }
}

@Composable
private fun BmiScaleRow(range: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = range, fontSize = 13.sp, color = MCIGrayLight, modifier = Modifier.width(100.dp))
        Text(text = label, fontSize = 13.sp, color = MCIWhite)
    }
}
