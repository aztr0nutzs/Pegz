package com.neon.peggame.game

data class PegPiece(val type: PegPieceType)

enum class PegPieceType {
    PINK,
    BLUE,
    GREEN,
    SKULL,
    TEAL
}

data class PegCell(
    val row: Int,
    val col: Int,
    val piece: PegPiece?
)

data class PegMove(
    val from: Int,
    val over: Int,
    val to: Int
)

/**
 * 5-row triangular peg solitaire board (15 holes).
 *
 * Coordinate system: (row r, col c) where 0 <= r <= 4 and 0 <= c <= r.
 * Neighbor deltas in (dr, dc):
 *  - left/right: (0, -1) / (0, +1)
 *  - up-left/up-right: (-1, -1) / (-1, 0)
 *  - down-left/down-right: (+1, 0) / (+1, +1)
 */
class PegBoard private constructor(
    val cells: List<PegCell>
) {

    private val indexByCoord: Map<Pair<Int, Int>, Int> = buildMap {
        cells.forEachIndexed { idx, cell -> put(cell.row to cell.col, idx) }
    }

    fun applyMove(move: PegMove): PegBoard {
        val newCells = cells.toMutableList()
        val moving = newCells[move.from].piece
        newCells[move.from] = newCells[move.from].copy(piece = null)
        newCells[move.over] = newCells[move.over].copy(piece = null)
        newCells[move.to] = newCells[move.to].copy(piece = moving)
        return PegBoard(newCells)
    }

    fun findMove(fromIndex: Int, toIndex: Int): PegMove? {
        val from = cells.getOrNull(fromIndex) ?: return null
        val to = cells.getOrNull(toIndex) ?: return null
        if (from.piece == null) return null
        if (to.piece != null) return null

        val dr = to.row - from.row
        val dc = to.col - from.col

        // Jump must be exactly 2 steps in one of the 6 directions
        val directions = listOf(
            0 to -1,
            0 to 1,
            -1 to -1,
            -1 to 0,
            1 to 0,
            1 to 1
        )

        val match = directions.firstOrNull { (r, c) -> dr == 2 * r && dc == 2 * c } ?: return null

        val overRow = from.row + match.first
        val overCol = from.col + match.second
        val overIdx = indexByCoord[overRow to overCol] ?: return null
        val over = cells[overIdx]
        if (over.piece == null) return null

        return PegMove(from = fromIndex, over = overIdx, to = toIndex)
    }

    fun hasAnyMoves(): Boolean {
        // Any occupied cell with a legal jump to empty.
        cells.forEachIndexed { idx, cell ->
            if (cell.piece != null) {
                if (legalMovesFrom(idx).isNotEmpty()) return true
            }
        }
        return false
    }

    fun legalMovesFrom(fromIndex: Int): List<PegMove> {
        val from = cells.getOrNull(fromIndex) ?: return emptyList()
        if (from.piece == null) return emptyList()

        val directions = listOf(
            0 to -1,
            0 to 1,
            -1 to -1,
            -1 to 0,
            1 to 0,
            1 to 1
        )

        val out = ArrayList<PegMove>(6)
        for ((dr, dc) in directions) {
            val overRow = from.row + dr
            val overCol = from.col + dc
            val toRow = from.row + 2 * dr
            val toCol = from.col + 2 * dc

            val overIdx = indexByCoord[overRow to overCol] ?: continue
            val toIdx = indexByCoord[toRow to toCol] ?: continue

            val over = cells[overIdx]
            val to = cells[toIdx]
            if (over.piece != null && to.piece == null) {
                out.add(PegMove(from = fromIndex, over = overIdx, to = toIdx))
            }
        }
        return out
    }

    companion object {
        fun newDefault(): PegBoard {
            val types = PegPieceType.entries
            var k = 0
            val cells = buildList {
                for (r in 0..4) {
                    for (c in 0..r) {
                        // default: all filled, one empty near center for playability
                        val isEmpty = (r == 2 && c == 1)
                        val piece = if (isEmpty) null else PegPiece(types[k++ % types.size])
                        add(PegCell(row = r, col = c, piece = piece))
                    }
                }
            }
            return PegBoard(cells)
        }
    }
}
