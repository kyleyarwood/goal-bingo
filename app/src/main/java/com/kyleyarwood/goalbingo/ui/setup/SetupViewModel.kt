package com.kyleyarwood.goalbingo.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyleyarwood.goalbingo.data.BingoCard
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.Square
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SetupViewModel(
    private val repository: BingoRepository,
    val year: Int,
) : ViewModel() {

    val card: StateFlow<BingoCard> = repository.observeCard(year).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = BingoCard.empty(year),
    )

    fun save(square: Square) {
        viewModelScope.launch { repository.upsertSquare(year, square) }
    }

    class Factory(
        private val repository: BingoRepository,
        private val year: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SetupViewModel(repository, year) as T
    }
}
