package com.neon.peggame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neon.peggame.ui.screens.GameScreen
import com.neon.peggame.ui.screens.HomeScreen
import com.neon.peggame.ui.screens.HowToPlayScreen
import com.neon.peggame.ui.screens.ModeSelectScreen
import com.neon.peggame.ui.screens.ResultScreen
import com.neon.peggame.ui.theme.PegzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PegzApp() }
    }
}

private object Routes {
    const val HOME = "home"
    const val MODE = "mode"
    const val HOWTO = "howto"
    const val GAME = "game"
    const val RESULT = "result"
}

@Composable
fun PegzApp() {
    PegzTheme {
        val navController = rememberNavController()
        val gameSession = remember { com.neon.peggame.viewmodel.GameSession() }

        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStart = { navController.navigate(Routes.MODE) },
                    onHowToPlay = { navController.navigate(Routes.HOWTO) }
                )
            }
            composable(Routes.HOWTO) {
                HowToPlayScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MODE) {
                ModeSelectScreen(
                    onBack = { navController.popBackStack() },
                    onStartClassic = {
                        gameSession.startClassic()
                        navController.navigate("${Routes.GAME}/classic")
                    },
                    onStartTimed = {
                        gameSession.startTimed(seconds = 65)
                        navController.navigate("${Routes.GAME}/timed")
                    }
                )
            }
            composable(
                route = "${Routes.GAME}/{mode}",
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "classic"
                GameScreen(
                    session = gameSession,
                    mode = mode,
                    onExitToMenu = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    },
                    onGameOver = {
                        navController.navigate(Routes.RESULT)
                    }
                )
            }
            composable(Routes.RESULT) {
                ResultScreen(
                    session = gameSession,
                    onReplay = {
                        // replay with same mode
                        val mode = gameSession.mode.name.lowercase()
                        gameSession.restart()
                        navController.navigate("${Routes.GAME}/$mode") {
                            popUpTo(Routes.RESULT) { inclusive = true }
                        }
                    },
                    onMenu = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }
        }
    }
}
