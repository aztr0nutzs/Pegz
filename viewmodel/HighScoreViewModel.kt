package com.neon.peggame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neon.peggame.data.PegScoreEntity
import com.neon.peggame.data.ScoreDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HighScoreUiState(
    val classicScores: List<PegScoreEntity> = emptyList(),
    val timedScores: List<PegScoreEntity> = emptyList(),
    val endlessScores: List<PegScoreEntity> = emptyList(),
    val vsAIScores: List<PegScoreEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HighScoreViewModel @Inject constructor(
    scoreDao: ScoreDao
) : ViewModel() {

    private val classicFlow = scoreDao.getScoresByMode(GameMode.CLASSIC)
    private val timedFlow = scoreDao.getScoresByMode(GameMode.TIMED)
    private val endlessFlow = scoreDao.getScoresByMode(GameMode.ENDLESS)
    private val vsAIFlow = scoreDao.getScoresByMode(GameMode.VERSUS_AI)

    /**
     * Combines all leaderboard flows into a single StateFlow for the UI.
     */
    val uiState: StateFlow<HighScoreUiState> = combine(
        classicFlow, timedFlow, endlessFlow, vsAIFlow
    ) { classic, timed, endless, vsAi ->
        HighScoreUiState(
            classicScores = classic,
            timedScores = timed,
            endlessScores = endless,
            vsAIScores = vsAi,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Start collecting when UI is active
        initialValue = HighScoreUiState()
    )
}
