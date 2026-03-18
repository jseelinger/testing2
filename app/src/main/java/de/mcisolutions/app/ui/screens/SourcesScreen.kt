package de.mcisolutions.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mcisolutions.app.ui.components.MCICard
import de.mcisolutions.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wissenschaftliche Quellen", color = MCIWhite) },
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
            Text(
                text = "Alle Berechnungen in dieser App basieren auf " +
                    "wissenschaftlich validierten Formeln und Empfehlungen " +
                    "anerkannter Organisationen.",
                style = MaterialTheme.typography.bodyLarge,
                color = MCIGrayLight
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BMI
            SourceSection(
                title = "BMI – Body Mass Index",
                formula = "BMI = Gewicht (kg) / Größe (m)²",
                sources = listOf(
                    Source(
                        "World Health Organization (WHO)",
                        "Global Database on Body Mass Index – BMI Classification",
                        "https://www.who.int/data/gho/data/themes/topics/topic-details/GHO/body-mass-index"
                    ),
                    Source(
                        "WHO Expert Consultation",
                        "Appropriate body-mass index for Asian populations and its " +
                            "implications for policy and intervention strategies. " +
                            "The Lancet, 2004; 363(9403): 157-163",
                        ""
                    )
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // BMR / TDEE
            SourceSection(
                title = "BMR – Grundumsatz (Mifflin-St Jeor)",
                formula = "Männer: BMR = (10 × kg) + (6.25 × cm) - (5 × Alter) + 5\n" +
                    "Frauen: BMR = (10 × kg) + (6.25 × cm) - (5 × Alter) - 161",
                sources = listOf(
                    Source(
                        "Mifflin MD, St Jeor ST, Hill LA, Scott BJ, Daugherty SA, Koh YO",
                        "A new predictive equation for resting energy expenditure " +
                            "in healthy individuals. American Journal of Clinical " +
                            "Nutrition, 1990; 51(2): 241-247",
                        "https://doi.org/10.1093/ajcn/51.2.241"
                    ),
                    Source(
                        "Frankenfield D, Roth-Yousey L, Compher C",
                        "Comparison of predictive equations for resting metabolic " +
                            "rate in healthy nonobese and obese adults: a systematic " +
                            "review. Journal of the American Dietetic Association, " +
                            "2005; 105(5): 775-789",
                        "https://doi.org/10.1016/j.jada.2005.02.005"
                    )
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // TDEE Activity Factors
            SourceSection(
                title = "TDEE – Aktivitätsfaktoren",
                formula = "TDEE = BMR × Aktivitätsfaktor\n" +
                    "Sitzend: 1.2 | Leicht aktiv: 1.375 | Moderat: 1.55\n" +
                    "Sehr aktiv: 1.725 | Extrem aktiv: 1.9",
                sources = listOf(
                    Source(
                        "Harris JA, Benedict FG",
                        "A Biometric Study of Human Basal Metabolism. " +
                            "Proceedings of the National Academy of Sciences, " +
                            "1918; 4(12): 370-373",
                        "https://doi.org/10.1073/pnas.4.12.370"
                    ),
                    Source(
                        "Roza AM, Shizgal HM",
                        "The Harris Benedict equation reevaluated: resting energy " +
                            "requirements and the body cell mass. American Journal " +
                            "of Clinical Nutrition, 1984; 40(1): 168-182",
                        ""
                    )
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Macros
            SourceSection(
                title = "Makronährstoffe",
                formula = "Protein: 1.6-2.2 g/kg (je nach Ziel)\n" +
                    "Fett: 25-30% der Gesamtkalorien\n" +
                    "Kohlenhydrate: Restliche Kalorien\n" +
                    "1g Protein = 4 kcal | 1g KH = 4 kcal | 1g Fett = 9 kcal",
                sources = listOf(
                    Source(
                        "Jäger R, Kerksick CM, Campbell BI, et al.",
                        "International Society of Sports Nutrition Position Stand: " +
                            "protein and exercise. Journal of the International " +
                            "Society of Sports Nutrition, 2017; 14: 20",
                        "https://doi.org/10.1186/s12970-017-0177-8"
                    ),
                    Source(
                        "Deutsche Gesellschaft für Ernährung (DGE)",
                        "Referenzwerte für die Nährstoffzufuhr. " +
                            "Bonn, 2. Auflage, 5. aktualisierte Ausgabe (2019)",
                        "https://www.dge.de/wissenschaft/referenzwerte/"
                    ),
                    Source(
                        "Morton RW, Murphy KT, McKellar SR, et al.",
                        "A systematic review, meta-analysis and meta-regression of " +
                            "the effect of protein supplementation on resistance " +
                            "training-induced gains in muscle mass and strength in " +
                            "healthy adults. British Journal of Sports Medicine, " +
                            "2018; 52(6): 376-384",
                        "https://doi.org/10.1136/bjsports-2017-097608"
                    )
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Training
            SourceSection(
                title = "Trainingsempfehlungen",
                formula = "",
                sources = listOf(
                    Source(
                        "American College of Sports Medicine (ACSM)",
                        "ACSM's Guidelines for Exercise Testing and Prescription, " +
                            "11th Edition. Wolters Kluwer, 2021",
                        ""
                    ),
                    Source(
                        "Schoenfeld BJ, Ogborn D, Krieger JW",
                        "Dose-response relationship between weekly resistance " +
                            "training volume and increases in muscle mass: A systematic " +
                            "review and meta-analysis. Journal of Sports Sciences, " +
                            "2017; 35(11): 1073-1082",
                        "https://doi.org/10.1080/02640414.2016.1210197"
                    ),
                    Source(
                        "Schoenfeld BJ, Grgic J, Van Every DW, Plotkin DL",
                        "Loading Recommendations for Muscle Strength, Hypertrophy, " +
                            "and Local Endurance: A Re-Examination of the Repetition " +
                            "Continuum. Sports, 2021; 9(2): 32",
                        "https://doi.org/10.3390/sports9020032"
                    )
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Disclaimer
            MCICard {
                Text(
                    text = "Hinweis",
                    style = MaterialTheme.typography.titleMedium,
                    color = MCIAmber
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Diese App dient ausschließlich zu Informationszwecken und " +
                        "ersetzt keine professionelle medizinische oder " +
                        "ernährungswissenschaftliche Beratung. Bei gesundheitlichen " +
                        "Fragen wende dich bitte an qualifiziertes Fachpersonal. " +
                        "Die Berechnungen sind Schätzungen und können individuell abweichen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MCIGrayLight
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class Source(
    val authors: String,
    val title: String,
    val doi: String
)

@Composable
private fun SourceSection(title: String, formula: String, sources: List<Source>) {
    MCICard {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MCIOrange
        )

        if (formula.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MCIBlack.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = formula,
                    fontSize = 13.sp,
                    color = MCIAmberLight,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        sources.forEachIndexed { index, source ->
            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = source.authors,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MCIWhite
            )
            Text(
                text = source.title,
                fontSize = 12.sp,
                color = MCIGrayLight,
                lineHeight = 18.sp
            )
            if (source.doi.isNotEmpty()) {
                Text(
                    text = source.doi,
                    fontSize = 11.sp,
                    color = MCIOrange.copy(alpha = 0.7f)
                )
            }
        }
    }
}
