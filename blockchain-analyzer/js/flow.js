/* ========================================
   ChainScope - Flow Visualization Engine
   ======================================== */

const FlowEngine = {
    canvas: null,
    ctx: null,
    width: 0,
    height: 0,
    nodes: [],
    edges: [],
    scale: 1,
    offsetX: 0,
    offsetY: 0,
    dragging: false,
    dragStart: { x: 0, y: 0 },
    hoveredNode: null,
    animationFrame: null,
    particleTime: 0,

    typeColors: {
        exchange: '#00d4ff',
        mixer: '#ff3d71',
        defi: '#a855f7',
        darknet: '#ff3d71',
        ransomware: '#ff3d71',
        sanctioned: '#ff3d71',
        scam: '#ff9100',
        unknown: '#5a6a7a',
    },

    init(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;
        this.ctx = this.canvas.getContext('2d');
        this.resize();

        // Mouse events
        this.canvas.addEventListener('mousedown', (e) => this.onMouseDown(e));
        this.canvas.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.canvas.addEventListener('mouseup', () => this.onMouseUp());
        this.canvas.addEventListener('wheel', (e) => this.onWheel(e));

        window.addEventListener('resize', () => this.resize());
    },

    resize() {
        if (!this.canvas) return;
        const rect = this.canvas.parentElement.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        this.width = rect.width;
        this.height = rect.height;
        this.canvas.width = rect.width * dpr;
        this.canvas.height = rect.height * dpr;
        this.canvas.style.width = rect.width + 'px';
        this.canvas.style.height = rect.height + 'px';
        this.ctx.scale(dpr, dpr);
        this.render();
    },

    setData(graphData) {
        this.nodes = [];
        this.edges = [];

        // Layout nodes using force-directed simple layout
        const levelGroups = {};
        graphData.nodes.forEach(n => {
            if (!levelGroups[n.level]) levelGroups[n.level] = [];
            levelGroups[n.level].push(n);
        });

        const maxLevel = Math.max(...Object.keys(levelGroups).map(Number));
        const centerX = this.width / 2;
        const centerY = this.height / 2;

        Object.entries(levelGroups).forEach(([level, nodes]) => {
            const lvl = parseInt(level);
            const radius = lvl * 150;
            const angleStep = (Math.PI * 2) / Math.max(nodes.length, 1);

            nodes.forEach((node, i) => {
                const angle = angleStep * i - Math.PI / 2;
                const jitterX = (Math.random() - 0.5) * 40;
                const jitterY = (Math.random() - 0.5) * 40;

                this.nodes.push({
                    ...node,
                    x: centerX + Math.cos(angle) * radius + jitterX,
                    y: centerY + Math.sin(angle) * radius + jitterY,
                    radius: lvl === 0 ? 28 : 20,
                    vx: 0,
                    vy: 0,
                });
            });
        });

        // Map edges
        this.edges = graphData.edges.map(e => ({
            ...e,
            fromNode: this.nodes.find(n => n.id === e.from),
            toNode: this.nodes.find(n => n.id === e.to),
        })).filter(e => e.fromNode && e.toNode);

        // Apply simple force layout
        this.applyForceLayout();
        this.startAnimation();
    },

    applyForceLayout() {
        const iterations = 80;
        const repulsionForce = 8000;
        const attractionForce = 0.005;
        const damping = 0.9;

        for (let iter = 0; iter < iterations; iter++) {
            // Repulsion between all nodes
            for (let i = 0; i < this.nodes.length; i++) {
                for (let j = i + 1; j < this.nodes.length; j++) {
                    const dx = this.nodes[j].x - this.nodes[i].x;
                    const dy = this.nodes[j].y - this.nodes[i].y;
                    const dist = Math.sqrt(dx * dx + dy * dy) || 1;
                    const force = repulsionForce / (dist * dist);
                    const fx = (dx / dist) * force;
                    const fy = (dy / dist) * force;

                    this.nodes[i].vx -= fx;
                    this.nodes[i].vy -= fy;
                    this.nodes[j].vx += fx;
                    this.nodes[j].vy += fy;
                }
            }

            // Attraction along edges
            this.edges.forEach(e => {
                const dx = e.toNode.x - e.fromNode.x;
                const dy = e.toNode.y - e.fromNode.y;
                const dist = Math.sqrt(dx * dx + dy * dy) || 1;
                const force = dist * attractionForce;
                const fx = (dx / dist) * force;
                const fy = (dy / dist) * force;

                e.fromNode.vx += fx;
                e.fromNode.vy += fy;
                e.toNode.vx -= fx;
                e.toNode.vy -= fy;
            });

            // Apply velocities
            this.nodes.forEach(n => {
                if (n.level === 0) return; // Keep root in center
                n.vx *= damping;
                n.vy *= damping;
                n.x += n.vx;
                n.y += n.vy;

                // Keep in bounds
                n.x = Math.max(50, Math.min(this.width - 50, n.x));
                n.y = Math.max(50, Math.min(this.height - 50, n.y));
            });
        }
    },

    startAnimation() {
        if (this.animationFrame) cancelAnimationFrame(this.animationFrame);

        const animate = () => {
            this.particleTime += 0.02;
            this.render();
            this.animationFrame = requestAnimationFrame(animate);
        };
        animate();
    },

    stopAnimation() {
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
            this.animationFrame = null;
        }
    },

    render() {
        if (!this.ctx) return;
        const ctx = this.ctx;

        ctx.clearRect(0, 0, this.width, this.height);
        ctx.save();
        ctx.translate(this.offsetX, this.offsetY);
        ctx.scale(this.scale, this.scale);

        // Draw edges
        this.edges.forEach(edge => {
            if (!edge.fromNode || !edge.toNode) return;
            const from = edge.fromNode;
            const to = edge.toNode;

            // Edge line
            ctx.beginPath();
            ctx.moveTo(from.x, from.y);
            ctx.lineTo(to.x, to.y);
            ctx.strokeStyle = 'rgba(30, 45, 61, 0.6)';
            ctx.lineWidth = Math.min(3, Math.max(0.5, edge.amount / 10));
            ctx.stroke();

            // Animated particle
            const t = (this.particleTime * 0.5) % 1;
            const px = from.x + (to.x - from.x) * t;
            const py = from.y + (to.y - from.y) * t;

            ctx.beginPath();
            ctx.arc(px, py, 3, 0, Math.PI * 2);
            ctx.fillStyle = this.typeColors[from.type] || this.typeColors.unknown;
            ctx.globalAlpha = 0.7;
            ctx.fill();
            ctx.globalAlpha = 1;

            // Arrow
            const angle = Math.atan2(to.y - from.y, to.x - from.x);
            const arrowDist = to.radius + 6;
            const ax = to.x - Math.cos(angle) * arrowDist;
            const ay = to.y - Math.sin(angle) * arrowDist;

            ctx.beginPath();
            ctx.moveTo(ax, ay);
            ctx.lineTo(ax - Math.cos(angle - 0.3) * 10, ay - Math.sin(angle - 0.3) * 10);
            ctx.lineTo(ax - Math.cos(angle + 0.3) * 10, ay - Math.sin(angle + 0.3) * 10);
            ctx.closePath();
            ctx.fillStyle = 'rgba(30, 45, 61, 0.8)';
            ctx.fill();

            // Amount label
            const mx = (from.x + to.x) / 2;
            const my = (from.y + to.y) / 2;
            ctx.fillStyle = '#8899aa';
            ctx.font = '10px -apple-system, sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText(edge.amount.toFixed(2) + ' ETH', mx, my - 8);
        });

        // Draw nodes
        this.nodes.forEach(node => {
            const color = this.typeColors[node.type] || this.typeColors.unknown;
            const isHovered = this.hoveredNode === node;

            // Glow
            if (node.risk > 70 || isHovered) {
                ctx.beginPath();
                ctx.arc(node.x, node.y, node.radius + 10, 0, Math.PI * 2);
                const glow = ctx.createRadialGradient(node.x, node.y, node.radius, node.x, node.y, node.radius + 10);
                glow.addColorStop(0, color.replace(')', ', 0.3)').replace('rgb', 'rgba'));
                glow.addColorStop(1, 'transparent');
                ctx.fillStyle = glow;
                ctx.fill();
            }

            // Node circle
            ctx.beginPath();
            ctx.arc(node.x, node.y, node.radius, 0, Math.PI * 2);
            ctx.fillStyle = isHovered ? color : '#1a2332';
            ctx.fill();
            ctx.strokeStyle = color;
            ctx.lineWidth = isHovered ? 3 : 2;
            ctx.stroke();

            // Node label
            ctx.fillStyle = isHovered ? '#0a0e17' : '#e8edf5';
            ctx.font = `${node.level === 0 ? 'bold 11' : '10'}px -apple-system, sans-serif`;
            ctx.textAlign = 'center';

            const label = node.label.length > 10 ? node.label.substring(0, 10) + '...' : node.label;
            ctx.fillText(label, node.x, node.y + 3);
        });

        ctx.restore();
    },

    getNodeAt(x, y) {
        const mx = (x - this.offsetX) / this.scale;
        const my = (y - this.offsetY) / this.scale;

        for (let i = this.nodes.length - 1; i >= 0; i--) {
            const node = this.nodes[i];
            const dx = mx - node.x;
            const dy = my - node.y;
            if (dx * dx + dy * dy <= node.radius * node.radius) {
                return node;
            }
        }
        return null;
    },

    onMouseDown(e) {
        const rect = this.canvas.getBoundingClientRect();
        this.dragging = true;
        this.dragStart = { x: e.clientX - this.offsetX, y: e.clientY - this.offsetY };
    },

    onMouseMove(e) {
        const rect = this.canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        if (this.dragging) {
            this.offsetX = e.clientX - this.dragStart.x;
            this.offsetY = e.clientY - this.dragStart.y;
        }

        const node = this.getNodeAt(x, y);
        this.hoveredNode = node;
        this.canvas.style.cursor = node ? 'pointer' : (this.dragging ? 'grabbing' : 'grab');

        // Show tooltip
        const tooltip = document.getElementById('flow-tooltip');
        if (node && tooltip) {
            tooltip.classList.remove('hidden');
            tooltip.style.left = (x + 15) + 'px';
            tooltip.style.top = (y - 10) + 'px';
            tooltip.innerHTML = `
                <div style="font-weight:700;margin-bottom:4px">${node.label}</div>
                <div style="font-family:monospace;font-size:0.75rem;color:#8899aa;margin-bottom:4px">${DataEngine.shortenAddress(node.id)}</div>
                <div>Typ: <span style="color:${this.typeColors[node.type]}">${node.type}</span></div>
                <div>Risiko: <span style="font-weight:700;color:${node.risk > 70 ? '#ff3d71' : node.risk > 40 ? '#ff9100' : '#00e676'}">${node.risk}/100</span></div>
            `;
        } else if (tooltip) {
            tooltip.classList.add('hidden');
        }
    },

    onMouseUp() {
        this.dragging = false;
    },

    onWheel(e) {
        e.preventDefault();
        const delta = e.deltaY > 0 ? 0.9 : 1.1;
        this.scale = Math.max(0.3, Math.min(3, this.scale * delta));
    },

    zoomIn() { this.scale = Math.min(3, this.scale * 1.2); },
    zoomOut() { this.scale = Math.max(0.3, this.scale * 0.8); },

    reset() {
        this.scale = 1;
        this.offsetX = 0;
        this.offsetY = 0;
    }
};
