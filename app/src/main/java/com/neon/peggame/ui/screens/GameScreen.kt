package com.neon.peggame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.peggame.R
import com.neon.peggame.ui.art.rememberPegPieceBitmaps
import com.neon.peggame.ui.components.PegBoardView
import com.neon.peggame.viewmodel.GameMode
import com.neon.peggame.viewmodel.GameSession
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    session: GameSession,
    mode: String,
    onExitToMenu: () -> Unit,
    onGameOver: () -> Unit
) {
    // ensure session mode matches route
    LaunchedEffect(mode) {
        if (mode.lowercase() == "timed") session.startTimed(seconds = session.secondsLeft.coerceAtLeast(65))
        else session.startClassic()
    }

    val background: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.pegz2)
    val pieceBitmaps = rememberPegPieceBitmaps(androidx.compose.ui.platform.LocalContext.current)

    // Timer tick
    LaunchedEffect(session.mode, session.isGameOver) {
        if (session.mode == GameMode.TIMED) {
            while (!session.isGameOver) {
                delay(1000)
                session.tickOneSecond()
                if (session.isGameOver) break
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // board + art
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HudBar(
                score = session.score,
                time = if (session.mode == GameMode.TIMED) session.secondsLeft else null,
                onMenu = {
                    onExitToMenu()
                }
            )

            PegBoardView(
                background = background,
                board = session.board,
                selectedIndex = session.selectedIndex,
                pieceBitmaps = pieceBitmaps,
                onTapHole = { idx ->
                    session.tapHole(idx)
                    if (session.isGameOver) onGameOver()
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )

            if (session.isGameOver) {
                // if game ends because no moves (classic) or timer hit 0 (timed), push result screen
                LaunchedEffect(Unit) {
                    onGameOver()
                }
            }
        }
    }
}

@Composable
private fun HudBar(
    score: Int,
    time: Int?,
    onMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SCORE: $score",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )
            if (time != null) {
                Text(
                    text = "TIME: ${time.toString().padStart(2, '0')}",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "MODE: CLASSIC",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        Button(
            onClick = onMenu,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.70f),
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(text = "MENU")
        }
    }
}
