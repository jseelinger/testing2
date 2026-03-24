// ==================== Dashboard Config ====================
const DASHBOARDS = [
    {
        id: 'attack_map',
        name: 'Attack Map',
        description: 'Echtzeit-Weltkarte der Angriffe auf deine Honeypots',
        path: '/map/',
        portKey: 'web',
        icon: 'public',
        category: 'Monitoring'
    },
    {
        id: 'kibana',
        name: 'Kibana',
        description: 'Haupt-Dashboard mit allen Honeypot-Daten und Visualisierungen',
        path: '/kibana/',
        portKey: 'web',
        icon: 'dashboard',
        category: 'Monitoring'
    },
    {
        id: 'cockpit',
        name: 'Cockpit',
        description: 'System-Monitoring: CPU, RAM, Netzwerk, Container-Status',
        path: '/',
        portKey: 'cockpit',
        icon: 'speed',
        category: 'Monitoring'
    },
    {
        id: 'elasticvue',
        name: 'Elasticvue',
        description: 'Elasticsearch-Daten durchsuchen und verwalten',
        path: '/elasticvue/',
        portKey: 'web',
        icon: 'storage',
        category: 'Analyse'
    },
    {
        id: 'spiderfoot',
        name: 'SpiderFoot',
        description: 'OSINT-Reconnaissance und Threat Intelligence',
        path: '/spiderfoot/',
        portKey: 'web',
        icon: 'bug_report',
        category: 'Analyse'
    },
    {
        id: 'cyberchef',
        name: 'CyberChef',
        description: 'Daten-Analyse, Encoding, Decoding und Transformation',
        path: '/cyberchef/',
        portKey: 'web',
        icon: 'code',
        category: 'Tools'
    }
];

// ==================== State ====================
let state = {
    serverUrl: '',
    username: '',
    password: '',
    portWeb: 64297,
    portCockpit: 64294,
    currentScreen: 'welcome',
    currentDashboard: null
};

// ==================== DOM References ====================
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

const sidebar = $('#sidebar');
const navList = $('#navList');
const connectionStatus = $('#connectionStatus');
const mainContent = $('#mainContent');
const settingsModal = $('#settingsModal');
const toast = $('#toast');
const dashboardGrid = $('#dashboardGrid');
const dashboardFrame = $('#dashboardFrame');
const loadingBar = $('#loadingBar');
const iframeError = $('#iframeError');

// ==================== Storage ====================
function loadSettings() {
    try {
        const saved = localStorage.getItem('tpot_settings');
        if (saved) {
            const parsed = JSON.parse(saved);
            state = { ...state, ...parsed };
        }
    } catch (e) {
        console.error('Failed to load settings:', e);
    }
}

function saveSettings() {
    try {
        localStorage.setItem('tpot_settings', JSON.stringify({
            serverUrl: state.serverUrl,
            username: state.username,
            password: state.password,
            portWeb: state.portWeb,
            portCockpit: state.portCockpit
        }));
    } catch (e) {
        console.error('Failed to save settings:', e);
    }
}

// ==================== URL Builder ====================
function buildDashboardUrl(dashboard) {
    const baseUrl = state.serverUrl.replace(/\/+$/, '');
    const port = dashboard.portKey === 'cockpit' ? state.portCockpit : state.portWeb;

    // Build URL with basic auth embedded for iframe usage
    let url = `${baseUrl}:${port}${dashboard.path}`;

    // For iframe, we embed credentials in the URL if using https
    if (state.username && url.startsWith('https://')) {
        const encoded = encodeURIComponent(state.username) + ':' + encodeURIComponent(state.password);
        url = url.replace('https://', `https://${encoded}@`);
    } else if (state.username && url.startsWith('http://')) {
        const encoded = encodeURIComponent(state.username) + ':' + encodeURIComponent(state.password);
        url = url.replace('http://', `http://${encoded}@`);
    }

    return url;
}

