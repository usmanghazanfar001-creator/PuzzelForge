package com.example.ui.screens.puzzles.sudokusolver

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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
fun SudokuSolverScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var initialGrid by remember { mutableStateOf(Array(9) { IntArray(9) }) }
    var currentGrid by remember { mutableStateOf(Array(9) { IntArray(9) }) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var solverSteps by remember { mutableStateOf<List<SudokuAlgorithm.SolverStep>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(-1) }
    var isAutoPlaying by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Tap any cell and enter numbers, or load a sample puzzle to solve.") }
    var isSolved by remember { mutableStateOf(false) }

    LaunchedEffect(isAutoPlaying, currentStepIndex) {
        if (isAutoPlaying && currentStepIndex < solverSteps.size - 1) {
            delay(120)
            currentStepIndex++
            val step = solverSteps[currentStepIndex]
            val copy = Array(9) { r -> currentGrid[r].clone() }
            copy[step.row][step.col] = step.value
            currentGrid = copy
            statusMessage = step.explanation
            if (currentStepIndex == solverSteps.size - 1) {
                isAutoPlaying = false
                isSolved = true
                SoundManager.playWin()
            }
        }
    }

    fun loadSamplePuzzle() {
        val sample = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )
        initialGrid = Array(9) { r -> sample[r].clone() }
        currentGrid = Array(9) { r -> sample[r].clone() }
        solverSteps = emptyList()
        currentStepIndex = -1
        isAutoPlaying = false
        isSolved = false
        statusMessage = "Sample puzzle loaded! Tap 'Solve Step-by-Step' or 'Instant Solve'."
        SoundManager.playSuccess()
    }

    fun validateBoard(): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val num = currentGrid[r][c]
                if (num != 0) {
                    currentGrid[r][c] = 0
                    val valid = SudokuAlgorithm.isValidMove(currentGrid, r, c, num)
                    currentGrid[r][c] = num
                    if (!valid) {
                        statusMessage = "Conflict detected at row ${r + 1}, column ${c + 1} with number $num!"
                        SoundManager.playError()
                        return false
                    }
                }
            }
        }
        return true
    }

    fun solveInstantly() {
        if (!validateBoard()) return
        val copy = Array(9) { r -> currentGrid[r].clone() }
        val ok = SudokuAlgorithm.solve(copy)
        if (ok) {
            currentGrid = copy
            isSolved = true
            statusMessage = "Puzzle solved instantly using backtracking & constraint propagation!"
            SoundManager.playWin()
            SoundManager.triggerHaptic(context, 3)
        } else {
            statusMessage = "No solution exists for this configuration."
            SoundManager.playError()
        }
    }

    fun prepareStepSolver() {
        if (!validateBoard()) return
        initialGrid = Array(9) { r -> currentGrid[r].clone() }
        val steps = SudokuAlgorithm.solveWithSteps(currentGrid)
        if (steps.isNotEmpty()) {
            solverSteps = steps
            currentStepIndex = 0
            val step = steps[0]
            val copy = Array(9) { r -> currentGrid[r].clone() }
            copy[step.row][step.col] = step.value
            currentGrid = copy
            statusMessage = step.explanation
            SoundManager.playMove()
        } else {
            statusMessage = "Puzzle is already solved or unsolvable."
        }
    }

    fun stepForward() {
        if (currentStepIndex < solverSteps.size - 1) {
            currentStepIndex++
            val step = solverSteps[currentStepIndex]
            val copy = Array(9) { r -> currentGrid[r].clone() }
            copy[step.row][step.col] = step.value
            currentGrid = copy
            statusMessage = step.explanation
            SoundManager.playMove()
        }
    }

    fun stepBackward() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            // Recompute board up to currentStepIndex
            val copy = Array(9) { r -> initialGrid[r].clone() }
            for (i in 0..currentStepIndex) {
                val s = solverSteps[i]
                copy[s.row][s.col] = s.value
            }
            currentGrid = copy
            statusMessage = solverSteps[currentStepIndex].explanation
            SoundManager.playClick()
        }
    }

    fun resetBoard() {
        initialGrid = Array(9) { IntArray(9) }
        currentGrid = Array(9) { IntArray(9) }
        solverSteps = emptyList()
        currentStepIndex = -1
        isAutoPlaying = false
        isSolved = false
        selectedCell = null
        statusMessage = "Board reset. Enter numbers or load sample."
        SoundManager.playClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("solver_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sudoku Solver",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = { loadSamplePuzzle() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("load_sample_button")
                    ) {
                        Text("Sample", fontSize = 11.sp, color = AmberGold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { resetBoard() }, modifier = Modifier.testTag("reset_solver_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextSecondary)
                    }
                }
            }
        }

        // Reasoning / status message card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.5f))
        ) {
            Text(
                text = statusMessage,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 9x9 Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("sudoku_solver_grid"),
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
                            val isInitial = initialGrid[r][c] != 0
                            val isSelected = selectedCell?.first == r && selectedCell?.second == c
                            val isCurrentStepCell = solverSteps.isNotEmpty() && currentStepIndex in solverSteps.indices &&
                                    solverSteps[currentStepIndex].row == r && solverSteps[currentStepIndex].col == c

                            val cellBg = when {
                                isCurrentStepCell -> AmberGold.copy(alpha = 0.4f)
                                isSelected -> NeonCyan.copy(alpha = 0.35f)
                                isInitial -> Color(0xFF1E293B)
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
                                    .testTag("solver_cell_${r}_${c}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = "$value",
                                        color = if (isInitial) TextPrimary else NeonCyan,
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

        Spacer(modifier = Modifier.height(8.dp))

        // Step Playback Controls if solver steps prepared
        if (solverSteps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { stepBackward() },
                    enabled = currentStepIndex > 0
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = if (currentStepIndex > 0) NeonCyan else TextMuted)
                }

                Button(
                    onClick = { isAutoPlaying = !isAutoPlaying },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAutoPlaying) AmberGold else NeonCyan)
                ) {
                    Icon(
                        imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAutoPlaying) "Pause" else "Auto Play", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { stepForward() },
                    enabled = currentStepIndex < solverSteps.size - 1
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = if (currentStepIndex < solverSteps.size - 1) NeonCyan else TextMuted)
                }
            }
        } else {
            // Instant & Step-by-Step trigger buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { prepareStepSolver() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Step-by-Step", color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { solveInstantly() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Instant Solve", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Number Input for manual entry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (num in 1..9) {
                Button(
                    onClick = {
                        val (r, c) = selectedCell ?: return@Button
                        val copy = Array(9) { row -> currentGrid[row].clone() }
                        copy[r][c] = num
                        currentGrid = copy
                        SoundManager.playClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Text("$num", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            IconButton(
                onClick = {
                    val (r, c) = selectedCell ?: return@IconButton
                    val copy = Array(9) { row -> currentGrid[row].clone() }
                    copy[r][c] = 0
                    currentGrid = copy
                    SoundManager.playClick()
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF371B1B), RoundedCornerShape(6.dp))
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Erase", tint = NeonRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}
