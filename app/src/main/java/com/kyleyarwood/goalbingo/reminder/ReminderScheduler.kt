package com.kyleyarwood.goalbingo.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kyleyarwood.goalbingo.data.BingoCard
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.ReminderConfig
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Schedules / cancels exact daily alarms for each goal's reminder.
 *
 * Pairs with [AlarmReceiver], which posts the notification when an alarm fires
 * and then asks the scheduler to set the next day's alarm.
 */
class ReminderScheduler(
    private val context: Context,
    private val repository: BingoRepository,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(year: Int, position: Int, reminder: ReminderConfig) {
        val pending = pendingIntent(year, position, mutable = false)
        val triggerAt = nextTrigger(reminder)
        val am = alarmManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, INEXACT_WINDOW_MS, pending)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(year: Int, position: Int) {
        val pending = pendingIntent(year, position, mutable = false)
        alarmManager?.cancel(pending)
    }

    /**
     * Re-applies the scheduling intent for every goal in the current year.
     * Called on boot and on app upgrade — alarms don't survive either.
     */
    suspend fun rescheduleAll(year: Int) {
        val card = repository.observeCard(year).first()
        card.squares.forEach { sq ->
            val reminder = sq.goal?.reminder
            if (reminder != null) {
                schedule(year, sq.position, reminder)
            } else {
                cancel(year, sq.position)
            }
        }
    }

    private fun pendingIntent(year: Int, position: Int, mutable: Boolean): PendingIntent {
        val intent = AlarmReceiver.intent(context, year, position)
        val flags = (if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE) or
            PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, requestCode(year, position), intent, flags)
    }

    companion object {
        private const val INEXACT_WINDOW_MS = 15L * 60 * 1000

        fun requestCode(year: Int, position: Int): Int = year * BingoCard.SQUARE_COUNT + position

        fun nextTrigger(reminder: ReminderConfig, now: Calendar = Calendar.getInstance()): Long {
            val target = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (!target.after(now)) {
                target.add(Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis
        }
    }
}
