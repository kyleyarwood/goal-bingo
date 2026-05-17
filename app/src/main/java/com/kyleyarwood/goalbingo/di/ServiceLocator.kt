package com.kyleyarwood.goalbingo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.SettingsRepository
import com.kyleyarwood.goalbingo.data.local.ALL_MIGRATIONS
import com.kyleyarwood.goalbingo.data.local.BingoDatabase
import com.kyleyarwood.goalbingo.data.local.DataStoreSettingsRepository
import com.kyleyarwood.goalbingo.data.local.LocalBingoRepository
import com.kyleyarwood.goalbingo.data.local.SchedulingBingoRepository
import com.kyleyarwood.goalbingo.reminder.ReminderScheduler

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
        .addMigrations(*ALL_MIGRATIONS)
        // Backstop only: if a future schema bump ships without a matching
        // Migration object, prefer wiping over crashing. Remove for prod builds.
        .fallbackToDestructiveMigration()
        .build()

    private val localRepository: BingoRepository = LocalBingoRepository(database.squareDao())

    val reminderScheduler: ReminderScheduler = ReminderScheduler(appContext, localRepository)

    val repository: BingoRepository = SchedulingBingoRepository(localRepository, reminderScheduler)

    val settings: SettingsRepository = DataStoreSettingsRepository(appContext.settingsDataStore)
}
