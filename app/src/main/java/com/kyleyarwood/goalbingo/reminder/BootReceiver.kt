package com.kyleyarwood.goalbingo.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kyleyarwood.goalbingo.GoalBingoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as GoalBingoApplication
                val year = Calendar.getInstance().get(Calendar.YEAR)
                app.services.reminderScheduler.rescheduleAll(year)
            } finally {
                pending.finish()
            }
        }
    }
}
