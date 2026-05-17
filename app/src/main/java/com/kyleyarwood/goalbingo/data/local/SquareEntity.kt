package com.kyleyarwood.goalbingo.data.local

import androidx.room.Entity
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.ReminderConfig
import com.kyleyarwood.goalbingo.data.Square
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
            },
        )
    }

    companion object {
        fun from(year: Int, square: Square): SquareEntity = when (val g = square.goal) {
            null -> SquareEntity(year, square.position, null, null, null, null, null, null, null, null)
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
            )
        }
    }
}

enum class GoalType { CHECKBOX, COUNTER }
