package com.kyleyarwood.goalbingo.data.local

import androidx.room.Entity
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.ReminderConfig
import com.kyleyarwood.goalbingo.data.Square
import com.kyleyarwood.goalbingo.data.StreakCadence
import java.time.LocalDate

@Entity(tableName = "squares", primaryKeys = ["year", "position"])
data class SquareEntity(
    val year: Int,
    val position: Int,
    val goalType: GoalType?,
    val title: String?,
    val target: Int?,
    val progress: Int?,
    val done: Boolean?,
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val lastIncrementedDate: String?,
    val streakAchieved: Boolean?,
    val streakConfirmedDates: String?,
    val streakCadence: String?,
) {
    fun toSquare(): Square {
        val reminder = if (reminderHour != null && reminderMinute != null) {
            ReminderConfig(reminderHour, reminderMinute)
        } else {
            null
        }
        return Square(
            position = position,
            goal = when (goalType) {
                null -> null
                GoalType.CHECKBOX -> Goal.Checkbox(
                    title = title.orEmpty(),
                    done = done ?: false,
                    reminder = reminder,
                )
                GoalType.COUNTER -> Goal.Counter(
                    title = title.orEmpty(),
                    target = target ?: 1,
                    progress = progress ?: 0,
                    reminder = reminder,
                    lastIncrementedDate = lastIncrementedDate?.let(LocalDate::parse),
                )
                GoalType.STREAK -> Goal.Streak(
                    title = title.orEmpty(),
                    cadence = streakCadence?.let { runCatching { StreakCadence.valueOf(it) }.getOrNull() }
                        ?: StreakCadence.MonthlyAllDays,
                    achieved = streakAchieved ?: false,
                    confirmedDates = parseDateSet(streakConfirmedDates),
                    reminder = reminder,
                )
            },
        )
    }

    companion object {
        fun from(year: Int, square: Square): SquareEntity = when (val g = square.goal) {
            null -> SquareEntity(year, square.position, null, null, null, null, null, null, null, null, null, null, null)
            is Goal.Checkbox -> SquareEntity(
                year = year,
                position = square.position,
                goalType = GoalType.CHECKBOX,
                title = g.title,
                target = null,
                progress = null,
                done = g.done,
                reminderHour = g.reminder?.hour,
                reminderMinute = g.reminder?.minute,
                lastIncrementedDate = null,
                streakAchieved = null,
                streakConfirmedDates = null,
                streakCadence = null,
            )
            is Goal.Counter -> SquareEntity(
                year = year,
                position = square.position,
                goalType = GoalType.COUNTER,
                title = g.title,
                target = g.target,
                progress = g.progress,
                done = null,
                reminderHour = g.reminder?.hour,
                reminderMinute = g.reminder?.minute,
                lastIncrementedDate = g.lastIncrementedDate?.toString(),
                streakAchieved = null,
                streakConfirmedDates = null,
                streakCadence = null,
            )
            is Goal.Streak -> SquareEntity(
                year = year,
                position = square.position,
                goalType = GoalType.STREAK,
                title = g.title,
                target = null,
                progress = null,
                done = null,
                reminderHour = g.reminder?.hour,
                reminderMinute = g.reminder?.minute,
                lastIncrementedDate = null,
                streakAchieved = g.achieved,
                streakConfirmedDates = formatDateSet(g.confirmedDates),
                streakCadence = g.cadence.name,
            )
        }
    }
}

enum class GoalType { CHECKBOX, COUNTER, STREAK }

private fun parseDateSet(value: String?): Set<LocalDate> {
    if (value.isNullOrBlank()) return emptySet()
    return value.split(",")
        .mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }
        .toSet()
}

private fun formatDateSet(dates: Set<LocalDate>): String? {
    if (dates.isEmpty()) return null
    return dates.sorted().joinToString(",")
}
