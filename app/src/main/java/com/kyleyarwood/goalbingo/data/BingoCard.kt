package com.kyleyarwood.goalbingo.data

data class BingoCard(
    val year: Int,
    val squares: List<Square>,
) {
    init {
        require(squares.size == SQUARE_COUNT) {
            "BingoCard must have exactly $SQUARE_COUNT squares, got ${squares.size}"
        }
    }

    fun square(position: Int): Square = squares[position]

    fun withSquare(square: Square): BingoCard =
        copy(squares = squares.toMutableList().also { it[square.position] = square })

    companion object {
        const val SIZE = 5
        const val SQUARE_COUNT = SIZE * SIZE

        fun empty(year: Int): BingoCard =
            BingoCard(year, List(SQUARE_COUNT) { Square(position = it, goal = null) })
    }
}
