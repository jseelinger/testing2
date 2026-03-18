package de.mcisolutions.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import de.mcisolutions.app.ui.navigation.MCINavHost
import de.mcisolutions.app.ui.theme.MCIFitnessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MCIFitnessTheme {
                val navController = rememberNavController()
                MCINavHost(navController = navController)
            }
        }
    }
}