function buildCleanUrl(dashboard) {
    const baseUrl = state.serverUrl.replace(/\/+$/, '');
    const port = dashboard.portKey === 'cockpit' ? state.portCockpit : state.portWeb;
    return `${baseUrl}:${port}${dashboard.path}`;
}

// ==================== UI Updates ====================
function updateConnectionStatus() {
    const isConnected = state.serverUrl.length > 0;
    connectionStatus.className = 'connection-status' + (isConnected ? ' connected' : '');
    connectionStatus.querySelector('.status-text').textContent =
        isConnected ? 'Verbunden' : 'Nicht verbunden';
}

function showScreen(name) {
    $$('.screen').forEach(s => s.classList.remove('active'));
    const screen = $(`#screen${name.charAt(0).toUpperCase() + name.slice(1)}`);
    if (screen) screen.classList.add('active');
    state.currentScreen = name;

    // Update nav active state
    $$('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.id === state.currentDashboard?.id);
    });
}

function renderDashboardGrid() {
    dashboardGrid.innerHTML = '';

    const categories = [...new Set(DASHBOARDS.map(d => d.category))];

    categories.forEach(category => {
        const dashboards = DASHBOARDS.filter(d => d.category === category);

        dashboards.forEach(d => {
            const card = document.createElement('div');
            card.className = 'dash-card';
            card.innerHTML = `
                <div class="dash-card-icon">
                    <span class="material-icons">${d.icon}</span>
                </div>
                <div class="dash-card-info">
                    <div class="dash-card-name">${d.name}</div>
                    <div class="dash-card-desc">${d.description}</div>
                </div>
                <span class="material-icons dash-card-arrow">chevron_right</span>
            `;
            card.addEventListener('click', () => openDashboard(d));
            dashboardGrid.appendChild(card);
        });
    });
}

function updateServerInfo() {
    $('#infoHost').textContent = state.serverUrl || '-';
    $('#infoUser').textContent = state.username || '-';
}

// ==================== Dashboard Loading ====================
function openDashboard(dashboard) {
    state.currentDashboard = dashboard;
    showScreen('dashboard');

    const url = buildDashboardUrl(dashboard);
    const cleanUrl = buildCleanUrl(dashboard);

    $('#dashboardName').textContent = dashboard.name;
    $('#dashboardUrl').textContent = cleanUrl;

    // Show loading
    loadingBar.classList.add('active');
    iframeError.classList.remove('active');

    // Load in iframe
    dashboardFrame.src = url;

    // Detect load errors (iframe won't fire onerror for X-Frame-Options)
    // Use a timeout as fallback
    const loadTimeout = setTimeout(() => {
        loadingBar.classList.remove('active');
        // Check if iframe loaded by trying to access it
        try {
            // This will throw if cross-origin or blocked
            const doc = dashboardFrame.contentDocument;
            if (!doc || !doc.body || doc.body.innerHTML === '') {
                showIframeError(cleanUrl);
            }
        } catch (e) {
            showIframeError(cleanUrl);
        }
    }, 5000);

    dashboardFrame.onload = () => {
        clearTimeout(loadTimeout);
        loadingBar.classList.remove('active');
    };

    dashboardFrame.onerror = () => {
        clearTimeout(loadTimeout);
        loadingBar.classList.remove('active');
        showIframeError(cleanUrl);
    };

    // Update sidebar active
    $$('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.id === dashboard.id);
    });

    // Close mobile sidebar
    closeSidebar();
}

function showIframeError(url) {
    iframeError.classList.add('active');
    $('#btnOpenDirect').onclick = () => window.open(url, '_blank');
}

// ==================== Settings Modal ====================
function openSettings() {
    $('#inputServerUrl').value = state.serverUrl;
    $('#inputUsername').value = state.username;
    $('#inputPassword').value = state.password;
    $('#inputPortWeb').value = state.portWeb;
    $('#inputPortCockpit').value = state.portCockpit;
    settingsModal.classList.add('active');
}

function closeSettings() {
    settingsModal.classList.remove('active');
}

