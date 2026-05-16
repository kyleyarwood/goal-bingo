package com.kyleyarwood.goalbingo.data

/**
 * A "line" is a winning set of positions on the card: any row, column, or diagonal
 * whose every square is complete.
 */
object BingoLines {
    private val allLines: List<Set<Int>> = buildList {
        val size = BingoCard.SIZE
        // Rows.
        for (r in 0 until size) {
            add((0 until size).map { c -> r * size + c }.toSet())
        }
        // Columns.
        for (c in 0 until size) {
            add((0 until size).map { r -> r * size + c }.toSet())
        }
        // Diagonals.
        add((0 until size).map { i -> i * size + i }.toSet())
        add((0 until size).map { i -> i * size + (size - 1 - i) }.toSet())
    }

    /** Positions that belong to a completed row, column, or diagonal. */
    fun completedPositions(card: BingoCard): Set<Int> {
        val completed = card.squares.filter { it.isComplete }.map { it.position }.toSet()
        return allLines
            .filter { line -> completed.containsAll(line) }
            .flatten()
            .toSet()
    }

    fun hasBingo(card: BingoCard): Boolean = completedPositions(card).isNotEmpty()
}
