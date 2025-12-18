// model/BoardState.kt (MODIFIED - MECHANICS + LEVEL TILES)

package com.neon.peggame.model

import androidx.compose.runtime.Immutable
import kotlin.random.Random

/**
 * Represents the state of the Cracker Barrel Peg Game board.
 * The board is a 5x5 grid internally, but only 15 specific cells are valid.
 *
 * Board values:
 *  - PEG (1) : peg present
 *  - EMPTY (0) : empty valid hole
 *  - INVALID (-1) : not part of the triangle
 *
 * Tile overlay values (tileMask):
 *  - TILE_NORMAL (0)
 *  - TILE_CONTAMINATED (1) : cannot land
 *  - TILE_SLICK (2) : triggers post-jump slide
 */
const val PEG = 1
const val EMPTY = 0
const val INVALID = -1
const val BOARD_SIZE = 5
const val WIN_CONDITION_PEGS = 1

const val TILE_NORMAL = 0
const val TILE_CONTAMINATED = 1
const val TILE_SLICK = 2

/**
 * UI-friendly enum for tile overlay rendering.
 * We store these as ints in [tileMask] to avoid allocating objects during gameplay.
 */
enum class TileType(val code: Int) {
    NORMAL(TILE_NORMAL),
    CONTAMINATED(TILE_CONTAMINATED),
    SLICK(TILE_SLICK)
}

@Immutable
data class Position(val row: Int, val col: Int)

@Immutable
data class Move(
    val from: Position,
    /** Final landing position (after slick slide if any). */
    val to: Position,
    /** The jumped-over peg position (removed). */
    val jumped: Position,
    /** The first landing position (the normal jump target). */
    val jumpLanding: Position = to,
    /** If a slick slide occurred, this is the slide destination. */
    val slideTo: Position? = null
)

class BoardState(initialEmptyPos: Position = Position(0, 2)) {

