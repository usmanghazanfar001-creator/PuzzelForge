package com.example.ui.screens.airace

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.model.Difficulty
import com.example.data.model.GameResult
import com.example.data.model.GameType
import com.example.data.storage.PuzzleStorageRepository
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
import kotlin.random.Random

@Composable
fun AiRaceScreen(
    storage: PuzzleStorageRepository,
    onNavigateBack: () -> Unit,
    onLaunchGame: (GameType) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var selectedMode by remember { mutableStateOf(GameType.SUDOKU) }
    var isRacing by remember { mutableStateOf(false) }
    var raceEnded by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<String?>(null) }
    var playerProgress by remember { mutableFloatStateOf(0f) }
    var aiProgress by remember { mutableFloatStateOf(0f) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var playerScore by remember { mutableIntStateOf(0) }
    var aiScore by remember { mutableIntStateOf(0) }

    // Race loop
    LaunchedEffect(isRacing) {
        if (isRacing) {
            while (isRacing && !raceEnded) {
                delay(800)
                timeSeconds++
                // AI progresses steadily with occasional variance
                val aiStep = Random.nextFloat() * 0.04f + 0.02f
                aiProgress = (aiProgress + aiStep).coerceAtMost(1f)
                aiScore = (aiProgress * 1500).toInt()

                if (aiProgress >= 1f && playerProgress < 1f) {
                    raceEnded = true
                    isRacing = false
                    winner = "AI"
                    SoundManager.playError()
                    break
                }
            }
        }
    }

    fun startRace() {
        playerProgress = 0f
        aiProgress = 0f
        playerScore = 0
        aiScore = 0
        timeSeconds = 0
        raceEnded = false
        winner = null
        isRacing = true
        SoundManager.playMove()
    }

    fun simulatePlayerMove() {
        if (!isRacing || raceEnded) return
        playerProgress = (playerProgress + 0.12f).coerceAtMost(1f)
        playerScore = (playerProgress * 1600).toInt()
        SoundManager.playSuccess()
        SoundManager.triggerHaptic(context, 1)

        if (playerProgress >= 1f) {
            raceEnded = true
            isRacing = false
            winner = "Player"
            SoundManager.playWin()
            SoundManager.triggerHaptic(context, 3)

            val res = GameResult(
                gameType = selectedMode,
                difficulty = Difficulty.HARD,
                score = playerScore + 500,
                timeSeconds = timeSeconds,
                moves = 10,
                mistakes = 0,
                hintsUsed = 0,
                isWin = true,
                xpEarned = 400
            )
            storage.recordGameResult(res)
        }
    }

    val animatedPlayerProg by animateFloatAsState(targetValue = playerProgress, label = "player_prog")
    val animatedAiProg by animateFloatAsState(targetValue = aiProgress, label = "ai_prog")

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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("ai_race_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI Challenge Arena",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Race",
                    tint = AmberGold,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Mode selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Pair(GameType.SUDOKU, "Sudoku Race"),
                Pair(GameType.GAME_2048, "2048 AI"),
                Pair(GameType.SLIDING_PUZZLE, "Slide Race"),
                Pair(GameType.QUEENS_8, "Queens Race")
            ).forEach { (type, label) ->
                Button(
                    onClick = {
                        if (!isRacing) {
                            selectedMode = type
                            startRace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == type) NeonCyan else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        color = if (selectedMode == type) Color.Black else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Race Track Visualizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Player vs AI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PLAYER", color = NeonCyan, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("Score: $playerScore", color = TextSecondary, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .border(1.dp, AmberGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val mins = timeSeconds / 60
                        val secs = timeSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            color = AmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("AI SOLVER", color = ElectricPurple, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("Score: $aiScore", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Player Lane
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Player Progress", color = TextPrimary, fontSize = 13.sp)
                        Text("${(playerProgress * 100).toInt()}%", color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedPlayerProg },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = NeonCyan,
                        trackColor = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // AI Lane
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI Algorithm Pace", color = TextPrimary, fontSize = 13.sp)
                        Text("${(aiProgress * 100).toInt()}%", color = ElectricPurple, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedAiProg },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = ElectricPurple,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Winner Banner if race ended
        if (raceEnded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (winner == "Player") Color(0xFF064E3B) else Color(0xFF450A0A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (winner == "Player") Icons.Default.EmojiEvents else Icons.Default.Psychology,
                        contentDescription = "Result",
                        tint = if (winner == "Player") AmberGold else NeonRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (winner == "Player") "🏆 VICTORY! You outpaced the AI Solver!" else "AI Solver crossed first!",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (winner == "Player") "+400 XP awarded for exceptional speed!" else "Keep practicing to refine your solve rate.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Race Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isRacing && !raceEnded) {
                Button(
                    onClick = { startRace() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_race_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("START AI RACE", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            } else if (isRacing) {
                Button(
                    onClick = { simulatePlayerMove() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("tap_solve_step_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "Step", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SOLVE NEXT STEP (TAP FAST!)", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { startRace() },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Rematch AI", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onLaunchGame(selectedMode) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple)
                    ) {
                        Text("Play Full Game", color = ElectricPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
