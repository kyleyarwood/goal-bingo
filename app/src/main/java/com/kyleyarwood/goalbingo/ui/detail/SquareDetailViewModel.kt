package com.kyleyarwood.goalbingo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.Goal
import com.kyleyarwood.goalbingo.data.Square
import com.kyleyarwood.goalbingo.data.toggled
import com.kyleyarwood.goalbingo.data.withProgressDelta
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

    val square: StateFlow<Square> = repository.observeCard(year)
        .map { it.square(position) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = Square(position = position, goal = null),
        )

    fun increment(delta: Int) {
        val current = square.value
        val goal = current.goal ?: return
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal.withProgressDelta(delta)))
        }
    }

    fun toggleDone() {
        val current = square.value
        val goal = current.goal ?: return
        viewModelScope.launch {
            repository.upsertSquare(year, current.copy(goal = goal.toggled()))
        }
    }

    fun save(goal: Goal) {
        viewModelScope.launch {
            repository.upsertSquare(year, square.value.copy(goal = goal))
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.upsertSquare(year, square.value.copy(goal = null))
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
