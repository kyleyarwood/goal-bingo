package com.kyleyarwood.goalbingo.data

import java.time.LocalDate

data class ReminderConfig(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23) { "hour must be 0..23, got $hour" }
        require(minute in 0..59) { "minute must be 0..59, got $minute" }
    }
}

sealed interface Goal {
    val title: String
    val isComplete: Boolean
    val reminder: ReminderConfig?

    data class Checkbox(
        override val title: String,
        val done: Boolean = false,
        override val reminder: ReminderConfig? = null,
    ) : Goal {
        override val isComplete: Boolean get() = done
    }

    data class Counter(
        override val title: String,
        val target: Int,
        val progress: Int = 0,
        override val reminder: ReminderConfig? = null,
        val lastIncrementedDate: LocalDate? = null,
    ) : Goal {
        override val isComplete: Boolean get() = progress >= target
        fun wasIncrementedOn(date: LocalDate): Boolean = lastIncrementedDate == date
    }
}

fun Goal.withProgressDelta(delta: Int): Goal = when (this) {
    is Goal.Checkbox -> copy(done = if (delta == 0) done else delta > 0)
    is Goal.Counter -> copy(progress = (progress + delta).coerceAtLeast(0))
}

fun Goal.toggled(): Goal = when (this) {
    is Goal.Checkbox -> copy(done = !done)
    is Goal.Counter -> copy(progress = if (isComplete) 0 else target)
}

fun Goal.withReminder(reminder: ReminderConfig?): Goal = when (this) {
    is Goal.Checkbox -> copy(reminder = reminder)
    is Goal.Counter -> copy(reminder = reminder)
}
