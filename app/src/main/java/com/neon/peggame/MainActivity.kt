// MainActivity.kt (UPDATED)

package com.neon.peggame

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neon.peggame.model.GameMode
import com.neon.peggame.model.Position
import com.neon.peggame.ui.CyberpunkTheme
import com.neon.peggame.ui.GameScreen
import com.neon.peggame.ui.HighScoreScreen
import com.neon.peggame.ui.ModeSelector
import com.neon.peggame.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PegNeonApp()
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Menu : Screen("menu")
    object HighScores : Screen("high_scores")
    object Game : Screen("game/{mode}/{emptyPos}") { 
        fun createRoute(mode: GameMode, emptyPosIndex: Int) = "game/${mode.name}/$emptyPosIndex"
    }
    object Settings : Screen("settings")
}

@Composable
fun PegNeonApp() {
    CyberpunkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CyberpunkTheme.colors.background
        ) {
            val navController = rememberNavController()
            val activity = LocalContext.current as Activity // Get Activity for billing/ads

            var isPremium by remember { mutableStateOf(false) }

            NavHost(
                navController = navController,
                startDestination = Screen.Menu.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Menu.route) { backStackEntry ->
                    val viewModel: GameViewModel = hiltViewModel(backStackEntry)
                    ModeSelector(
                        onStartGame = { mode, emptyPosIndex ->
                            val pos = when(emptyPosIndex) {
                                0 -> Position(0, 2)
                                1 -> Position(2, 2)
                                2 -> Position(4, 2)
                                else -> Position(2, 2)
                            }
                            
                            navController.navigate(Screen.Game.createRoute(mode, emptyPosIndex)) {
                                launchSingleTop = true
                            }
                            viewModel.newGame(mode, pos)
                        },
                        onViewScores = { navController.navigate(Screen.HighScores.route) },
                        // Premium flow is stubbed for now
                        onPurchase = { isPremium = true },
                        isPremium = isPremium
                    )
                }
                
                composable(Screen.HighScores.route) {
                    HighScoreScreen(
                        viewModel = hiltViewModel(),
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Game.route,
                    arguments = listOf(
                        navArgument("mode") { type = NavType.StringType },
                        navArgument("emptyPos") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val viewModel: GameViewModel = hiltViewModel(backStackEntry) 
                    GameScreen(
                        viewModel = viewModel,
                        // Pass the activity to the GameScreen to handle ad showing
                        activity = activity, 
                        onBackToMenu = { navController.popBackStack(Screen.Menu.route, false) }
                    )
                }
            }
        }
    }
}