function saveSettingsFromForm() {
    state.serverUrl = $('#inputServerUrl').value.replace(/\/+$/, '');
    state.username = $('#inputUsername').value;
    state.password = $('#inputPassword').value;
    state.portWeb = parseInt($('#inputPortWeb').value) || 64297;
    state.portCockpit = parseInt($('#inputPortCockpit').value) || 64294;

    saveSettings();
    updateConnectionStatus();
    updateServerInfo();
    closeSettings();
    showToast('Verbindung gespeichert!');

    // If we were on welcome, switch to home
    if (state.serverUrl && state.currentScreen === 'welcome') {
        showScreen('home');
    }
}

function clearSettings() {
    state.serverUrl = '';
    state.username = '';
    state.password = '';
    state.portWeb = 64297;
    state.portCockpit = 64294;

    saveSettings();
    updateConnectionStatus();
    updateServerInfo();
    closeSettings();
    showScreen('welcome');
    showToast('Verbindungsdaten geloescht');
}

// ==================== Toast ====================
function showToast(message) {
    toast.textContent = message;
    toast.classList.add('active');
    setTimeout(() => toast.classList.remove('active'), 2500);
}

// ==================== Mobile Sidebar ====================
let overlay = null;

function openSidebar() {
    sidebar.classList.add('open');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.className = 'sidebar-overlay';
        overlay.addEventListener('click', closeSidebar);
        document.body.appendChild(overlay);
    }
    overlay.classList.add('active');
}

function closeSidebar() {
    sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('active');
}

// ==================== Event Listeners ====================
function initEvents() {
    // Settings buttons
    $('#btnSettings').addEventListener('click', openSettings);
    $('#btnSettingsMobile').addEventListener('click', openSettings);
    $('#btnSetupConnect').addEventListener('click', openSettings);
    $('#btnCloseSettings').addEventListener('click', closeSettings);
    $('#btnSave').addEventListener('click', saveSettingsFromForm);
    $('#btnClear').addEventListener('click', clearSettings);

    // Close modal on overlay click
    settingsModal.addEventListener('click', (e) => {
        if (e.target === settingsModal) closeSettings();
    });

    // Password toggle
    $('#btnTogglePw').addEventListener('click', () => {
        const input = $('#inputPassword');
        const icon = $('#btnTogglePw .material-icons');
        if (input.type === 'password') {
            input.type = 'text';
            icon.textContent = 'visibility_off';
        } else {
            input.type = 'password';
            icon.textContent = 'visibility';
        }
    });

    // Mobile menu
    $('#btnMenu').addEventListener('click', openSidebar);

    // Sidebar nav items
    $$('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const dashboard = DASHBOARDS.find(d => d.id === item.dataset.id);
            if (dashboard) {
                if (!state.serverUrl) {
                    openSettings();
                    showToast('Bitte zuerst Server konfigurieren');
                } else {
                    openDashboard(dashboard);
                }
            }
        });
    });

    // Dashboard toolbar
    $('#btnBack').addEventListener('click', () => {
        state.currentDashboard = null;
        dashboardFrame.src = 'about:blank';
        showScreen('home');
        $$('.nav-item').forEach(i => i.classList.remove('active'));
    });

    $('#btnRefresh').addEventListener('click', () => {
        if (state.currentDashboard) {
            openDashboard(state.currentDashboard);
        }
    });

    $('#btnOpenExternal').addEventListener('click', () => {
        if (state.currentDashboard) {
            window.open(buildCleanUrl(state.currentDashboard), '_blank');
        }
    });

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (settingsModal.classList.contains('active')) {
                closeSettings();
            } else if (state.currentScreen === 'dashboard') {
                $('#btnBack').click();
            }
        }
    });
}

// ==================== Init ====================
function init() {
    loadSettings();
    updateConnectionStatus();
    updateServerInfo();
    renderDashboardGrid();
    initEvents();

    // Show appropriate screen
    if (state.serverUrl) {
        showScreen('home');
    } else {
        showScreen('welcome');
    }
}

document.addEventListener('DOMContentLoaded', init);
