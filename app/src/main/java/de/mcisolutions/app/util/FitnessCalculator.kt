package de.mcisolutions.app.util

import de.mcisolutions.app.data.model.*
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Zentrale Berechnungslogik für alle Fitness-Metriken.
 *
 * Quellen:
 * - BMI: WHO (World Health Organization) - Body Mass Index Classification
 * - BMR: Mifflin-St Jeor Equation (1990) – genaueste Formel für BMR-Schätzung
 *   Mifflin MD, St Jeor ST, et al. "A new predictive equation for resting energy
 *   expenditure in healthy individuals." Am J Clin Nutr. 1990;51(2):241-247.
 * - TDEE: Aktivitätsfaktoren basierend auf Harris-Benedict Multiplikatoren
 * - Makronährstoffe: Empfehlungen der DGE (Deutsche Gesellschaft für Ernährung)
 *   und ISSN (International Society of Sports Nutrition)
 */
object FitnessCalculator {

    // ==================== BMI ====================

    /**
     * Berechnet den Body Mass Index.
     * Formel: BMI = Gewicht(kg) / Größe(m)²
     * Quelle: WHO Global Database on Body Mass Index
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult {
        val heightM = heightCm / 100.0
        val bmi = weightKg / heightM.pow(2)
        val (category, color) = getBmiCategory(bmi)
        return BmiResult(
            bmi = (bmi * 10).roundToInt() / 10.0,
            category = category,
            color = color
        )
    }

    private fun getBmiCategory(bmi: Double): Pair<String, Long> {
        return when {
            bmi < 16.0 -> "Starkes Untergewicht" to 0xFFE53935  // Red
            bmi < 17.0 -> "Mäßiges Untergewicht" to 0xFFFF7043  // Orange-Red
            bmi < 18.5 -> "Leichtes Untergewicht" to 0xFFFFA726 // Orange
            bmi < 25.0 -> "Normalgewicht" to 0xFF66BB6A          // Green
            bmi < 30.0 -> "Übergewicht (Präadipositas)" to 0xFFFFA726 // Orange
            bmi < 35.0 -> "Adipositas Grad I" to 0xFFFF7043      // Orange-Red
            bmi < 40.0 -> "Adipositas Grad II" to 0xFFE53935     // Red
            else -> "Adipositas Grad III" to 0xFFB71C1C           // Dark Red
        }
    }

    // ==================== BMR / TDEE ====================

    /**
     * Berechnet den Grundumsatz (BMR) nach der Mifflin-St Jeor Gleichung.
     *
     * Männer:  BMR = (10 × Gewicht in kg) + (6.25 × Größe in cm) - (5 × Alter) + 5
     * Frauen:  BMR = (10 × Gewicht in kg) + (6.25 × Größe in cm) - (5 × Alter) - 161
     *
     * Quelle: Mifflin MD, St Jeor ST, Hill LA, Scott BJ, Daugherty SA, Koh YO.
     * "A new predictive equation for resting energy expenditure in healthy individuals."
     * Am J Clin Nutr. 1990 Feb;51(2):241-7.
     */
    fun calculateBmr(weightKg: Double, heightCm: Double, age: Int, gender: Gender): Double {
        val base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
        return when (gender) {
            Gender.MALE -> base + 5
            Gender.FEMALE -> base - 161
        }
    }

    /**
     * Berechnet den Gesamtenergieumsatz (TDEE).
     * TDEE = BMR × Aktivitätsfaktor
     *
     * Aktivitätsfaktoren nach Harris-Benedict Revision:
     * - Sitzend: 1.2
     * - Leicht aktiv: 1.375
     * - Moderat aktiv: 1.55
     * - Sehr aktiv: 1.725
     * - Extrem aktiv: 1.9
     */
    fun calculateTdee(profile: UserProfile): TdeeResult {
        val bmr = calculateBmr(profile.weightKg, profile.heightCm, profile.age, profile.gender)
        val tdee = bmr * profile.activityLevel.factor
        val targetCalories = tdee + profile.fitnessGoal.calorieAdjustment

        return TdeeResult(
            bmr = (bmr * 10).roundToInt() / 10.0,
            tdee = (tdee * 10).roundToInt() / 10.0,
            targetCalories = (targetCalories * 10).roundToInt() / 10.0,
            goal = profile.fitnessGoal
        )
    }

    // ==================== Makronährstoffe ====================

