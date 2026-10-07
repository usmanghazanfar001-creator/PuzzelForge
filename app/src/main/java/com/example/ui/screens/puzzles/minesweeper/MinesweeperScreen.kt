package com.example.ui.screens.puzzles.minesweeper

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithms.MinesweeperAlgorithm
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
fun MinesweeperScreen(
    storage: PuzzleStorageRepository,
    difficulty: Difficulty = Difficulty.EASY,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    val config = remember(difficulty) { MinesweeperAlgorithm.getConfig(difficulty) }
    var board by remember { mutableStateOf(MinesweeperAlgorithm.initBoard(config.rows, config.cols)) }
    var isFirstClick by remember { mutableStateOf(true) }
    var isFlagMode by remember { mutableStateOf(false) }
    var flagCount by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var activeHint by remember { mutableStateOf<String?>(null) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var soundEnabled by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var completionResult by remember { mutableStateOf<GameResult?>(null) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSeconds++
        }
    }

    fun handleCellClick(r: Int, c: Int) {
        if (isGameOver || hasWon) return

        if (isFirstClick) {
            MinesweeperAlgorithm.placeMines(board, config.rows, config.cols, config.mines, r, c)
            isFirstClick = false
            isTimerRunning = true
        }

        val cell = board[r][c]

        if (isFlagMode) {
            if (!cell.isRevealed) {
                cell.isFlagged = !cell.isFlagged
                flagCount = if (cell.isFlagged) flagCount + 1 else flagCount - 1
                board = Array(config.rows) { row -> board[row].clone() }
                SoundManager.playClick()
                SoundManager.triggerHaptic(context, 1)
            }
            return
        }

        if (cell.isFlagged || cell.isRevealed) return

        val hitMine = MinesweeperAlgorithm.revealCell(board, config.rows, config.cols, r, c)
        board = Array(config.rows) { row -> board[row].clone() }

        if (hitMine) {
            // Game Over
            isGameOver = true
            isTimerRunning = false
            // Reveal all mines
            for (row in 0 until config.rows) {
                for (col in 0 until config.cols) {
                    if (board[row][col].hasMine) board[row][col].isRevealed = true
                }
            }
            SoundManager.playError()
            SoundManager.triggerHaptic(context, 3)

            val result = GameResult(
                gameType = GameType.MINESWEEPER,
                difficulty = difficulty,
                score = maxOf(50, (config.rows * config.cols - config.mines) * 10),
                timeSeconds = timeSeconds,
                moves = flagCount,
                mistakes = 1,
                hintsUsed = hintsUsed,
                isWin = false,
                xpEarned = 50
            )
            storage.recordGameResult(result)
            completionResult = result
        } else {
            SoundManager.playMove()
            SoundManager.triggerHaptic(context, 1)

            if (MinesweeperAlgorithm.checkWin(board, config.rows, config.cols)) {
                hasWon = true
                isTimerRunning = false
                val score = maxOf(300, (config.mines * 150) + (300 - timeSeconds) * 2 - (hintsUsed * 100))
                val result = GameResult(
                    gameType = GameType.MINESWEEPER,
                    difficulty = difficulty,
                    score = score,
                    timeSeconds = timeSeconds,
                    moves = flagCount,
                    mistakes = 0,
                    hintsUsed = hintsUsed,
                    isWin = true,
                    xpEarned = 350
                )
                storage.recordGameResult(result)
                SoundManager.playWin()
                SoundManager.triggerHaptic(context, 3)
                completionResult = result
            }
        }
    }

    fun restartGame() {
        board = MinesweeperAlgorithm.initBoard(config.rows, config.cols)
        isFirstClick = true
        isFlagMode = false
        flagCount = 0
        isGameOver = false
        hasWon = false
        timeSeconds = 0
        isTimerRunning = false
        activeHint = null
        hintsUsed = 0
        completionResult = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        GameHeaderBar(
            title = "Minesweeper",
            score = maxOf(0, (config.mines - flagCount) * 100 + (timeSeconds * 2)),
            timeSeconds = timeSeconds,
            hintsRemaining = 3 - hintsUsed,
            onBack = onNavigateBack,
            onRestart = { restartGame() },
            onHint = {
                if (!isFirstClick) {
                    val hint = MinesweeperAlgorithm.getIntelligentHint(board, config.rows, config.cols)
                    if (hint != null) {
                        activeHint = hint.second
                        hintsUsed++
                        SoundManager.playSuccess()
                    } else {
                        activeHint = "Scan area carefully: check cells where flagged mines match adjacent count."
                    }
                } else {
                    activeHint = "Tap any cell to make a guaranteed-safe opening move!"
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                SoundManager.isSoundEnabled = soundEnabled
            }
        )

        IntelligentHintCard(hintText = activeHint, onDismiss = { activeHint = null })

        // Controls bar: Remaining mines counter, Face status, Mode toggle
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
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pin,
                        contentDescription = "Mines",
                        tint = NeonRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${config.mines - flagCount}",
                        color = NeonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Face status button
            IconButton(
                onClick = { restartGame() },
                modifier = Modifier
                    .size(42.dp)
                    .background(DarkSurfaceCard, CircleShape)
                    .border(1.dp, AmberGold, CircleShape)
                    .testTag("minesweeper_face")
            ) {
                Text(
                    text = when {
                        hasWon -> "😎"
                        isGameOver -> "😵"
                        isFirstClick -> "🙂"
                        else -> "🧐"
                    },
                    fontSize = 20.sp
                )
            }

            // Dig / Flag Mode Switch
            Button(
                onClick = {
                    isFlagMode = !isFlagMode
                    SoundManager.playClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFlagMode) AmberGold else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isFlagMode) AmberGold else DarkBorder),
                modifier = Modifier.testTag("mode_toggle")
            ) {
                Icon(
                    imageVector = if (isFlagMode) Icons.Default.Flag else Icons.Default.Shield,
                    contentDescription = "Mode",
                    tint = if (isFlagMode) Color.Black else NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFlagMode) "FLAG" else "DIG",
                    color = if (isFlagMode) Color.Black else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Minesweeper Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("minesweeper_grid"),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                for (r in 0 until config.rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until config.cols) {
                            val cell = board[r][c]
                            val cellBg = when {
                                cell.isRevealed && cell.hasMine -> NeonRed.copy(alpha = 0.8f)
                                cell.isRevealed -> Color(0xFF1E293B)
                                cell.isFlagged -> AmberGold.copy(alpha = 0.25f)
                                else -> Color(0xFF334155)
                            }

                            val numColor = when (cell.adjacentMines) {
                                1 -> NeonCyan
                                2 -> NeonGreen
                                3 -> NeonRed
                                4 -> ElectricPurple
                                5 -> AmberGold
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellBg)
                                    .clickable { handleCellClick(r, c) }
                                    .testTag("cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    cell.isRevealed && cell.hasMine -> {
                                        Text("💣", fontSize = 14.sp)
                                    }
                                    cell.isFlagged -> {
                                        Text("🚩", fontSize = 14.sp)
                                    }
                                    cell.isRevealed && cell.adjacentMines > 0 -> {
                                        Text(
                                            text = "${cell.adjacentMines}",
                                            color = numColor,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
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
