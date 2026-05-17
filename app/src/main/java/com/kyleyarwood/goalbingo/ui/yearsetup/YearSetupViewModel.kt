package com.kyleyarwood.goalbingo.ui.yearsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyleyarwood.goalbingo.data.BingoCard
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.ReminderConfig
import com.kyleyarwood.goalbingo.data.Square
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * In-memory representation of one editable row in the setup screen.
 *
 * Newly-created rows: blank target → checkbox; numeric target → counter.
 * Streak goals can't be created from this screen (use the per-goal editor),
 * but if a row was loaded as a Streak [originalStreak] preserves it through
 * shuffles and title edits.
 */
data class GoalRow(
    val title: String = "",
    val target: String = "",
    val originalProgress: Int? = null,
    val originalDone: Boolean? = null,
    val originalReminder: ReminderConfig? = null,
    val originalLastIncrementedDate: LocalDate? = null,
    val originalStreak: Goal.Streak? = null,
) {
    val isStreak: Boolean get() = originalStreak != null

    fun toGoal(): Goal? {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return null
        if (originalStreak != null) {
            return originalStreak.copy(title = trimmed)
        }
        val targetInt = target.toIntOrNull()
        return if (targetInt != null && targetInt > 0) {
            Goal.Counter(
                title = trimmed,
                target = targetInt,
                progress = originalProgress ?: 0,
                reminder = originalReminder,
                lastIncrementedDate = originalLastIncrementedDate,
            )
        } else {
            Goal.Checkbox(
                title = trimmed,
                done = originalDone ?: false,
                reminder = originalReminder,
            )
        }
    }
}

class YearSetupViewModel(
    private val repository: BingoRepository,
    val year: Int,
) : ViewModel() {

    private val _rows = MutableStateFlow(List(BingoCard.SQUARE_COUNT) { GoalRow() })
    val rows: StateFlow<List<GoalRow>> = _rows.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        viewModelScope.launch {
            val card = repository.observeCard(year).first()
            _rows.value = card.squares.map { sq ->
                when (val g = sq.goal) {
                    null -> GoalRow()
                    is Goal.Checkbox -> GoalRow(
                        title = g.title,
                        originalDone = g.done,
                        originalReminder = g.reminder,
                    )
                    is Goal.Counter -> GoalRow(
                        title = g.title,
                        target = g.target.toString(),
                        originalProgress = g.progress,
                        originalReminder = g.reminder,
                        originalLastIncrementedDate = g.lastIncrementedDate,
                    )
                    is Goal.Streak -> GoalRow(
                        title = g.title,
                        originalReminder = g.reminder,
                        originalStreak = g,
                    )
                }
            }
            _loaded.value = true
        }
    }

    fun updateTitle(index: Int, title: String) {
        _rows.update(index) { it.copy(title = title) }
    }

    fun updateTarget(index: Int, target: String) {
        val sanitized = target.filter(Char::isDigit).take(5)
        _rows.update(index) { it.copy(target = sanitized) }
    }

    fun shuffle() {
        _rows.value = _rows.value.shuffled()
    }

    fun clearAll() {
        _rows.value = List(BingoCard.SQUARE_COUNT) { GoalRow() }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _rows.value.forEachIndexed { index, row ->
                repository.upsertSquare(year, Square(position = index, goal = row.toGoal()))
            }
            onDone()
        }
    }

    private fun MutableStateFlow<List<GoalRow>>.update(index: Int, transform: (GoalRow) -> GoalRow) {
        value = value.toMutableList().also { it[index] = transform(it[index]) }
    }

    class Factory(
        private val repository: BingoRepository,
        private val year: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            YearSetupViewModel(repository, year) as T
    }
}
