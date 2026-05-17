package com.kyleyarwood.goalbingo.data.local

import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.Square
import com.kyleyarwood.goalbingo.reminder.ReminderScheduler

/**
 * Decorates a [BingoRepository] so every write also reconciles the alarm
 * schedule for that goal — schedule when a reminder is present, cancel
 * otherwise. Reads delegate straight through.
 */
class SchedulingBingoRepository(
    private val delegate: BingoRepository,
    private val scheduler: ReminderScheduler,
) : BingoRepository by delegate {

    override suspend fun upsertSquare(year: Int, square: Square) {
        delegate.upsertSquare(year, square)
        val reminder = square.goal?.reminder
        if (reminder != null) {
            scheduler.schedule(year, square.position, reminder)
        } else {
            scheduler.cancel(year, square.position)
        }
    }
}
