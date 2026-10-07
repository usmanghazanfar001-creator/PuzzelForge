package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Achievement
import com.example.data.model.AppSettings
import com.example.data.model.DailyChallengeInfo
import com.example.data.model.Difficulty
import com.example.data.model.GameResult
import com.example.data.model.GameType
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface PuzzleStorageRepository {
    val userProfileFlow: StateFlow<UserProfile>
    val appSettingsFlow: StateFlow<AppSettings>
    val achievementsFlow: StateFlow<List<Achievement>>
    val dailyChallengeFlow: StateFlow<DailyChallengeInfo>

    fun recordGameResult(result: GameResult): Int // returns newly added XP
    fun getBestScore(gameType: GameType, difficulty: Difficulty): Int
    fun updateSettings(settings: AppSettings)
    fun updateUsername(name: String)
    fun updateAvatar(avatarId: Int)
    fun getLeaderboard(tabIndex: Int): List<LeaderboardEntry>
    fun completeDailyChallenge(score: Int)
    fun resetProgress()
}

class SharedPreferencesPuzzleStorage(context: Context) : PuzzleStorageRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("puzzle_forge_prefs", Context.MODE_PRIVATE)

    private val defaultAchievements = listOf(
        Achievement("first_puzzle", "First Step", "Complete your very first puzzle", "emoji_events", 100),
        Achievement("sudoku_master", "Sudoku Master", "Solve a Sudoku on Hard or Expert", "grid_on", 300),
        Achievement("speed_solver", "Speed Demon", "Solve any puzzle under 2 minutes", "speed", 250),
        Achievement("perfect_game", "Flawless Logic", "Complete a puzzle with zero mistakes and no hints", "verified", 400),
        Achievement("puzzles_10", "Puzzle Adept", "Complete 10 total puzzles", "military_tech", 500, maxProgress = 10),
        Achievement("puzzles_50", "Forge Veteran", "Complete 50 total puzzles", "workspace_premium", 1500, maxProgress = 50),
        Achievement("puzzles_100", "Puzzle Grandmaster", "Complete 100 total puzzles", "stars", 3000, maxProgress = 100),
        Achievement("no_hint_hero", "Self Reliant", "Win 5 games consecutively without hints", "psychology", 600, maxProgress = 5),
        Achievement("streak_7", "Unstoppable", "Reach a 7-day play streak", "local_fire_department", 1000, maxProgress = 7),
        Achievement("queens_master", "Royal Tactician", "Solve the 8-Queens puzzle with 0 conflicts", "chess", 350),
        Achievement("minesweeper_expert", "Bomb Squad Elite", "Win Minesweeper on Expert mode", "emergency", 500),
        Achievement("game_2048_champ", "Tile Titan", "Create the 2048 tile in a single game", "dialpad", 700)
    )

    private val _userProfile = MutableStateFlow(loadProfile())
    override val userProfileFlow: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _appSettings = MutableStateFlow(loadSettings())
    override val appSettingsFlow: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _achievements = MutableStateFlow(loadAchievements())
    override val achievementsFlow: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _dailyChallenge = MutableStateFlow(loadDailyChallenge())
    override val dailyChallengeFlow: StateFlow<DailyChallengeInfo> = _dailyChallenge.asStateFlow()

    private fun loadProfile(): UserProfile {
        val username = prefs.getString("username", "ForgeMaster") ?: "ForgeMaster"
        val avatarId = prefs.getInt("avatar_id", 1)
        val level = prefs.getInt("level", 1)
        val xp = prefs.getInt("current_xp", 250)
        val score = prefs.getInt("total_score", 3200)
        val games = prefs.getInt("games_completed", 4)
        val wins = prefs.getInt("games_won", 4)
        val streak = prefs.getInt("current_streak", 3)
        val bestStreak = prefs.getInt("best_streak", 5)
        val favGameStr = prefs.getString("fav_game", GameType.SUDOKU.name) ?: GameType.SUDOKU.name
        val favGame = try { GameType.valueOf(favGameStr) } catch (_: Exception) { GameType.SUDOKU }

        return UserProfile(
            username = username,
            avatarId = avatarId,
            level = level,
            currentXp = xp,
            totalScore = score,
            gamesCompleted = games,
            gamesWon = wins,
            currentStreak = streak,
            bestStreak = bestStreak,
            favoriteGame = favGame
        )
    }

    private fun loadSettings(): AppSettings {
        val sound = prefs.getBoolean("setting_sound", true)
        val haptics = prefs.getBoolean("setting_haptics", true)
        val contrast = prefs.getBoolean("setting_contrast", false)
        val diffName = prefs.getString("setting_diff", Difficulty.MEDIUM.name) ?: Difficulty.MEDIUM.name
        val diff = try { Difficulty.valueOf(diffName) } catch (_: Exception) { Difficulty.MEDIUM }
        val fastAi = prefs.getBoolean("setting_fast_ai", false)
        return AppSettings(sound, haptics, contrast, diff, fastAi)
    }

    private fun loadAchievements(): List<Achievement> {
        return defaultAchievements.map { ach ->
            val unlocked = prefs.getBoolean("ach_unlocked_${ach.id}", false)
            val progress = prefs.getInt("ach_prog_${ach.id}", 0)
            ach.copy(isUnlocked = unlocked, currentProgress = progress)
        }
    }

    private fun loadDailyChallenge(): DailyChallengeInfo {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("daily_date", "")
        val isCompleted = if (savedDate == todayStr) prefs.getBoolean("daily_completed", false) else false

        val dayOfYear = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        val rotationGames = listOf(GameType.SUDOKU, GameType.GAME_2048, GameType.MINESWEEPER, GameType.SLIDING_PUZZLE, GameType.QUEENS_8)
        val game = rotationGames[dayOfYear % rotationGames.size]

        return DailyChallengeInfo(
            dateString = todayStr,
            gameType = game,
            difficulty = Difficulty.HARD,
            title = "Daily ${game.title} Mastery",
            description = "Solve today's featured ${game.title} puzzle on Hard difficulty to claim +500 XP and sustain your streak!",
            xpReward = 500,
            isCompleted = isCompleted,
            bestScore = prefs.getInt("daily_score_${todayStr}", 0)
        )
    }

    override fun getBestScore(gameType: GameType, difficulty: Difficulty): Int {
        return prefs.getInt("best_score_${gameType.name}_${difficulty.name}", 0)
    }

    override fun recordGameResult(result: GameResult): Int {
        val currentBest = getBestScore(result.gameType, result.difficulty)
        if (result.score > currentBest) {
            prefs.edit().putInt("best_score_${result.gameType.name}_${result.difficulty.name}", result.score).apply()
        }

        val profile = _userProfile.value
        val newScore = profile.totalScore + result.score
        val newCompleted = profile.gamesCompleted + 1
        val newWon = if (result.isWin) profile.gamesWon + 1 else profile.gamesWon
        val newXp = profile.currentXp + result.xpEarned
        val newLevel = 1 + (newXp / 1000)

        prefs.edit()
            .putInt("total_score", newScore)
            .putInt("games_completed", newCompleted)
            .putInt("games_won", newWon)
            .putInt("current_xp", newXp)
            .putInt("level", newLevel)
            .apply()

        _userProfile.value = profile.copy(
            totalScore = newScore,
            gamesCompleted = newCompleted,
            gamesWon = newWon,
            currentXp = newXp,
            level = newLevel
        )

        // Check and update achievements
        checkAchievements(result, newCompleted)

        return result.xpEarned
    }

    private fun checkAchievements(result: GameResult, totalCompleted: Int) {
        val updated = _achievements.value.map { ach ->
            var unlocked = ach.isUnlocked
            var prog = ach.currentProgress

            when (ach.id) {
                "first_puzzle" -> if (result.isWin) unlocked = true
                "sudoku_master" -> if (result.gameType == GameType.SUDOKU && (result.difficulty == Difficulty.HARD || result.difficulty == Difficulty.EXPERT) && result.isWin) unlocked = true
                "speed_solver" -> if (result.isWin && result.timeSeconds < 120 && result.timeSeconds > 10) unlocked = true
                "perfect_game" -> if (result.isWin && result.mistakes == 0 && result.hintsUsed == 0) unlocked = true
                "puzzles_10" -> {
                    prog = totalCompleted.coerceAtMost(10)
                    if (prog >= 10) unlocked = true
                }
                "puzzles_50" -> {
                    prog = totalCompleted.coerceAtMost(50)
                    if (prog >= 50) unlocked = true
                }
                "puzzles_100" -> {
                    prog = totalCompleted.coerceAtMost(100)
                    if (prog >= 100) unlocked = true
                }
                "queens_master" -> if (result.gameType == GameType.QUEENS_8 && result.isWin) unlocked = true
                "minesweeper_expert" -> if (result.gameType == GameType.MINESWEEPER && result.difficulty == Difficulty.EXPERT && result.isWin) unlocked = true
                "game_2048_champ" -> if (result.gameType == GameType.GAME_2048 && result.score >= 2048) unlocked = true
            }

            prefs.edit()
                .putBoolean("ach_unlocked_${ach.id}", unlocked)
                .putInt("ach_prog_${ach.id}", prog)
                .apply()

            ach.copy(isUnlocked = unlocked, currentProgress = prog)
        }
        _achievements.value = updated
    }

    override fun updateSettings(settings: AppSettings) {
        prefs.edit()
            .putBoolean("setting_sound", settings.soundEnabled)
            .putBoolean("setting_haptics", settings.hapticsEnabled)
            .putBoolean("setting_contrast", settings.highContrast)
            .putString("setting_diff", settings.defaultDifficulty.name)
            .putBoolean("setting_fast_ai", settings.fastAiSolver)
            .apply()
        _appSettings.value = settings
    }

    override fun updateUsername(name: String) {
        prefs.edit().putString("username", name).apply()
        _userProfile.value = _userProfile.value.copy(username = name)
    }

    override fun updateAvatar(avatarId: Int) {
        prefs.edit().putInt("avatar_id", avatarId).apply()
        _userProfile.value = _userProfile.value.copy(avatarId = avatarId)
    }

    override fun completeDailyChallenge(score: Int) {
        val todayStr = _dailyChallenge.value.dateString
        prefs.edit()
            .putString("daily_date", todayStr)
            .putBoolean("daily_completed", true)
            .putInt("daily_score_${todayStr}", score)
            .apply()

        val curStreak = _userProfile.value.currentStreak + 1
        val bestStreak = maxOf(curStreak, _userProfile.value.bestStreak)
        val newXp = _userProfile.value.currentXp + 500
        val newLevel = 1 + (newXp / 1000)

        prefs.edit()
            .putInt("current_streak", curStreak)
            .putInt("best_streak", bestStreak)
            .putInt("current_xp", newXp)
            .putInt("level", newLevel)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            currentStreak = curStreak,
            bestStreak = bestStreak,
            currentXp = newXp,
            level = newLevel
        )

        _dailyChallenge.value = _dailyChallenge.value.copy(
            isCompleted = true,
            bestScore = score
        )
    }

    override fun getLeaderboard(tabIndex: Int): List<LeaderboardEntry> {
        val user = _userProfile.value
        val baseUsers = listOf(
            LeaderboardEntry(1, "CipherKnight", 48200, 78, 14, false, "Grandmaster"),
            LeaderboardEntry(2, "ApexLogic", 42150, 65, 9, false, "Master"),
            LeaderboardEntry(3, "QuantumSolve", 39800, 59, 12, false, "Master"),
            LeaderboardEntry(4, "NeonMind", 31400, 48, 6, false, "Diamond"),
            LeaderboardEntry(5, user.username, user.totalScore, user.gamesCompleted, user.currentStreak, true, "Gold"),
            LeaderboardEntry(6, "ByteSeeker", 19200, 31, 4, false, "Gold"),
            LeaderboardEntry(7, "PrismRunner", 15800, 24, 2, false, "Silver"),
            LeaderboardEntry(8, "VortexBrain", 12300, 19, 5, false, "Silver"),
            LeaderboardEntry(9, "AlgoRhythm", 9400, 14, 1, false, "Bronze"),
            LeaderboardEntry(10, "LogicWeaver", 7100, 11, 2, false, "Bronze")
        )

        val multiplier = when (tabIndex) {
            1 -> 0.35 // Weekly
            2 -> 0.75 // Monthly
            3 -> 0.5 // Friends
            else -> 1.0 // Global
        }

        return baseUsers.mapIndexed { idx, entry ->
            entry.copy(
                rank = idx + 1,
                score = (entry.score * multiplier).toInt()
            )
        }
    }

    override fun resetProgress() {
        prefs.edit().clear().apply()
        _userProfile.value = loadProfile()
        _achievements.value = loadAchievements()
        _dailyChallenge.value = loadDailyChallenge()
    }
}
