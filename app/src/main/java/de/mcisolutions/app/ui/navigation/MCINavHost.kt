package de.mcisolutions.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.mcisolutions.app.ui.screens.*

@Composable
fun MCINavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToBmi = { navController.navigate(NavRoutes.BMI) },
                onNavigateToTdee = { navController.navigate(NavRoutes.TDEE) },
                onNavigateToMacros = { navController.navigate(NavRoutes.MACROS) },
                onNavigateToTraining = { navController.navigate(NavRoutes.TRAINING) },
                onNavigateToProgress = { navController.navigate(NavRoutes.PROGRESS) },
                onNavigateToSources = { navController.navigate(NavRoutes.SOURCES) }
            )
        }

        composable(NavRoutes.BMI) {
            BmiScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.TDEE) {
            TdeeScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.MACROS) {
            MacroScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.TRAINING) {
            TrainingScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.PROGRESS) {
            ProgressScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.SOURCES) {
            SourcesScreen(onBack = { navController.popBackStack() })
        }
    }
}
