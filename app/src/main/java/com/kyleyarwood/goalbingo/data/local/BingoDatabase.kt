package com.kyleyarwood.goalbingo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SquareEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BingoDatabase : RoomDatabase() {
    abstract fun squareDao(): SquareDao

    companion object {
        const val NAME = "goal-bingo.db"
    }
}
