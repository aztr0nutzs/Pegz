// ui/GameScreen.kt (COMPLETE)

package com.neon.peggame.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType // Haptics Import
import androidx.compose.ui.platform.LocalHapticFeedback // Haptics Import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.peggame.model.Position
import com.neon.peggame.viewmodel.GameViewModel
import com.neon.peggame.viewmodel.GameMode

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    activity: Activity,
    onBackToMenu: () -> Unit
) {
    val uiState = viewModel.uiState
    val haptics = LocalHapticFeedback.current

    // Collect state flows
    val isPremium by viewModel.isPremiumUser.collectAsState(initial = false)
    val isPGSsignedIn by viewModel.isPlayGamesSignedIn.collectAsState(initial = false)

    // Ensure post‑game actions only run once per session
    var isPostGameActionHandled by remember { mutableStateOf(false) }

    // --- HAPTIC FEEDBACK ---
    LaunchedEffect(uiState.selectedPos) {
        if (uiState.selectedPos != null) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    LaunchedEffect(uiState.lastMoveTrigger) {
        if (uiState.lastMoveTrigger > 0L) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // --- POST GAME ADS ---
    LaunchedEffect(uiState.isGameOver, uiState.isWin) {
        if ((uiState.isGameOver || uiState.isWin) && !isPostGameActionHandled) {
            viewModel.onGameOver(activity) {
                isPostGameActionHandled = true
            }
        }
    }

    // Main layout: a Row splits the board and scoreboard panel.  This mirrors
    // the concept art where the scoreboard/chalkboard sits to the right of
    // the triangular peg board.  Below the row sit the global control buttons.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberpunkTheme.colors.background)
            .padding(12.dp)
    ) {
        GameHeader(uiState = uiState)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Board occupies majority of horizontal space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = !uiState.aiThinking && !uiState.isGameOver && !uiState.isWin) { },
                contentAlignment = Alignment.Center
            ) {
                PegBoard(
                    uiState = uiState,
                    viewModel = viewModel,
                    onPositionTap = viewModel::handleTap
                )
            }

            // Scoreboard panel always visible.  Its content changes based on game state.
            ScoreboardPanel(
                uiState = uiState,
                isPremium = isPremium,
                onReplay = {
                    isPostGameActionHandled = false
                    viewModel.newGame(uiState.mode, Position(2, 2))
                },
                onMenu = onBackToMenu
            )
        }

        // Control buttons: Undo, Achievements, Menu
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = viewModel::undoMove,
                enabled = uiState.movesMade > 0 && !uiState.isGameOver && !uiState.aiThinking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberpunkTheme.colors.secondary.copy(alpha = 0.6f),
                    contentColor = CyberpunkTheme.colors.onPrimary
                ),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("UNDO", fontSize = 14.sp)
            }
            Button(
                onClick = {
                    if (isPGSsignedIn) {
                        viewModel.showPlayGamesAchievements(activity)
                    } else {
                        viewModel.signInPlayGames(activity)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPGSsignedIn) CyberpunkTheme.colors.primary else CyberpunkTheme.colors.boardFrame,
                    contentColor = CyberpunkTheme.colors.onPrimary
                ),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(48.dp)
            ) {
                Icon(
                    Icons.Filled.MilitaryTech,
                    contentDescription = "Achievements",
                    tint = CyberpunkTheme.colors.onPrimary
                )
            }
            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberpunkTheme.colors.primary,
                    contentColor = CyberpunkTheme.colors.onPrimary
                ),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("MENU", fontSize = 14.sp)
            }
        }
    }
}

