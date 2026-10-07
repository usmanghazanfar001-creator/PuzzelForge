package com.example.data.model

enum class GameType(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val category: String,
    val estimatedMinutes: Int
) {
    SUDOKU("Sudoku", "Classic 9x9 logic grid challenge", "grid_on", "Logic Grid", 8),
    GAME_2048("2048", "Combine numbers to reach 2048", "dialpad", "Mathematical", 5),
    MINESWEEPER("Minesweeper", "Flag mines and uncover safe terrain", "emergency", "Deduction", 6),
    SLIDING_PUZZLE("Sliding Puzzle", "Slide numbered tiles into order", "view_quilt", "Spatial", 4),
    WORD_SEARCH("Word Search", "Locate hidden words across directions", "search", "Vocabulary", 5),
    SUDOKU_SOLVER("Sudoku Solver", "Interactive step-by-step logic solver", "psychology", "Algorithmic", 2),
    QUEENS_8("8 Queens", "Place 8 non-attacking queens", "chess", "Backtracking", 5),
    N_QUEENS("N-Queens Solver", "Visualize backtracking algorithm live", "auto_graph", "Algorithmic", 3)
}

enum class Difficulty(val label: String, val multiplier: Float, val baseScore: Int) {
    EASY("Easy", 1.0f, 1000),
    MEDIUM("Medium", 1.5f, 1800),
    HARD("Hard", 2.0f, 2800),
    EXPERT("Expert", 2.8f, 4000)
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1
)

data class UserProfile(
    val username: String = "ForgeMaster",
    val avatarId: Int = 1,
    val level: Int = 1,
    val currentXp: Int = 450,
    val totalScore: Int = 12450,
    val gamesCompleted: Int = 18,
    val gamesWon: Int = 15,
    val currentStreak: Int = 5,
    val bestStreak: Int = 12,
    val favoriteGame: GameType = GameType.SUDOKU
) {
    val xpForNextLevel: Int = level * 1000
    val levelProgress: Float = (currentXp % 1000) / 1000f
    val winRate: Int = if (gamesCompleted > 0) (gamesWon * 100) / gamesCompleted else 0
}

data class GameResult(
    val gameType: GameType,
    val difficulty: Difficulty,
    val score: Int,
    val timeSeconds: Int,
    val moves: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val isWin: Boolean,
    val xpEarned: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val score: Int,
    val puzzlesSolved: Int,
    val streak: Int,
    val isUser: Boolean = false,
    val badge: String = "Novice"
)

data class DailyChallengeInfo(
    val dateString: String,
    val gameType: GameType,
    val difficulty: Difficulty,
    val title: String,
    val description: String,
    val xpReward: Int = 500,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0
)

data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val highContrast: Boolean = false,
    val defaultDifficulty: Difficulty = Difficulty.MEDIUM,
    val fastAiSolver: Boolean = false
)
