package com.neon.peggame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.neon.peggame.viewmodel.GameMode

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: PegScoreEntity)

    /**
     * Retrieves scores for a specific mode.
     * Order: Score DESC (for timed), then PegsLeft ASC, then Moves ASC.
     */
    @Query("""
        SELECT * FROM high_scores 
        WHERE mode = :mode 
        ORDER BY 
            CASE WHEN mode = 'TIMED' THEN score ELSE 0 END DESC, 
            pegsLeft ASC, 
            moves ASC, 
            timestamp DESC 
        LIMIT 50
    """)
    fun getScoresByMode(mode: GameMode): Flow<List<PegScoreEntity>>
}
