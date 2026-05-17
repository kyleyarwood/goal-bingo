package com.kyleyarwood.goalbingo.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kyleyarwood.goalbingo.GoalBingoApplication
import com.kyleyarwood.goalbingo.MainActivity
import com.kyleyarwood.goalbingo.R
import com.kyleyarwood.goalbingo.data.Goal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val year = intent.getIntExtra(EXTRA_YEAR, -1)
        val position = intent.getIntExtra(EXTRA_POSITION, -1)
        if (year < 0 || position < 0) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as GoalBingoApplication
                val card = app.services.repository.observeCard(year).first()
                val goal = card.square(position).goal ?: return@launch
                postNotification(context, year, position, goal)

                val reminder = goal.reminder
                if (reminder != null) {
                    app.services.reminderScheduler.schedule(year, position, reminder)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotification(context: Context, year: Int, position: Int, goal: Goal) {
        val nm = NotificationManagerCompat.from(context)
        // Silently no-op if user denied POST_NOTIFICATIONS — we never crashed scheduling.
        if (!nm.areNotificationsEnabled()) return

        val title = goal.title
        val body = when (goal) {
            is Goal.Counter -> context.getString(R.string.progress_format, goal.progress, goal.target)
            is Goal.Checkbox -> context.getString(
                if (goal.done) R.string.notification_body_done else R.string.notification_body_not_done,
            )
            is Goal.Streak -> context.getString(R.string.notification_body_streak)
        }

        val builder = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))

        // Always offer the action — the in-app +1 will surface a confirmation
        // dialog if the user already tapped +1 from a previous notification today.
        // Streak goals deliberately have no action button: confirming a streak
        // is a deliberate "Kept it today" tap in the app, not a one-tap from a
        // notification we can't be sure the user actually read.
        when (goal) {
            is Goal.Streak -> Unit
            is Goal.Counter -> builder.addAction(
                0,
                context.getString(R.string.add_one),
                ReminderActionReceiver.actionIntent(context, year, position, ReminderActionReceiver.ACTION_INCREMENT),
            )
            is Goal.Checkbox -> if (!goal.done) {
                builder.addAction(
                    0,
                    context.getString(R.string.mark_done),
                    ReminderActionReceiver.actionIntent(context, year, position, ReminderActionReceiver.ACTION_TOGGLE),
                )
            }
        }

        nm.notify(Notifications.notificationId(year, position), builder.build())
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_YEAR = "year"
        const val EXTRA_POSITION = "position"

        fun intent(context: Context, year: Int, position: Int): Intent =
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_YEAR, year)
                putExtra(EXTRA_POSITION, position)
            }
    }
}
