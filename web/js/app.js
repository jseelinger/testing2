/**
 * MCI Fitness – Haupt-App-Logik
 * Navigation, UI-State & Event-Handling
 */

// ==================== STATE ====================
let currentGender = 'male';
let currentActivityFactor = 1.55;
let currentTdeeGoal = 'maintain';
let currentMacroGoal = 'maintain';
let currentTrainGoal = 'gain';

const goalAdjustments = { lose: -500, maintain: 0, gain: 300 };
const goalLabels = { lose: 'Abnehmen', maintain: 'Gewicht halten', gain: 'Muskelaufbau' };

// ==================== NAVIGATION ====================
function navigateTo(pageId) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));

    // Show target page
    const target = document.getElementById('page-' + pageId);
    if (target) target.classList.add('active');

    // Update nav
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.page === pageId);
    });

    // Special page init
    if (pageId === 'training') {
        TrainingPlans.render(currentTrainGoal, 'training-plan');
    }
    if (pageId === 'progress') {
        ProgressTracker.render();
    }

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ==================== BMI ====================
function calculateBmi() {
    const weight = parseFloat(document.getElementById('bmi-weight').value);
    const height = parseFloat(document.getElementById('bmi-height').value);

    if (!weight || !height || weight <= 0 || height <= 0) return;

    const result = FitnessCalc.calculateBmi(weight, height);

    document.getElementById('bmi-value').textContent = result.bmi;
    document.getElementById('bmi-value').style.color = result.color;

    const badge = document.getElementById('bmi-category');
    badge.textContent = result.category;
    badge.style.color = result.color;
    badge.style.backgroundColor = result.color + '22';

    document.getElementById('bmi-result').classList.remove('hidden');
}

// ==================== TDEE ====================
function selectGender(gender) {
    currentGender = gender;
    document.querySelectorAll('[data-gender]').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.gender === gender);
    });
}

function selectActivity(factor) {
    currentActivityFactor = factor;
    document.querySelectorAll('#activity-level .radio-option').forEach(opt => {
        const val = parseFloat(opt.querySelector('input').value);
        opt.classList.toggle('selected', val === factor);
    });
}

function selectTdeeGoal(goal) {
    currentTdeeGoal = goal;
    document.querySelectorAll('#tdee-goal .radio-option').forEach(opt => {
        const val = opt.querySelector('input').value;
        opt.classList.toggle('selected', val === goal);
    });
}

function calculateTdee() {
    const weight = parseFloat(document.getElementById('tdee-weight').value);
    const height = parseFloat(document.getElementById('tdee-height').value);
    const age = parseInt(document.getElementById('tdee-age').value);

    if (!weight || !height || !age) return;

    const result = FitnessCalc.calculateTdee(
        weight, height, age, currentGender,
        currentActivityFactor, goalAdjustments[currentTdeeGoal]
    );

    document.getElementById('tdee-target').textContent = result.target + ' kcal';
    document.getElementById('tdee-goal-label').textContent = 'Ziel: ' + goalLabels[currentTdeeGoal];
    document.getElementById('tdee-bmr').textContent = result.bmr;
    document.getElementById('tdee-total').textContent = result.tdee;
    document.getElementById('tdee-result').classList.remove('hidden');
}

// ==================== MACROS ====================
function selectMacroGoal(goal) {
    currentMacroGoal = goal;
    document.querySelectorAll('[data-macro-goal]').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.macroGoal === goal);
    });
}

function calculateMacros() {
    const calories = parseFloat(document.getElementById('macro-calories').value);
    const weight = parseFloat(document.getElementById('macro-weight').value);

    if (!calories || !weight || calories <= 0 || weight <= 0) return;

    const result = FitnessCalc.calculateMacros(calories, weight, currentMacroGoal);

    document.getElementById('macro-total-cal').textContent = Math.round(result.calories);
    document.getElementById('macro-protein-g').textContent = result.proteinG + 'g';
    document.getElementById('macro-carbs-g').textContent = result.carbsG + 'g';
    document.getElementById('macro-fat-g').textContent = result.fatG + 'g';
    document.getElementById('macro-protein-pct').textContent = result.proteinPct + '%';
    document.getElementById('macro-carbs-pct').textContent = result.carbsPct + '%';
    document.getElementById('macro-fat-pct').textContent = result.fatPct + '%';

    drawMacroDonut(result);
    document.getElementById('macro-result').classList.remove('hidden');
}

function drawMacroDonut(macros) {
    const canvas = document.getElementById('macroChart');
    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;

    canvas.width = 200 * dpr;
    canvas.height = 200 * dpr;
    canvas.style.width = '200px';
    canvas.style.height = '200px';
    ctx.scale(dpr, dpr);

    const cx = 100, cy = 100, radius = 75, lineWidth = 22;

    ctx.clearRect(0, 0, 200, 200);

    const segments = [
        { pct: macros.proteinPct, color: '#FF6E40' },
        { pct: macros.carbsPct, color: '#FFAA00' },
        { pct: macros.fatPct, color: '#66BB6A' }
    ];

    let startAngle = -Math.PI / 2;
    const gap = 0.04; // small gap between segments

    segments.forEach(seg => {
        const sweep = (seg.pct / 100) * (Math.PI * 2 - gap * segments.length);
        ctx.beginPath();
        ctx.arc(cx, cy, radius, startAngle, startAngle + sweep);
        ctx.strokeStyle = seg.color;
        ctx.lineWidth = lineWidth;
        ctx.lineCap = 'round';
        ctx.stroke();
        startAngle += sweep + gap;
    });
}

// ==================== TRAINING ====================
function selectTrainGoal(goal) {
    currentTrainGoal = goal;
    document.querySelectorAll('[data-train-goal]').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.trainGoal === goal);
    });
    TrainingPlans.render(goal, 'training-plan');
}

// ==================== INIT ====================
document.addEventListener('DOMContentLoaded', () => {
    // Nav click handler
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => {
            const page = btn.dataset.page;
            if (page) navigateTo(page);
        });
    });

    // Sources nav (special – no nav button)
    // handled via onclick in HTML

    // Render initial training plan
    TrainingPlans.render(currentTrainGoal, 'training-plan');

    // Handle window resize for chart
    window.addEventListener('resize', () => {
        if (document.getElementById('page-progress').classList.contains('active')) {
            ProgressTracker.renderChart();
        }
    });
});
