package com.example.algorithms

import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

object WordSearchAlgorithm {

    data class PlacedWord(
        val word: String,
        val positions: List<Pair<Int, Int>>,
        var isFound: Boolean = false
    )

    data class WordSearchGame(
        val size: Int,
        val grid: Array<CharArray>,
        val placedWords: List<PlacedWord>,
        val theme: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is WordSearchGame) return false
            return size == other.size &&
                    grid.contentDeepEquals(other.grid) &&
                    placedWords == other.placedWords &&
                    theme == other.theme
        }

        override fun hashCode(): Int {
            var result = size
            result = 31 * result + grid.contentDeepHashCode()
            result = 31 * result + placedWords.hashCode()
            result = 31 * result + theme.hashCode()
            return result
        }
    }

    private val wordBanks = mapOf(
        "Logic & Code" to listOf("ALGORITHM", "BINARY", "KOTLIN", "COMPOSE", "RECURSION", "STACK", "QUEUE", "FORGE", "LOGIC", "SEARCH"),
        "Space & Cosmos" to listOf("GALAXY", "NEBULA", "PULSAR", "ORBIT", "PLANET", "QUASAR", "COSMOS", "COMET", "METEOR", "STELLAR"),
        "Cyber & Matrix" to listOf("CIPHER", "VECTOR", "CRYPTO", "NEURAL", "MATRIX", "FIREWALL", "ROUTER", "QUANTUM", "SIGNAL", "SOCKET")
    )

    private val directions = listOf(
        Pair(0, 1),   // right
        Pair(0, -1),  // left
        Pair(1, 0),   // down
        Pair(-1, 0),  // up
        Pair(1, 1),   // down-right
        Pair(-1, -1), // up-left
        Pair(1, -1),  // down-left
        Pair(-1, 1)   // up-right
    )

    fun generatePuzzle(size: Int = 10, themeName: String? = null): WordSearchGame {
        val theme = themeName ?: wordBanks.keys.random()
        val bank = wordBanks[theme] ?: wordBanks.values.first()
        val wordsToPlace = bank.shuffled().take(6)

        val grid = Array(size) { CharArray(size) { ' ' } }
        val placedList = mutableListOf<PlacedWord>()

        for (word in wordsToPlace) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                attempts++
                val dir = directions.random()
                val dr = dir.first
                val dc = dir.second
                val startR = Random.nextInt(size)
                val startC = Random.nextInt(size)

                val endR = startR + dr * (word.length - 1)
                val endC = startC + dc * (word.length - 1)

                if (endR in 0 until size && endC in 0 until size) {
                    var canFit = true
                    val coords = mutableListOf<Pair<Int, Int>>()
                    for (i in word.indices) {
                        val cr = startR + dr * i
                        val cc = startC + dc * i
                        if (grid[cr][cc] != ' ' && grid[cr][cc] != word[i]) {
                            canFit = false
                            break
                        }
                        coords.add(Pair(cr, cc))
                    }

                    if (canFit) {
                        for (i in word.indices) {
                            val cr = startR + dr * i
                            val cc = startC + dc * i
                            grid[cr][cc] = word[i]
                        }
                        placedList.add(PlacedWord(word, coords))
                        placed = true
                    }
                }
            }
        }

        // Fill remaining spaces with random uppercase letters
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (grid[r][c] == ' ') {
                    grid[r][c] = ('A'..'Z').random()
                }
            }
        }

        return WordSearchGame(size, grid, placedList, theme)
    }

    fun getLineCoordinates(startR: Int, startC: Int, endR: Int, endC: Int): List<Pair<Int, Int>> {
        val dr = endR - startR
        val dc = endC - startC
        val steps = max(abs(dr), abs(dc))
        if (steps == 0) return listOf(Pair(startR, startC))

        // Check if pure horizontal, vertical, or 45-degree diagonal
        val isHorizontal = dr == 0
        val isVertical = dc == 0
        val isDiagonal = abs(dr) == abs(dc)

        if (!isHorizontal && !isVertical && !isDiagonal) return emptyList()

        val stepR = if (dr == 0) 0 else dr / steps
        val stepC = if (dc == 0) 0 else dc / steps

        val coords = mutableListOf<Pair<Int, Int>>()
        for (i in 0..steps) {
            coords.add(Pair(startR + stepR * i, startC + stepC * i))
        }
        return coords
    }
}
