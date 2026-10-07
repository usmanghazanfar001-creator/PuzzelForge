package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.GameType
import com.example.data.storage.PuzzleStorageRepository
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

@Composable
fun HomeScreen(
    storage: PuzzleStorageRepository,
    onLaunchGame: (GameType) -> Unit,
    onNavigateDaily: () -> Unit,
    onNavigateAiRace: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val userProfile by storage.userProfileFlow.collectAsState()
    val dailyChallenge by storage.dailyChallengeFlow.collectAsState()
    val achievements by storage.achievementsFlow.collectAsState()

    val gamesList = listOf(
        GameType.SUDOKU,
        GameType.GAME_2048,
        GameType.MINESWEEPER,
        GameType.SLIDING_PUZZLE,
        GameType.WORD_SEARCH,
        GameType.SUDOKU_SOLVER,
        GameType.QUEENS_8,
        GameType.N_QUEENS
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Bar / Top Identity
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(NeonCyan, ElectricPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PuzzleForge",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "MULTI-LOGIC ENGINE",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                // Player mini profile pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .clickable { onNavigateProfile() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥 ${userProfile.currentStreak}", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lv.${userProfile.level}", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Hero Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("hero_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10192C)),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonCyan, ElectricPurple)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Think. Solve. Master.",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "One playground for puzzles, logic challenges, and intelligent solving.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onLaunchGame(GameType.SUDOKU) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("hero_play_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play Now", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = { onNavigateAiRace() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("hero_ai_race_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple)
                        ) {
                            Text("AI Race Mode", color = ElectricPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Player Quick Stats Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickStatCard("TOTAL SCORE", "${userProfile.totalScore}", AmberGold, Modifier.weight(1f))
                QuickStatCard("STREAK", "🔥 ${userProfile.currentStreak}", Color(0xFFF97316), Modifier.weight(1f))
                QuickStatCard("COMPLETED", "${userProfile.gamesCompleted}", NeonCyan, Modifier.weight(1f))
                QuickStatCard("ACHIEVEMENTS", "${achievements.count { it.isUnlocked }}", NeonGreen, Modifier.weight(1f))
            }
        }

        // Daily Challenge Spotlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateDaily() }
                    .testTag("home_daily_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F36)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF451A03)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("DAILY CHALLENGE", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (dailyChallenge.isCompleted) {
                                Text("• SOLVED", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(dailyChallenge.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("+${dailyChallenge.xpReward} XP Reward", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onNavigateDaily,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (dailyChallenge.isCompleted) "View" else "Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Puzzle Catalog Section Header
        item {
            Text(
                text = "PUZZLE CATALOG",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 8 Puzzle Cards
        items(gamesList) { game ->
            val bestScore = storage.getBestScore(game, Difficulty.MEDIUM)
            PuzzleCatalogCard(
                game = game,
                bestScore = bestScore,
                onPlay = { onLaunchGame(game) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuickStatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun PuzzleCatalogCard(
    game: GameType,
    bestScore: Int,
    onPlay: () -> Unit
) {
    val iconVector: ImageVector = when (game) {
        GameType.SUDOKU -> Icons.Default.GridOn
        GameType.GAME_2048 -> Icons.Default.Dialpad
        GameType.MINESWEEPER -> Icons.Default.Emergency
        GameType.SLIDING_PUZZLE -> Icons.Default.ViewQuilt
        GameType.WORD_SEARCH -> Icons.Default.Search
        GameType.SUDOKU_SOLVER -> Icons.Default.Psychology
        GameType.QUEENS_8 -> Icons.Default.Casino
        GameType.N_QUEENS -> Icons.Default.AutoGraph
    }

    val accentColor = when (game) {
        GameType.SUDOKU -> NeonCyan
        GameType.GAME_2048 -> AmberGold
        GameType.MINESWEEPER -> NeonGreen
        GameType.SLIDING_PUZZLE -> ElectricPurple
        GameType.WORD_SEARCH -> Color(0xFF38BDF8)
        GameType.SUDOKU_SOLVER -> Color(0xFFA855F7)
        GameType.QUEENS_8 -> Color(0xFFF59E0B)
        GameType.N_QUEENS -> Color(0xFFEC4899)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("puzzle_card_${game.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = game.title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(game.category, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                if (bestScore > 0) {
                    Text("Best Score: $bestScore", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("play_btn_${game.name.lowercase()}")
            ) {
                Text(if (game == GameType.SUDOKU_SOLVER || game == GameType.N_QUEENS) "Solve" else "Play", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}
