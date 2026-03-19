/* ========================================
   ChainScope - Main Application
   ======================================== */

const App = {
    currentPage: 'dashboard',
    transactions: [],
    txPage: 1,
    txPerPage: 15,
    txSortKey: 'time',
    txSortDir: 'desc',
    liveTxInterval: null,
    priceInterval: null,

    init() {
        this.setupNavigation();
        this.setupSearch();
        this.setupSidebar();
        this.loadDashboard();
        this.startLiveFeed();
        this.startPriceUpdates();
        this.setupKeyboardShortcuts();
        this.setupModal();

        // Initialize flow engine
        FlowEngine.init('flow-canvas');
    },

    // ========================================
    // Navigation
    // ========================================
    setupNavigation() {
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', () => {
                const page = item.dataset.page;
                this.navigateTo(page);
            });
        });

        // View all links
        document.querySelectorAll('[data-page]').forEach(el => {
            if (el.classList.contains('nav-item')) return;
            el.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigateTo(el.dataset.page);
            });
        });
    },

    navigateTo(page) {
        this.currentPage = page;

        // Update nav
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.toggle('active', item.dataset.page === page);
        });

        // Update pages
        document.querySelectorAll('.page').forEach(p => {
            p.classList.toggle('active', p.id === 'page-' + page);
        });

        // Load page data
        switch (page) {
            case 'dashboard': this.loadDashboard(); break;
            case 'explorer': this.loadExplorer(); break;
            case 'wallets': break;
            case 'flowview': this.loadFlowView(); break;
            case 'risk': this.loadRiskPage(); break;
            case 'alerts': this.loadAlerts(); break;
            case 'reports': this.loadReports(); break;
        }

        // Close mobile sidebar
        document.getElementById('sidebar').classList.remove('open');
    },

    // ========================================
    // Sidebar
    // ========================================
    setupSidebar() {
        document.getElementById('sidebar-toggle').addEventListener('click', () => {
            const sidebar = document.getElementById('sidebar');
            sidebar.classList.toggle('open');
        });
    },

    // ========================================
    // Search
    // ========================================
    setupSearch() {
        const overlay = document.getElementById('search-overlay');
        const input = document.getElementById('search-modal-input');
        const results = document.getElementById('search-results');

        // Global search input
        document.getElementById('global-search').addEventListener('focus', () => {
            overlay.classList.remove('hidden');
            input.focus();
        });

        // Close
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) overlay.classList.add('hidden');
        });

        // Search input
        input.addEventListener('input', () => {
            const query = input.value.trim().toLowerCase();
            if (!query) { results.innerHTML = ''; return; }

            const searchResults = [];

            // Search known addresses
            Object.entries(DataEngine.knownAddresses).forEach(([addr, info]) => {
                if (addr.toLowerCase().includes(query) || info.label.toLowerCase().includes(query)) {
                    searchResults.push({
                        type: 'Adresse',
                        value: DataEngine.shortenAddress(addr),
                        label: info.label,
                        fullAddress: addr
                    });
                }
            });

            // Check if it looks like a TX hash
            if (query.startsWith('0x') && query.length > 10) {
                searchResults.unshift({
                    type: 'TX',
                    value: query.slice(0, 16) + '...',
                    label: 'Transaktion suchen'
                });
            }

            results.innerHTML = searchResults.slice(0, 8).map(r => `
                <div class="search-result-item" data-address="${r.fullAddress || ''}">
                    <span class="result-type">${r.type}</span>
                    <span class="result-value">${r.value}</span>
                    <span class="result-label">${r.label}</span>
                </div>
            `).join('');

            // Click on result
            results.querySelectorAll('.search-result-item').forEach(item => {
                item.addEventListener('click', () => {
                    const addr = item.dataset.address;
                    if (addr) {
                        overlay.classList.add('hidden');
                        this.navigateTo('wallets');
                        document.getElementById('wallet-search').value = addr;
                        this.analyzeWallet(addr);
                    }
                });
            });
        });
    },

    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                document.getElementById('search-overlay').classList.remove('hidden');
                document.getElementById('search-modal-input').focus();
            }
            if (e.key === 'Escape') {
                document.getElementById('search-overlay').classList.add('hidden');
                document.getElementById('modal-overlay').classList.add('hidden');
            }
        });
    },

    // ========================================
    // Modal
    // ========================================
    setupModal() {
        document.getElementById('modal-close').addEventListener('click', () => {
            document.getElementById('modal-overlay').classList.add('hidden');
        });
        document.getElementById('modal-overlay').addEventListener('click', (e) => {
            if (e.target.id === 'modal-overlay') {
                e.target.classList.add('hidden');
            }
        });
    },

    showModal(title, content) {
        document.getElementById('modal-title').textContent = title;
        document.getElementById('modal-body').innerHTML = content;
        document.getElementById('modal-overlay').classList.remove('hidden');
    },

    // ========================================
    // Toast
    // ========================================
    showToast(message, type = 'info') {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span class="toast-message">${message}</span>
            <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
        `;
        container.appendChild(toast);

        setTimeout(() => toast.remove(), 4000);
    },

    // ========================================
    // Dashboard
    // ========================================
    loadDashboard() {
        // Volume chart
        const volumeData = DataEngine.generateVolumeData(24);
        ChartEngine.drawAreaChart('volume-chart', volumeData);

        // Risk distribution chart
        ChartEngine.drawDoughnutChart('risk-chart', [
            { label: 'Low Risk', value: 8420, color: '#00e676' },
            { label: 'Medium', value: 2340, color: '#ffd600' },
            { label: 'High', value: 890, color: '#ff9100' },
            { label: 'Critical', value: 234, color: '#ff3d71' },
        ]);

        // Suspicious addresses
        this.loadSuspiciousList();

        // Time filter
        document.querySelectorAll('.time-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.time-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const volumeData = DataEngine.generateVolumeData(btn.dataset.range === '1h' ? 12 : btn.dataset.range === '24h' ? 24 : btn.dataset.range === '7d' ? 28 : 30);
                ChartEngine.drawAreaChart('volume-chart', volumeData);
            });
        });

        // Handle window resize for charts
        window.addEventListener('resize', () => {
            if (this.currentPage === 'dashboard') {
                const vd = DataEngine.generateVolumeData(24);
                ChartEngine.drawAreaChart('volume-chart', vd);
            }
        });
    },

    loadSuspiciousList() {
        const list = document.getElementById('suspicious-list');
        const addresses = DataEngine.generateRiskAddresses(5);

        list.innerHTML = addresses.map((a, i) => `
            <div class="suspicious-item" data-address="${a.address}">
                <span class="suspicious-rank">#${i + 1}</span>
                <span class="suspicious-addr">${DataEngine.shortenAddress(a.address)}</span>
                <span class="suspicious-score ${a.riskScore >= 90 ? 'critical' : 'high'}">${a.riskScore}</span>
            </div>
        `).join('');

        list.querySelectorAll('.suspicious-item').forEach(item => {
            item.addEventListener('click', () => {
                this.navigateTo('wallets');
                document.getElementById('wallet-search').value = item.dataset.address;
                this.analyzeWallet(item.dataset.address);
            });
        });
    },

    // ========================================
    // Live Feed
    // ========================================
    startLiveFeed() {
        const feed = document.getElementById('live-tx-feed');
        if (!feed) return;

        // Initial items
        for (let i = 0; i < 8; i++) {
            this.addLiveTx(feed, false);
        }

        // Add new every 3 seconds
        this.liveTxInterval = setInterval(() => {
            if (this.currentPage === 'dashboard') {
                this.addLiveTx(feed, true);
            }
        }, 3000);
    },

    addLiveTx(feed, animate) {
        const tx = DataEngine.generateTransaction();
        const item = document.createElement('div');
        item.className = 'live-tx-item';
        if (!animate) item.style.animation = 'none';

        const amountColor = tx.risk === 'critical' ? 'accent-red' :
                           tx.risk === 'high' ? 'accent-orange' : 'accent-green';

        item.innerHTML = `
            <span class="live-tx-hash">${tx.hash.slice(0, 10)}...</span>
            <span class="live-tx-flow">
                ${DataEngine.shortenAddress(tx.from)}
                <span class="arrow">&rarr;</span>
                ${DataEngine.shortenAddress(tx.to)}
            </span>
            <span class="live-tx-amount" style="color:var(--${amountColor})">${tx.amount} ETH</span>
            <span class="live-tx-time">${DataEngine.timeAgo(tx.time)}</span>
        `;

        feed.insertBefore(item, feed.firstChild);

        // Keep max 20 items
        while (feed.children.length > 20) {
            feed.removeChild(feed.lastChild);
        }
    },

    // ========================================
    // Price Updates
    // ========================================
    startPriceUpdates() {
        this.priceInterval = setInterval(() => {
            const btcPrice = 68432 + (Math.random() - 0.5) * 500;
            const ethPrice = 3891 + (Math.random() - 0.5) * 50;
            const gasPrice = Math.floor(Math.random() * 30) + 15;

            document.getElementById('btc-price').textContent = `BTC $${btcPrice.toLocaleString('en-US', { maximumFractionDigits: 0 })}`;
            document.getElementById('eth-price').textContent = `ETH $${ethPrice.toLocaleString('en-US', { maximumFractionDigits: 0 })}`;
            document.getElementById('gas-price').textContent = `Gas: ${gasPrice} Gwei`;
        }, 5000);
    },

    // ========================================
    // Transaction Explorer
    // ========================================
    loadExplorer() {
        this.transactions = DataEngine.generateTransactions(200);
        this.txPage = 1;
        this.renderTransactionTable();
        this.setupExplorerEvents();
    },

    setupExplorerEvents() {
        // Sort
        document.querySelectorAll('#tx-table th.sortable').forEach(th => {
            th.addEventListener('click', () => {
                const key = th.dataset.sort;
                if (this.txSortKey === key) {
                    this.txSortDir = this.txSortDir === 'asc' ? 'desc' : 'asc';
                } else {
                    this.txSortKey = key;
                    this.txSortDir = 'desc';
                }
                this.renderTransactionTable();
            });
        });

        // Filters
        document.getElementById('apply-filters').addEventListener('click', () => {
            this.txPage = 1;
            this.renderTransactionTable();
        });

        // CSV Export
        document.getElementById('export-csv').addEventListener('click', () => {
            this.exportCSV();
        });
    },

    getFilteredTransactions() {
        let txs = [...this.transactions];

        const minAmount = parseFloat(document.getElementById('filter-min-amount').value) || 0;
        const maxAmount = parseFloat(document.getElementById('filter-max-amount').value) || Infinity;
        const riskFilter = document.getElementById('filter-risk').value;

        txs = txs.filter(tx => {
            if (tx.amount < minAmount || tx.amount > maxAmount) return false;
            if (riskFilter !== 'all' && tx.risk !== riskFilter) return false;
            return true;
        });

        // Sort
        txs.sort((a, b) => {
            let va = a[this.txSortKey];
            let vb = b[this.txSortKey];
            if (va instanceof Date) { va = va.getTime(); vb = vb.getTime(); }
            if (typeof va === 'string') { va = va.toLowerCase(); vb = vb.toLowerCase(); }
            return this.txSortDir === 'asc' ? (va > vb ? 1 : -1) : (va < vb ? 1 : -1);
        });

        return txs;
    },

    renderTransactionTable() {
        const txs = this.getFilteredTransactions();
        const totalPages = Math.ceil(txs.length / this.txPerPage);
        const start = (this.txPage - 1) * this.txPerPage;
        const pageTxs = txs.slice(start, start + this.txPerPage);

        const tbody = document.getElementById('tx-table-body');
        tbody.innerHTML = pageTxs.map(tx => `
            <tr>
                <td><span class="hash-cell" data-hash="${tx.hash}">${tx.hash.slice(0, 14)}...</span></td>
                <td>
                    <span class="address-cell" title="${tx.from}">${DataEngine.shortenAddress(tx.from)}</span>
                    ${tx.fromInfo.label !== 'Unbekannt' ? `<br><small style="color:var(--text-muted)">${tx.fromInfo.label}</small>` : ''}
                </td>
                <td>
                    <span class="address-cell" title="${tx.to}">${DataEngine.shortenAddress(tx.to)}</span>
                    ${tx.toInfo.label !== 'Unbekannt' ? `<br><small style="color:var(--text-muted)">${tx.toInfo.label}</small>` : ''}
                </td>
                <td>
                    <strong>${tx.amount} ETH</strong><br>
                    <small style="color:var(--text-muted)">${DataEngine.formatUSD(tx.usdValue)}</small>
                </td>
                <td>${DataEngine.timeAgo(tx.time)}</td>
                <td><span class="risk-badge ${tx.risk}">${tx.risk}</span></td>
                <td>
                    <button class="action-btn" title="Details" onclick="App.showTxDetails('${tx.hash}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4m0-4h.01"/></svg>
                    </button>
                    <button class="action-btn" title="Flow tracen" onclick="App.traceFromTx('${tx.from}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="6" r="3"/><circle cx="18" cy="18" r="3"/><path d="M6 9v6l6-6h6"/></svg>
                    </button>
                </td>
            </tr>
        `).join('');

        // Pagination
        const pag = document.getElementById('tx-pagination');
        let pagHtml = `<button ${this.txPage <= 1 ? 'disabled' : ''} onclick="App.txGoToPage(${this.txPage - 1})">&laquo;</button>`;

        const maxButtons = 7;
        let startPage = Math.max(1, this.txPage - 3);
        let endPage = Math.min(totalPages, startPage + maxButtons - 1);
        startPage = Math.max(1, endPage - maxButtons + 1);

        for (let i = startPage; i <= endPage; i++) {
            pagHtml += `<button class="${i === this.txPage ? 'active' : ''}" onclick="App.txGoToPage(${i})">${i}</button>`;
        }

        pagHtml += `<button ${this.txPage >= totalPages ? 'disabled' : ''} onclick="App.txGoToPage(${this.txPage + 1})">&raquo;</button>`;
        pag.innerHTML = pagHtml;
    },

    txGoToPage(page) {
        this.txPage = page;
        this.renderTransactionTable();
    },

    showTxDetails(hash) {
        const tx = this.transactions.find(t => t.hash === hash);
        if (!tx) return;

        this.showModal('Transaktionsdetails', `
            <div class="detail-row">
                <span class="detail-label">TX Hash</span>
                <span class="detail-value mono">${tx.hash}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Status</span>
                <span class="detail-value">${tx.status === 'confirmed' ? '&#9989; Bestätigt' : '&#9203; Pending'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Von</span>
                <span class="detail-value mono">${tx.from}<br><small>${tx.fromInfo.label}</small></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">An</span>
                <span class="detail-value mono">${tx.to}<br><small>${tx.toInfo.label}</small></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Betrag</span>
                <span class="detail-value">${tx.amount} ETH (${DataEngine.formatUSD(tx.usdValue)})</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Block</span>
                <span class="detail-value">${tx.block.toLocaleString()}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Gas Used</span>
                <span class="detail-value">${tx.gasUsed.toLocaleString()}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Gas Price</span>
                <span class="detail-value">${tx.gasPrice} Gwei</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Risiko</span>
                <span class="detail-value"><span class="risk-badge ${tx.risk}">${tx.risk}</span></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Zeitstempel</span>
                <span class="detail-value">${tx.time.toLocaleString('de-DE')}</span>
            </div>
        `);
    },

    traceFromTx(address) {
        this.navigateTo('flowview');
        document.getElementById('flow-start-address').value = address;
        this.traceFlow(address);
    },

    exportCSV() {
        const txs = this.getFilteredTransactions();
        let csv = 'Hash,Von,An,Betrag (ETH),USD Wert,Risiko,Zeit\n';
        txs.forEach(tx => {
            csv += `${tx.hash},${tx.from},${tx.to},${tx.amount},${tx.usdValue},${tx.risk},${tx.time.toISOString()}\n`;
        });

        const blob = new Blob([csv], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'chainscope-transactions.csv';
        a.click();
        URL.revokeObjectURL(url);
        this.showToast('CSV Export erfolgreich', 'success');
    },

    // ========================================
    // Wallet Analysis
    // ========================================
    analyzeWallet(address) {
        if (!address) return;

        const data = DataEngine.generateWalletData(address);
        const result = document.getElementById('wallet-result');
        result.classList.remove('hidden');

        // Header
        document.getElementById('wallet-address-display').textContent = DataEngine.shortenAddress(address);
        document.getElementById('wallet-avatar').textContent = address.slice(2, 4).toUpperCase();

        // Tags
        document.getElementById('wallet-tags').innerHTML = data.tags.map(t =>
            `<span class="wallet-tag ${t.class}">${t.text}</span>`
        ).join('');

        // Risk score
        const riskColor = data.riskScore >= 80 ? 'var(--accent-red)' :
                         data.riskScore >= 50 ? 'var(--accent-orange)' :
                         data.riskScore >= 30 ? 'var(--accent-yellow)' : 'var(--accent-green)';

        document.getElementById('wallet-risk-display').innerHTML = `
            <div class="score-value" style="color:${riskColor}">${data.riskScore}</div>
            <div class="score-label">Risk Score</div>
        `;

        // Stats
        document.getElementById('wallet-balance').textContent = data.balance + ' ETH';
        document.getElementById('wallet-received').textContent = data.totalReceived + ' ETH';
        document.getElementById('wallet-sent').textContent = data.totalSent + ' ETH';
        document.getElementById('wallet-tx-count').textContent = data.txCount.toLocaleString();
        document.getElementById('wallet-first-tx').textContent = data.firstTx.toLocaleDateString('de-DE');
        document.getElementById('wallet-last-tx').textContent = data.lastTx.toLocaleDateString('de-DE');

        // Activity chart
        ChartEngine.drawBarChart('wallet-activity-chart', data.activityData);

        // Cluster view
        this.renderClusterView(data.clusters, address);

        // Transaction history
        const historyBody = document.getElementById('wallet-tx-history');
        historyBody.innerHTML = data.history.map(h => `
            <tr>
                <td><span class="hash-cell">${h.hash.slice(0, 14)}...</span></td>
                <td><span class="direction-badge ${h.direction}">${h.direction === 'in' ? 'Eingehend' : 'Ausgehend'}</span></td>
                <td>
                    <span class="address-cell">${DataEngine.shortenAddress(h.counterparty)}</span>
                    ${h.counterpartyInfo.label !== 'Unbekannt' ? `<br><small style="color:var(--text-muted)">${h.counterpartyInfo.label}</small>` : ''}
                </td>
                <td class="${h.direction === 'in' ? 'amount-positive' : 'amount-negative'}">${h.direction === 'in' ? '+' : '-'}${h.amount} ETH</td>
                <td>${DataEngine.timeAgo(h.time)}</td>
            </tr>
        `).join('');
    },

    renderClusterView(clusters, centerAddress) {
        const container = document.getElementById('wallet-cluster');
        container.innerHTML = '';

        const w = container.offsetWidth;
        const h = 300;
        container.style.height = h + 'px';

        // Center node
        const center = document.createElement('div');
        center.className = 'cluster-node center';
        center.style.left = (w / 2 - 35) + 'px';
        center.style.top = (h / 2 - 35) + 'px';
        center.style.background = 'var(--accent-blue-dim)';
        center.style.borderColor = 'var(--accent-blue)';
        center.style.color = 'var(--accent-blue)';
        center.textContent = centerAddress.slice(2, 6);
        container.appendChild(center);

        // Cluster nodes
        clusters.forEach((c, i) => {
            const angle = (Math.PI * 2 / clusters.length) * i;
            const radius = 110;
            const x = w / 2 + Math.cos(angle) * radius - 25;
            const y = h / 2 + Math.sin(angle) * radius - 25;

            const typeColor = FlowEngine.typeColors[c.info.type] || FlowEngine.typeColors.unknown;

            const node = document.createElement('div');
            node.className = 'cluster-node';
            node.style.left = x + 'px';
            node.style.top = y + 'px';
            node.style.background = typeColor + '22';
            node.style.borderColor = typeColor;
            node.style.color = typeColor;
            node.textContent = c.address.slice(2, 6);
            node.title = `${c.info.label}\nTXs: ${c.txCount}\nVol: ${c.volume} ETH`;
            container.appendChild(node);

            // Draw line (using an SVG overlay would be better, but this is simpler)
            const line = document.createElement('div');
            const dx = x + 25 - w / 2;
            const dy = y + 25 - h / 2;
            const length = Math.sqrt(dx * dx + dy * dy);
            const angleDeg = Math.atan2(dy, dx) * (180 / Math.PI);

            line.style.cssText = `
                position: absolute;
                left: ${w / 2}px;
                top: ${h / 2}px;
                width: ${length}px;
                height: 1px;
                background: ${typeColor}44;
                transform-origin: 0 0;
                transform: rotate(${angleDeg}deg);
                pointer-events: none;
            `;
            container.appendChild(line);
        });
    },

    // ========================================
    // Flow View
    // ========================================
    loadFlowView() {
        document.getElementById('trace-flow').addEventListener('click', () => {
            const addr = document.getElementById('flow-start-address').value || DataEngine.randomAddress();
            this.traceFlow(addr);
        });

        document.getElementById('flow-zoom-in').addEventListener('click', () => FlowEngine.zoomIn());
        document.getElementById('flow-zoom-out').addEventListener('click', () => FlowEngine.zoomOut());
        document.getElementById('flow-reset').addEventListener('click', () => FlowEngine.reset());

        // Auto-load demo
        setTimeout(() => {
            if (this.currentPage === 'flowview') {
                const demoAddr = Object.keys(DataEngine.knownAddresses)[0];
                document.getElementById('flow-start-address').value = demoAddr;
                this.traceFlow(demoAddr);
            }
        }, 300);
    },

    traceFlow(address) {
        const depth = parseInt(document.getElementById('flow-depth').value) || 2;
        const minAmount = parseFloat(document.getElementById('flow-min-amount').value) || 0.1;

        const graphData = DataEngine.generateFlowGraph(address, depth, minAmount);
        FlowEngine.resize();
        FlowEngine.setData(graphData);
        this.showToast(`Flow getracet: ${graphData.nodes.length} Knoten, ${graphData.edges.length} Kanten`, 'info');
    },

    // ========================================
    // Risk Scoring
    // ========================================
    loadRiskPage() {
        // Gauge meter
        ChartEngine.drawGaugeMeter('risk-meter-canvas', 64);

        // Risk table
        const addresses = DataEngine.generateRiskAddresses(20);
        const tbody = document.getElementById('risk-table-body');

        tbody.innerHTML = addresses.map(a => `
            <tr>
                <td>
                    <span class="address-cell">${DataEngine.shortenAddress(a.address)}</span>
                    ${a.info.label !== 'Unbekannt' ? `<br><small style="color:var(--text-muted)">${a.info.label}</small>` : ''}
                </td>
                <td>
                    <span class="risk-badge ${a.riskScore >= 90 ? 'critical' : a.riskScore >= 70 ? 'high' : a.riskScore >= 40 ? 'medium' : 'low'}">
                        ${a.riskScore}
                    </span>
                </td>
                <td>${a.category}</td>
                <td>${a.volume30d} ETH</td>
                <td>${DataEngine.timeAgo(a.lastActivity)}</td>
                <td>
                    <button class="action-btn" title="Analysieren" onclick="App.navigateTo('wallets'); document.getElementById('wallet-search').value='${a.address}'; App.analyzeWallet('${a.address}');">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                    </button>
                    <button class="action-btn" title="Flow tracen" onclick="App.traceFromTx('${a.address}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="6" r="3"/><circle cx="18" cy="18" r="3"/><path d="M6 9v6l6-6h6"/></svg>
                    </button>
                </td>
            </tr>
        `).join('');

        // Export button
        document.getElementById('export-risk-report').addEventListener('click', () => {
            this.showToast('Risk Report wird generiert...', 'info');
            setTimeout(() => this.showToast('Risk Report exportiert', 'success'), 2000);
        });
    },

    // ========================================
    // Alerts
    // ========================================
    loadAlerts() {
        // Alert rules
        const rules = [
            { name: 'Große Transaktionen', desc: 'Alert bei TX > 100 ETH an Mixer', icon: 'red', active: true },
            { name: 'Sanktionsliste', desc: 'OFAC/EU Sanktions-Monitoring', icon: 'orange', active: true },
            { name: 'Ungewöhnliche Muster', desc: 'ML-basierte Anomalie-Erkennung', icon: 'blue', active: true },
            { name: 'Whale Alerts', desc: 'Bewegungen > 1000 BTC/10000 ETH', icon: 'orange', active: false },
        ];

        document.getElementById('alert-rules-list').innerHTML = rules.map((r, i) => `
            <div class="alert-rule-item">
                <div class="alert-rule-icon ${r.icon}">
                    ${r.icon === 'red' ? '&#9888;' : r.icon === 'orange' ? '&#128269;' : '&#9881;'}
                </div>
                <div class="alert-rule-info">
                    <div class="alert-rule-name">${r.name}</div>
                    <div class="alert-rule-desc">${r.desc}</div>
                </div>
                <label class="alert-rule-toggle">
                    <input type="checkbox" ${r.active ? 'checked' : ''}>
                    <span class="toggle-slider"></span>
                </label>
            </div>
        `).join('');

        // Alert feed
        const alerts = DataEngine.generateAlerts(15);
        document.getElementById('alert-feed').innerHTML = alerts.map(a => `
            <div class="alert-feed-item ${a.severity}">
                <div class="alert-content">
                    <div class="alert-title">${a.title}</div>
                    <div class="alert-desc">${a.description}</div>
                    <div class="alert-meta">
                        <span>${a.time.toLocaleString('de-DE')}</span>
                        <span>${a.severity.toUpperCase()}</span>
                    </div>
                </div>
            </div>
        `).join('');

        // Create alert rule button
        document.getElementById('create-alert-rule').addEventListener('click', () => {
            this.showModal('Neue Alert-Regel', `
                <div class="form-group">
                    <label>Name</label>
                    <input type="text" placeholder="z.B. Mixer-Monitoring">
                </div>
                <div class="form-group">
                    <label>Trigger-Typ</label>
                    <select>
                        <option>Transaktion an Adresse</option>
                        <option>Betrag überschreitet</option>
                        <option>Risk Score über Schwellwert</option>
                        <option>Neuer Cluster erkannt</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Schwellwert</label>
                    <input type="number" placeholder="z.B. 100">
                </div>
                <div class="form-group">
                    <label>Beschreibung</label>
                    <textarea placeholder="Beschreibung der Regel..."></textarea>
                </div>
                <div class="form-actions">
                    <button class="btn btn-secondary" onclick="document.getElementById('modal-overlay').classList.add('hidden')">Abbrechen</button>
                    <button class="btn btn-primary" onclick="App.showToast('Alert-Regel erstellt', 'success'); document.getElementById('modal-overlay').classList.add('hidden');">Erstellen</button>
                </div>
            `);
        });

        // Update badge
        const unreadCount = alerts.filter(a => !a.read).length;
        document.getElementById('alert-badge').textContent = unreadCount;
    },

    // ========================================
    // Reports
    // ========================================
    loadReports() {
        const grid = document.getElementById('reports-grid');
        grid.innerHTML = DataEngine.reports.map(r => `
            <div class="report-card">
                <div class="report-icon" style="background:var(--bg-input)">${r.icon}</div>
                <h4>${r.title}</h4>
                <p>${r.desc}</p>
                <div class="report-meta">
                    <span>${r.date}</span>
                    <span style="color:${r.status === 'Laufend' ? 'var(--accent-blue)' : 'var(--accent-green)'}">${r.status}</span>
                </div>
            </div>
        `).join('');

        // Generate report button
        document.getElementById('generate-report').addEventListener('click', () => {
            this.showModal('Report generieren', `
                <div class="form-group">
                    <label>Report-Typ</label>
                    <select>
                        <option>AML/Compliance Report</option>
                        <option>Sanctions Screening</option>
                        <option>Risk Assessment</option>
                        <option>Adress-Cluster Report</option>
                        <option>Cross-Chain Analyse</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Zeitraum</label>
                    <select>
                        <option>Letzte 7 Tage</option>
                        <option>Letzte 30 Tage</option>
                        <option>Letzte 90 Tage</option>
                        <option>Individuell</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Format</label>
                    <select>
                        <option>PDF</option>
                        <option>CSV</option>
                        <option>JSON</option>
                    </select>
                </div>
                <div class="form-actions">
                    <button class="btn btn-secondary" onclick="document.getElementById('modal-overlay').classList.add('hidden')">Abbrechen</button>
                    <button class="btn btn-primary" onclick="App.showToast('Report wird generiert...', 'info'); document.getElementById('modal-overlay').classList.add('hidden'); setTimeout(() => App.showToast('Report bereit zum Download', 'success'), 3000);">Generieren</button>
                </div>
            `);
        });
    }
};

// ========================================
// Initialize wallet analyze button
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    App.init();

    // Wallet analyze button
    document.getElementById('analyze-wallet').addEventListener('click', () => {
        const addr = document.getElementById('wallet-search').value.trim();
        if (addr) App.analyzeWallet(addr);
    });

    // Enter key for wallet search
    document.getElementById('wallet-search').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            const addr = e.target.value.trim();
            if (addr) App.analyzeWallet(addr);
        }
    });
});
