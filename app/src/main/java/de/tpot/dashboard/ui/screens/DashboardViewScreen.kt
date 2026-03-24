package de.tpot.dashboard.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Base64
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.tpot.dashboard.data.ConnectionStore
import de.tpot.dashboard.data.TpotConnection
import de.tpot.dashboard.data.TpotDashboard
import de.tpot.dashboard.data.TpotDashboards
import de.tpot.dashboard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DashboardViewScreen(
    dashboardId: String,
    connectionStore: ConnectionStore,
    onBack: () -> Unit,
    onSwitchDashboard: (String) -> Unit
) {
    val connection by connectionStore.connection.collectAsState(initial = TpotConnection())
    val dashboard = TpotDashboards.getAll().find { it.id == dashboardId }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var currentTitle by remember { mutableStateOf("") }
    var showDashboardPicker by remember { mutableStateOf(false) }

    if (dashboard == null) {
        onBack()
        return
    }

    val url = TpotDashboards.buildUrl(connection, dashboard)
    val allDashboards = remember { TpotDashboards.getAll() }

    // Handle Android back button - navigate within WebView first
    BackHandler {
        when {
            showDashboardPicker -> showDashboardPicker = false
            isFullscreen -> isFullscreen = false
            canGoBack -> webView?.goBack()
            else -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // WebView fills the entire screen
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        setSupportMultipleWindows(false)
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = settings.userAgentString + " TPotDashboard/1.0"
                        // Allow file access for CyberChef etc.
                        allowFileAccess = true
                        allowContentAccess = true
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    // Enable cookies for session persistence
                    CookieManager.getInstance().apply {
                        setAcceptCookie(true)
                        setAcceptThirdPartyCookies(this@apply, true)
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            loadError = null
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            canGoBack = view?.canGoBack() == true
                            canGoForward = view?.canGoForward() == true
                            // Persist cookies
                            CookieManager.getInstance().flush()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                                loadError = "Fehler: ${error?.description}"
                            }
                        }

                        override fun onReceivedHttpAuthRequest(
                            view: WebView?,
                            handler: HttpAuthHandler?,
                            host: String?,
                            realm: String?
                        ) {
                            if (connection.username.isNotBlank()) {
                                handler?.proceed(connection.username, connection.password)
                            } else {
                                handler?.cancel()
                            }
                        }

                        @SuppressLint("WebViewClientOnReceivedSslError")
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            if (connection.acceptSelfSigned) {
                                handler?.proceed()
                            } else {
                                handler?.cancel()
                                loadError = "SSL-Zertifikat ungueltig. Aktiviere 'Selbstsignierte Zertifikate' in den Einstellungen."
                            }
                        }

                        // Keep navigation within WebView (don't open external browser)
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress
                            if (newProgress >= 100) {
                                isLoading = false
                            }
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            currentTitle = title ?: ""
                        }

                        // Handle JS alerts
                        override fun onJsAlert(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            result: JsResult?
                        ): Boolean {
                            return false // Use default dialog
                        }

                        override fun onJsConfirm(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            result: JsResult?
                        ): Boolean {
                            return false
                        }

                        override fun onJsPrompt(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            defaultValue: String?,
                            result: JsPromptResult?
                        ): Boolean {
                            return false
                        }
                    }

                    // Set basic auth header
                    val authHeader = if (connection.username.isNotBlank()) {
                        val credentials = "${connection.username}:${connection.password}"
                        val encoded = Base64.encodeToString(
                            credentials.toByteArray(),
                            Base64.NO_WRAP
                        )
                        mapOf("Authorization" to "Basic $encoded")
                    } else {
                        emptyMap()
                    }

                    loadUrl(url, authHeader)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isFullscreen) Modifier.padding(top = 56.dp) else Modifier
                )
        )

        // Top toolbar (hideable for fullscreen)
        AnimatedVisibility(
            visible = !isFullscreen,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Surface(
                color = TpotSurface.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(onClick = {
                        if (canGoBack) webView?.goBack() else onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurueck",
                            tint = TpotTextPrimary
                        )
                    }

                    // Dashboard name & URL
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDashboardPicker = true }
                    ) {
                        Text(
                            text = dashboard.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TpotGreen,
                            maxLines = 1
                        )
                        Text(
                            text = if (currentTitle.isNotBlank()) currentTitle else url,
                            style = MaterialTheme.typography.labelSmall,
                            color = TpotTextTertiary,
                            maxLines = 1
                        )
                    }

                    // Quick-switch dashboards
                    IconButton(onClick = { showDashboardPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Dashboard wechseln",
                            tint = TpotTextSecondary
                        )
                    }

                    // Refresh
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Neu laden",
                            tint = TpotTextSecondary
                        )
                    }

                    // Fullscreen toggle
                    IconButton(onClick = { isFullscreen = true }) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Vollbild",
                            tint = TpotTextSecondary
                        )
                    }
                }
            }
        }

        // Loading progress bar
        if (isLoading) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (!isFullscreen) Modifier.padding(top = 56.dp) else Modifier
                    ),
                color = TpotGreen,
                trackColor = TpotSurface
            )
        }

        // Fullscreen: small floating exit button
        if (isFullscreen) {
            FloatingActionButton(
                onClick = { isFullscreen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
                    .size(36.dp),
                containerColor = TpotSurface.copy(alpha = 0.7f),
                contentColor = TpotTextSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Vollbild beenden",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Error state
        if (loadError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TpotBackground.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = TpotAmber,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Verbindungsfehler",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TpotTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TpotTextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            loadError = null
                            webView?.reload()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TpotGreen,
                            contentColor = TpotBackground
                        )
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Erneut versuchen")
                    }
                }
            }
        }

        // Dashboard quick-switch overlay
        if (showDashboardPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TpotBackground.copy(alpha = 0.85f))
                    .clickable { showDashboardPicker = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = TpotSurface,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "> Dashboard wechseln",
                            style = MaterialTheme.typography.titleSmall,
                            color = TpotGreen,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        allDashboards.forEach { d ->
                            val isActive = d.id == dashboardId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) TpotGreen.copy(alpha = 0.12f)
                                        else TpotBackground.copy(alpha = 0.01f)
                                    )
                                    .clickable {
                                        showDashboardPicker = false
                                        if (!isActive) {
                                            onSwitchDashboard(d.id)
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = d.icon,
                                    contentDescription = null,
                                    tint = if (isActive) TpotGreen else TpotTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = d.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isActive) TpotGreen else TpotTextPrimary
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TpotGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
