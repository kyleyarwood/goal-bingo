package com.kyleyarwood.goalbingo.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.kyleyarwood.goalbingo.R

object Notifications {
    const val CHANNEL_ID = "goal_reminders"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.reminder_channel_description)
            },
        )
    }

    /** Stable per-square notification id derived from (year, position). */
    fun notificationId(year: Int, position: Int): Int = year * 100 + position
}
