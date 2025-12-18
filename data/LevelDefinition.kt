package com.neon.peggame.data

import com.neon.peggame.model.Position
import com.neon.peggame.model.TileType

data class LevelDefinition(
    val id: Int,
    val name: String,
    val contaminated: List<Position>,
    val slick: List<Position>,
    val timed: Boolean
)
