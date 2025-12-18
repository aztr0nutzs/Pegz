// ai/MinimaxAI.kt

package com.neon.peggame.ai

import com.neon.peggame.model.Move
import com.neon.peggame.model.Position
import com.neon.peggame.model.PEG
import com.neon.peggame.model.EMPTY
import com.neon.peggame.model.INVALID
import com.neon.peggame.model.BOARD_SIZE
import com.neon.peggame.viewmodel.AiDifficulty
import kotlin.math.max
import kotlin.math.min

class MinimaxAI(private val maxDepth: Int) {

    // Internal class representing the board state for the AI search tree
    data class AiBoard(val board: Array<IntArray>) {
        fun copy(): AiBoard = AiBoard(board.map { it.clone() }.toTypedArray())
    }

    fun findBestMove(currentBoard: Array<IntArray>, difficulty: AiDifficulty): Move? {
        val depth = when(difficulty) {
            AiDifficulty.EASY -> maxDepth / 3
            AiDifficulty.MEDIUM -> maxDepth * 2 / 3
            AiDifficulty.HARD -> maxDepth
        }

        val availableMoves = getAllPossibleMoves(AiBoard(currentBoard))
        if (availableMoves.isEmpty()) return null

        var bestMove: Move? = null
        var bestScore = Int.MIN_VALUE

        // Iterate through all possible first moves
        for (move in availableMoves) {
            val nextBoard = applyMove(AiBoard(currentBoard).copy(), move)
            // Minimax assumes the opponent (player) will try to minimize the AI's score (maximize player's score)
            val score = minimax(nextBoard, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, isMaximizingPlayer = false)

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    /**
     * Minimax algorithm with Alpha-Beta pruning.
     * Since this is a single-player game turned into a competitive one, the AI (maximizer)
     * aims to reduce the remaining pegs, while the "Player" (minimizer) aims to leave more pegs.
     * The utility function is based on the number of pegs remaining.
     */
    private fun minimax(
        board: AiBoard,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizingPlayer: Boolean
    ): Int {
        val moves = getAllPossibleMoves(board)
        val pegCount = board.board.sumOf { row -> row.count { it == PEG } }

        // Terminal States: Max depth reached, Win (1 peg), or No moves left
        if (depth == 0 || pegCount == 1 || moves.isEmpty()) {
            return heuristicEvaluation(pegCount)
        }

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizingPlayer) {
            var maxEval = Int.MIN_VALUE
            for (move in moves) {
                val nextBoard = applyMove(board.copy(), move)
                val eval = minimax(nextBoard, depth - 1, currentAlpha, currentBeta, isMaximizingPlayer = false)
                maxEval = max(maxEval, eval)
                currentAlpha = max(currentAlpha, maxEval)
                if (currentBeta <= currentAlpha) break // Alpha-Beta Pruning
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in moves) {
                val nextBoard = applyMove(board.copy(), move)
                val eval = minimax(nextBoard, depth - 1, currentAlpha, currentBeta, isMaximizingPlayer = true)
                minEval = min(minEval, eval)
                currentBeta = min(currentBeta, minEval)
                if (currentBeta <= currentAlpha) break // Alpha-Beta Pruning
            }
            return minEval
        }
    }

    /**
     * Heuristic: Utility is higher for fewer pegs.
     * Range: 1 (best) to 14 (worst)
     */
    private fun heuristicEvaluation(pegCount: Int): Int {
        return 16 - pegCount // Maps 1 peg to 15 (best score) and 14 pegs to 2 (worst score)
    }

    // --- Board Manipulation Logic (Simplified for AI) ---

    private val jumpDirections = listOf(
        Pair(-2, 0), Pair(2, 0), Pair(0, -2), Pair(0, 2),
        Pair(-2, -2), Pair(-2, 2), Pair(2, -2), Pair(2, 2)
    )

    private fun getAllPossibleMoves(aiBoard: AiBoard): List<Move> {
        val moves = mutableListOf<Move>()
        val board = aiBoard.board

        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (board[r][c] == PEG) {
                    val from = Position(r, c)
                    for ((dr, dc) in jumpDirections) {
                        val toR = r + dr
                        val toC = c + dc
                        val jumpedR = r + dr / 2
                        val jumpedC = c + dc / 2

                        if (isValidMovePositions(r, c, toR, toC, jumpedR, jumpedC, board)) {
                            moves.add(Move(from, Position(toR, toC), Position(jumpedR, jumpedC)))
                        }
                    }
                }
            }
        }
        return moves
    }

    private fun isValidMovePositions(r: Int, c: Int, toR: Int, toC: Int, jumpedR: Int, jumpedC: Int, board: Array<IntArray>): Boolean {
        // Bounds check
        if (toR !in 0 until BOARD_SIZE || toC !in 0 until BOARD_SIZE) return false
        if (jumpedR !in 0 until BOARD_SIZE || jumpedC !in 0 until BOARD_SIZE) return false
        
        // Board layout check
        if (board[r][c] == INVALID || board[toR][toC] == INVALID || board[jumpedR][jumpedC] == INVALID) return false

        // Peg state check: From=PEG, To=EMPTY, Jumped=PEG
        return board[r][c] == PEG && board[toR][toC] == EMPTY && board[jumpedR][jumpedC] == PEG
    }

    private fun applyMove(aiBoard: AiBoard, move: Move): AiBoard {
        val board = aiBoard.board
        board[move.to.row][move.to.col] = PEG
        board[move.from.row][move.from.col] = EMPTY
        board[move.jumped.row][move.jumped.col] = EMPTY
        return aiBoard
    }
}
