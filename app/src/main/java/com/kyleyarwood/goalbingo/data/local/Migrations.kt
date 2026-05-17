package com.kyleyarwood.goalbingo.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema migrations for [BingoDatabase].
 *
 * When you bump the database version, add a new migration here and register it
 * in [com.kyleyarwood.goalbingo.di.ServiceLocator] — this preserves user data
 * across schema changes instead of wiping it.
 */
internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE squares ADD COLUMN reminderHour INTEGER")
        db.execSQL("ALTER TABLE squares ADD COLUMN reminderMinute INTEGER")
    }
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE squares ADD COLUMN lastIncrementedDate TEXT")
    }
}

internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE squares ADD COLUMN streakAchieved INTEGER")
        db.execSQL("ALTER TABLE squares ADD COLUMN streakConfirmedDates TEXT")
    }
}

internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Existing Streak rows (if any) keep null cadence; the entity reads
        // null as the original MonthlyAllDays behavior.
        db.execSQL("ALTER TABLE squares ADD COLUMN streakCadence TEXT")
    }
}

internal val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
)
