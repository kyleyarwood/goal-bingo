package com.kyleyarwood.goalbingo.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kyleyarwood.goalbingo.GoalBingoApplication
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.toggled
import com.kyleyarwood.goalbingo.data.withProgressDelta
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Handles "+1" / "Mark done" buttons inside a reminder notification.
 *
 * Writes via the same repository as the UI so the change appears immediately
 * the next time the user opens the app.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val year = intent.getIntExtra(EXTRA_YEAR, -1)
        val position = intent.getIntExtra(EXTRA_POSITION, -1)
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return
        if (year < 0 || position < 0) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as GoalBingoApplication
                val repo = app.services.repository
                val square = repo.observeCard(year).first().square(position)
                val goal = square.goal ?: return@launch
                val updated = when (action) {
                    ACTION_INCREMENT -> if (goal is Goal.Counter) {
                        goal.copy(
                            progress = (goal.progress + 1).coerceAtLeast(0),
                            lastIncrementedDate = LocalDate.now(),
                        )
                    } else {
                        goal.withProgressDelta(1)
                    }
                    ACTION_TOGGLE -> goal.toggled()
                    else -> return@launch
                }
                repo.upsertSquare(year, square.copy(goal = updated))
                NotificationManagerCompat.from(context).cancel(Notifications.notificationId(year, position))
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_YEAR = "year"
        const val EXTRA_POSITION = "position"
        const val EXTRA_ACTION = "action"

        const val ACTION_INCREMENT = "increment"
        const val ACTION_TOGGLE = "toggle"

        fun actionIntent(context: Context, year: Int, position: Int, action: String): PendingIntent {
            val intent = Intent(context, ReminderActionReceiver::class.java).apply {
                putExtra(EXTRA_YEAR, year)
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_ACTION, action)
            }
            val requestCode = ReminderScheduler.requestCode(year, position) + action.hashCode()
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
