package com.example.algorithms

import com.example.data.model.Difficulty
import kotlin.random.Random

object MinesweeperAlgorithm {

    data class Cell(
        var hasMine: Boolean = false,
        var isRevealed: Boolean = false,
        var isFlagged: Boolean = false,
        var adjacentMines: Int = 0
    )

    data class BoardConfig(
        val rows: Int,
        val cols: Int,
        val mines: Int
    )

    fun getConfig(difficulty: Difficulty): BoardConfig {
        return when (difficulty) {
            Difficulty.EASY -> BoardConfig(8, 8, 10)
            Difficulty.MEDIUM -> BoardConfig(10, 10, 18)
            Difficulty.HARD, Difficulty.EXPERT -> BoardConfig(12, 12, 28)
        }
    }

    fun initBoard(rows: Int, cols: Int): Array<Array<Cell>> {
        return Array(rows) { Array(cols) { Cell() } }
    }

    fun placeMines(board: Array<Array<Cell>>, rows: Int, cols: Int, totalMines: Int, safeRow: Int, safeCol: Int) {
        var placed = 0
        while (placed < totalMines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            // Ensure safe zone around first click
            val isNearSafe = kotlin.math.abs(r - safeRow) <= 1 && kotlin.math.abs(c - safeCol) <= 1
            if (!isNearSafe && !board[r][c].hasMine) {
                board[r][c].hasMine = true
                placed++
            }
        }

        // Calculate adjacent counts
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!board[r][c].hasMine) {
                    var count = 0
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until rows && nc in 0 until cols && board[nr][nc].hasMine) {
                                count++
                            }
                        }
                    }
                    board[r][c].adjacentMines = count
                }
            }
        }
    }

    fun revealCell(board: Array<Array<Cell>>, rows: Int, cols: Int, r: Int, c: Int): Boolean {
        if (r !in 0 until rows || c !in 0 until cols) return false
        val cell = board[r][c]
        if (cell.isRevealed || cell.isFlagged) return false

        cell.isRevealed = true
        if (cell.hasMine) return true // Hit mine!

        // If 0 adjacent mines, flood fill
        if (cell.adjacentMines == 0) {
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(Pair(r, c))

            while (queue.isNotEmpty()) {
                val (curR, curC) = queue.removeFirst()
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = curR + dr
                        val nc = curC + dc
                        if (nr in 0 until rows && nc in 0 until cols) {
                            val neighbor = board[nr][nc]
                            if (!neighbor.isRevealed && !neighbor.isFlagged && !neighbor.hasMine) {
                                neighbor.isRevealed = true
                                if (neighbor.adjacentMines == 0) {
                                    queue.add(Pair(nr, nc))
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    fun checkWin(board: Array<Array<Cell>>, rows: Int, cols: Int): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (!cell.hasMine && !cell.isRevealed) return false
            }
        }
        return true
    }

    fun getIntelligentHint(board: Array<Array<Cell>>, rows: Int, cols: Int): Pair<Pair<Int, Int>, String>? {
        // Find safe cell by constraint satisfaction
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (cell.isRevealed && cell.adjacentMines > 0) {
                    var flagCount = 0
                    val unrevealedNeighbors = mutableListOf<Pair<Int, Int>>()

                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until rows && nc in 0 until cols) {
                                val neighbor = board[nr][nc]
                                if (neighbor.isFlagged) flagCount++
                                else if (!neighbor.isRevealed) unrevealedNeighbors.add(Pair(nr, nc))
                            }
                        }
                    }

                    // If flagged count equals adjacent mines, all remaining unrevealed are SAFE
                    if (flagCount == cell.adjacentMines && unrevealedNeighbors.isNotEmpty()) {
                        val safeTarget = unrevealedNeighbors.first()
                        return Pair(
                            safeTarget,
                            "Cell (${safeTarget.first + 1}, ${safeTarget.second + 1}) is guaranteed SAFE because cell (${r + 1}, ${c + 1}) already has all its $flagCount mines flagged."
                        )
                    }

                    // If unrevealed + flagged equals adjacent mines, all remaining unrevealed are MINES!
                    if (flagCount + unrevealedNeighbors.size == cell.adjacentMines && unrevealedNeighbors.isNotEmpty()) {
                        val mineTarget = unrevealedNeighbors.first()
                        return Pair(
                            mineTarget,
                            "Cell (${mineTarget.first + 1}, ${mineTarget.second + 1}) is a guaranteed MINE. Flag it safely!"
                        )
                    }
                }
            }
        }

        // Fallback: pick any safe unrevealed non-mine cell
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (!cell.isRevealed && !cell.isFlagged && !cell.hasMine) {
                    return Pair(Pair(r, c), "Strategic scan: Cell (${r + 1}, ${c + 1}) is clear.")
                }
            }
        }
        return null
    }
}
