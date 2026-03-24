package de.tpot.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import de.tpot.dashboard.data.ConnectionStore
import de.tpot.dashboard.ui.navigation.TpotNavHost
import de.tpot.dashboard.ui.theme.TpotBackground
import de.tpot.dashboard.ui.theme.TpotDashboardTheme

class MainActivity : ComponentActivity() {

    private lateinit var connectionStore: ConnectionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        connectionStore = ConnectionStore(applicationContext)

        setContent {
            TpotDashboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TpotBackground
                ) {
                    val navController = rememberNavController()
                    TpotNavHost(
                        navController = navController,
                        connectionStore = connectionStore
                    )
                }
            }
        }
    }
}
