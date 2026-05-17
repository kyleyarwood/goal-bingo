package com.kyleyarwood.goalbingo.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

data class ReminderConfig(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23) { "hour must be 0..23, got $hour" }
        require(minute in 0..59) { "minute must be 0..59, got $minute" }
    }
}

/** Three flavors of streak with different "do it" + achievement rules. */
enum class StreakCadence {
    /** Confirm every day for a full calendar month. */
    MonthlyAllDays,
    /** Confirm every day for a full ISO week (Mon–Sun). */
    WeeklyAllDays,
    /** Confirm at least once each ISO week, for every week of the year. */
    YearlyOncePerWeek,
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

    /**
     * Streak goal. Cadence picks the achievement rule; [confirmedDates] is
     * the same source-of-truth regardless. Once achieved the tile stays
     * complete for the year.
     */
    data class Streak(
        override val title: String,
        val cadence: StreakCadence = StreakCadence.MonthlyAllDays,
        val achieved: Boolean = false,
        val confirmedDates: Set<LocalDate> = emptySet(),
        override val reminder: ReminderConfig? = null,
    ) : Goal {
        override val isComplete: Boolean get() = achieved
    }
}

fun Goal.withProgressDelta(delta: Int): Goal = when (this) {
    is Goal.Checkbox -> copy(done = if (delta == 0) done else delta > 0)
    is Goal.Counter -> copy(progress = (progress + delta).coerceAtLeast(0))
    is Goal.Streak -> this
}

fun Goal.toggled(): Goal = when (this) {
    is Goal.Checkbox -> copy(done = !done)
    is Goal.Counter -> copy(progress = if (isComplete) 0 else target)
    is Goal.Streak -> this
}

fun Goal.withReminder(reminder: ReminderConfig?): Goal = when (this) {
    is Goal.Checkbox -> copy(reminder = reminder)
    is Goal.Counter -> copy(reminder = reminder)
    is Goal.Streak -> copy(reminder = reminder)
}

/**
 * Snapshot of a streak's progress within its current period (week, month, or year)
 * — generic across cadences so the UI only needs four cases.
 *
 * For [StreakCadence.YearlyOncePerWeek], "units" are weeks; [Active.confirmedNow]
 * means "this week has at least one confirmation." For the daily cadences, "units"
 * are days and [Active.confirmedNow] means "today is confirmed."
 */
sealed interface StreakStatus {
    data object Achieved : StreakStatus
    data object NotStarted : StreakStatus
    data class Active(val confirmed: Int, val total: Int, val confirmedNow: Boolean) : StreakStatus
    data class Broken(val confirmed: Int, val total: Int) : StreakStatus
}

private val WEEK_FIELDS = WeekFields.ISO

fun Goal.Streak.statusOn(today: LocalDate): StreakStatus {
    if (achieved) return StreakStatus.Achieved
    return when (cadence) {
        StreakCadence.MonthlyAllDays -> monthlyStatus(today)
        StreakCadence.WeeklyAllDays -> weeklyStatus(today)
        StreakCadence.YearlyOncePerWeek -> yearlyOncePerWeekStatus(today)
    }
}

fun Goal.Streak.confirmedOn(today: LocalDate): Goal.Streak {
    if (achieved || today in confirmedDates) return this
    val updated = copy(confirmedDates = confirmedDates + today)
    val nowAchieved = when (cadence) {
        StreakCadence.MonthlyAllDays -> monthFullyConfirmed(updated.confirmedDates, today)
        StreakCadence.WeeklyAllDays -> weekFullyConfirmed(updated.confirmedDates, today)
        StreakCadence.YearlyOncePerWeek -> yearFullyConfirmedOncePerWeek(updated.confirmedDates, today)
    }
    return if (nowAchieved) updated.copy(achieved = true) else updated
}

// --- Monthly ---

