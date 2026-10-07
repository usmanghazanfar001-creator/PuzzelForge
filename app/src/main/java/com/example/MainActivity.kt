package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.model.Difficulty
import com.example.data.model.GameType
import com.example.data.storage.PuzzleStorageRepository
import com.example.data.storage.SharedPreferencesPuzzleStorage
import com.example.ui.screens.airace.AiRaceScreen
import com.example.ui.screens.daily.DailyChallengeScreen
import com.example.ui.screens.dashboard.HomeScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.puzzles.game2048.Game2048Screen
import com.example.ui.screens.puzzles.minesweeper.MinesweeperScreen
import com.example.ui.screens.puzzles.nqueens.NQueensVisualizerScreen
import com.example.ui.screens.puzzles.queens.Queens8Screen
import com.example.ui.screens.puzzles.sliding.SlidingPuzzleScreen
import com.example.ui.screens.puzzles.sudoku.SudokuScreen
import com.example.ui.screens.puzzles.sudokusolver.SudokuSolverScreen
import com.example.ui.screens.puzzles.wordsearch.WordSearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class AppNavScreen {
    object Home : AppNavScreen()
    object Daily : AppNavScreen()
    object AiRace : AppNavScreen()
    object Leaderboard : AppNavScreen()
    object Profile : AppNavScreen()
    object Settings : AppNavScreen()
    data class PlayGame(val gameType: GameType, val difficulty: Difficulty = Difficulty.MEDIUM) : AppNavScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val storage = remember { SharedPreferencesPuzzleStorage(context) }
                PuzzleForgeApp(storage = storage)
            }
        }
    }
}

@Composable
fun PuzzleForgeApp(storage: PuzzleStorageRepository) {
    var currentScreen by remember { mutableStateOf<AppNavScreen>(AppNavScreen.Home) }

    val isTopLevelScreen = currentScreen is AppNavScreen.Home ||
            currentScreen is AppNavScreen.Daily ||
            currentScreen is AppNavScreen.AiRace ||
            currentScreen is AppNavScreen.Leaderboard ||
            currentScreen is AppNavScreen.Profile ||
            currentScreen is AppNavScreen.Settings

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = DarkSurfaceCard,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        NavTabItem("Home", Icons.Default.Home, AppNavScreen.Home),
                        NavTabItem("Daily", Icons.Default.LocalFireDepartment, AppNavScreen.Daily),
                        NavTabItem("AI Race", Icons.Default.Bolt, AppNavScreen.AiRace),
                        NavTabItem("Ranks", Icons.Default.EmojiEvents, AppNavScreen.Leaderboard),
                        NavTabItem("Profile", Icons.Default.Person, AppNavScreen.Profile),
                        NavTabItem("Settings", Icons.Default.Settings, AppNavScreen.Settings)
                    )

                    navItems.forEach { item ->
                        val isSelected = currentScreen::class == item.screen::class
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentScreen != item.screen) {
                                    currentScreen = item.screen
                                    SoundManager.playClick()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) NeonCyan else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    color = if (isSelected) NeonCyan else TextMuted,
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF00384D)
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.label.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    is AppNavScreen.Home -> {
                        HomeScreen(
                            storage = storage,
                            onLaunchGame = { gameType ->
                                currentScreen = AppNavScreen.PlayGame(gameType)
                            },
                            onNavigateDaily = { currentScreen = AppNavScreen.Daily },
                            onNavigateAiRace = { currentScreen = AppNavScreen.AiRace },
                            onNavigateProfile = { currentScreen = AppNavScreen.Profile }
                        )
                    }

                    is AppNavScreen.Daily -> {
                        DailyChallengeScreen(
                            storage = storage,
                            onNavigateBack = { currentScreen = AppNavScreen.Home },
                            onPlayDailyGame = { gameType ->
                                currentScreen = AppNavScreen.PlayGame(gameType, Difficulty.HARD)
                            }
                        )
                    }

                    is AppNavScreen.AiRace -> {
                        AiRaceScreen(
                            storage = storage,
                            onNavigateBack = { currentScreen = AppNavScreen.Home },
                            onLaunchGame = { gameType ->
                                currentScreen = AppNavScreen.PlayGame(gameType)
                            }
                        )
                    }

                    is AppNavScreen.Leaderboard -> {
                        LeaderboardScreen(
                            storage = storage,
                            onNavigateBack = { currentScreen = AppNavScreen.Home }
                        )
                    }

                    is AppNavScreen.Profile -> {
                        ProfileScreen(
                            storage = storage,
                            onNavigateBack = { currentScreen = AppNavScreen.Home }
                        )
                    }

                    is AppNavScreen.Settings -> {
                        SettingsScreen(
                            storage = storage,
                            onNavigateBack = { currentScreen = AppNavScreen.Home }
                        )
                    }

                    is AppNavScreen.PlayGame -> {
                        when (targetScreen.gameType) {
                            GameType.SUDOKU -> SudokuScreen(
                                storage = storage,
                                difficulty = targetScreen.difficulty,
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.GAME_2048 -> Game2048Screen(
                                storage = storage,
                                difficulty = targetScreen.difficulty,
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.MINESWEEPER -> MinesweeperScreen(
                                storage = storage,
                                difficulty = targetScreen.difficulty,
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.SLIDING_PUZZLE -> SlidingPuzzleScreen(
                                storage = storage,
                                difficulty = targetScreen.difficulty,
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.WORD_SEARCH -> WordSearchScreen(
                                storage = storage,
                                difficulty = targetScreen.difficulty,
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.SUDOKU_SOLVER -> SudokuSolverScreen(
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.QUEENS_8 -> Queens8Screen(
                                storage = storage,
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )

                            GameType.N_QUEENS -> NQueensVisualizerScreen(
                                onNavigateBack = { currentScreen = AppNavScreen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavTabItem(
    val label: String,
    val icon: ImageVector,
    val screen: AppNavScreen
)
