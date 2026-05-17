package com.kyleyarwood.goalbingo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.ReminderConfig
import com.kyleyarwood.goalbingo.data.Square
import com.kyleyarwood.goalbingo.data.toggled
import com.kyleyarwood.goalbingo.data.withProgressDelta
import com.kyleyarwood.goalbingo.data.withReminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SquareDetailViewModel(
    private val repository: BingoRepository,
    val year: Int,
    val position: Int,
) : ViewModel() {

    val square: StateFlow<Square?> = repository.observeCard(year)
        .map { it.square(position) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null,
        )

    fun increment(delta: Int) {
        val current = square.value ?: return
        val goal = current.goal ?: return
        // In-app +1 does NOT stamp lastIncrementedDate — that field tracks
        // notification-driven increments only, so the confirmation dialog can
        // tell "you already +1'd from the reminder" apart from a normal tap.
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal.withProgressDelta(delta)))
        }
    }

    fun setProgress(value: Int) {
        val current = square.value ?: return
        val goal = current.goal as? Goal.Counter ?: return
        // Allow exceeding target — over-target is still "complete" but the
        // overflow is meaningful (e.g. ran 12 sub-2hr halves vs a target of 1).
        val clamped = value.coerceAtLeast(0)
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal.copy(progress = clamped)))
        }
    }

    fun toggleDone() {
        val current = square.value ?: return
        val goal = current.goal ?: return
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal.toggled()))
        }
    }

    fun save(goal: Goal) {
        val current = square.value ?: return
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal))
        }
    }

    fun clear() {
        val current = square.value ?: return
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = null))
        }
    }

    fun setReminder(reminder: ReminderConfig?) {
        val current = square.value ?: return
        val goal = current.goal ?: return
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal.withReminder(reminder)))
        }
    }

    class Factory(
        private val repository: BingoRepository,
        private val year: Int,
        private val position: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SquareDetailViewModel(repository, year, position) as T
    }
}
