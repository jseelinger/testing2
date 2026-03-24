package de.tpot.dashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.tpot.dashboard.data.ConnectionStore
import de.tpot.dashboard.ui.screens.DashboardViewScreen
import de.tpot.dashboard.ui.screens.HomeScreen
import de.tpot.dashboard.ui.screens.SettingsScreen

@Composable
fun TpotNavHost(
    navController: NavHostController,
    connectionStore: ConnectionStore
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                connectionStore = connectionStore,
                onDashboardClick = { dashboardId ->
                    navController.navigate(NavRoutes.dashboardView(dashboardId))
                },
                onSettingsClick = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                connectionStore = connectionStore,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.DASHBOARD_VIEW,
            arguments = listOf(navArgument("dashboardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dashboardId = backStackEntry.arguments?.getString("dashboardId") ?: return@composable
            DashboardViewScreen(
                dashboardId = dashboardId,
                connectionStore = connectionStore,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
