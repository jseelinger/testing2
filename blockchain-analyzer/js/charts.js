/* ========================================
   ChainScope - Chart Engine (Pure Canvas)
   ======================================== */

const ChartEngine = {
    // Color definitions
    colors: {
        blue: '#00d4ff',
        blueDim: 'rgba(0, 212, 255, 0.2)',
        green: '#00e676',
        greenDim: 'rgba(0, 230, 118, 0.2)',
        orange: '#ff9100',
        red: '#ff3d71',
        purple: '#a855f7',
        yellow: '#ffd600',
        grid: 'rgba(30, 45, 61, 0.5)',
        text: '#5a6a7a',
        bg: '#1a2332',
    },

    // Setup canvas with proper DPI
    setupCanvas(canvas) {
        const rect = canvas.parentElement.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        canvas.width = rect.width * dpr;
        canvas.height = (rect.height - 50) * dpr; // Account for header
        canvas.style.width = rect.width + 'px';
        canvas.style.height = (rect.height - 50) + 'px';
        const ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);
        return { ctx, width: rect.width, height: rect.height - 50 };
    },

    // Draw area chart (Volume Chart)
    drawAreaChart(canvasId, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        const { ctx, width, height } = this.setupCanvas(canvas);
        const padding = { top: 20, right: 20, bottom: 40, left: 60 };
        const chartW = width - padding.left - padding.right;
        const chartH = height - padding.top - padding.bottom;

        // Clear
        ctx.clearRect(0, 0, width, height);

        const allValues = [...data.btcData, ...data.ethData];
        const maxVal = Math.max(...allValues) * 1.1;

        // Grid lines
        ctx.strokeStyle = this.colors.grid;
        ctx.lineWidth = 0.5;
        for (let i = 0; i <= 4; i++) {
            const y = padding.top + (chartH / 4) * i;
            ctx.beginPath();
            ctx.moveTo(padding.left, y);
            ctx.lineTo(width - padding.right, y);
            ctx.stroke();

            // Y-axis labels
            ctx.fillStyle = this.colors.text;
            ctx.font = '11px -apple-system, sans-serif';
            ctx.textAlign = 'right';
            const val = maxVal - (maxVal / 4) * i;
            ctx.fillText('$' + val.toFixed(0) + 'M', padding.left - 8, y + 4);
        }

        // X-axis labels
        ctx.textAlign = 'center';
        const step = Math.max(1, Math.floor(data.labels.length / 8));
        for (let i = 0; i < data.labels.length; i += step) {
            const x = padding.left + (chartW / (data.labels.length - 1)) * i;
            ctx.fillStyle = this.colors.text;
            ctx.fillText(data.labels[i], x, height - padding.bottom + 20);
        }

        // Draw area + line for each dataset
        const drawDataset = (values, color, colorDim) => {
            const points = values.map((v, i) => ({
                x: padding.left + (chartW / (values.length - 1)) * i,
                y: padding.top + chartH - (v / maxVal) * chartH
            }));

            // Area fill
            ctx.beginPath();
            ctx.moveTo(points[0].x, padding.top + chartH);
            points.forEach(p => ctx.lineTo(p.x, p.y));
            ctx.lineTo(points[points.length - 1].x, padding.top + chartH);
            ctx.closePath();

            const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartH);
            gradient.addColorStop(0, colorDim);
            gradient.addColorStop(1, 'transparent');
            ctx.fillStyle = gradient;
            ctx.fill();

            // Line
            ctx.beginPath();
            ctx.strokeStyle = color;
            ctx.lineWidth = 2;
            ctx.lineJoin = 'round';
            points.forEach((p, i) => {
                if (i === 0) ctx.moveTo(p.x, p.y);
                else ctx.lineTo(p.x, p.y);
            });
            ctx.stroke();
        };

        drawDataset(data.btcData, this.colors.blue, this.colors.blueDim);
        drawDataset(data.ethData, this.colors.green, this.colors.greenDim);
    },

    // Draw doughnut chart (Risk Distribution)
    drawDoughnutChart(canvasId, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        const { ctx, width, height } = this.setupCanvas(canvas);
        const centerX = width / 2;
        const centerY = height / 2;
        const radius = Math.min(width, height) / 2 - 40;
        const innerRadius = radius * 0.65;

        ctx.clearRect(0, 0, width, height);

        const total = data.reduce((sum, d) => sum + d.value, 0);
        let startAngle = -Math.PI / 2;

        data.forEach((item) => {
            const sliceAngle = (item.value / total) * Math.PI * 2;

            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
            ctx.arc(centerX, centerY, innerRadius, startAngle + sliceAngle, startAngle, true);
            ctx.closePath();
            ctx.fillStyle = item.color;
            ctx.fill();

            // Label
            const midAngle = startAngle + sliceAngle / 2;
            const labelRadius = radius + 20;
            const lx = centerX + Math.cos(midAngle) * labelRadius;
            const ly = centerY + Math.sin(midAngle) * labelRadius;

            ctx.fillStyle = this.colors.text;
            ctx.font = '11px -apple-system, sans-serif';
            ctx.textAlign = midAngle > Math.PI / 2 && midAngle < Math.PI * 1.5 ? 'right' : 'left';
            ctx.fillText(item.label, lx, ly);

            startAngle += sliceAngle;
        });

        // Center text
        ctx.fillStyle = '#e8edf5';
        ctx.font = 'bold 20px -apple-system, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(total.toLocaleString(), centerX, centerY - 5);
        ctx.fillStyle = this.colors.text;
        ctx.font = '11px -apple-system, sans-serif';
        ctx.fillText('Gesamt TXs', centerX, centerY + 15);
    },

    // Draw bar chart (Wallet Activity)
    drawBarChart(canvasId, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        const { ctx, width, height } = this.setupCanvas(canvas);
        const padding = { top: 20, right: 20, bottom: 40, left: 50 };
        const chartW = width - padding.left - padding.right;
        const chartH = height - padding.top - padding.bottom;

        ctx.clearRect(0, 0, width, height);

        const maxVal = Math.max(...data.map(d => Math.max(d.incoming, d.outgoing))) * 1.2;
        const barWidth = (chartW / data.length) * 0.35;
        const gap = chartW / data.length;

        // Grid
        ctx.strokeStyle = this.colors.grid;
        ctx.lineWidth = 0.5;
        for (let i = 0; i <= 4; i++) {
            const y = padding.top + (chartH / 4) * i;
            ctx.beginPath();
            ctx.moveTo(padding.left, y);
            ctx.lineTo(width - padding.right, y);
            ctx.stroke();

            ctx.fillStyle = this.colors.text;
            ctx.font = '10px -apple-system, sans-serif';
            ctx.textAlign = 'right';
            ctx.fillText(Math.round(maxVal - (maxVal / 4) * i), padding.left - 8, y + 4);
        }

        data.forEach((d, i) => {
            const x = padding.left + gap * i + gap / 2;

            // Incoming bar
            const h1 = (d.incoming / maxVal) * chartH;
            ctx.fillStyle = this.colors.green;
            ctx.beginPath();
            ctx.roundRect(x - barWidth - 1, padding.top + chartH - h1, barWidth, h1, [3, 3, 0, 0]);
            ctx.fill();

            // Outgoing bar
            const h2 = (d.outgoing / maxVal) * chartH;
            ctx.fillStyle = this.colors.red;
            ctx.beginPath();
            ctx.roundRect(x + 1, padding.top + chartH - h2, barWidth, h2, [3, 3, 0, 0]);
            ctx.fill();

            // X label
            if (i % 5 === 0) {
                ctx.fillStyle = this.colors.text;
                ctx.font = '10px -apple-system, sans-serif';
                ctx.textAlign = 'center';
                ctx.fillText(d.date, x, height - padding.bottom + 16);
            }
        });
    },

    // Draw gauge meter (Risk Score)
    drawGaugeMeter(canvasId, score) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        const rect = canvas.parentElement.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        canvas.width = 200 * dpr;
        canvas.height = 120 * dpr;
        canvas.style.width = '200px';
        canvas.style.height = '120px';
        const ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);

        const centerX = 100;
        const centerY = 100;
        const radius = 80;

        ctx.clearRect(0, 0, 200, 120);

        // Background arc
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, Math.PI, 0);
        ctx.lineWidth = 14;
        ctx.strokeStyle = 'rgba(30, 45, 61, 0.5)';
        ctx.lineCap = 'round';
        ctx.stroke();

        // Score arc
        const scoreAngle = Math.PI + (score / 100) * Math.PI;
        const gradient = ctx.createLinearGradient(20, 0, 180, 0);
        gradient.addColorStop(0, this.colors.green);
        gradient.addColorStop(0.5, this.colors.yellow);
        gradient.addColorStop(1, this.colors.red);

        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, Math.PI, scoreAngle);
        ctx.lineWidth = 14;
        ctx.strokeStyle = gradient;
        ctx.lineCap = 'round';
        ctx.stroke();

        // Needle
        const needleAngle = Math.PI + (score / 100) * Math.PI;
        const needleLen = radius - 25;
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(
            centerX + Math.cos(needleAngle) * needleLen,
            centerY + Math.sin(needleAngle) * needleLen
        );
        ctx.strokeStyle = '#e8edf5';
        ctx.lineWidth = 2;
        ctx.lineCap = 'round';
        ctx.stroke();

        // Center dot
        ctx.beginPath();
        ctx.arc(centerX, centerY, 5, 0, Math.PI * 2);
        ctx.fillStyle = '#e8edf5';
        ctx.fill();
    },
};
