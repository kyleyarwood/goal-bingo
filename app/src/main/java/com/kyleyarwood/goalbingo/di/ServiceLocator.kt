package com.kyleyarwood.goalbingo.di

import android.content.Context
import androidx.room.Room
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.local.BingoDatabase
import com.kyleyarwood.goalbingo.data.local.LocalBingoRepository

/**
 * Minimal manual DI. Swap [LocalBingoRepository] for another [BingoRepository]
 * implementation (e.g. a future cloud-backed one) here and everything upstream still works.
 */
class ServiceLocator(context: Context) {

    private val database: BingoDatabase = Room.databaseBuilder(
        context.applicationContext,
        BingoDatabase::class.java,
        BingoDatabase.NAME,
    ).build()

    val repository: BingoRepository = LocalBingoRepository(database.squareDao())
}