    /**
     * Berechnet die Makronährstoff-Verteilung basierend auf dem Ziel.
     *
     * Quellen:
     * - Protein: ISSN Position Stand (Jäger et al., 2017) empfiehlt 1.6-2.2g/kg
     *   für Muskelaufbau, 1.2-1.6g/kg für Erhalt
     * - Fett: DGE empfiehlt 30% der Gesamtkalorien
     * - Kohlenhydrate: Restliche Kalorien
     *
     * Energiegehalt:
     * - 1g Protein = 4 kcal
     * - 1g Kohlenhydrate = 4 kcal
     * - 1g Fett = 9 kcal
     */
    fun calculateMacros(calories: Double, weightKg: Double, goal: FitnessGoal): MacroResult {
        val (proteinPerKg, fatPercent) = when (goal) {
            FitnessGoal.LOSE_WEIGHT -> 2.0 to 0.25
            FitnessGoal.MAINTAIN -> 1.6 to 0.30
            FitnessGoal.BUILD_MUSCLE -> 2.2 to 0.25
        }

        val proteinGrams = weightKg * proteinPerKg
        val proteinCalories = proteinGrams * 4
        val fatCalories = calories * fatPercent
        val fatGrams = fatCalories / 9
        val carbsCalories = calories - proteinCalories - fatCalories
        val carbsGrams = carbsCalories / 4

        val proteinPct = ((proteinCalories / calories) * 100).roundToInt()
        val fatPct = (fatPercent * 100).roundToInt()
        val carbsPct = 100 - proteinPct - fatPct

        return MacroResult(
            calories = calories,
            proteinGrams = (proteinGrams * 10).roundToInt() / 10.0,
            carbsGrams = (carbsGrams.coerceAtLeast(0.0) * 10).roundToInt() / 10.0,
            fatGrams = (fatGrams * 10).roundToInt() / 10.0,
            proteinPercent = proteinPct,
            carbsPercent = carbsPct,
            fatPercent = fatPct
        )
    }

    // ==================== Trainingsplan ====================

    /**
     * Generiert einen einfachen Trainingsplan basierend auf dem Ziel.
     * Basiert auf allgemeinen Empfehlungen der ACSM (American College of Sports Medicine).
     */
    fun generateTrainingPlan(goal: FitnessGoal): List<TrainingDay> {
        return when (goal) {
            FitnessGoal.LOSE_WEIGHT -> fatLossTrainingPlan()
            FitnessGoal.MAINTAIN -> maintenanceTrainingPlan()
            FitnessGoal.BUILD_MUSCLE -> muscleGainTrainingPlan()
        }
    }

    private fun muscleGainTrainingPlan(): List<TrainingDay> = listOf(
        TrainingDay(
            dayName = "Montag", muscleGroup = "Brust & Trizeps",
            exercises = listOf(
                Exercise("Bankdrücken", 4, "8-10", 90),
                Exercise("Schrägbankdrücken (KH)", 3, "10-12", 75),
                Exercise("Cable Flys", 3, "12-15", 60),
                Exercise("Trizeps-Dips", 3, "8-12", 75),
                Exercise("Trizepsdrücken am Kabel", 3, "12-15", 60)
            )
        ),
        TrainingDay(
            dayName = "Dienstag", muscleGroup = "Rücken & Bizeps",
            exercises = listOf(
                Exercise("Kreuzheben", 4, "6-8", 120),
                Exercise("Klimmzüge", 4, "8-10", 90),
                Exercise("Rudern (Langhantel)", 3, "8-12", 75),
                Exercise("Latzug eng", 3, "10-12", 75),
                Exercise("Bizeps-Curls (SZ)", 3, "10-12", 60)
            )
        ),
        TrainingDay(
            dayName = "Mittwoch", muscleGroup = "Ruhetag",
            exercises = listOf(
                Exercise("Leichtes Cardio / Dehnen", 1, "20-30 Min", 0)
            )
        ),
        TrainingDay(
            dayName = "Donnerstag", muscleGroup = "Schultern & Nacken",
            exercises = listOf(
                Exercise("Schulterdrücken (KH)", 4, "8-10", 90),
                Exercise("Seitheben", 4, "12-15", 60),
                Exercise("Frontheben", 3, "12-15", 60),
                Exercise("Face Pulls", 3, "15-20", 45),
                Exercise("Shrugs", 3, "12-15", 60)
            )
        ),
        TrainingDay(
            dayName = "Freitag", muscleGroup = "Beine",
            exercises = listOf(
                Exercise("Kniebeugen", 4, "6-8", 120),
                Exercise("Beinpresse", 3, "10-12", 90),
                Exercise("Rumänisches Kreuzheben", 3, "10-12", 90),
                Exercise("Beinstrecker", 3, "12-15", 60),
                Exercise("Wadenheben", 4, "15-20", 45)
            )
        ),
        TrainingDay(
            dayName = "Samstag", muscleGroup = "Ganzkörper / Schwachstellen",
            exercises = listOf(
                Exercise("Klimmzüge", 3, "max", 90),
                Exercise("Dips", 3, "max", 90),
                Exercise("Ausfallschritte", 3, "12 pro Seite", 60),
                Exercise("Plank", 3, "60 Sek", 45)
            )
        ),
        TrainingDay(
            dayName = "Sonntag", muscleGroup = "Ruhetag",
            exercises = listOf(
                Exercise("Aktive Erholung / Spaziergang", 1, "30-60 Min", 0)
            )
        )
    )

