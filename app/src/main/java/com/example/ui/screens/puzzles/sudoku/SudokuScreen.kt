package com.example.ui.screens.puzzles.sudoku

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithms.SudokuAlgorithm
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
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SudokuScreen(
    storage: PuzzleStorageRepository,
    difficulty: Difficulty = Difficulty.MEDIUM,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var puzzle by remember { mutableStateOf(SudokuAlgorithm.generatePuzzle(difficulty)) }
    var currentGrid by remember { mutableStateOf(Array(9) { r -> puzzle.initial[r].clone() }) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var activeHint by remember { mutableStateOf<String?>(null) }
    var soundEnabled by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var notesMode by remember { mutableStateOf(false) }
    var gameCompletedResult by remember { mutableStateOf<GameResult?>(null) }

    // Timer loop
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSeconds++
        }
    }

    fun checkWinCondition(): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (currentGrid[r][c] != puzzle.solution[r][c]) return false
            }
        }
        return true
    }

    fun finishGame(isWin: Boolean) {
        isTimerRunning = false
        val base = difficulty.baseScore
        val speedBonus = maxOf(0, (600 - timeSeconds) * 2)
        val mistakePenalty = mistakes * 100
        val hintPenalty = hintsUsed * 150
        val finalScore = maxOf(200, (base * difficulty.multiplier).toInt() + speedBonus - mistakePenalty - hintPenalty)
        val xpEarned = if (isWin) 250 + (finalScore / 10) else 50

        val result = GameResult(
            gameType = GameType.SUDOKU,
            difficulty = difficulty,
            score = finalScore,
            timeSeconds = timeSeconds,
            moves = 81,
            mistakes = mistakes,
            hintsUsed = hintsUsed,
            isWin = isWin,
            xpEarned = xpEarned
        )
        storage.recordGameResult(result)
        gameCompletedResult = result
        if (isWin) {
            SoundManager.playWin()
            SoundManager.triggerHaptic(context, 3)
        }
    }

    fun handleNumberInput(num: Int) {
        val (r, c) = selectedCell ?: return
        if (puzzle.initial[r][c] != 0) return // Fixed initial clue

        if (num == 0) {
            // Erase
            val newGrid = Array(9) { row -> currentGrid[row].clone() }
            newGrid[r][c] = 0
            currentGrid = newGrid
            SoundManager.playMove()
            return
        }

        val expected = puzzle.solution[r][c]
        val newGrid = Array(9) { row -> currentGrid[row].clone() }
        newGrid[r][c] = num
        currentGrid = newGrid

        if (num == expected) {
            SoundManager.playSuccess()
            SoundManager.triggerHaptic(context, 1)
            if (checkWinCondition()) {
                finishGame(true)
            }
        } else {
            mistakes++
            SoundManager.playError()
            SoundManager.triggerHaptic(context, 2)
            if (mistakes >= 3) {
                // Warning on mistakes
                activeHint = "Watch out: 3 mistakes accumulated! Double-check rows and columns."
            }
        }
    }

    fun restartPuzzle() {
        currentGrid = Array(9) { r -> puzzle.initial[r].clone() }
        selectedCell = null
        mistakes = 0
        hintsUsed = 0
        timeSeconds = 0
        isTimerRunning = true
        activeHint = null
        gameCompletedResult = null
    }

    fun nextPuzzle() {
        puzzle = SudokuAlgorithm.generatePuzzle(difficulty)
        restartPuzzle()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        GameHeaderBar(
            title = "Sudoku",
            score = maxOf(0, 1000 - (mistakes * 100) - (hintsUsed * 150) + (timeSeconds / 5)),
            timeSeconds = timeSeconds,
            hintsRemaining = 3 - hintsUsed,
            onBack = onNavigateBack,
            onRestart = { restartPuzzle() },
            onHint = {
                if (hintsUsed < 3) {
                    val hint = SudokuAlgorithm.getIntelligentHint(currentGrid, puzzle.solution)
                    if (hint != null) {
                        selectedCell = hint.first
                        activeHint = hint.second
                        hintsUsed++
                        SoundManager.playSuccess()
                    } else {
                        activeHint = "Board is progressing well! No obvious single candidate found."
                    }
                } else {
                    activeHint = "No hints remaining for this round!"
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                SoundManager.isSoundEnabled = soundEnabled
            }
        )

        IntelligentHintCard(
            hintText = activeHint,
            onDismiss = { activeHint = null }
        )

        // Difficulty tag & mistakes counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Difficulty: ${difficulty.label}",
                color = NeonCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = "Mistakes: $mistakes / 3",
                color = if (mistakes > 0) NeonRed else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 9x9 Sudoku Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("sudoku_grid"),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until 9) {
                            val value = currentGrid[r][c]
                            val isInitial = puzzle.initial[r][c] != 0
                            val isSelected = selectedCell?.first == r && selectedCell?.second == c
                            val inSelectedRowOrCol = selectedCell?.first == r || selectedCell?.second == c
                            val sameNumber = selectedCell?.let { (sr, sc) ->
                                val selVal = currentGrid[sr][sc]
                                selVal != 0 && selVal == value
                            } ?: false
                            val isWrong = value != 0 && !isInitial && value != puzzle.solution[r][c]

                            val cellBg = when {
                                isSelected -> NeonCyan.copy(alpha = 0.35f)
                                isWrong -> NeonRed.copy(alpha = 0.3f)
                                sameNumber -> AmberGold.copy(alpha = 0.25f)
                                inSelectedRowOrCol -> ElectricPurple.copy(alpha = 0.12f)
                                else -> Color.Transparent
                            }

                            val borderRight = if ((c + 1) % 3 == 0 && c != 8) 2.dp else 0.5.dp
                            val borderBottom = if ((r + 1) % 3 == 0 && r != 8) 2.dp else 0.5.dp

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(cellBg)
                                    .border(borderRight, DarkBorder)
                                    .clickable {
                                        selectedCell = Pair(r, c)
                                        SoundManager.playClick()
                                    }
                                    .testTag("cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = "$value",
                                        color = when {
                                            isWrong -> NeonRed
                                            isInitial -> TextPrimary
                                            else -> NeonCyan
                                        },
                                        fontWeight = if (isInitial) FontWeight.ExtraBold else FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Number Pad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (num in 1..5) {
                    Button(
                        onClick = { handleNumberInput(num) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("numpad_$num"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = "$num",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (num in 6..9) {
                    Button(
                        onClick = { handleNumberInput(num) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("numpad_$num"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = "$num",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Erase Button
                Button(
                    onClick = { handleNumberInput(0) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("erase_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF371B1B)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Erase",
                        tint = NeonRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Actions: Auto-Solve Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Solve automatically
                        currentGrid = Array(9) { r -> puzzle.solution[r].clone() }
                        finishGame(true)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("solve_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Solve",
                        tint = ElectricPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto Solve", color = ElectricPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    gameCompletedResult?.let { result ->
        CompletionVictoryDialog(
            result = result,
            onNextPuzzle = { nextPuzzle() },
            onPlayAgain = { restartPuzzle() },
            onBackToPuzzles = onNavigateBack
        )
    }
}
