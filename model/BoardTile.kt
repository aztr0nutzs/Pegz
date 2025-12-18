package com.neon.peggame.model

enum class TileType {
    NORMAL,
    CONTAMINATED,
    SLICK
}

data class BoardTile(
    val row: Int,
    val col: Int,
    var hasPeg: Boolean,
    var type: TileType = TileType.NORMAL
)
