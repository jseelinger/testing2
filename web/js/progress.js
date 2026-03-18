/**
 * MCI Fitness – Fortschrittsverfolgung mit LocalStorage-Persistenz
 */

const ProgressTracker = {
    storageKey: 'mci_progress_entries',

    getEntries() {
        const data = localStorage.getItem(this.storageKey);
        if (data) return JSON.parse(data);
        // Default demo data
        return [
            { date: '2024-01-01', weight: 80.0, note: 'Start' },
            { date: '2024-01-15', weight: 79.2, note: '' },
            { date: '2024-02-01', weight: 78.5, note: '' },
            { date: '2024-02-15', weight: 77.8, note: '' },
            { date: '2024-03-01', weight: 77.0, note: 'Zwischenziel erreicht!' }
        ];
    },

    saveEntries(entries) {
        localStorage.setItem(this.storageKey, JSON.stringify(entries));
    },

    addEntry(weight, note) {
        const entries = this.getEntries();
        const today = new Date().toISOString().split('T')[0];
        entries.push({ date: today, weight, note: note || '' });
        this.saveEntries(entries);
        this.render();
    },

    deleteEntry(index) {
        const entries = this.getEntries();
        entries.splice(index, 1);
        this.saveEntries(entries);
        this.render();
    },

    renderStats() {
        const entries = this.getEntries();
        const container = document.getElementById('progress-stats');
        if (entries.length < 2) {
            container.innerHTML = '';
            return;
        }

        const first = entries[0].weight;
        const last = entries[entries.length - 1].weight;
        const diff = last - first;
        const diffStr = (diff > 0 ? '+' : '') + diff.toFixed(1);
        const diffColor = diff <= 0 ? 'green' : 'red';

        container.innerHTML = `
            <div class="stat-box">
                <span class="stat-value">${first}</span>
                <span class="stat-label">Start (kg)</span>
            </div>
            <div class="stat-box">
                <span class="stat-value amber">${last}</span>
                <span class="stat-label">Aktuell (kg)</span>
            </div>
            <div class="stat-box">
                <span class="stat-value ${diffColor}">${diffStr}</span>
                <span class="stat-label">Differenz (kg)</span>
            </div>
        `;
    },

    renderChart() {
        const entries = this.getEntries();
        const canvas = document.getElementById('progressChart');
        if (!canvas || entries.length < 2) return;

        const ctx = canvas.getContext('2d');
        const dpr = window.devicePixelRatio || 1;
        const rect = canvas.parentElement.getBoundingClientRect();
        canvas.width = rect.width * dpr;
        canvas.height = 200 * dpr;
        canvas.style.width = rect.width + 'px';
        canvas.style.height = '200px';
        ctx.scale(dpr, dpr);

        const W = rect.width;
        const H = 200;
        const pad = 12;

        ctx.clearRect(0, 0, W, H);

        const weights = entries.map(e => e.weight);
        const minW = Math.min(...weights) - 1;
        const maxW = Math.max(...weights) + 1;
        const range = Math.max(maxW - minW, 1);

        const points = weights.map((w, i) => ({
            x: pad + (i / (weights.length - 1)) * (W - 2 * pad),
            y: pad + (1 - (w - minW) / range) * (H - 2 * pad)
        }));

        // Draw line
        ctx.beginPath();
        ctx.strokeStyle = '#FF6E40';
        ctx.lineWidth = 2.5;
        ctx.lineJoin = 'round';
        ctx.lineCap = 'round';
        points.forEach((p, i) => {
            if (i === 0) ctx.moveTo(p.x, p.y);
            else ctx.lineTo(p.x, p.y);
        });
        ctx.stroke();

        // Draw dots
        points.forEach(p => {
            ctx.beginPath();
            ctx.arc(p.x, p.y, 5, 0, Math.PI * 2);
            ctx.fillStyle = '#FF6E40';
            ctx.fill();
            ctx.beginPath();
            ctx.arc(p.x, p.y, 3, 0, Math.PI * 2);
            ctx.fillStyle = '#1E1E1E';
            ctx.fill();
        });

        // Y-axis labels
        ctx.fillStyle = '#757575';
        ctx.font = '10px Montserrat, sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(maxW.toFixed(0) + 'kg', W - 4, pad + 10);
        ctx.fillText(minW.toFixed(0) + 'kg', W - 4, H - pad);
    },

    renderEntries() {
        const entries = this.getEntries();
        const container = document.getElementById('progress-entries');
        container.innerHTML = '';

        [...entries].reverse().forEach((entry, revIdx) => {
            const realIndex = entries.length - 1 - revIdx;
            const div = document.createElement('div');
            div.className = 'progress-entry';
            div.innerHTML = `
                <div class="entry-info">
                    <span class="entry-date">${entry.date}</span>
                    <span class="entry-weight">${entry.weight} kg</span>
                    ${entry.note ? `<span class="entry-note">${entry.note}</span>` : ''}
                </div>
                <button class="delete-btn" onclick="ProgressTracker.deleteEntry(${realIndex})">
                    <span class="material-icons-round">delete</span>
                </button>
            `;
            container.appendChild(div);
        });

        if (entries.length === 0) {
            container.innerHTML = '<p style="text-align:center;color:var(--gray-medium);padding:40px">Noch keine Einträge. Füge einen hinzu!</p>';
        }
    },

    render() {
        this.renderStats();
        this.renderChart();
        this.renderEntries();
    }
};

function addProgressEntry() {
    const weightInput = document.getElementById('progress-weight');
    const noteInput = document.getElementById('progress-note');
    const weight = parseFloat(weightInput.value);

    if (!weight || weight <= 0) return;

    ProgressTracker.addEntry(weight, noteInput.value);
    weightInput.value = '';
    noteInput.value = '';
}