    private fun fatLossTrainingPlan(): List<TrainingDay> = listOf(
        TrainingDay(
            dayName = "Montag", muscleGroup = "Ganzkörper + HIIT",
            exercises = listOf(
                Exercise("Kniebeugen", 3, "12-15", 60),
                Exercise("Bankdrücken", 3, "12-15", 60),
                Exercise("Rudern (KH)", 3, "12-15", 60),
                Exercise("HIIT Intervalle", 1, "15 Min", 0)
            )
        ),
        TrainingDay(
            dayName = "Dienstag", muscleGroup = "Cardio",
            exercises = listOf(
                Exercise("Laufen / Radfahren", 1, "30-45 Min", 0),
                Exercise("Core-Training", 3, "15-20", 30)
            )
        ),
        TrainingDay(
            dayName = "Mittwoch", muscleGroup = "Ganzkörper + HIIT",
            exercises = listOf(
                Exercise("Kreuzheben", 3, "12-15", 60),
                Exercise("Schulterdrücken", 3, "12-15", 60),
                Exercise("Latzug", 3, "12-15", 60),
                Exercise("HIIT Intervalle", 1, "15 Min", 0)
            )
        ),
        TrainingDay(
            dayName = "Donnerstag", muscleGroup = "Aktive Erholung",
            exercises = listOf(
                Exercise("Leichtes Joggen / Yoga", 1, "30-45 Min", 0)
            )
        ),
        TrainingDay(
            dayName = "Freitag", muscleGroup = "Ganzkörper + Cardio",
            exercises = listOf(
                Exercise("Ausfallschritte", 3, "12 pro Seite", 60),
                Exercise("Liegestütze", 3, "15-20", 45),
                Exercise("Klimmzüge (assistiert)", 3, "8-12", 60),
                Exercise("Burpees", 3, "10", 60)
            )
        ),
        TrainingDay(
            dayName = "Samstag", muscleGroup = "Cardio",
            exercises = listOf(
                Exercise("Laufen / Radfahren / Schwimmen", 1, "45-60 Min", 0)
            )
        ),
        TrainingDay(
            dayName = "Sonntag", muscleGroup = "Ruhetag",
            exercises = listOf(
                Exercise("Spaziergang & Dehnen", 1, "20-30 Min", 0)
            )
        )
    )

    private fun maintenanceTrainingPlan(): List<TrainingDay> = listOf(
        TrainingDay(
            dayName = "Montag", muscleGroup = "Oberkörper",
            exercises = listOf(
                Exercise("Bankdrücken", 3, "8-12", 75),
                Exercise("Rudern (KH)", 3, "8-12", 75),
                Exercise("Schulterdrücken", 3, "10-12", 60),
                Exercise("Bizeps-Curls", 2, "12-15", 45),
                Exercise("Trizepsdrücken", 2, "12-15", 45)
            )
        ),
        TrainingDay(
            dayName = "Dienstag", muscleGroup = "Unterkörper",
            exercises = listOf(
                Exercise("Kniebeugen", 3, "8-12", 90),
                Exercise("Rumänisches Kreuzheben", 3, "10-12", 75),
                Exercise("Beinpresse", 3, "12-15", 60),
                Exercise("Wadenheben", 3, "15-20", 45)
            )
        ),
        TrainingDay(
            dayName = "Mittwoch", muscleGroup = "Cardio & Core",
            exercises = listOf(
                Exercise("Laufen / Radfahren", 1, "30 Min", 0),
                Exercise("Plank", 3, "45-60 Sek", 30),
                Exercise("Russian Twist", 3, "20", 30)
            )
        ),
        TrainingDay(
            dayName = "Donnerstag", muscleGroup = "Oberkörper",
            exercises = listOf(
                Exercise("Klimmzüge", 3, "8-10", 90),
                Exercise("Schrägbankdrücken", 3, "10-12", 75),
                Exercise("Seitheben", 3, "12-15", 60),
                Exercise("Face Pulls", 3, "15-20", 45)
            )
        ),
        TrainingDay(
            dayName = "Freitag", muscleGroup = "Unterkörper",
            exercises = listOf(
                Exercise("Kreuzheben", 3, "6-8", 120),
                Exercise("Ausfallschritte", 3, "12 pro Seite", 60),
                Exercise("Beinstrecker", 3, "12-15", 60),
                Exercise("Beinbeuger", 3, "12-15", 60)
            )
        ),
        TrainingDay(
            dayName = "Samstag", muscleGroup = "Aktive Erholung",
            exercises = listOf(
                Exercise("Sport nach Wahl / Wandern", 1, "45-60 Min", 0)
            )
        ),
        TrainingDay(
            dayName = "Sonntag", muscleGroup = "Ruhetag",
            exercises = listOf(
                Exercise("Dehnen / Yoga", 1, "20-30 Min", 0)
            )
        )
    )
}
