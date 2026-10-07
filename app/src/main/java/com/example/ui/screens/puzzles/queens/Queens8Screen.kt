package com.example.ui.screens.puzzles.queens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
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
import com.example.algorithms.NQueensAlgorithm
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
fun Queens8Screen(
    storage: PuzzleStorageRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var queens by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var conflicts by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var activeHint by remember { mutableStateOf<String?>(null) }
    var soundEnabled by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var completionResult by remember { mutableStateOf<GameResult?>(null) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSeconds++
        }
    }

    LaunchedEffect(queens) {
        conflicts = NQueensAlgorithm.countConflicts(queens.toList())
        if (queens.size == 8 && conflicts.isEmpty()) {
            isTimerRunning = false
            SoundManager.playWin()
            SoundManager.triggerHaptic(context, 3)

            val score = maxOf(400, 1500 - (timeSeconds * 2) - (hintsUsed * 100))
            val result = GameResult(
                gameType = GameType.QUEENS_8,
                difficulty = Difficulty.MEDIUM,
                score = score,
                timeSeconds = timeSeconds,
                moves = queens.size,
                mistakes = conflicts.size,
                hintsUsed = hintsUsed,
                isWin = true,
                xpEarned = 350
            )
            storage.recordGameResult(result)
            completionResult = result
        }
    }

    fun handleSquareClick(r: Int, c: Int) {
        val target = Pair(r, c)
        if (queens.contains(target)) {
            queens = queens - target
            SoundManager.playClick()
        } else {
            if (queens.size < 8) {
                queens = queens + target
                SoundManager.playMove()
                SoundManager.triggerHaptic(context, 1)
            } else {
                activeHint = "You can only place up to 8 queens on the board!"
            }
        }
    }

    fun resetBoard() {
        queens = emptySet()
        conflicts = emptyList()
        timeSeconds = 0
        hintsUsed = 0
        isTimerRunning = true
        activeHint = null
        completionResult = null
    }

    fun autoSolveDemo() {
        val solution = NQueensAlgorithm.findOneSolution(8)
        queens = solution.toSet()
        activeHint = "Demonstration solution applied with 0 conflicts."
        SoundManager.playSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        GameHeaderBar(
            title = "8 Queens Challenge",
            score = maxOf(0, (queens.size * 150) - (conflicts.size * 80)),
            timeSeconds = timeSeconds,
            hintsRemaining = 3 - hintsUsed,
            onBack = onNavigateBack,
            onRestart = { resetBoard() },
            onHint = {
                val solution = NQueensAlgorithm.findOneSolution(8)
                val missing = solution.find { !queens.contains(it) }
                if (missing != null) {
                    activeHint = "Tactical recommendation: Place a queen at Row ${missing.first + 1}, Column ${missing.second + 1}."
                    hintsUsed++
                    SoundManager.playSuccess()
                } else {
                    activeHint = "No hints needed; all 8 positions placed!"
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                SoundManager.isSoundEnabled = soundEnabled
            }
        )

        IntelligentHintCard(hintText = activeHint, onDismiss = { activeHint = null })

        // Stats Bar: Queens Placed & Conflicts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Queens Placed: ${queens.size} / 8",
                color = if (queens.size == 8 && conflicts.isEmpty()) NeonGreen else NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = "Conflicts: ${conflicts.size / 2}",
                color = if (conflicts.isNotEmpty()) NeonRed else NeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 8x8 Chessboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("chessboard_8queens"),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until 8) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until 8) {
                            val isDarkSquare = (r + c) % 2 == 1
                            val hasQueen = queens.contains(Pair(r, c))
                            val isConflicted = conflicts.contains(Pair(r, c))

                            val squareBg = when {
                                hasQueen && isConflicted -> NeonRed.copy(alpha = 0.5f)
                                hasQueen -> NeonGreen.copy(alpha = 0.35f)
                                isDarkSquare -> Color(0xFF1E293B)
                                else -> Color(0xFF334155)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(squareBg)
                                    .clickable { handleSquareClick(r, c) }
                                    .testTag("chess_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasQueen) {
                                    Text(
                                        text = "♛",
                                        fontSize = 24.sp,
                                        color = if (isConflicted) NeonRed else AmberGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { autoSolveDemo() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = "Demo", tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Show Solution", color = TextPrimary)
            }

            OutlinedButton(
                onClick = { resetBoard() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear Board", color = TextSecondary)
            }
        }
    }

    completionResult?.let { result ->
        CompletionVictoryDialog(
            result = result,
            onNextPuzzle = { resetBoard() },
            onPlayAgain = { resetBoard() },
            onBackToPuzzles = onNavigateBack
        )
    }
}
