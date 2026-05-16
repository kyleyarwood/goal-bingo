package com.kyleyarwood.goalbingo.data

data class Square(
    val position: Int,
    val goal: Goal?,
) {
    val row: Int get() = position / BingoCard.SIZE
    val column: Int get() = position % BingoCard.SIZE
    val isComplete: Boolean get() = goal?.isComplete == true
}
