package de.tpot.dashboard.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DASHBOARD_VIEW = "dashboard/{dashboardId}"

    fun dashboardView(dashboardId: String) = "dashboard/$dashboardId"
}
