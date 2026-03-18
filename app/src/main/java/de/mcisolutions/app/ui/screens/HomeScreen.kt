package de.mcisolutions.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.ui.components.FeatureCard
import de.mcisolutions.app.ui.components.MCIGradientCard
import de.mcisolutions.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToBmi: () -> Unit,
    onNavigateToTdee: () -> Unit,
    onNavigateToMacros: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSources: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MCIBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "MCI",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MCIOrange
        )
        Text(
            text = "Dein Personal Training & AI",
            style = MaterialTheme.typography.bodyLarge,
            color = MCIGrayLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome Card
        MCIGradientCard {
            Text(
                text = "Willkommen!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MCIWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Starte jetzt mit deinem personalisierten Fitness-Plan. " +
                    "Berechne deine Werte und erreiche deine Ziele.",
                fontSize = 14.sp,
                color = MCIWhite.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Stats Row
        Text(
            text = "Rechner",
            style = MaterialTheme.typography.titleLarge,
            color = MCIWhite
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Feature Cards
        FeatureCard(
            icon = Icons.Default.MonitorWeight,
            title = "BMI Rechner",
            description = "Body Mass Index berechnen",
            onClick = onNavigateToBmi
        )
        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.LocalFireDepartment,
            title = "Kalorienbedarf (TDEE)",
            description = "Täglichen Energieumsatz berechnen",
            onClick = onNavigateToTdee
        )
        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.Restaurant,
            title = "Makronährstoffe",
            description = "Protein, Kohlenhydrate & Fett",
            onClick = onNavigateToMacros
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Training & Fortschritt",
            style = MaterialTheme.typography.titleLarge,
            color = MCIWhite
        )
        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.FitnessCenter,
            title = "Trainingsplan",
            description = "Personalisierter Wochenplan",
            onClick = onNavigateToTraining
        )
        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.TrendingUp,
            title = "Fortschritt",
            description = "Gewicht & Erfolge tracken",
            onClick = onNavigateToProgress
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Sources
        FeatureCard(
            icon = Icons.Default.MenuBook,
            title = "Wissenschaftliche Quellen",
            description = "Berechnungsgrundlagen & Referenzen",
            onClick = onNavigateToSources
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MCI Fitness",
                    fontSize = 12.sp,
                    color = MCIGrayMedium
                )
                Text(
                    text = "Inspiriert von mcisolutions.de",
                    fontSize = 11.sp,
                    color = MCIGrayMedium.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
