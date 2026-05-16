package com.kyleyarwood.goalbingo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SquareDao {
    @Query("SELECT * FROM squares WHERE year = :year ORDER BY position ASC")
    fun observeSquares(year: Int): Flow<List<SquareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(square: SquareEntity)
}
