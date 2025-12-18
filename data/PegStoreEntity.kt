package com.neon.peggame.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.neon.peggame.viewmodel.GameMode

/**
 * Represents a single score recorded by the player.
 * The primary keys for sorting are handled in the DAO:
 * - Timed Mode: Sort by Score DESC.
 * - Classic/Endless/VS AI: Sort by PegsLeft ASC (fewer is better), then Moves ASC (efficiency).
 */
@Entity(tableName = "high_scores")
data class PegScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mode: GameMode, // Stored as a String via TypeConverter
    val score: Int = 0, // Used for TIMED mode
    val moves: Int,
    val pegsLeft: Int,
    val timestamp: Long = System.currentTimeMillis()
)