// --- SCOREBOARD PANEL ---
@Composable
private fun ScoreboardPanel(
    uiState: com.neon.peggame.viewmodel.GameUiState,
    isPremium: Boolean,
    onReplay: () -> Unit,
    onMenu: () -> Unit
) {
    // Calculate formatted time string for timed mode
    val minutes = uiState.timer / 60
    val seconds = uiState.timer % 60
    val timeString = String.format("%d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = CyberpunkTheme.colors.boardFrame.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Combo indicator
            if (uiState.comboCounter > 1 && !uiState.isGameOver && !uiState.isWin && uiState.mode == GameMode.TIMED) {
                Text(
                    text = "COMBO x${uiState.comboCounter}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CyberpunkTheme.colors.primary
                )
            }

            // Game state header
            val header = when {
                uiState.isWin -> "VICTORY"
                uiState.isGameOver -> "GAME OVER"
                else -> uiState.mode.name.replace("_", " ")
            }
            Text(
                text = header,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CyberpunkTheme.colors.primary
            )

            // Mode or result summary
            if (uiState.isGameOver || uiState.isWin) {
                // Show summary depending on mode
                when (uiState.mode) {
                    GameMode.TIMED -> {
                        Text("SCORE: ${uiState.score}", fontSize = 14.sp, color = CyberpunkTheme.colors.onPrimary)
                        Text("TIME: $timeString", fontSize = 14.sp, color = CyberpunkTheme.colors.onPrimary)
                    }
                    else -> {
                        Text("PEGS LEFT: ${uiState.pegCount}", fontSize = 14.sp, color = CyberpunkTheme.colors.onPrimary)
                        Text("MOVES: ${uiState.movesMade}", fontSize = 14.sp, color = CyberpunkTheme.colors.onPrimary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Inform about ads if non‑premium
                if (!isPremium) {
                    Text("Ad scheduled", fontSize = 10.sp, color = CyberpunkTheme.colors.error)
                }
                // Replay and Menu buttons
                Button(
                    onClick = onReplay,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberpunkTheme.colors.primary,
                        contentColor = CyberpunkTheme.colors.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.Replay, contentDescription = "Replay", tint = CyberpunkTheme.colors.onPrimary)
                    Spacer(Modifier.width(4.dp))
                    Text("REPLAY", fontSize = 12.sp)
                }
                Button(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberpunkTheme.colors.secondary,
                        contentColor = CyberpunkTheme.colors.onPrimary
                    )
                ) {
                    Text("MENU", fontSize = 12.sp)
                }
            } else {
                // Ongoing game: show dynamic stats
                when (uiState.mode) {
                    GameMode.TIMED -> {
                        Text("SCORE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberpunkTheme.colors.secondary)
                        Text("${uiState.score}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberpunkTheme.colors.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("TIME", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberpunkTheme.colors.secondary)
                        Text(timeString, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberpunkTheme.colors.primary)
                    }
                    else -> {
                        Text("PEGS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberpunkTheme.colors.secondary)
                        Text("${uiState.pegCount}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberpunkTheme.colors.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("MOVES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberpunkTheme.colors.secondary)
                        Text("${uiState.movesMade}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberpunkTheme.colors.primary)
                    }
                }
            }
        }
    }
}


// --- SUPPORTING COMPOSABLES (STUBS for Completeness) ---

@Composable
fun GameHeader(uiState: com.neon.peggame.viewmodel.GameUiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = uiState.mode.name.replace("_", " "),
            fontSize = 20.sp,
            color = CyberpunkTheme.colors.secondary
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("PEGS: ${uiState.pegCount}", color = CyberpunkTheme.colors.onBackground)
            Text("MOVES: ${uiState.movesMade}", color = CyberpunkTheme.colors.onBackground)
            if (uiState.mode == com.neon.peggame.viewmodel.GameMode.TIMED) {
                Text("TIME: ${uiState.timer}s", color = CyberpunkTheme.colors.primary)
            }
        }
        Text(
            text = uiState.message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (uiState.comboCounter > 1) Color.Yellow else CyberpunkTheme.colors.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Legacy stub retained for reference only.  The real board is implemented in ui/PegBoard.kt.
@Composable
fun LegacyPegBoard(
    board: Array<IntArray>,
    selectedPos: Position?,
    validJumps: List<Position>,
    onPegTap: (Position) -> Unit,
    lastMove: com.neon.peggame.model.Move?,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .fillMaxWidth(0.9f)
            .background(CyberpunkTheme.colors.boardFrame.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Peg Board Rendering [Canvas]", color = Color.Gray)
    }
}

@Composable
fun GameOverOverlay(
    isWin: Boolean,
    isGenius: Boolean,
    pegsLeft: Int,
    score: Int,
    timeLeft: Int,
    mode: com.neon.peggame.viewmodel.GameMode,
    onMenu: () -> Unit,
    onReplay: () -> Unit,
    isPremium: Boolean
) {
    // Format timeLeft in seconds to mm:ss for display.  When not in timed mode this will show 00:00.
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = CyberpunkTheme.colors.boardFrame.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title based on win state
            val title = when {
                isGenius -> "GENIUS UNLOCKED!"
                isWin -> "VICTORY!"
                else -> "GAME OVER"
            }
            Text(
                title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = CyberpunkTheme.colors.secondary
            )
            Spacer(Modifier.height(12.dp))

            // Mode-specific summary information
            when (mode) {
                com.neon.peggame.viewmodel.GameMode.TIMED -> {
                    Text(
                        text = "TIMED MODE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberpunkTheme.colors.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SCORE: $score",
                        fontSize = 16.sp,
                        color = CyberpunkTheme.colors.onBackground
                    )
                    Text(
                        text = "TIME: $timeString",
                        fontSize = 16.sp,
                        color = CyberpunkTheme.colors.onBackground
                    )
                }
                else -> {
                    Text(
                        text = "PEGS LEFT: $pegsLeft",
                        fontSize = 20.sp,
                        color = CyberpunkTheme.colors.primary
                    )
                    Text(
                        text = "MOVES: $score",
                        fontSize = 16.sp,
                        color = CyberpunkTheme.colors.onBackground
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Inform the user if an ad will show on replay (non-premium players)
            if (!isPremium) {
                Text(
                    "Ad scheduled. Thank you for playing!",
                    fontSize = 12.sp,
                    color = Color.Red.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Replay button
            Button(
                onClick = onReplay,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberpunkTheme.colors.primary)
            ) {
                Icon(Icons.Filled.Replay, contentDescription = "Replay")
                Spacer(Modifier.width(8.dp))
                Text("REPLAY")
            }
            Spacer(Modifier.height(8.dp))

            // Menu button
            Button(
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberpunkTheme.colors.onBackground.copy(alpha = 0.5f)
                )
            ) {
                Text("MENU")
            }
        }
    }
}
