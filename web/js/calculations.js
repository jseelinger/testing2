/**
 * MCI Fitness – Berechnungslogik
 *
 * Quellen:
 * - BMI: WHO (World Health Organization) – Body Mass Index Classification
 * - BMR: Mifflin-St Jeor Equation (1990)
 *   Mifflin MD, St Jeor ST, et al. Am J Clin Nutr. 1990;51(2):241-247
 * - TDEE: Harris-Benedict Aktivitätsmultiplikatoren
 * - Makronährstoffe: ISSN Position Stand (Jäger et al., 2017), DGE
 */

const FitnessCalc = {

    // ==================== BMI ====================
    calculateBmi(weightKg, heightCm) {
        const heightM = heightCm / 100;
        const bmi = weightKg / (heightM * heightM);
        const rounded = Math.round(bmi * 10) / 10;

        let category, color;
        if (bmi < 16) { category = 'Starkes Untergewicht'; color = '#E53935'; }
        else if (bmi < 17) { category = 'Mäßiges Untergewicht'; color = '#FF7043'; }
        else if (bmi < 18.5) { category = 'Leichtes Untergewicht'; color = '#FFA726'; }
        else if (bmi < 25) { category = 'Normalgewicht'; color = '#66BB6A'; }
        else if (bmi < 30) { category = 'Übergewicht (Präadipositas)'; color = '#FFA726'; }
        else if (bmi < 35) { category = 'Adipositas Grad I'; color = '#FF7043'; }
        else if (bmi < 40) { category = 'Adipositas Grad II'; color = '#E53935'; }
        else { category = 'Adipositas Grad III'; color = '#B71C1C'; }

        return { bmi: rounded, category, color };
    },

    // ==================== BMR (Mifflin-St Jeor) ====================
    /**
     * Männer:  BMR = (10 × kg) + (6.25 × cm) - (5 × Alter) + 5
     * Frauen:  BMR = (10 × kg) + (6.25 × cm) - (5 × Alter) - 161
     */
    calculateBmr(weightKg, heightCm, age, gender) {
        const base = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        return gender === 'male' ? base + 5 : base - 161;
    },

    // ==================== TDEE ====================
    calculateTdee(weightKg, heightCm, age, gender, activityFactor, goalAdjustment) {
        const bmr = this.calculateBmr(weightKg, heightCm, age, gender);
        const tdee = bmr * activityFactor;
        const target = tdee + goalAdjustment;

        return {
            bmr: Math.round(bmr),
            tdee: Math.round(tdee),
            target: Math.round(target)
        };
    },

    // ==================== Makronährstoffe ====================
    /**
     * Protein: ISSN empfiehlt 1.6-2.2g/kg
     * Fett: DGE empfiehlt ~30%
     * KH: restliche Kalorien
     */
    calculateMacros(calories, weightKg, goal) {
        let proteinPerKg, fatPercent;

        switch (goal) {
            case 'lose':
                proteinPerKg = 2.0; fatPercent = 0.25; break;
            case 'gain':
                proteinPerKg = 2.2; fatPercent = 0.25; break;
            default: // maintain
                proteinPerKg = 1.6; fatPercent = 0.30; break;
        }

        const proteinG = Math.round(weightKg * proteinPerKg * 10) / 10;
        const proteinCal = proteinG * 4;
        const fatCal = calories * fatPercent;
        const fatG = Math.round((fatCal / 9) * 10) / 10;
        const carbsCal = Math.max(0, calories - proteinCal - fatCal);
        const carbsG = Math.round((carbsCal / 4) * 10) / 10;

        const proteinPct = Math.round((proteinCal / calories) * 100);
        const fatPct = Math.round(fatPercent * 100);
        const carbsPct = 100 - proteinPct - fatPct;

        return {
            proteinG, carbsG, fatG,
            proteinPct, carbsPct, fatPct,
            calories
        };
    }
};
