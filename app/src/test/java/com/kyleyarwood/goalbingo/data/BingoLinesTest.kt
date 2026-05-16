package com.kyleyarwood.goalbingo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BingoLinesTest {

    @Test
    fun `empty card has no bingo`() {
        val card = BingoCard.empty(2026)
        assertFalse(BingoLines.hasBingo(card))
        assertEquals(emptySet<Int>(), BingoLines.completedPositions(card))
    }

    @Test
    fun `top row complete triggers bingo`() {
        val topRow = setOf(0, 1, 2, 3, 4)
        val card = cardWithCompleted(topRow)
        assertTrue(BingoLines.hasBingo(card))
        assertEquals(topRow, BingoLines.completedPositions(card))
    }

    @Test
    fun `main diagonal complete triggers bingo`() {
        val diagonal = setOf(0, 6, 12, 18, 24)
        val card = cardWithCompleted(diagonal)
        assertEquals(diagonal, BingoLines.completedPositions(card))
    }

    @Test
    fun `anti-diagonal complete triggers bingo`() {
        val anti = setOf(4, 8, 12, 16, 20)
        val card = cardWithCompleted(anti)
        assertEquals(anti, BingoLines.completedPositions(card))
    }

    @Test
    fun `four-in-a-row is not enough`() {
        val partial = setOf(0, 1, 2, 3)
        assertFalse(BingoLines.hasBingo(cardWithCompleted(partial)))
    }

    @Test
    fun `multiple completed lines return their union`() {
        val topRow = setOf(0, 1, 2, 3, 4)
        val leftCol = setOf(0, 5, 10, 15, 20)
        val card = cardWithCompleted(topRow + leftCol)
        assertEquals(topRow + leftCol, BingoLines.completedPositions(card))
    }

    private fun cardWithCompleted(positions: Set<Int>): BingoCard {
        val squares = List(BingoCard.SQUARE_COUNT) { pos ->
            val goal = if (pos in positions) {
                Goal.Checkbox(title = "g$pos", done = true)
            } else {
                null
            }
            Square(position = pos, goal = goal)
        }
        return BingoCard(year = 2026, squares = squares)
    }
}
