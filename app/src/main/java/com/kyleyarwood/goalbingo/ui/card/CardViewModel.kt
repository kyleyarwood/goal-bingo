package com.kyleyarwood.goalbingo.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyleyarwood.goalbingo.data.BingoCard
import com.kyleyarwood.goalbingo.data.BingoLines
import com.kyleyarwood.goalbingo.data.BingoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CardUiState(
    val card: BingoCard,
    val completedPositions: Set<Int>,
) {
    val hasBingo: Boolean get() = completedPositions.isNotEmpty()
}

class CardViewModel(
    private val repository: BingoRepository,
    val year: Int,
) : ViewModel() {

    val state: StateFlow<CardUiState> = repository.observeCard(year)
        .map { card -> CardUiState(card, BingoLines.completedPositions(card)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CardUiState(BingoCard.empty(year), emptySet()),
        )

    class Factory(
        private val repository: BingoRepository,
        private val year: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CardViewModel(repository, year) as T
    }
}
