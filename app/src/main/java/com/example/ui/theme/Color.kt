package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Neon Cyber Gaming Palette
val DarkBg = Color(0xFF090D16)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceCard = Color(0xFF1E293B)
val DarkSurfaceCardElevated = Color(0xFF283548)
val DarkBorder = Color(0xFF334155)

// Accent Colors
val NeonCyan = Color(0xFF00E5FF)
val NeonCyanDim = Color(0xFF00B4D8)
val ElectricPurple = Color(0xFFA855F7)
val DeepViolet = Color(0xFF7C3AED)
val AmberGold = Color(0xFFFFB703)
val NeonGreen = Color(0xFF10B981)
val NeonRed = Color(0xFFEF4444)
val NeonOrange = Color(0xFFF97316)

// Text
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// 2048 Tile Colors
fun getTileColor(value: Int): Color {
    return when (value) {
        2 -> Color(0xFF334155)
        4 -> Color(0xFF475569)
        8 -> Color(0xFFF97316)
        16 -> Color(0xFFEA580C)
        32 -> Color(0xFFDC2626)
        64 -> Color(0xFFE11D48)
        128 -> Color(0xFFF59E0B)
        256 -> Color(0xFFD97706)
        512 -> Color(0xFF10B981)
        1024 -> Color(0xFF06B6D4)
        2048 -> Color(0xFF8B5CF6)
        4096 -> Color(0xFFD946EF)
        else -> Color(0xFF6366F1)
    }
}