private fun Goal.Streak.monthlyStatus(today: LocalDate): StreakStatus {
    val month = YearMonth.from(today)
    val total = month.lengthOfMonth()
    val thisMonth = confirmedDates.filter { YearMonth.from(it) == month }.toSortedSet()
    if (thisMonth.isEmpty()) return StreakStatus.NotStarted

    val mostRecent = thisMonth.last()
    val expectedRun = (1..mostRecent.dayOfMonth).map { month.atDay(it) }
    val broken = !thisMonth.containsAll(expectedRun)

    return if (broken) {
        StreakStatus.Broken(confirmed = thisMonth.size, total = total)
    } else {
        StreakStatus.Active(
            confirmed = thisMonth.size,
            total = total,
            confirmedNow = today in thisMonth,
        )
    }
}

private fun monthFullyConfirmed(dates: Set<LocalDate>, anchor: LocalDate): Boolean {
    val month = YearMonth.from(anchor)
    val allDays = (1..month.lengthOfMonth()).map { month.atDay(it) }
    return dates.containsAll(allDays)
}

// --- Weekly ---

private fun Goal.Streak.weeklyStatus(today: LocalDate): StreakStatus {
    val weekStart = today.with(WEEK_FIELDS.dayOfWeek(), 1)
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
    val thisWeek = confirmedDates.filter { it in weekDays }.toSortedSet()
    if (thisWeek.isEmpty()) return StreakStatus.NotStarted

    val mostRecent = thisWeek.last()
    val expectedRun = weekDays.takeWhile { it <= mostRecent }
    val broken = !thisWeek.containsAll(expectedRun)

    return if (broken) {
        StreakStatus.Broken(confirmed = thisWeek.size, total = 7)
    } else {
        StreakStatus.Active(
            confirmed = thisWeek.size,
            total = 7,
            confirmedNow = today in thisWeek,
        )
    }
}

private fun weekFullyConfirmed(dates: Set<LocalDate>, anchor: LocalDate): Boolean {
    val weekStart = anchor.with(WEEK_FIELDS.dayOfWeek(), 1)
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
    return dates.containsAll(weekDays)
}

// --- Yearly (≥1 per ISO week, every week of the year) ---

private fun Goal.Streak.yearlyOncePerWeekStatus(today: LocalDate): StreakStatus {
    val year = today.get(WEEK_FIELDS.weekBasedYear())
    val totalWeeks = today.range(WEEK_FIELDS.weekOfWeekBasedYear()).maximum.toInt()
    val currentWeek = today.get(WEEK_FIELDS.weekOfWeekBasedYear())

    val weeksConfirmed = confirmedDates
        .filter { it.get(WEEK_FIELDS.weekBasedYear()) == year }
        .map { it.get(WEEK_FIELDS.weekOfWeekBasedYear()) }
        .toSet()
    if (weeksConfirmed.isEmpty()) return StreakStatus.NotStarted

    val confirmedThisWeek = currentWeek in weeksConfirmed
    val expectedPastWeeks = (1 until currentWeek).toSet()
    val broken = !weeksConfirmed.containsAll(expectedPastWeeks)

    return if (broken) {
        StreakStatus.Broken(confirmed = weeksConfirmed.size, total = totalWeeks)
    } else {
        StreakStatus.Active(
            confirmed = weeksConfirmed.size,
            total = totalWeeks,
            confirmedNow = confirmedThisWeek,
        )
    }
}

private fun yearFullyConfirmedOncePerWeek(dates: Set<LocalDate>, anchor: LocalDate): Boolean {
    val year = anchor.get(WEEK_FIELDS.weekBasedYear())
    val totalWeeks = anchor.range(WEEK_FIELDS.weekOfWeekBasedYear()).maximum.toInt()
    val weeksConfirmed = dates
        .filter { it.get(WEEK_FIELDS.weekBasedYear()) == year }
        .map { it.get(WEEK_FIELDS.weekOfWeekBasedYear()) }
        .toSet()
    return weeksConfirmed.size >= totalWeeks
}
