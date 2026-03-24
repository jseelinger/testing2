package de.tpot.dashboard.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * T-Pot server connection configuration.
 */
data class TpotConnection(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val acceptSelfSigned: Boolean = true
)

/**
 * Represents a T-Pot dashboard/service that can be viewed.
 */
data class TpotDashboard(
    val id: String,
    val name: String,
    val description: String,
    val path: String,
    val port: Int,
    val icon: ImageVector,
    val category: DashboardCategory
)

enum class DashboardCategory(val label: String) {
    MONITORING("Monitoring"),
    ANALYSIS("Analyse"),
    TOOLS("Tools"),
    MANAGEMENT("Verwaltung")
}

/**
 * All available T-Pot dashboards with their default paths and ports.
 */
object TpotDashboards {

    fun getAll(): List<TpotDashboard> = listOf(
        // Monitoring
        TpotDashboard(
            id = "attack_map",
            name = "Attack Map",
            description = "Echtzeit-Weltkarte der Angriffe auf deine Honeypots",
            path = "/map/",
            port = 64297,
            icon = Icons.Default.Public,
            category = DashboardCategory.MONITORING
        ),
        TpotDashboard(
            id = "kibana",
            name = "Kibana",
            description = "Haupt-Dashboard mit allen Honeypot-Daten und Visualisierungen",
            path = "/kibana/",
            port = 64297,
            icon = Icons.Default.Dashboard,
            category = DashboardCategory.MONITORING
        ),
        TpotDashboard(
            id = "cockpit",
            name = "Cockpit",
            description = "System-Monitoring: CPU, RAM, Netzwerk, Container-Status",
            path = "/",
            port = 64294,
            icon = Icons.Default.Speed,
            category = DashboardCategory.MONITORING
        ),

        // Analysis
        TpotDashboard(
            id = "elasticvue",
            name = "Elasticvue",
            description = "Elasticsearch-Daten durchsuchen und verwalten",
            path = "/elasticvue/",
            port = 64297,
            icon = Icons.Default.Storage,
            category = DashboardCategory.ANALYSIS
        ),
        TpotDashboard(
            id = "spiderfoot",
            name = "SpiderFoot",
            description = "OSINT-Reconnaissance und Threat Intelligence",
            path = "/spiderfoot/",
            port = 64297,
            icon = Icons.Default.BugReport,
            category = DashboardCategory.ANALYSIS
        ),

        // Tools
        TpotDashboard(
            id = "cyberchef",
            name = "CyberChef",
            description = "Daten-Analyse, Encoding, Decoding und Transformation",
            path = "/cyberchef/",
            port = 64297,
            icon = Icons.Default.Code,
            category = DashboardCategory.TOOLS
        ),
    )

    fun getByCategory(): Map<DashboardCategory, List<TpotDashboard>> =
        getAll().groupBy { it.category }

    fun buildUrl(connection: TpotConnection, dashboard: TpotDashboard): String {
        val baseUrl = connection.serverUrl.trimEnd('/')
        return if (dashboard.port == 64297) {
            "$baseUrl:${dashboard.port}${dashboard.path}"
        } else {
            "$baseUrl:${dashboard.port}${dashboard.path}"
        }
    }
}
