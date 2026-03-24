package de.tpot.dashboard.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Base64
import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.tpot.dashboard.data.ConnectionStore
import de.tpot.dashboard.data.TpotConnection
import de.tpot.dashboard.data.TpotDashboards
import de.tpot.dashboard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DashboardViewScreen(
    dashboardId: String,
    connectionStore: ConnectionStore,
    onBack: () -> Unit
) {
    val connection by connectionStore.connection.collectAsState(initial = TpotConnection())
    val dashboard = TpotDashboards.getAll().find { it.id == dashboardId }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    if (dashboard == null) {
        onBack()
        return
    }

    val url = TpotDashboards.buildUrl(connection, dashboard)

    Scaffold(
        containerColor = TpotBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = dashboard.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TpotTextPrimary
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.labelSmall,
                            color = TpotTextTertiary,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurueck",
                            tint = TpotTextPrimary
                        )
                    }
                },
                actions = {
                    // Refresh
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Neu laden",
                            tint = TpotTextSecondary
                        )
                    }
                    // Forward
                    IconButton(onClick = { webView?.goForward() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Vorwaerts",
                            tint = TpotTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TpotSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            userAgentString = settings.userAgentString + " TPotDashboard/1.0"
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                loadError = null
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
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
                        }

                        webChromeClient = object : WebChromeClient() {}

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
                modifier = Modifier.fillMaxSize()
            )

            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = TpotGreen,
                    trackColor = TpotSurface
                )
            }

            // Error state
            if (loadError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TpotBackground.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = TpotRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Verbindungsfehler",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TpotRed
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
        }
    }
}
