/* ========================================
   ChainScope - Mock Data & Data Engine
   ======================================== */

const DataEngine = {
    // Known addresses with labels
    knownAddresses: {
        '0x742d35Cc6634C0532925a3b844Bc9e7595f2bD38': { label: 'Binance Hot Wallet', type: 'exchange', risk: 12 },
        '0xBE0eB53F46cd790Cd13851d5EFf43D12404d33E8': { label: 'Binance Cold Storage', type: 'exchange', risk: 5 },
        '0x3f5CE5FBFe3E9af3971dD833D26bA9b5C936f0bE': { label: 'Binance 2', type: 'exchange', risk: 8 },
        '0xDA9dfA130Df4dE4673b89022EE50ff26f6EA73Cf': { label: 'Kraken', type: 'exchange', risk: 10 },
        '0x267be1C1D684F78cb4F6a176C4911b741E4Ffdc0': { label: 'Kraken Cold', type: 'exchange', risk: 6 },
        '0x8894E0a0c962CB723c1ef8580d0d3Bfe139D5b26': { label: 'Tornado Cash', type: 'mixer', risk: 95 },
        '0xd90e2f925DA726b50C4Ed8D0Fb90Ad053324F31b': { label: 'Tornado Cash Router', type: 'mixer', risk: 98 },
        '0x722122dF12D4e14e13Ac3b6895a86e84145b6967': { label: 'Tornado Cash Proxy', type: 'mixer', risk: 96 },
        '0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D': { label: 'Uniswap V2 Router', type: 'defi', risk: 15 },
        '0xE592427A0AEce92De3Edee1F18E0157C05861564': { label: 'Uniswap V3 Router', type: 'defi', risk: 12 },
        '0xd9e1cE17f2641f24aE83637ab66a2cca9C378B9F': { label: 'SushiSwap Router', type: 'defi', risk: 14 },
        '0x7Be8076f4EA4A4AD08075C2508e481d6C946D12b': { label: 'OpenSea', type: 'defi', risk: 18 },
        '0xDef1C0ded9bec7F1a1670819833240f027b25EfF': { label: '0x Exchange', type: 'defi', risk: 11 },
        '0x3cD751E6b0078Be393132286c442345e68C7C9a1': { label: 'Darknet Market X', type: 'darknet', risk: 99 },
        '0x9845e1909dCa337944a0272F1f9f7249833D2D19': { label: 'Ransomware Wallet', type: 'ransomware', risk: 100 },
        '0xA160cdAB225685dA1d56aa342Ad8841c3b53f291': { label: 'OFAC Sanctioned', type: 'sanctioned', risk: 100 },
        '0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359': { label: 'Verdächtig - Phishing', type: 'scam', risk: 92 },
        '0x1dA5821544e25c636c1417Ba96Ade4Cf6D2f9B5A': { label: 'Lazarus Group', type: 'sanctioned', risk: 100 },
        '0xc098B2a3Aa256032d8948fC7F1C52e5B5b9D4b34': { label: 'Unbekannt - Whale', type: 'unknown', risk: 45 },
        '0xE853c56864A2ebe4576a807D26Fdc4A0adA51919': { label: 'Unbekannt', type: 'unknown', risk: 50 },
    },

    // Generate random hex
    randomHex(length) {
        const chars = '0123456789abcdef';
        let result = '';
        for (let i = 0; i < length; i++) {
            result += chars[Math.floor(Math.random() * chars.length)];
        }
        return result;
    },

    // Generate random address
    randomAddress() {
        const knownKeys = Object.keys(this.knownAddresses);
        if (Math.random() < 0.3) {
            return knownKeys[Math.floor(Math.random() * knownKeys.length)];
        }
        return '0x' + this.randomHex(40);
    },

    // Shorten address
    shortenAddress(addr) {
        if (!addr) return '';
        return addr.slice(0, 8) + '...' + addr.slice(-6);
    },

    // Get address info
    getAddressInfo(addr) {
        return this.knownAddresses[addr] || { label: 'Unbekannt', type: 'unknown', risk: Math.floor(Math.random() * 60) + 10 };
    },

    // Generate transaction
    generateTransaction() {
        const from = this.randomAddress();
        const to = this.randomAddress();
        const amount = (Math.random() * 100).toFixed(4);
        const usdValue = (amount * 68432).toFixed(2);
        const risks = ['low', 'low', 'low', 'medium', 'medium', 'high', 'critical'];
        const risk = risks[Math.floor(Math.random() * risks.length)];
        const now = new Date();
        const timeOffset = Math.floor(Math.random() * 86400000);
        const time = new Date(now - timeOffset);

        return {
            hash: '0x' + this.randomHex(64),
            from,
            to,
            amount: parseFloat(amount),
            usdValue: parseFloat(usdValue),
            currency: 'ETH',
            risk,
            time,
            block: 19200000 + Math.floor(Math.random() * 100000),
            gasUsed: Math.floor(Math.random() * 200000) + 21000,
            gasPrice: Math.floor(Math.random() * 50) + 10,
            status: Math.random() > 0.02 ? 'confirmed' : 'pending',
            fromInfo: this.getAddressInfo(from),
            toInfo: this.getAddressInfo(to),
        };
    },

    // Generate batch of transactions
    generateTransactions(count) {
        const txs = [];
        for (let i = 0; i < count; i++) {
            txs.push(this.generateTransaction());
        }
        return txs.sort((a, b) => b.time - a.time);
    },

    // Generate wallet data
    generateWalletData(address) {
        const info = this.getAddressInfo(address);
        const balance = (Math.random() * 1000).toFixed(4);
        const totalReceived = (parseFloat(balance) + Math.random() * 5000).toFixed(4);
        const totalSent = (parseFloat(totalReceived) - parseFloat(balance)).toFixed(4);
        const txCount = Math.floor(Math.random() * 5000) + 100;

        const tags = [];
        if (info.type === 'exchange') tags.push({ text: 'Exchange', class: 'exchange' });
        if (info.type === 'defi') tags.push({ text: 'DeFi', class: 'defi' });
        if (info.type === 'mixer') tags.push({ text: 'Mixer', class: 'flagged' });
        if (info.type === 'sanctioned') tags.push({ text: 'Sanktioniert', class: 'flagged' });
        if (info.type === 'darknet') tags.push({ text: 'Darknet', class: 'flagged' });
        if (info.type === 'ransomware') tags.push({ text: 'Ransomware', class: 'flagged' });
        if (info.type === 'scam') tags.push({ text: 'Scam', class: 'flagged' });
        if (parseFloat(balance) > 500) tags.push({ text: 'Whale', class: 'whale' });

        const firstTx = new Date(Date.now() - Math.random() * 365 * 3 * 24 * 60 * 60 * 1000);
        const lastTx = new Date(Date.now() - Math.random() * 24 * 60 * 60 * 1000);

        // Activity data for chart (last 30 days)
        const activityData = [];
        for (let i = 29; i >= 0; i--) {
            const date = new Date(Date.now() - i * 24 * 60 * 60 * 1000);
            activityData.push({
                date: date.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit' }),
                incoming: Math.floor(Math.random() * 20),
                outgoing: Math.floor(Math.random() * 15),
                volume: (Math.random() * 50).toFixed(2)
            });
        }

        // Transaction history
        const history = [];
        for (let i = 0; i < 20; i++) {
            const isIncoming = Math.random() > 0.5;
            const counterparty = this.randomAddress();
            history.push({
                hash: '0x' + this.randomHex(64),
                direction: isIncoming ? 'in' : 'out',
                counterparty,
                counterpartyInfo: this.getAddressInfo(counterparty),
                amount: (Math.random() * 50).toFixed(4),
                time: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
            });
        }
        history.sort((a, b) => b.time - a.time);

        // Cluster data
        const clusters = [];
        const clusterCount = Math.floor(Math.random() * 6) + 4;
        for (let i = 0; i < clusterCount; i++) {
            const cAddr = this.randomAddress();
            clusters.push({
                address: cAddr,
                info: this.getAddressInfo(cAddr),
                txCount: Math.floor(Math.random() * 50) + 1,
                volume: (Math.random() * 100).toFixed(2)
            });
        }

        return {
            address,
            info,
            balance,
            totalReceived,
            totalSent,
            txCount,
            tags,
            firstTx,
            lastTx,
            activityData,
            history,
            clusters,
            riskScore: info.risk
        };
    },

    // Generate flow graph data
    generateFlowGraph(startAddress, depth, minAmount) {
        const nodes = [];
        const edges = [];
        const visited = new Set();

        const addNode = (addr, level) => {
            if (visited.has(addr)) return;
            visited.add(addr);
            const info = this.getAddressInfo(addr);
            nodes.push({ id: addr, label: info.label, type: info.type, risk: info.risk, level });

            if (level < depth) {
                const connections = Math.floor(Math.random() * 4) + 1;
                for (let i = 0; i < connections; i++) {
                    const target = this.randomAddress();
                    const amount = (Math.random() * 50 + parseFloat(minAmount)).toFixed(4);
                    edges.push({ from: addr, to: target, amount: parseFloat(amount) });
                    addNode(target, level + 1);
                }
            }
        };

        addNode(startAddress || this.randomAddress(), 0);
        return { nodes, edges };
    },

    // Risk table data
    generateRiskAddresses(count) {
        const addresses = [];
        const categories = ['Mixer', 'Darknet', 'Ransomware', 'Sanktioniert', 'Scam/Phishing', 'Geldwäsche'];
        for (let i = 0; i < count; i++) {
            const addr = this.randomAddress();
            const info = this.getAddressInfo(addr);
            addresses.push({
                address: addr,
                info,
                riskScore: Math.max(info.risk, Math.floor(Math.random() * 40) + 60),
                category: categories[Math.floor(Math.random() * categories.length)],
                volume30d: (Math.random() * 500).toFixed(2),
                lastActivity: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000)
            });
        }
        return addresses.sort((a, b) => b.riskScore - a.riskScore);
    },

    // Alert data
    generateAlerts(count) {
        const types = [
            { title: 'Große Transaktion erkannt', desc: 'Übertragung von {amount} ETH an Mixer-Adresse', severity: 'critical' },
            { title: 'Sanktionierte Adresse aktiv', desc: 'Neue Aktivität auf OFAC-gelisteter Adresse', severity: 'critical' },
            { title: 'Ungewöhnliches Muster', desc: 'Rapid-Fire Transaktionen von {addr} erkannt', severity: 'high' },
            { title: 'Darknet-Verbindung', desc: 'Funds-Flow zu bekanntem Darknet-Market', severity: 'high' },
            { title: 'Whale-Bewegung', desc: '{amount} BTC bewegt nach {hours}h Inaktivität', severity: 'medium' },
            { title: 'Neuer Cluster erkannt', desc: 'Adress-Cluster mit {count} verbundenen Wallets', severity: 'medium' },
        ];

        const alerts = [];
        for (let i = 0; i < count; i++) {
            const type = types[Math.floor(Math.random() * types.length)];
            const addr = this.randomAddress();
            alerts.push({
                id: i,
                title: type.title,
                description: type.desc
                    .replace('{amount}', (Math.random() * 100).toFixed(2))
                    .replace('{addr}', this.shortenAddress(addr))
                    .replace('{hours}', Math.floor(Math.random() * 48) + 1)
                    .replace('{count}', Math.floor(Math.random() * 20) + 5),
                severity: type.severity,
                address: addr,
                time: new Date(Date.now() - Math.random() * 48 * 60 * 60 * 1000),
                read: Math.random() > 0.3
            });
        }
        return alerts.sort((a, b) => b.time - a.time);
    },

    // Reports
    reports: [
        { title: 'Monatlicher AML-Report', desc: 'Zusammenfassung aller Anti-Geldwäsche-Aktivitäten', icon: '📊', date: '2026-03-15', status: 'Fertig' },
        { title: 'Sanctions Screening', desc: 'OFAC/EU Sanktionslisten-Abgleich', icon: '🔍', date: '2026-03-14', status: 'Fertig' },
        { title: 'Mixer-Analyse Q1', desc: 'Quartalsanalyse der Mixer-Aktivitäten', icon: '🌀', date: '2026-03-10', status: 'Fertig' },
        { title: 'Ransomware Tracker', desc: 'Aktive Ransomware-Wallet Überwachung', icon: '🔒', date: '2026-03-08', status: 'Laufend' },
        { title: 'DeFi Risk Assessment', desc: 'Risikobewertung der DeFi-Protokoll-Interaktionen', icon: '⚡', date: '2026-03-05', status: 'Fertig' },
        { title: 'Cross-Chain Report', desc: 'Bridge-Transaktionen und Cross-Chain Flows', icon: '🔗', date: '2026-03-01', status: 'Fertig' },
    ],

    // Volume chart data
    generateVolumeData(points) {
        const labels = [];
        const btcData = [];
        const ethData = [];
        for (let i = points - 1; i >= 0; i--) {
            const date = new Date(Date.now() - i * 3600000);
            labels.push(date.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' }));
            btcData.push(Math.random() * 500 + 200);
            ethData.push(Math.random() * 300 + 100);
        }
        return { labels, btcData, ethData };
    },

    // Format currency
    formatUSD(value) {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
    },

    // Format time ago
    timeAgo(date) {
        const seconds = Math.floor((Date.now() - date) / 1000);
        if (seconds < 60) return seconds + 's';
        const minutes = Math.floor(seconds / 60);
        if (minutes < 60) return minutes + 'min';
        const hours = Math.floor(minutes / 60);
        if (hours < 24) return hours + 'h';
        const days = Math.floor(hours / 24);
        return days + 'd';
    }
};
