package com.example.ui.screens.puzzles.game2048

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithms.Game2048Algorithm
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
import com.example.ui.theme.getTileColor
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun Game2048Screen(
    storage: PuzzleStorageRepository,
    difficulty: Difficulty = Difficulty.MEDIUM,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var grid by remember {
        mutableStateOf(
            Game2048Algorithm.createEmptyGrid().apply {
                Game2048Algorithm.spawnTile(this)
                Game2048Algorithm.spawnTile(this)
            }
        )
    }

    var historyStack by remember { mutableStateOf(listOf<Pair<Array<IntArray>, Int>>()) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(storage.getBestScore(GameType.GAME_2048, difficulty)) }
    var moves by remember { mutableIntStateOf(0) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var activeHint by remember { mutableStateOf<String?>(null) }
    var hasWon2048 by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var completionResult by remember { mutableStateOf<GameResult?>(null) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSeconds++
        }
    }

    fun makeMove(dir: Game2048Algorithm.Direction) {
        if (isGameOver) return
        val currentCopy = Array(4) { r -> grid[r].clone() }
        val result = Game2048Algorithm.move(grid, dir)

        if (result.hasChanged) {
            historyStack = (historyStack + Pair(currentCopy, score)).takeLast(10)
            grid = result.grid
            score += result.scoreDelta
            moves++
            if (score > bestScore) {
                bestScore = score
            }

            Game2048Algorithm.spawnTile(grid)
            SoundManager.playMove()
            SoundManager.triggerHaptic(context, 1)

            if (result.reached2048 && !hasWon2048) {
                hasWon2048 = true
                SoundManager.playWin()
                SoundManager.triggerHaptic(context, 3)
                activeHint = "🎉 Victory! You created the 2048 tile! Continue playing to reach 4096!"
            }

            if (Game2048Algorithm.isGameOver(grid)) {
                isGameOver = true
                isTimerRunning = false
                SoundManager.playError()

                val resultObj = GameResult(
                    gameType = GameType.GAME_2048,
                    difficulty = difficulty,
                    score = score,
                    timeSeconds = timeSeconds,
                    moves = moves,
                    mistakes = 0,
                    hintsUsed = hintsUsed,
                    isWin = hasWon2048 || score >= 2048,
                    xpEarned = maxOf(100, score / 10)
                )
                storage.recordGameResult(resultObj)
                completionResult = resultObj
            }
        }
    }

    fun undoMove() {
        if (historyStack.isNotEmpty()) {
            val lastState = historyStack.last()
            historyStack = historyStack.dropLast(1)
            grid = lastState.first
            score = lastState.second
            isGameOver = false
            SoundManager.playClick()
        }
    }

    fun restartGame() {
        grid = Game2048Algorithm.createEmptyGrid().apply {
            Game2048Algorithm.spawnTile(this)
            Game2048Algorithm.spawnTile(this)
        }
        historyStack = emptyList()
        score = 0
        moves = 0
        hintsUsed = 0
        timeSeconds = 0
        isTimerRunning = true
        activeHint = null
        hasWon2048 = false
        isGameOver = false
        completionResult = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        GameHeaderBar(
            title = "2048",
            score = score,
            timeSeconds = timeSeconds,
            hintsRemaining = 3 - hintsUsed,
            onBack = onNavigateBack,
            onRestart = { restartGame() },
            onHint = {
                val best = Game2048Algorithm.getBestMove(grid)
                if (best != null) {
                    activeHint = "AI Expectimax Recommendation: Swipe ${best.name} to maximize corner weight and open spaces."
                    hintsUsed++
                    SoundManager.playSuccess()
                } else {
                    activeHint = "No valid moves remaining!"
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                SoundManager.isSoundEnabled = soundEnabled
            }
        )

        IntelligentHintCard(hintText = activeHint, onDismiss = { activeHint = null })

        // Best Score and Moves Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Text(
                    text = "BEST: $bestScore",
                    color = AmberGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { undoMove() },
                    enabled = historyStack.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (historyStack.isNotEmpty()) ElectricPurple else DarkBorder),
                    modifier = Modifier.testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (historyStack.isNotEmpty()) ElectricPurple else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo", color = if (historyStack.isNotEmpty()) ElectricPurple else TextMuted, fontSize = 12.sp)
                }

                Button(
                    onClick = { restartGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Text("New", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2048 Board with Gesture Support
        var totalDragX = 0f
        var totalDragY = 0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        },
                        onDragEnd = {
                            val threshold = 40f
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX > threshold) makeMove(Game2048Algorithm.Direction.RIGHT)
                                else if (totalDragX < -threshold) makeMove(Game2048Algorithm.Direction.LEFT)
                            } else {
                                if (totalDragY > threshold) makeMove(Game2048Algorithm.Direction.DOWN)
                                else if (totalDragY < -threshold) makeMove(Game2048Algorithm.Direction.UP)
                            }
                        }
                    )
                }
                .testTag("game_2048_grid"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0 until 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0 until 4) {
                            val value = grid[r][c]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (value == 0) Color(0xFF1E293B) else getTileColor(value)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value > 0) {
                                    Text(
                                        text = "$value",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (value > 512) 20.sp else 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // On-Screen D-Pad Controls for accessible gameplay
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { makeMove(Game2048Algorithm.Direction.UP) },
                modifier = Modifier
                    .size(46.dp)
                    .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .testTag("dpad_up")
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = NeonCyan)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { makeMove(Game2048Algorithm.Direction.LEFT) },
                    modifier = Modifier
                        .size(46.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("dpad_left")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = NeonCyan)
                }

                IconButton(
                    onClick = { makeMove(Game2048Algorithm.Direction.DOWN) },
                    modifier = Modifier
                        .size(46.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("dpad_down")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = NeonCyan)
                }

                IconButton(
                    onClick = { makeMove(Game2048Algorithm.Direction.RIGHT) },
                    modifier = Modifier
                        .size(46.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("dpad_right")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = NeonCyan)
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
