package com.kyleyarwood.goalbingo.data.local

import androidx.room.Entity
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.Square

@Entity(tableName = "squares", primaryKeys = ["year", "position"])
data class SquareEntity(
    val year: Int,
    val position: Int,
    val goalType: GoalType?,
    val title: String?,
    val target: Int?,
    val progress: Int?,
    val done: Boolean?,
) {
    fun toSquare(): Square = Square(
        position = position,
        goal = when (goalType) {
            null -> null
            GoalType.CHECKBOX -> Goal.Checkbox(
                title = title.orEmpty(),
                done = done ?: false,
            )
            GoalType.COUNTER -> Goal.Counter(
                title = title.orEmpty(),
                target = target ?: 1,
                progress = progress ?: 0,
            )
        },
    )

    companion object {
        fun from(year: Int, square: Square): SquareEntity = when (val g = square.goal) {
            null -> SquareEntity(year, square.position, null, null, null, null, null)
            is Goal.Checkbox -> SquareEntity(
                year = year,
                position = square.position,
                goalType = GoalType.CHECKBOX,
                title = g.title,
                target = null,
                progress = null,
                done = g.done,
            )
            is Goal.Counter -> SquareEntity(
                year = year,
                position = square.position,
                goalType = GoalType.COUNTER,
                title = g.title,
                target = g.target,
                progress = g.progress,
                done = null,
            )
        }
    }
}

enum class GoalType { CHECKBOX, COUNTER }