    var board: Array<IntArray> = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) { INVALID } }
        private set

    /** Overlay: tile types for valid cells only (invalid cells contain INVALID). */
    private var tileMask: Array<IntArray> = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) { INVALID } }

    /** Optional TTL (in moves) for contaminated cells; 0 means not contaminated. */
    private var contaminationTtl: Array<IntArray> = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) }

    private val moveHistory = mutableListOf<Move>()
    var selectedPosition: Position? = null

    // Valid columns for each row to represent the triangle shape
    private val validCols = listOf(
        listOf(2),
        listOf(1, 3),
        listOf(0, 2, 4),
        listOf(0, 1, 3, 4),
        listOf(0, 1, 2, 3, 4)
    )

    init {
        initBoard(initialEmptyPos)
        clearTiles()
    }

    private fun initBoard(emptyPos: Position) {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (c in validCols.getOrElse(r) { emptyList() }) {
                    board[r][c] = if (r == emptyPos.row && c == emptyPos.col) EMPTY else PEG
                } else {
                    board[r][c] = INVALID
                }
            }
        }
        board = board.map { it.clone() }.toTypedArray()
    }

    private fun clearTiles() {
        tileMask = Array(BOARD_SIZE) { r ->
            IntArray(BOARD_SIZE) { c ->
                if (board[r][c] == INVALID) INVALID else TILE_NORMAL
            }
        }
        contaminationTtl = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) }
    }

    /**
     * Applies per-level modifiers.
     * - contaminated: cannot land
     * - slick: triggers a 1-step slide in the move direction after the jump
     */
    fun applyLevelModifiers(
        contaminated: List<Position>,
        slick: List<Position>,
        contaminationTtlMoves: Int = 6
    ) {
        clearTiles()
        contaminated.forEach { p ->
            if (isValidPosition(p)) {
                tileMask[p.row][p.col] = TILE_CONTAMINATED
                contaminationTtl[p.row][p.col] = contaminationTtlMoves.coerceAtLeast(1)
            }
        }
        slick.forEach { p ->
            if (isValidPosition(p) && tileMask[p.row][p.col] != TILE_CONTAMINATED) {
                tileMask[p.row][p.col] = TILE_SLICK
            }
        }
    }

    /** Backwards-compatible alias used by existing ViewModel code. */
    fun applyLevel(contaminated: List<Position>, slick: List<Position>, contaminationTtlMoves: Int = 6) {
        applyLevelModifiers(contaminated, slick, contaminationTtlMoves)
    }

    fun getTileMaskCopy(): Array<IntArray> = tileMask.map { it.clone() }.toTypedArray()

    /** Backwards-compatible alias used by existing UI/ViewModel code. */
    fun getTileMask(): Array<IntArray> = getTileMaskCopy()

    fun reset(emptyPos: Position) {
        moveHistory.clear()
        selectedPosition = null
        initBoard(emptyPos)
        clearTiles()
    }

    fun getPegCount(): Int = board.sumOf { row -> row.count { it == PEG } }

    fun getMoveCount(): Int = moveHistory.size

    fun copyBoard(): Array<IntArray> = board.map { it.clone() }.toTypedArray()

    fun isWin(): Boolean = getPegCount() == WIN_CONDITION_PEGS

    fun hasValidMoves(): Boolean {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (board[r][c] == PEG) {
                    if (getValidJumps(Position(r, c)).isNotEmpty()) return true
                }
            }
        }
        return false
    }

    /**
     * Valid jumps are two steps in a straight line, over a PEG, into an EMPTY hole.
     * We additionally block LANDING on contaminated tiles.
     */
    private val jumpDirections = listOf(
        Pair(-2, 0),
        Pair(2, 0),
        Pair(0, -2),
        Pair(0, 2),
        Pair(-2, -2),
        Pair(-2, 2),
        Pair(2, -2),
        Pair(2, 2)
    )

    fun getValidJumps(from: Position): List<Position> {
        if (!isValidPosition(from) || board[from.row][from.col] != PEG) return emptyList()

        val jumps = mutableListOf<Position>()
        for ((dr, dc) in jumpDirections) {
            val to = Position(from.row + dr, from.col + dc)
            val jumped = Position(from.row + dr / 2, from.col + dc / 2)

            if (!isValidPosition(to) || !isValidPosition(jumped)) continue

            // Must land on empty + non-contaminated
            if (board[to.row][to.col] == EMPTY &&
                tileMask[to.row][to.col] != TILE_CONTAMINATED &&
                board[jumped.row][jumped.col] == PEG
            ) {
                jumps.add(to)
            }
        }
        return jumps
    }

    fun isValidMove(from: Position, to: Position): Boolean = getValidJumps(from).contains(to)

    /**
     * Executes a move. If the landing tile is SLICK, attempts a 1-step slide
     * in the jump direction into a valid, empty, non-contaminated cell.
     *
     * @return the resulting Move (including slide info) if applied; null otherwise.
     */
    fun makeMove(from: Position, to: Position): Move? {
        if (!isValidMove(from, to)) return null

        val jumped = Position((from.row + to.row) / 2, (from.col + to.col) / 2)

        // Apply normal jump
        board[from.row][from.col] = EMPTY
        board[jumped.row][jumped.col] = EMPTY
        board[to.row][to.col] = PEG

        val jumpLanding = to
        var finalTo = to
        var slideTo: Position? = null

        // Slick slide check
        if (tileMask[jumpLanding.row][jumpLanding.col] == TILE_SLICK) {
            // Jump vector is 2 cells; slide one more cell in same direction.
            val stepR = (jumpLanding.row - from.row) / 2
            val stepC = (jumpLanding.col - from.col) / 2
            val candidate = Position(jumpLanding.row + stepR, jumpLanding.col + stepC)

            if (isValidPosition(candidate) &&
                board[candidate.row][candidate.col] == EMPTY &&
                tileMask[candidate.row][candidate.col] != TILE_CONTAMINATED
            ) {
                board[jumpLanding.row][jumpLanding.col] = EMPTY
                board[candidate.row][candidate.col] = PEG
                finalTo = candidate
                slideTo = candidate
            }
        }

        val appliedMove = Move(
            from = from,
            to = finalTo,
            jumped = jumped,
            jumpLanding = jumpLanding,
            slideTo = slideTo
        )

        moveHistory.add(appliedMove)

        // Post-move tile updates
        tickContamination()

        return appliedMove
    }

    fun undo(): Boolean {
        if (moveHistory.isEmpty()) return false

        val last = moveHistory.removeLast()

        // Clear the final destination
        board[last.to.row][last.to.col] = EMPTY

        // Restore jumped peg and original peg
        board[last.jumped.row][last.jumped.col] = PEG
        board[last.from.row][last.from.col] = PEG

        selectedPosition = null
        return true
    }

    private fun tickContamination() {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (tileMask[r][c] == TILE_CONTAMINATED) {
                    // 10% chance to self-repair each move
                    if (Random.nextFloat() < 0.10f) {
                        tileMask[r][c] = TILE_NORMAL
                        contaminationTtl[r][c] = 0
                        continue
                    }

                    if (contaminationTtl[r][c] > 0) {
                        contaminationTtl[r][c] -= 1
                        if (contaminationTtl[r][c] <= 0) {
                            tileMask[r][c] = TILE_NORMAL
                            contaminationTtl[r][c] = 0
                        }
                    }
                }
            }
        }
    }

    private fun isValidPosition(pos: Position): Boolean {
        return pos.row in 0 until BOARD_SIZE &&
            pos.col in 0 until BOARD_SIZE &&
            board[pos.row][pos.col] != INVALID
    }
}
