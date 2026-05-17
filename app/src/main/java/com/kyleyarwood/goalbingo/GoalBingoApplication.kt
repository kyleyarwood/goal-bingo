package com.kyleyarwood.goalbingo

import android.app.Application
import com.kyleyarwood.goalbingo.di.ServiceLocator
import com.kyleyarwood.goalbingo.reminder.Notifications

class GoalBingoApplication : Application() {
    lateinit var services: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        services = ServiceLocator(this)
        Notifications.ensureChannel(this)
    }
}
