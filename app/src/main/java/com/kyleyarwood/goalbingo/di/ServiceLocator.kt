package com.kyleyarwood.goalbingo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.SettingsRepository
import com.kyleyarwood.goalbingo.data.local.BingoDatabase
import com.kyleyarwood.goalbingo.data.local.DataStoreSettingsRepository
import com.kyleyarwood.goalbingo.data.local.LocalBingoRepository

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Minimal manual DI. Swap [LocalBingoRepository] for another [BingoRepository]
 * implementation (e.g. a future cloud-backed one) here and everything upstream still works.
 */
class ServiceLocator(context: Context) {

    private val appContext = context.applicationContext

    private val database: BingoDatabase = Room.databaseBuilder(
        appContext,
        BingoDatabase::class.java,
        BingoDatabase.NAME,
    )
        .fallbackToDestructiveMigration()
        .build()

    val repository: BingoRepository = LocalBingoRepository(database.squareDao())

    val settings: SettingsRepository = DataStoreSettingsRepository(appContext.settingsDataStore)
}
