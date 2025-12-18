// ui/HighScoreScreen.kt (NEW)

package com.neon.peggame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.peggame.data.PegScoreEntity
import com.neon.peggame.viewmodel.GameMode
import com.neon.peggame.viewmodel.HighScoreViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HighScoreScreen(
    viewModel: HighScoreViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val modes = listOf(GameMode.CLASSIC, GameMode.TIMED, GameMode.ENDLESS, GameMode.VERSUS_AI)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "LEADERBOARDS", 
                        color = CyberpunkTheme.colors.primary, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = CyberpunkTheme.colors.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberpunkTheme.colors.boardFrame.copy(alpha = 0.2f),
                    titleContentColor = CyberpunkTheme.colors.primary
                )
            )
        },
        containerColor = CyberpunkTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CyberpunkTheme.colors.boardFrame.copy(alpha = 0.5f),
                contentColor = CyberpunkTheme.colors.primary
            ) {
                modes.forEachIndexed { index, mode ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                mode.name.replace("_", " "),
                                color = if (selectedTabIndex == index) CyberpunkTheme.colors.primary else CyberpunkTheme.colors.onBackground,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = CyberpunkTheme.colors.secondary
                )
            } else {
                val currentScores = when (modes[selectedTabIndex]) {
                    GameMode.CLASSIC -> uiState.classicScores
                    GameMode.TIMED -> uiState.timedScores
                    GameMode.ENDLESS -> uiState.endlessScores
                    GameMode.VERSUS_AI -> uiState.vsAIScores
                }
                Leaderboard(
                    scores = currentScores,
                    mode = modes[selectedTabIndex]
                )
            }
        }
    }
}

// --- LEADERBOARD COMPOSABLE ---

@Composable
fun Leaderboard(
    scores: List<PegScoreEntity>,
    mode: GameMode
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (scores.isEmpty()) {
            Text(
                "No scores recorded for ${mode.name.replace("_", " ")} yet.",
                color = CyberpunkTheme.colors.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(32.dp)
            )
        } else {
            // Header
            LeaderboardRow(
                rank = "#",
                mainStat = if (mode == GameMode.TIMED) "SCORE" else "PEGS",
                secondaryStat = "MOVES",
                isHeader = true
            )
            
            Spacer(Modifier.height(8.dp))

            scores.forEachIndexed { index, score ->
                LeaderboardRow(
                    rank = (index + 1).toString(),
                    mainStat = if (mode == GameMode.TIMED) score.score.toString() else score.pegsLeft.toString(),
                    secondaryStat = score.moves.toString(),
                    timestamp = score.timestamp,
                    isHighlight = index == 0, // Highlight the #1 score
                    mode = mode
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        Spacer(Modifier.height(32.dp)) // Padding at bottom
    }
}

@Composable
fun LeaderboardRow(
    rank: String,
    mainStat: String,
    secondaryStat: String,
    timestamp: Long = 0L,
    isHeader: Boolean = false,
    isHighlight: Boolean = false,
    mode: GameMode = GameMode.CLASSIC
) {
    val rankColor = when {
        isHighlight -> CyberpunkTheme.colors.secondary // Magenta for #1
        isHeader -> CyberpunkTheme.colors.onBackground
        else -> CyberpunkTheme.colors.primary.copy(alpha = 0.8f) // Cyan
    }
    
    val timestampText = if (timestamp > 0L) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    } else ""
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isHeader) Color.Transparent else if (isHighlight) CyberpunkTheme.colors.primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.3f)
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(rank, style = TextStyle(
            fontSize = if (isHeader) 14.sp else 18.sp, 
            color = rankColor,
            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Bold
        ), modifier = Modifier.weight(0.1f))

        // Main Stat (Score or Pegs Left)
        Column(modifier = Modifier.weight(0.3f)) {
            Text(mainStat, style = TextStyle(
                fontSize = if (isHeader) 14.sp else 20.sp,
                color = if (mode == GameMode.TIMED && !isHeader) CyberpunkTheme.colors.secondary else CyberpunkTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold
            ))
            if (!isHeader) {
                Text(
                    if (mode == GameMode.TIMED) "Points" else "Pegs Left",
                    fontSize = 10.sp,
                    color = CyberpunkTheme.colors.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Secondary Stat (Moves)
        Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.End) {
            Text(secondaryStat, style = TextStyle(
                fontSize = if (isHeader) 14.sp else 16.sp,
                color = CyberpunkTheme.colors.onBackground.copy(alpha = 0.8f),
                fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Medium
            ))
            if (!isHeader) {
                Text("Moves", fontSize = 10.sp, color = CyberpunkTheme.colors.onBackground.copy(alpha = 0.5f))
            }
        }
        
        // Timestamp
        Text(
            timestampText, 
            style = TextStyle(fontSize = 12.sp, color = CyberpunkTheme.colors.onBackground.copy(alpha = 0.4f)),
            modifier = Modifier.weight(0.3f),
            textAlign = TextAlign.End
        )
    }
}
