package com.kyleyarwood.goalbingo.data

import kotlinx.coroutines.flow.Flow

/**
 * Storage-agnostic access to a single year's bingo card.
 *
 * A second implementation (e.g. cloud-backed) only needs to implement this interface;
 * the UI and viewmodels do not depend on Room.
 */
interface BingoRepository {
    fun observeCard(year: Int): Flow<BingoCard>
    suspend fun upsertSquare(year: Int, square: Square)
}
