package com.example.ui.screens.puzzles.wordsearch

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithms.WordSearchAlgorithm
import com.example.audio.SoundManager
import com.example.data.model.Difficulty
import com.example.data.model.GameResult
import com.example.data.model.GameType
import com.example.data.storage.PuzzleStorageRepository
import com.example.ui.components.CompletionVictoryDialog
import com.example.ui.components.GameHeaderBar
import com.example.ui.components.IntelligentHintCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun WordSearchScreen(
    storage: PuzzleStorageRepository,
    difficulty: Difficulty = Difficulty.MEDIUM,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var game by remember { mutableStateOf(WordSearchAlgorithm.generatePuzzle(10)) }
    var selectedStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedEnd by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var foundPositions by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var foundWords by remember { mutableStateOf(setOf<String>()) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var activeHint by remember { mutableStateOf<String?>(null) }
    var soundEnabled by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var completionResult by remember { mutableStateOf<GameResult?>(null) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSeconds++
        }
    }

    val currentLineCoords = remember(selectedStart, selectedEnd) {
        val s = selectedStart
        val e = selectedEnd
        if (s != null && e != null) {
            WordSearchAlgorithm.getLineCoordinates(s.first, s.second, e.first, e.second)
        } else if (s != null) {
            listOf(s)
        } else {
            emptyList()
        }
    }

    fun handleCellClick(r: Int, c: Int) {
        if (selectedStart == null) {
            selectedStart = Pair(r, c)
            SoundManager.playClick()
        } else if (selectedEnd == null) {
            selectedEnd = Pair(r, c)
            val line = WordSearchAlgorithm.getLineCoordinates(selectedStart!!.first, selectedStart!!.second, r, c)
            if (line.isNotEmpty()) {
                val forwardWord = line.map { game.grid[it.first][it.second] }.joinToString("")
                val reverseWord = forwardWord.reversed()

                val matchedWord = game.placedWords.find {
                    (it.word == forwardWord || it.word == reverseWord) && !foundWords.contains(it.word)
                }

                if (matchedWord != null) {
                    foundWords = foundWords + matchedWord.word
                    foundPositions = foundPositions + matchedWord.positions
                    SoundManager.playSuccess()
                    SoundManager.triggerHaptic(context, 2)

                    if (foundWords.size == game.placedWords.size) {
                        isTimerRunning = false
                        SoundManager.playWin()
                        SoundManager.triggerHaptic(context, 3)

                        val score = maxOf(300, (game.placedWords.size * 200) + (300 - timeSeconds) * 2 - (hintsUsed * 100))
                        val result = GameResult(
                            gameType = GameType.WORD_SEARCH,
                            difficulty = difficulty,
                            score = score,
                            timeSeconds = timeSeconds,
                            moves = foundWords.size,
                            mistakes = 0,
                            hintsUsed = hintsUsed,
                            isWin = true,
                            xpEarned = 300
                        )
                        storage.recordGameResult(result)
                        completionResult = result
                    }
                } else {
                    SoundManager.playMove()
                }
            }
            selectedStart = null
            selectedEnd = null
        }
    }

    fun restartGame() {
        game = WordSearchAlgorithm.generatePuzzle(10)
        selectedStart = null
        selectedEnd = null
        foundPositions = emptySet()
        foundWords = emptySet()
        hintsUsed = 0
        timeSeconds = 0
        isTimerRunning = true
        activeHint = null
        completionResult = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        GameHeaderBar(
            title = "Word Search",
            score = foundWords.size * 250,
            timeSeconds = timeSeconds,
            hintsRemaining = 3 - hintsUsed,
            onBack = onNavigateBack,
            onRestart = { restartGame() },
            onHint = {
                val unFound = game.placedWords.find { !foundWords.contains(it.word) }
                if (unFound != null) {
                    val startPos = unFound.positions.first()
                    activeHint = "Clue for '${unFound.word}': Starts near Row ${startPos.first + 1}, Column ${startPos.second + 1}."
                    hintsUsed++
                    SoundManager.playSuccess()
                } else {
                    activeHint = "All words discovered!"
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                SoundManager.isSoundEnabled = soundEnabled
            }
        )

        IntelligentHintCard(hintText = activeHint, onDismiss = { activeHint = null })

        // Theme and Progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Theme: ${game.theme}",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "Found: ${foundWords.size} / ${game.placedWords.size}",
                color = if (foundWords.size == game.placedWords.size) NeonGreen else AmberGold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Word Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("word_search_grid"),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                for (r in 0 until 10) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until 10) {
                            val char = game.grid[r][c]
                            val isFound = foundPositions.contains(Pair(r, c))
                            val isSelected = currentLineCoords.contains(Pair(r, c))

                            val bg = when {
                                isSelected -> NeonCyan.copy(alpha = 0.5f)
                                isFound -> ElectricPurple.copy(alpha = 0.35f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(bg)
                                    .clickable { handleCellClick(r, c) }
                                    .testTag("ws_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$char",
                                    color = if (isFound) NeonGreen else TextPrimary,
                                    fontWeight = if (isFound || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Words to find list
        Text(
            text = "WORDS TO FIND (Tap Start letter then End letter):",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(game.placedWords) { placed ->
                val isFound = foundWords.contains(placed.word)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFound) Color(0xFF064E3B) else DarkSurfaceCard
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isFound) NeonGreen else DarkBorder)
                ) {
                    Text(
                        text = placed.word,
                        color = if (isFound) NeonGreen else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    completionResult?.let { result ->
        CompletionVictoryDialog(
            result = result,
            onNextPuzzle = { restartGame() },
            onPlayAgain = { restartGame() },
            onBackToPuzzles = onNavigateBack
        )
    }
}
