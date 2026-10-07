package com.example.algorithms

import kotlin.math.abs

object NQueensAlgorithm {

    enum class StepType {
        TRY,
        CONFLICT,
        PLACE_OK,
        BACKTRACK,
        SOLUTION_FOUND
    }

    data class VisualStep(
        val row: Int,
        val col: Int,
        val type: StepType,
        val boardState: IntArray, // boardState[row] = col or -1
        val explanation: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is VisualStep) return false
            return row == other.row &&
                    col == other.col &&
                    type == other.type &&
                    boardState.contentEquals(other.boardState) &&
                    explanation == other.explanation
        }

        override fun hashCode(): Int {
            var result = row
            result = 31 * result + col
            result = 31 * result + type.hashCode()
            result = 31 * result + boardState.contentHashCode()
            result = 31 * result + explanation.hashCode()
            return result
        }
    }

    fun isSafe(board: IntArray, row: Int, col: Int): Boolean {
        for (r in 0 until row) {
            val c = board[r]
            if (c == col) return false // same column
            if (abs(r - row) == abs(c - col)) return false // diagonal
        }
        return true
    }

    fun countConflicts(queens: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val conflicting = mutableSetOf<Pair<Int, Int>>()
        for (i in queens.indices) {
            for (j in i + 1 until queens.size) {
                val q1 = queens[i]
                val q2 = queens[j]
                val sameRow = q1.first == q2.first
                val sameCol = q1.second == q2.second
                val sameDiag = abs(q1.first - q2.first) == abs(q1.second - q2.second)
                if (sameRow || sameCol || sameDiag) {
                    conflicting.add(q1)
                    conflicting.add(q2)
                }
            }
        }
        return conflicting.toList()
    }

    fun recordBacktrackingSteps(n: Int, maxSteps: Int = 500): List<VisualStep> {
        val steps = mutableListOf<VisualStep>()
        val board = IntArray(n) { -1 }

        fun backtrack(row: Int): Boolean {
            if (row == n) {
                steps.add(
                    VisualStep(
                        row = n - 1,
                        col = board[n - 1],
                        type = StepType.SOLUTION_FOUND,
                        boardState = board.clone(),
                        explanation = "Success! Found valid non-attacking configuration for all $n queens."
                    )
                )
                return true
            }

            for (col in 0 until n) {
                if (steps.size >= maxSteps) return false

                steps.add(
                    VisualStep(
                        row = row,
                        col = col,
                        type = StepType.TRY,
                        boardState = board.clone(),
                        explanation = "Row ${row + 1}: Testing position at Column ${col + 1}."
                    )
                )

                if (isSafe(board, row, col)) {
                    board[row] = col
                    steps.add(
                        VisualStep(
                            row = row,
                            col = col,
                            type = StepType.PLACE_OK,
                            boardState = board.clone(),
                            explanation = "Row ${row + 1}: Placed queen at Column ${col + 1} (no conflicts)."
                        )
                    )

                    if (backtrack(row + 1)) return true

                    board[row] = -1
                    steps.add(
                        VisualStep(
                            row = row,
                            col = col,
                            type = StepType.BACKTRACK,
                            boardState = board.clone(),
                            explanation = "Row ${row + 1}: Backtracking from Column ${col + 1}. Exploring next branch."
                        )
                    )
                } else {
                    steps.add(
                        VisualStep(
                            row = row,
                            col = col,
                            type = StepType.CONFLICT,
                            boardState = board.clone(),
                            explanation = "Row ${row + 1}: Column ${col + 1} attacked on column or diagonal. Invalid."
                        )
                    )
                }
            }
            return false
        }

        backtrack(0)
        return steps
    }

    fun findOneSolution(n: Int): List<Pair<Int, Int>> {
        val board = IntArray(n) { -1 }

        fun solve(row: Int): Boolean {
            if (row == n) return true
            for (col in 0 until n) {
                if (isSafe(board, row, col)) {
                    board[row] = col
                    if (solve(row + 1)) return true
                    board[row] = -1
                }
            }
            return false
        }

        solve(0)
        return board.mapIndexed { r, c -> Pair(r, c) }
    }
}
