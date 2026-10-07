package com.example.ui.screens.puzzles.sliding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithms.SlidingPuzzleAlgorithm
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
fun SlidingPuzzleScreen(
    storage: PuzzleStorageRepository,
    difficulty: Difficulty = Difficulty.MEDIUM,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var gridSize by remember { mutableIntStateOf(if (difficulty == Difficulty.EASY) 3 else 4) }
    var board by remember(gridSize) { mutableStateOf(SlidingPuzzleAlgorithm.generateSolvableBoard(gridSize)) }
    var moves by remember { mutableIntStateOf(0) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var activeHint by remember { mutableStateOf<String?>(null) }
    var highlightedTile by remember { mutableStateOf<Int?>(null) }
    var soundEnabled by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var completionResult by remember { mutableStateOf<GameResult?>(null) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSeconds++
        }
    }

    fun handleTileClick(index: Int) {
        val moved = SlidingPuzzleAlgorithm.moveTile(board, gridSize, index)
        if (moved) {
            board = board.clone()
            moves++
            highlightedTile = null
            SoundManager.playMove()
            SoundManager.triggerHaptic(context, 1)

            if (SlidingPuzzleAlgorithm.isSolved(board, gridSize)) {
                isTimerRunning = false
                SoundManager.playWin()
                SoundManager.triggerHaptic(context, 3)

                val score = maxOf(300, (gridSize * 500) + (300 - timeSeconds) * 2 - (moves * 5) - (hintsUsed * 100))
                val result = GameResult(
                    gameType = GameType.SLIDING_PUZZLE,
                    difficulty = difficulty,
                    score = score,
                    timeSeconds = timeSeconds,
                    moves = moves,
                    mistakes = 0,
                    hintsUsed = hintsUsed,
                    isWin = true,
                    xpEarned = 300
                )
                storage.recordGameResult(result)
                completionResult = result
            }
        }
    }

    fun shuffleBoard() {
        board = SlidingPuzzleAlgorithm.generateSolvableBoard(gridSize)
        moves = 0
        hintsUsed = 0
        timeSeconds = 0
        isTimerRunning = true
        activeHint = null
        highlightedTile = null
        completionResult = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        GameHeaderBar(
            title = "Sliding Puzzle",
            score = maxOf(0, 1000 - (moves * 10) - (hintsUsed * 100)),
            timeSeconds = timeSeconds,
            hintsRemaining = 3 - hintsUsed,
            onBack = onNavigateBack,
            onRestart = { shuffleBoard() },
            onHint = {
                val hint = SlidingPuzzleAlgorithm.getBestNextMove(board, gridSize)
                if (hint != null) {
                    highlightedTile = hint.first
                    activeHint = hint.second
                    hintsUsed++
                    SoundManager.playSuccess()
                } else {
                    activeHint = "Board is already solved!"
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                SoundManager.isSoundEnabled = soundEnabled
            }
        )

        IntelligentHintCard(hintText = activeHint, onDismiss = { activeHint = null })

        // Grid Size Selector & Moves indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(3, 4, 5).forEach { sz ->
                    Button(
                        onClick = {
                            gridSize = sz
                            shuffleBoard()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gridSize == sz) NeonCyan else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(width = 46.dp, height = 36.dp)
                    ) {
                        Text(
                            text = "${sz}x${sz}",
                            color = if (gridSize == sz) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = "MOVES: $moves",
                color = AmberGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sliding Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(16.dp))
                .testTag("sliding_grid"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (r in 0 until gridSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in 0 until gridSize) {
                            val idx = r * gridSize + c
                            val value = board[idx]
                            val isBlank = value == 0
                            val isHighlighted = highlightedTile == idx

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isBlank -> Color(0xFF0B111E)
                                            isHighlighted -> AmberGold
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                    .border(
                                        width = if (isHighlighted) 2.dp else 1.dp,
                                        color = if (isHighlighted) AmberGold else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = !isBlank) { handleTileClick(idx) }
                                    .testTag("tile_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isBlank) {
                                    Text(
                                        text = "$value",
                                        color = if (isHighlighted) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (gridSize == 5) 18.sp else 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { shuffleBoard() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.testTag("shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Shuffle",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shuffle Board", color = TextPrimary)
            }
        }
    }

    completionResult?.let { result ->
        CompletionVictoryDialog(
            result = result,
            onNextPuzzle = { shuffleBoard() },
            onPlayAgain = { shuffleBoard() },
            onBackToPuzzles = onNavigateBack
        )
    }
}
