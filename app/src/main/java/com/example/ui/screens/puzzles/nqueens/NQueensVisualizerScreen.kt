package com.example.ui.screens.puzzles.nqueens

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithms.NQueensAlgorithm
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
fun NQueensVisualizerScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    var nSize by remember { mutableIntStateOf(6) }
    var steps by remember(nSize) { mutableStateOf(NQueensAlgorithm.recordBacktrackingSteps(nSize)) }
    var currentStepIdx by remember(steps) { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeedMs by remember { mutableFloatStateOf(150f) }

    val currentStep = if (steps.isNotEmpty() && currentStepIdx in steps.indices) steps[currentStepIdx] else null

    LaunchedEffect(isPlaying, currentStepIdx, steps) {
        if (isPlaying && steps.isNotEmpty()) {
            if (currentStepIdx < steps.size - 1) {
                delay(playbackSpeedMs.toLong())
                currentStepIdx++
                if (steps[currentStepIdx].type == NQueensAlgorithm.StepType.SOLUTION_FOUND) {
                    isPlaying = false
                    SoundManager.playWin()
                } else if (currentStepIdx % 4 == 0) {
                    SoundManager.playMove()
                }
            } else {
                isPlaying = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Top Bar
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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("nqueens_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "N-Queens Solver",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                }

                IconButton(
                    onClick = {
                        isPlaying = false
                        currentStepIdx = 0
                    },
                    modifier = Modifier.testTag("nqueens_reset")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = AmberGold)
                }
            }
        }

        // N Selection chips: 4, 5, 6, 8, 10
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Size N:", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            listOf(4, 5, 6, 8, 10).forEach { n ->
                Button(
                    onClick = {
                        isPlaying = false
                        nSize = n
                        currentStepIdx = 0
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (nSize == n) NeonCyan else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(width = 44.dp, height = 34.dp)
                ) {
                    Text(
                        text = "$n",
                        color = if (nSize == n) Color.Black else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Explanation text card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (currentStep?.type) {
                    NQueensAlgorithm.StepType.SOLUTION_FOUND -> Color(0xFF064E3B)
                    NQueensAlgorithm.StepType.CONFLICT -> Color(0xFF450A0A)
                    NQueensAlgorithm.StepType.BACKTRACK -> Color(0xFF431407)
                    else -> Color(0xFF1E1B4B)
                }
            ),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = currentStep?.explanation ?: "Backtracking search initialized.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Step ${currentStepIdx + 1} of ${steps.size}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // N-Queens Chessboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("nqueens_board"),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until nSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until nSize) {
                            val isDarkSquare = (r + c) % 2 == 1
                            val hasQueen = currentStep?.boardState?.getOrNull(r) == c
                            val isTestingCell = currentStep?.row == r && currentStep.col == c

                            val cellBg = when {
                                isTestingCell && currentStep?.type == NQueensAlgorithm.StepType.CONFLICT -> NeonRed.copy(alpha = 0.5f)
                                isTestingCell && currentStep?.type == NQueensAlgorithm.StepType.BACKTRACK -> Color(0xFFF97316).copy(alpha = 0.4f)
                                isTestingCell && currentStep?.type == NQueensAlgorithm.StepType.TRY -> NeonCyan.copy(alpha = 0.35f)
                                hasQueen -> NeonGreen.copy(alpha = 0.35f)
                                isDarkSquare -> Color(0xFF1E293B)
                                else -> Color(0xFF334155)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(cellBg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasQueen || (isTestingCell && currentStep?.type == NQueensAlgorithm.StepType.TRY)) {
                                    Text(
                                        text = "♛",
                                        fontSize = if (nSize >= 8) 18.sp else 24.sp,
                                        color = if (hasQueen) AmberGold else NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Speed Slider & Playback Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Speed: ${playbackSpeedMs.toInt()}ms", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = playbackSpeedMs,
                    onValueChange = { playbackSpeedMs = it },
                    valueRange = 30f..400f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                    modifier = Modifier.width(180.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStepIdx > 0) currentStepIdx--
                    },
                    enabled = currentStepIdx > 0
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = if (currentStepIdx > 0) NeonCyan else TextMuted)
                }

                Button(
                    onClick = { isPlaying = !isPlaying },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) AmberGold else NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPlaying) "Pause" else "Run Backtracking", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        if (currentStepIdx < steps.size - 1) currentStepIdx++
                    },
                    enabled = currentStepIdx < steps.size - 1
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = if (currentStepIdx < steps.size - 1) NeonCyan else TextMuted)
                }
            }
        }
    }
}
