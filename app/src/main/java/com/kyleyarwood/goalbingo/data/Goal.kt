package com.kyleyarwood.goalbingo.data

sealed interface Goal {
    val title: String
    val description: String
    val isComplete: Boolean

    data class Checkbox(
        override val title: String,
        override val description: String = "",
        val done: Boolean = false,
    ) : Goal {
        override val isComplete: Boolean get() = done
    }

    data class Counter(
        override val title: String,
        override val description: String = "",
        val target: Int,
        val progress: Int = 0,
    ) : Goal {
        override val isComplete: Boolean get() = progress >= target
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
