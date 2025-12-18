// viewmodel/GameViewModel.kt (COMPLETE - LEVEL TILE MAPPING + THEMED EMPTY START)

package com.neon.peggame.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neon.peggame.ai.MinimaxAI
import com.neon.peggame.data.AdManager
import com.neon.peggame.data.PegScoreEntity
import com.neon.peggame.data.PlayGamesManager
import com.neon.peggame.data.ScoreDao
import com.neon.peggame.data.SettingsManager
import com.neon.peggame.model.BoardState
import com.neon.peggame.model.Move
import com.neon.peggame.model.Position
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

enum class GameMode { CLASSIC, TIMED, ENDLESS, VERSUS_AI }
enum class AiDifficulty { EASY, MEDIUM, HARD }

data class GameUiState(
    val board: Array<IntArray>,
    val tileMask: Array<IntArray>,
    val pegCount: Int,
    val movesMade: Int,
    val selectedPos: Position?,
    val validJumps: List<Position>,
    val isGameOver: Boolean,
    val isWin: Boolean,
    val geniusAchieved: Boolean,
    val message: String,
    val mode: GameMode,
    val levelId: Int,
    val score: Int,
    val timer: Int,
    val comboCounter: Int,
    val aiThinking: Boolean,
    val lastMoveTrigger: Long
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val scoreDao: ScoreDao,
    private val adManager: AdManager,
    private val settingsManager: SettingsManager,
    private val playGamesManager: PlayGamesManager
) : ViewModel() {

    private var boardState = BoardState(initialEmptyPos = Position(2, 2))
    private val minimaxAI = MinimaxAI(maxDepth = 6)
    private var timerJob: Job? = null

    private var lastMoveTimestamp: Long = 0
    private val comboTimeoutMs = 2000L

    private var lastMove: Move? = null

    val isPremiumUser = settingsManager.isPremiumUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPlayGamesSignedIn = playGamesManager.isSignedIn.asStateFlow()

    var uiState by mutableStateOf(getInitialUiState())
        private set

    private data class LevelPreset(
        val id: Int,
        val name: String,
        val contaminated: List<Position>,
        val slick: List<Position>,
        val contaminationTtlMoves: Int,
        val timed: Boolean,
        val themedEmptyPos: Position
    )

    /**
     * Hard-mapped tile locations on the 15 valid holes:
     * Row 0: (0,2)
     * Row 1: (1,1) (1,3)
     * Row 2: (2,0) (2,2) (2,4)
     * Row 3: (3,0) (3,1) (3,3) (3,4)
     * Row 4: (4,0) (4,1) (4,2) (4,3) (4,4)
     */
    private val levels = mapOf(
        1 to LevelPreset(
            id = 1,
            name = "NEON BIO-LAB",
            // Contamination: inner / mid holes (reads like a hazardous lab core)
            contaminated = listOf(
                Position(1, 1),
                Position(2, 2),
                Position(3, 1),
                Position(4, 2)
            ),
            // Slick: right-side lane (predictable “slide channel” for combos)
            slick = listOf(
                Position(2, 4),
                Position(3, 4),
                Position(4, 4)
            ),
            contaminationTtlMoves = 6,
            timed = false,
            themedEmptyPos = Position(0, 2) // top empty = “breach at the apex”
        ),

        2 to LevelPreset(
            id = 2,
            name = "STEAMY JUNGLE",
            // Contamination: outer edges / extremes (spores/vents vibe)
            contaminated = listOf(
                Position(0, 2),
                Position(2, 0),
                Position(3, 4),
                Position(4, 0)
            ),
            // Slick: lower-mid pathing (mud/vines slip feel)
            slick = listOf(
                Position(3, 0),
                Position(4, 1),
                Position(4, 3)
            ),
            contaminationTtlMoves = 5,
            timed = true,
            themedEmptyPos = Position(2, 2) // center empty = “clearing”
        )
    )

    private fun getInitialUiState(mode: GameMode = GameMode.CLASSIC, levelId: Int = 1): GameUiState {
        return GameUiState(
            board = boardState.board.map { it.clone() }.toTypedArray(),
            tileMask = boardState.getTileMask(),
            pegCount = boardState.getPegCount(),
            movesMade = 0,
            selectedPos = null,
            validJumps = emptyList(),
            isGameOver = false,
            isWin = false,
            geniusAchieved = false,
            message = "Select a peg to jump.",
            mode = mode,
            levelId = levelId,
            score = 0,
            timer = if (mode == GameMode.TIMED) 60 else 0,
            comboCounter = 0,
            aiThinking = false,
            lastMoveTrigger = 0L
        )
    }

    /**
     * Starts a new game.
     *
     * Notes:
     * - levelId chooses tile mapping + themed starting empty position
     * - if the selected level is timed, the mode becomes TIMED regardless of the requested mode
     */
    fun newGame(mode: GameMode, emptyPos: Position = Position(2, 2), levelId: Int = 1) {
        timerJob?.cancel()
        lastMove = null

        val preset = levels[levelId] ?: levels.getValue(1)

        // Use themed empty position by default, unless you explicitly pass a different emptyPos.
        // (If you want "always themed", just remove the conditional.)
        val effectiveEmpty = if (emptyPos == Position(2, 2)) preset.themedEmptyPos else emptyPos

        boardState.reset(effectiveEmpty)

        // Apply hard-mapped tile overlays
        boardState.applyLevel(
            contaminated = preset.contaminated,
            slick = preset.slick,
            contaminationTtlMoves = preset.contaminationTtlMoves
        )

        val actualMode = if (preset.timed) GameMode.TIMED else mode

        uiState = getInitialUiState(actualMode, preset.id).copy(
            board = boardState.board.map { it.clone() }.toTypedArray(),
            tileMask = boardState.getTileMask(),
            timer = if (actualMode == GameMode.TIMED) 60 else 0,
            message = preset.name,
            comboCounter = 0,
            score = 0,
            aiThinking = false,
            lastMoveTrigger = 0L
        )

        if (actualMode == GameMode.TIMED) startTimer()
        updateUiState(isInitialSetup = true)

        if (!isPremiumUser.value) {
            adManager.loadAd()
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (uiState.timer > 0 && isActive) {
                delay(1.seconds)
                uiState = uiState.copy(timer = uiState.timer - 1)
            }
            if (isActive && uiState.timer == 0) {
                uiState = uiState.copy(
                    isGameOver = true,
                    message = "TIME'S UP! Final Score: ${uiState.score}"
                )
                saveScore()
            }
        }
    }

    private fun updateUiState(isInitialSetup: Boolean = false) {
        val pegCount = boardState.getPegCount()
        val isWin = boardState.isWin()
        val hasMoves = boardState.hasValidMoves()

        val timeEnded = (uiState.mode == GameMode.TIMED && uiState.timer <= 0)
        val gameOver = timeEnded || (!hasMoves && !isWin)

        // Keep “genius” logic conservative; this can be redefined later.
        val genius = isWin && pegCount == 1

        val presetName = levels[uiState.levelId]?.name ?: "PEGZ"

        val message = when {
            isWin -> "GENIUS! $presetName CLEARED!"
            gameOver -> if (timeEnded) "TIME'S UP!" else "Game Over. $pegCount Pegs Left."
            boardState.selectedPosition != null -> "Tap an empty landing hole."
            else -> "Select a peg to jump."
        }

        uiState = uiState.copy(
            board = boardState.board.map { it.clone() }.toTypedArray(),
            tileMask = boardState.getTileMask(),
            pegCount = pegCount,
            movesMade = boardState.getMoveCount(),
            selectedPos = boardState.selectedPosition,
            validJumps = boardState.selectedPosition?.let { boardState.getValidJumps(it) } ?: emptyList(),
            isGameOver = gameOver,
            isWin = isWin,
            geniusAchieved = genius,
            message = message,
            aiThinking = if (isInitialSetup) false else uiState.aiThinking
        )

        if (uiState.isGameOver || uiState.isWin) {
            saveScore()
        }
    }

    fun handleTap(pos: Position) {
        if (uiState.isGameOver || uiState.aiThinking) return

        val currentBoardValue = boardState.board[pos.row][pos.col]
        val moved = processPlayerInput(pos, currentBoardValue)

        if (moved) processGameAction()
        updateUiState()
    }

    private fun processPlayerInput(pos: Position, currentBoardValue: Int): Boolean {
        var moved = false
        val currentSelection = uiState.selectedPos

        if (currentSelection != null) {
            if (boardState.isValidMove(currentSelection, pos)) {
                val move = boardState.makeMove(currentSelection, pos)
                if (move != null) {
                    lastMove = move
                    boardState.selectedPosition = null
                    moved = true
                    uiState = uiState.copy(lastMoveTrigger = System.currentTimeMillis())
                }
            } else if (currentBoardValue == com.neon.peggame.model.PEG) {
                boardState.selectedPosition = pos
            } else {
                boardState.selectedPosition = null
            }
        } else if (currentBoardValue == com.neon.peggame.model.PEG) {
            boardState.selectedPosition = pos
        }

        return moved
    }

    private fun processGameAction() {
        if (uiState.mode == GameMode.TIMED) {
            val now = System.currentTimeMillis()
            val isCombo = (now - lastMoveTimestamp) < comboTimeoutMs

            val newCombo = if (isCombo) uiState.comboCounter + 1 else 1
            val points = 100 * newCombo

            uiState = uiState.copy(
                score = uiState.score + points,
                comboCounter = newCombo,
                message = if (isCombo) "COMBO x$newCombo! +$points" else "Jump! +$points"
            )
            lastMoveTimestamp = now
        }

        if (uiState.mode == GameMode.VERSUS_AI && !uiState.isWin && !uiState.isGameOver) {
            viewModelScope.launch { aiMove() }
        }
    }

    fun undoMove() {
        if (boardState.undo()) {
            if (uiState.mode == GameMode.TIMED) {
                uiState = uiState.copy(
                    score = (uiState.score - 100).coerceAtLeast(0),
                    comboCounter = 0
                )
            }
            lastMove = null
            updateUiState()
        }
    }

    private suspend fun aiMove() {
        if (uiState.isGameOver || uiState.isWin) return

        uiState = uiState.copy(aiThinking = true, message = "AI is thinking...")
        delay(800)

        val boardCopy = boardState.copyBoard()
        val bestMove: Move? = minimaxAI.findBestMove(boardCopy, AiDifficulty.MEDIUM)

        if (bestMove != null) {
            val applied = boardState.makeMove(bestMove.from, bestMove.to)
            if (applied != null) {
                lastMove = applied
                uiState = uiState.copy(lastMoveTrigger = System.currentTimeMillis())
            }
        }

        uiState = uiState.copy(aiThinking = false)
        updateUiState()
    }

    fun consumeLastMove(): Move? {
        val move = lastMove
        lastMove = null
        return move
    }

    private fun saveScore() {
        if (uiState.movesMade == 0) return

        viewModelScope.launch {
            val scoreEntity = PegScoreEntity(
                mode = uiState.mode,
                score = uiState.score,
                moves = uiState.movesMade,
                pegsLeft = uiState.pegCount
            )
            scoreDao.insertScore(scoreEntity)
            playGamesManager.unlockAchievement(scoreEntity.pegsLeft, scoreEntity.moves)
        }
    }

    fun onGameOver(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isPremiumUser.value) {
            adManager.showAd(activity, onAdDismissed)
        } else {
            onAdDismissed()
        }
    }

    fun signInPlayGames(activity: Activity) = playGamesManager.signIn(activity)
    fun showPlayGamesAchievements(activity: Activity) = playGamesManager.showAchievements(activity)
}
