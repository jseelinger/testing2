package de.mcisolutions.app.data.model

/**
 * Ergebnis der BMI-Berechnung.
 */
data class BmiResult(
    val bmi: Double,
    val category: String,
    val color: Long // ARGB color
)

/**
 * Ergebnis der TDEE-/Kalorienberechnung.
 */
data class TdeeResult(
    val bmr: Double,
    val tdee: Double,
    val targetCalories: Double,
    val goal: FitnessGoal
)

/**
 * Makronährstoff-Verteilung.
 */
data class MacroResult(
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int
)

/**
 * Einzelner Fortschritts-Eintrag.
 */
data class ProgressEntry(
    val date: String, // ISO format YYYY-MM-DD
    val weightKg: Double,
    val note: String = ""
)

/**
 * Trainingstag mit Übungen.
 */
data class TrainingDay(
    val dayName: String,
    val muscleGroup: String,
    val exercises: List<Exercise>
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int
)
