package com.example

import com.example.algorithms.Game2048Algorithm
import com.example.algorithms.MinesweeperAlgorithm
import com.example.algorithms.NQueensAlgorithm
import com.example.algorithms.SlidingPuzzleAlgorithm
import com.example.algorithms.SudokuAlgorithm
import com.example.algorithms.WordSearchAlgorithm
import com.example.data.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleAlgorithmUnitTest {

    @Test
    fun testSudokuGenerationAndSolving() {
        val puzzle = SudokuAlgorithm.generatePuzzle(Difficulty.EASY)
        assertNotNull(puzzle)
        assertEquals(9, puzzle.initial.size)
        assertEquals(9, puzzle.solution.size)

        // Verify solution is valid
        for (r in 0 until 9) {
            val seenRow = mutableSetOf<Int>()
            for (c in 0 until 9) {
                val num = puzzle.solution[r][c]
                assertTrue(num in 1..9)
                assertFalse(seenRow.contains(num))
                seenRow.add(num)
            }
        }
    }

    @Test
    fun testGame2048Movement() {
        val grid = Game2048Algorithm.createEmptyGrid()
        grid[0][0] = 2
        grid[0][1] = 2

        val result = Game2048Algorithm.move(grid, Game2048Algorithm.Direction.LEFT)
        assertTrue(result.hasChanged)
        assertEquals(4, result.grid[0][0])
        assertEquals(0, result.grid[0][1])
        assertEquals(4, result.scoreDelta)
    }

    @Test
    fun testSlidingPuzzleSolvabilityAndMoves() {
        val board = SlidingPuzzleAlgorithm.createSolvedBoard(3)
        assertTrue(SlidingPuzzleAlgorithm.isSolved(board, 3))
        assertTrue(SlidingPuzzleAlgorithm.isSolvable(board, 3))

        val shuffled = SlidingPuzzleAlgorithm.generateSolvableBoard(3, 20)
        assertTrue(SlidingPuzzleAlgorithm.isSolvable(shuffled, 3))
    }

    @Test
    fun testMinesweeperInitialization() {
        val config = MinesweeperAlgorithm.getConfig(Difficulty.EASY)
        assertEquals(8, config.rows)
        assertEquals(8, config.cols)
        assertEquals(10, config.mines)

        val board = MinesweeperAlgorithm.initBoard(config.rows, config.cols)
        MinesweeperAlgorithm.placeMines(board, config.rows, config.cols, config.mines, 0, 0)
        assertFalse(board[0][0].hasMine) // First click safe
    }

    @Test
    fun testWordSearchCoordinates() {
        val line = WordSearchAlgorithm.getLineCoordinates(0, 0, 0, 4)
        assertEquals(5, line.size)
        assertEquals(Pair(0, 0), line.first())
        assertEquals(Pair(0, 4), line.last())
    }

    @Test
    fun testNQueensBacktracking() {
        val steps = NQueensAlgorithm.recordBacktrackingSteps(4)
        assertTrue(steps.isNotEmpty())
        val hasSolution = steps.any { it.type == NQueensAlgorithm.StepType.SOLUTION_FOUND }
        assertTrue(hasSolution)

        val solution8 = NQueensAlgorithm.findOneSolution(8)
        assertEquals(8, solution8.size)
        val conflicts = NQueensAlgorithm.countConflicts(solution8)
        assertEquals(0, conflicts.size)
    }
}
