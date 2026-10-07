package com.example.algorithms

import kotlin.math.abs
import kotlin.random.Random

object Game2048Algorithm {
    enum class Direction { UP, DOWN, LEFT, RIGHT }

    data class MoveResult(
        val grid: Array<IntArray>,
        val scoreDelta: Int,
        val hasChanged: Boolean,
        val reached2048: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MoveResult) return false
            return grid.contentDeepEquals(other.grid) &&
                    scoreDelta == other.scoreDelta &&
                    hasChanged == other.hasChanged &&
                    reached2048 == other.reached2048
        }

        override fun hashCode(): Int {
            var result = grid.contentDeepHashCode()
            result = 31 * result + scoreDelta
            result = 31 * result + hasChanged.hashCode()
            result = 31 * result + reached2048.hashCode()
            return result
        }
    }

    fun createEmptyGrid(): Array<IntArray> = Array(4) { IntArray(4) }

    fun spawnTile(grid: Array<IntArray>): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (grid[r][c] == 0) emptyCells.add(Pair(r, c))
            }
        }
        if (emptyCells.isEmpty()) return null
        val cell = emptyCells.random()
        val value = if (Random.nextFloat() < 0.9f) 2 else 4
        grid[cell.first][cell.second] = value
        return cell
    }

    fun move(grid: Array<IntArray>, direction: Direction): MoveResult {
        val newGrid = Array(4) { r -> grid[r].clone() }
        var scoreDelta = 0
        var hasChanged = false
        var reached2048 = false

        when (direction) {
            Direction.LEFT -> {
                for (r in 0 until 4) {
                    val row = newGrid[r]
                    val (mergedRow, score, changed) = mergeLine(row)
                    newGrid[r] = mergedRow
                    scoreDelta += score
                    if (changed) hasChanged = true
                }
            }
            Direction.RIGHT -> {
                for (r in 0 until 4) {
                    val row = newGrid[r].reversedArray()
                    val (mergedRow, score, changed) = mergeLine(row)
                    newGrid[r] = mergedRow.reversedArray()
                    scoreDelta += score
                    if (changed) hasChanged = true
                }
            }
            Direction.UP -> {
                for (c in 0 until 4) {
                    val col = IntArray(4) { r -> newGrid[r][c] }
                    val (mergedCol, score, changed) = mergeLine(col)
                    for (r in 0 until 4) newGrid[r][c] = mergedCol[r]
                    scoreDelta += score
                    if (changed) hasChanged = true
                }
            }
            Direction.DOWN -> {
                for (c in 0 until 4) {
                    val col = IntArray(4) { r -> newGrid[3 - r][c] }
                    val (mergedCol, score, changed) = mergeLine(col)
                    for (r in 0 until 4) newGrid[3 - r][c] = mergedCol[r]
                    scoreDelta += score
                    if (changed) hasChanged = true
                }
            }
        }

        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (newGrid[r][c] >= 2048 && grid[r][c] < 2048) {
                    reached2048 = true
                }
            }
        }

        return MoveResult(newGrid, scoreDelta, hasChanged, reached2048)
    }

    private fun mergeLine(line: IntArray): Triple<IntArray, Int, Boolean> {
        val nonZero = line.filter { it != 0 }
        val result = IntArray(4)
        var score = 0
        var writeIdx = 0
        var i = 0

        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val mergedVal = nonZero[i] * 2
                result[writeIdx++] = mergedVal
                score += mergedVal
                i += 2
            } else {
                result[writeIdx++] = nonZero[i]
                i++
            }
        }

        var changed = false
        for (j in 0 until 4) {
            if (line[j] != result[j]) {
                changed = true
                break
            }
        }

        return Triple(result, score, changed)
    }

    fun isGameOver(grid: Array<IntArray>): Boolean {
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (grid[r][c] == 0) return false
                if (c + 1 < 4 && grid[r][c] == grid[r][c + 1]) return false
                if (r + 1 < 4 && grid[r][c] == grid[r + 1][c]) return false
            }
        }
        return true
    }

    // Heuristic AI evaluator: evaluates monotonicity, smoothness, empty tiles, max corner
    fun evaluateGrid(grid: Array<IntArray>): Double {
        var emptyCount = 0
        var smoothness = 0.0
        var maxTile = 0

        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val v = grid[r][c]
                if (v == 0) {
                    emptyCount++
                } else {
                    if (v > maxTile) maxTile = v
                    if (c + 1 < 4 && grid[r][c + 1] != 0) {
                        smoothness -= abs(v - grid[r][c + 1]).toDouble()
                    }
                    if (r + 1 < 4 && grid[r + 1][c] != 0) {
                        smoothness -= abs(v - grid[r + 1][c]).toDouble()
                    }
                }
            }
        }

        // Snake monotonicity weight matrix
        val weights = arrayOf(
            intArrayOf(65536, 32768, 16384, 8192),
            intArrayOf(512, 1024, 2048, 4096),
            intArrayOf(256, 128, 64, 32),
            intArrayOf(2, 4, 8, 16)
        )

        var weightedSum = 0.0
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                weightedSum += grid[r][c] * weights[r][c]
            }
        }

        return weightedSum + (emptyCount * 10000.0) + (smoothness * 2.0)
    }

    fun getBestMove(grid: Array<IntArray>): Direction? {
        var bestDir: Direction? = null
        var bestScore = -Double.MAX_VALUE

        for (dir in Direction.values()) {
            val res = move(grid, dir)
            if (res.hasChanged) {
                val eval = evaluateGrid(res.grid)
                if (eval > bestScore) {
                    bestScore = eval
                    bestDir = dir
                }
            }
        }
        return bestDir
    }
}
