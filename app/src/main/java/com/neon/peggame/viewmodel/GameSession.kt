package com.neon.peggame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neon.peggame.game.PegBoard
import com.neon.peggame.game.PegMove
import com.neon.peggame.game.PegPiece

enum class GameMode { CLASSIC, TIMED }

class GameSession {

    var mode: GameMode by mutableStateOf(GameMode.CLASSIC)
        private set

    var board: PegBoard by mutableStateOf(PegBoard.newDefault())
        private set

    var score: Int by mutableStateOf(0)
        private set

    var secondsLeft: Int by mutableStateOf(0)
        private set

    var isGameOver: Boolean by mutableStateOf(false)
        private set

    // selection UI state
    var selectedIndex: Int? by mutableStateOf(null)
        private set

    fun startClassic() {
        mode = GameMode.CLASSIC
        restart()
    }

    fun startTimed(seconds: Int) {
        mode = GameMode.TIMED
        secondsLeft = seconds
        restart()
    }

    fun restart() {
        board = PegBoard.newDefault()
        score = 0
        isGameOver = false
        selectedIndex = null
        if (mode == GameMode.TIMED && secondsLeft <= 0) {
            secondsLeft = 65
        }
    }

    fun tickOneSecond() {
        if (mode != GameMode.TIMED) return
        if (isGameOver) return
        secondsLeft = (secondsLeft - 1).coerceAtLeast(0)
        if (secondsLeft == 0) {
            isGameOver = true
            selectedIndex = null
        }
    }

    fun tapHole(index: Int) {
        if (isGameOver) return

        val cell = board.cells[index]
        val sel = selectedIndex

        if (sel == null) {
            if (cell.piece != null) selectedIndex = index
            return
        }

        if (sel == index) {
            selectedIndex = null
            return
        }

        val from = board.cells[sel]
        val to = cell

        // If tapping another occupied hole, just switch selection
        if (to.piece != null) {
            selectedIndex = index
            return
        }

        val move = board.findMove(fromIndex = sel, toIndex = index)
        if (move != null) {
            applyMove(move)
            selectedIndex = null
            if (!board.hasAnyMoves()) {
                isGameOver = true
            }
        } else {
            // invalid target: deselect to avoid frustration loops
            selectedIndex = null
        }
    }

    private fun applyMove(move: PegMove) {
        val movingPiece: PegPiece = board.cells[move.from].piece ?: return
        board = board.applyMove(move)

        // scoring: base + combo-ish for TIMED
        val base = 10
        val bonus = if (mode == GameMode.TIMED) 5 else 0
        score += (base + bonus)

        // Tiny reward: in timed mode, add a second for successful moves (keeps flow spicy)
        if (mode == GameMode.TIMED) {
            secondsLeft = (secondsLeft + 1).coerceAtMost(99)
        }
    }
}
