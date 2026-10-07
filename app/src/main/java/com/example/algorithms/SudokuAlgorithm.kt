package com.example.algorithms

import com.example.data.model.Difficulty
import kotlin.random.Random

object SudokuAlgorithm {

    data class SudokuBoard(
        val initial: Array<IntArray>,
        val solution: Array<IntArray>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SudokuBoard) return false
            return initial.contentDeepEquals(other.initial) && solution.contentDeepEquals(other.solution)
        }

        override fun hashCode(): Int {
            var result = initial.contentDeepHashCode()
            result = 31 * result + solution.contentDeepHashCode()
            return result
        }
    }

    data class SolverStep(
        val row: Int,
        val col: Int,
        val value: Int,
        val explanation: String,
        val isBacktrack: Boolean = false
    )

    fun isValidMove(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until 9) {
            if (grid[row][i] == num && i != col) return false
            if (grid[i][col] == num && i != row) return false
        }
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val cr = startRow + r
                val cc = startCol + c
                if (grid[cr][cc] == num && !(cr == row && cc == col)) return false
            }
        }
        return true
    }

    fun solve(grid: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (grid[row][col] == 0) {
                    for (num in 1..9) {
                        if (isValidMove(grid, row, col, num)) {
                            grid[row][col] = num
                            if (solve(grid)) return true
                            grid[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun generatePuzzle(difficulty: Difficulty): SudokuBoard {
        val solution = Array(9) { IntArray(9) }
        fillDiagonalBoxes(solution)
        solve(solution)

        val initial = Array(9) { r -> solution[r].clone() }
        val cluesToKeep = when (difficulty) {
            Difficulty.EASY -> 42
            Difficulty.MEDIUM -> 34
            Difficulty.HARD -> 28
            Difficulty.EXPERT -> 24
        }
        val toRemove = 81 - cluesToKeep
        val positions = (0 until 81).shuffled(Random(System.currentTimeMillis()))

        var removed = 0
        for (pos in positions) {
            if (removed >= toRemove) break
            val r = pos / 9
            val c = pos % 9
            if (initial[r][c] != 0) {
                initial[r][c] = 0
                removed++
            }
        }

        return SudokuBoard(initial = initial, solution = solution)
    }

    private fun fillDiagonalBoxes(grid: Array<IntArray>) {
        for (box in 0 until 9 step 3) {
            val nums = (1..9).shuffled(Random(System.currentTimeMillis()))
            var idx = 0
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    grid[box + r][box + c] = nums[idx++]
                }
            }
        }
    }

    fun getCandidates(grid: Array<IntArray>, row: Int, col: Int): List<Int> {
        if (grid[row][col] != 0) return emptyList()
        val candidates = mutableListOf<Int>()
        for (num in 1..9) {
            if (isValidMove(grid, row, col, num)) {
                candidates.add(num)
            }
        }
        return candidates
    }

    fun getIntelligentHint(grid: Array<IntArray>, solution: Array<IntArray>): Pair<Pair<Int, Int>, String>? {
        // 1. Look for naked singles (cells where only 1 candidate fits)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0) {
                    val cand = getCandidates(grid, r, c)
                    if (cand.size == 1) {
                        val num = cand.first()
                        return Pair(
                            Pair(r, c),
                            "Row ${r + 1}, Column ${c + 1}: The number $num is a Naked Single. No other digit can legally be placed here."
                        )
                    }
                }
            }
        }

        // 2. Look for hidden single in row/col/block
        for (r in 0 until 9) {
            for (num in 1..9) {
                val possibleCols = mutableListOf<Int>()
                for (c in 0 until 9) {
                    if (grid[r][c] == 0 && isValidMove(grid, r, c, num)) {
                        possibleCols.add(c)
                    }
                }
                if (possibleCols.size == 1) {
                    val c = possibleCols.first()
                    return Pair(
                        Pair(r, c),
                        "In Row ${r + 1}, digit $num can only fit in Column ${c + 1} without conflicting."
                    )
                }
            }
        }

        // 3. Fallback: select cell with fewest candidates
        var bestPos: Pair<Int, Int>? = null
        var minCand = 10
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0) {
                    val cand = getCandidates(grid, r, c)
                    if (cand.isNotEmpty() && cand.size < minCand) {
                        minCand = cand.size
                        bestPos = Pair(r, c)
                    }
                }
            }
        }

        bestPos?.let { (r, c) ->
            val correctVal = solution[r][c]
            return Pair(
                Pair(r, c),
                "Try examining Row ${r + 1}, Column ${c + 1}. Its candidates are constrained. Correct value is $correctVal."
            )
        }

        return null
    }

    fun solveWithSteps(grid: Array<IntArray>): List<SolverStep> {
        val steps = mutableListOf<SolverStep>()
        val copy = Array(9) { r -> grid[r].clone() }

        fun solveRecursive(): Boolean {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    if (copy[row][col] == 0) {
                        val candidates = getCandidates(copy, row, col)
                        for (num in candidates) {
                            val reason = if (candidates.size == 1) {
                                "Naked single: only $num can be placed in cell (${row + 1}, ${col + 1})"
                            } else {
                                "Constraint check: testing $num at cell (${row + 1}, ${col + 1})"
                            }
                            copy[row][col] = num
                            steps.add(SolverStep(row, col, num, reason, false))
                            if (solveRecursive()) return true
                            copy[row][col] = 0
                            steps.add(SolverStep(row, col, 0, "Conflict encountered, backtracking from cell (${row + 1}, ${col + 1})", true))
                        }
                        return false
                    }
                }
            }
            return true
        }

        solveRecursive()
        return steps
    }
}
