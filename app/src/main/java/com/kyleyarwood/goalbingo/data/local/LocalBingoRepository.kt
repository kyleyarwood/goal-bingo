package com.kyleyarwood.goalbingo.data.local

import com.kyleyarwood.goalbingo.data.BingoCard
import com.kyleyarwood.goalbingo.data.BingoRepository
import com.kyleyarwood.goalbingo.data.Square
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalBingoRepository(private val dao: SquareDao) : BingoRepository {

    override fun observeCard(year: Int): Flow<BingoCard> =
        dao.observeSquares(year).map { rows -> hydrate(year, rows) }

    override suspend fun upsertSquare(year: Int, square: Square) {
        dao.upsert(SquareEntity.from(year, square))
    }

    private fun hydrate(year: Int, rows: List<SquareEntity>): BingoCard {
        val byPosition = rows.associateBy { it.position }
        val squares = List(BingoCard.SQUARE_COUNT) { position ->
            byPosition[position]?.toSquare() ?: Square(position = position, goal = null)
        }
        return BingoCard(year = year, squares = squares)
    }
}
