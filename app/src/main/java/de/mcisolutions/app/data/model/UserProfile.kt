package de.mcisolutions.app.data.model

/**
 * Geschlecht des Nutzers.
 */
enum class Gender {
    MALE, FEMALE
}

/**
 * Aktivitätslevel für TDEE-Berechnung.
 * Faktoren nach Harris-Benedict / Mifflin-St Jeor Aktivitätsmultiplikatoren.
 */
enum class ActivityLevel(val factor: Double, val label: String) {
    SEDENTARY(1.2, "Sitzend (kaum Bewegung)"),
    LIGHTLY_ACTIVE(1.375, "Leicht aktiv (1-3x/Woche)"),
    MODERATELY_ACTIVE(1.55, "Moderat aktiv (3-5x/Woche)"),
    VERY_ACTIVE(1.725, "Sehr aktiv (6-7x/Woche)"),
    EXTRA_ACTIVE(1.9, "Extrem aktiv (2x täglich)")
}

/**
 * Trainingsziel des Nutzers.
 */
enum class FitnessGoal(val label: String, val calorieAdjustment: Int) {
    LOSE_WEIGHT("Abnehmen", -500),
    MAINTAIN("Gewicht halten", 0),
    BUILD_MUSCLE("Muskelaufbau", 300)
}

/**
 * Nutzerprofil mit allen relevanten Daten.
 */
data class UserProfile(
    val name: String = "",
    val age: Int = 25,
    val gender: Gender = Gender.MALE,
    val heightCm: Double = 175.0,
    val weightKg: Double = 75.0,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val fitnessGoal: FitnessGoal = FitnessGoal.MAINTAIN
)
