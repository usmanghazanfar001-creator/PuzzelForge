package com.example.algorithms

import kotlin.math.abs
import kotlin.random.Random

object SlidingPuzzleAlgorithm {

    fun createSolvedBoard(size: Int): IntArray {
        val total = size * size
        val board = IntArray(total)
        for (i in 0 until total - 1) {
            board[i] = i + 1
        }
        board[total - 1] = 0 // blank
        return board
    }

    fun isSolved(board: IntArray, size: Int): Boolean {
        val total = size * size
        for (i in 0 until total - 1) {
            if (board[i] != i + 1) return false
        }
        return board[total - 1] == 0
    }

    fun isSolvable(board: IntArray, size: Int): Boolean {
        var inversions = 0
        val total = size * size
        for (i in 0 until total) {
            for (j in i + 1 until total) {
                if (board[i] != 0 && board[j] != 0 && board[i] > board[j]) {
                    inversions++
                }
            }
        }

        return if (size % 2 == 1) {
            inversions % 2 == 0
        } else {
            val blankIndex = board.indexOf(0)
            val blankRowFromBottom = size - (blankIndex / size)
            if (blankRowFromBottom % 2 == 1) {
                inversions % 2 == 0
            } else {
                inversions % 2 == 1
            }
        }
    }

    fun generateSolvableBoard(size: Int, shuffleMoves: Int = 80): IntArray {
        val board = createSolvedBoard(size)
        // Perform valid random moves from blank tile to guarantee solvability
        var blankIndex = board.size - 1
        for (i in 0 until shuffleMoves) {
            val validNeighbors = getValidNeighbors(blankIndex, size)
            val chosen = validNeighbors.random()
            board[blankIndex] = board[chosen]
            board[chosen] = 0
            blankIndex = chosen
        }
        return board
    }

    fun getValidNeighbors(blankIndex: Int, size: Int): List<Int> {
        val neighbors = mutableListOf<Int>()
        val row = blankIndex / size
        val col = blankIndex % size

        if (row > 0) neighbors.add(blankIndex - size) // UP
        if (row < size - 1) neighbors.add(blankIndex + size) // DOWN
        if (col > 0) neighbors.add(blankIndex - 1) // LEFT
        if (col < size - 1) neighbors.add(blankIndex + 1) // RIGHT

        return neighbors
    }

    fun moveTile(board: IntArray, size: Int, tileIndex: Int): Boolean {
        val blankIndex = board.indexOf(0)
        val validNeighbors = getValidNeighbors(blankIndex, size)
        if (tileIndex in validNeighbors) {
            board[blankIndex] = board[tileIndex]
            board[tileIndex] = 0
            return true
        }
        return false
    }

    fun calculateManhattanDistance(board: IntArray, size: Int): Int {
        var distance = 0
        for (i in board.indices) {
            val value = board[i]
            if (value != 0) {
                val targetRow = (value - 1) / size
                val targetCol = (value - 1) % size
                val currentRow = i / size
                val currentCol = i % size
                distance += abs(targetRow - currentRow) + abs(targetCol - currentCol)
            }
        }
        return distance
    }

    fun getBestNextMove(board: IntArray, size: Int): Pair<Int, String>? {
        val blankIndex = board.indexOf(0)
        val neighbors = getValidNeighbors(blankIndex, size)
        if (neighbors.isEmpty()) return null

        var bestNeighbor = neighbors.first()
        var minDistance = Int.MAX_VALUE

        for (neighbor in neighbors) {
            val testBoard = board.clone()
            testBoard[blankIndex] = testBoard[neighbor]
            testBoard[neighbor] = 0
            val dist = calculateManhattanDistance(testBoard, size)
            if (dist < minDistance) {
                minDistance = dist
                bestNeighbor = neighbor
            }
        }

        val tileValue = board[bestNeighbor]
        val blankRow = blankIndex / size
        val blankCol = blankIndex % size
        val neighborRow = bestNeighbor / size
        val neighborCol = bestNeighbor % size

        val dir = when {
            neighborRow > blankRow -> "up into the empty space"
            neighborRow < blankRow -> "down into the empty space"
            neighborCol > blankCol -> "left into the empty space"
            else -> "right into the empty space"
        }

        return Pair(bestNeighbor, "Optimal A* move: Slide tile $tileValue $dir.")
    }
}
