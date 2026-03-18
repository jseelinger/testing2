/**
 * MCI Fitness – Trainingspläne
 * Basierend auf ACSM Guidelines for Exercise Testing and Prescription
 */

const TrainingPlans = {

    gain: [
        {
            day: 'Montag', group: 'Brust & Trizeps',
            exercises: [
                { name: 'Bankdrücken', sets: 4, reps: '8-10', rest: 90 },
                { name: 'Schrägbankdrücken (KH)', sets: 3, reps: '10-12', rest: 75 },
                { name: 'Cable Flys', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Trizeps-Dips', sets: 3, reps: '8-12', rest: 75 },
                { name: 'Trizepsdrücken am Kabel', sets: 3, reps: '12-15', rest: 60 }
            ]
        },
        {
            day: 'Dienstag', group: 'Rücken & Bizeps',
            exercises: [
                { name: 'Kreuzheben', sets: 4, reps: '6-8', rest: 120 },
                { name: 'Klimmzüge', sets: 4, reps: '8-10', rest: 90 },
                { name: 'Rudern (Langhantel)', sets: 3, reps: '8-12', rest: 75 },
                { name: 'Latzug eng', sets: 3, reps: '10-12', rest: 75 },
                { name: 'Bizeps-Curls (SZ)', sets: 3, reps: '10-12', rest: 60 }
            ]
        },
        {
            day: 'Mittwoch', group: 'Ruhetag',
            exercises: [
                { name: 'Leichtes Cardio / Dehnen', sets: 1, reps: '20-30 Min', rest: 0 }
            ]
        },
        {
            day: 'Donnerstag', group: 'Schultern & Nacken',
            exercises: [
                { name: 'Schulterdrücken (KH)', sets: 4, reps: '8-10', rest: 90 },
                { name: 'Seitheben', sets: 4, reps: '12-15', rest: 60 },
                { name: 'Frontheben', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Face Pulls', sets: 3, reps: '15-20', rest: 45 },
                { name: 'Shrugs', sets: 3, reps: '12-15', rest: 60 }
            ]
        },
        {
            day: 'Freitag', group: 'Beine',
            exercises: [
                { name: 'Kniebeugen', sets: 4, reps: '6-8', rest: 120 },
                { name: 'Beinpresse', sets: 3, reps: '10-12', rest: 90 },
                { name: 'Rumänisches Kreuzheben', sets: 3, reps: '10-12', rest: 90 },
                { name: 'Beinstrecker', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Wadenheben', sets: 4, reps: '15-20', rest: 45 }
            ]
        },
        {
            day: 'Samstag', group: 'Ganzkörper / Schwachstellen',
            exercises: [
                { name: 'Klimmzüge', sets: 3, reps: 'max', rest: 90 },
                { name: 'Dips', sets: 3, reps: 'max', rest: 90 },
                { name: 'Ausfallschritte', sets: 3, reps: '12 pro Seite', rest: 60 },
                { name: 'Plank', sets: 3, reps: '60 Sek', rest: 45 }
            ]
        },
        {
            day: 'Sonntag', group: 'Ruhetag',
            exercises: [
                { name: 'Aktive Erholung / Spaziergang', sets: 1, reps: '30-60 Min', rest: 0 }
            ]
        }
    ],

    lose: [
        {
            day: 'Montag', group: 'Ganzkörper + HIIT',
            exercises: [
                { name: 'Kniebeugen', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Bankdrücken', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Rudern (KH)', sets: 3, reps: '12-15', rest: 60 },
                { name: 'HIIT Intervalle', sets: 1, reps: '15 Min', rest: 0 }
            ]
        },
        {
            day: 'Dienstag', group: 'Cardio',
            exercises: [
                { name: 'Laufen / Radfahren', sets: 1, reps: '30-45 Min', rest: 0 },
                { name: 'Core-Training', sets: 3, reps: '15-20', rest: 30 }
            ]
        },
        {
            day: 'Mittwoch', group: 'Ganzkörper + HIIT',
            exercises: [
                { name: 'Kreuzheben', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Schulterdrücken', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Latzug', sets: 3, reps: '12-15', rest: 60 },
                { name: 'HIIT Intervalle', sets: 1, reps: '15 Min', rest: 0 }
            ]
        },
        {
            day: 'Donnerstag', group: 'Aktive Erholung',
            exercises: [
                { name: 'Leichtes Joggen / Yoga', sets: 1, reps: '30-45 Min', rest: 0 }
            ]
        },
        {
            day: 'Freitag', group: 'Ganzkörper + Cardio',
            exercises: [
                { name: 'Ausfallschritte', sets: 3, reps: '12 pro Seite', rest: 60 },
                { name: 'Liegestütze', sets: 3, reps: '15-20', rest: 45 },
                { name: 'Klimmzüge (assistiert)', sets: 3, reps: '8-12', rest: 60 },
                { name: 'Burpees', sets: 3, reps: '10', rest: 60 }
            ]
        },
        {
            day: 'Samstag', group: 'Cardio',
            exercises: [
                { name: 'Laufen / Radfahren / Schwimmen', sets: 1, reps: '45-60 Min', rest: 0 }
            ]
        },
        {
            day: 'Sonntag', group: 'Ruhetag',
            exercises: [
                { name: 'Spaziergang & Dehnen', sets: 1, reps: '20-30 Min', rest: 0 }
            ]
        }
    ],

    maintain: [
        {
            day: 'Montag', group: 'Oberkörper',
            exercises: [
                { name: 'Bankdrücken', sets: 3, reps: '8-12', rest: 75 },
                { name: 'Rudern (KH)', sets: 3, reps: '8-12', rest: 75 },
                { name: 'Schulterdrücken', sets: 3, reps: '10-12', rest: 60 },
                { name: 'Bizeps-Curls', sets: 2, reps: '12-15', rest: 45 },
                { name: 'Trizepsdrücken', sets: 2, reps: '12-15', rest: 45 }
            ]
        },
        {
            day: 'Dienstag', group: 'Unterkörper',
            exercises: [
                { name: 'Kniebeugen', sets: 3, reps: '8-12', rest: 90 },
                { name: 'Rumänisches Kreuzheben', sets: 3, reps: '10-12', rest: 75 },
                { name: 'Beinpresse', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Wadenheben', sets: 3, reps: '15-20', rest: 45 }
            ]
        },
        {
            day: 'Mittwoch', group: 'Cardio & Core',
            exercises: [
                { name: 'Laufen / Radfahren', sets: 1, reps: '30 Min', rest: 0 },
                { name: 'Plank', sets: 3, reps: '45-60 Sek', rest: 30 },
                { name: 'Russian Twist', sets: 3, reps: '20', rest: 30 }
            ]
        },
        {
            day: 'Donnerstag', group: 'Oberkörper',
            exercises: [
                { name: 'Klimmzüge', sets: 3, reps: '8-10', rest: 90 },
                { name: 'Schrägbankdrücken', sets: 3, reps: '10-12', rest: 75 },
                { name: 'Seitheben', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Face Pulls', sets: 3, reps: '15-20', rest: 45 }
            ]
        },
        {
            day: 'Freitag', group: 'Unterkörper',
            exercises: [
                { name: 'Kreuzheben', sets: 3, reps: '6-8', rest: 120 },
                { name: 'Ausfallschritte', sets: 3, reps: '12 pro Seite', rest: 60 },
                { name: 'Beinstrecker', sets: 3, reps: '12-15', rest: 60 },
                { name: 'Beinbeuger', sets: 3, reps: '12-15', rest: 60 }
            ]
        },
        {
            day: 'Samstag', group: 'Aktive Erholung',
            exercises: [
                { name: 'Sport nach Wahl / Wandern', sets: 1, reps: '45-60 Min', rest: 0 }
            ]
        },
        {
            day: 'Sonntag', group: 'Ruhetag',
            exercises: [
                { name: 'Dehnen / Yoga', sets: 1, reps: '20-30 Min', rest: 0 }
            ]
        }
    ],

    render(goal, containerId) {
        const plan = this[goal] || this.gain;
        const container = document.getElementById(containerId);
        container.innerHTML = '';

        plan.forEach((day, i) => {
            const div = document.createElement('div');
            div.className = 'training-day';
            div.innerHTML = `
                <div class="training-day-header" onclick="toggleTrainingDay(this)">
                    <div class="day-info">
                        <strong>${day.day}</strong>
                        <span>${day.group}</span>
                    </div>
                    <span class="material-icons-round">expand_more</span>
                </div>
                <div class="training-day-body">
                    <div class="training-day-body-inner">
                        ${day.exercises.map(ex => `
                            <div class="exercise-row">
                                <span class="exercise-name">${ex.name}</span>
                                <span class="exercise-detail">${ex.sets} × ${ex.reps}</span>
                                ${ex.rest > 0 ? `
                                    <span class="exercise-rest">
                                        <span class="material-icons-round">timer</span>
                                        ${ex.rest}s
                                    </span>` : ''}
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
            container.appendChild(div);
        });
    }
};

function toggleTrainingDay(header) {
    const day = header.parentElement;
    day.classList.toggle('open');
}
