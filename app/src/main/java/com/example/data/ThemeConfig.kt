package com.example.data

import androidx.compose.ui.graphics.Color

data class KeyboardColorTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val keyboardBackground: Color,
    val keyBackground: Color,
    val keyBorderColor: Color,
    val keyPressedBackground: Color,
    val actionKeyBackground: Color,
    val actionKeyContent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentColor: Color,
    val suggestionBackground: Color,
    val suggestionActive: Color,
    val dividerColor: Color
)

object ThemePresets {
    val DarkSlateTeal = KeyboardColorTheme(
        id = "slate_teal",
        name = "Slate Teal (Default)",
        isDark = true,
        keyboardBackground = Color(0xFF101917),
        keyBackground = Color(0xFF1E2D2A),
        keyBorderColor = Color(0xFF293E3A),
        keyPressedBackground = Color(0xFF2F4642),
        actionKeyBackground = Color(0xFF3B6E65),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF8CA39E),
        accentColor = Color(0xFF4DB6AC),
        suggestionBackground = Color(0xFF14211E),
        suggestionActive = Color(0xFF4DB6AC),
        dividerColor = Color(0xFF233531)
    )

    val OledBlack = KeyboardColorTheme(
        id = "oled_black",
        name = "OLED Pure Black (Battery Saver)",
        isDark = true,
        keyboardBackground = Color(0xFF000000),
        keyBackground = Color(0xFF141414),
        keyBorderColor = Color(0xFF262626),
        keyPressedBackground = Color(0xFF2E2E2E),
        actionKeyBackground = Color(0xFF2E7D32),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF888888),
        accentColor = Color(0xFF4CAF50),
        suggestionBackground = Color(0xFF080808),
        suggestionActive = Color(0xFF4CAF50),
        dividerColor = Color(0xFF202020)
    )

    val CharcoalDark = KeyboardColorTheme(
        id = "charcoal_dark",
        name = "Charcoal Dark",
        isDark = true,
        keyboardBackground = Color(0xFF1A1A1A),
        keyBackground = Color(0xFF2A2A2A),
        keyBorderColor = Color(0xFF383838),
        keyPressedBackground = Color(0xFF3C3C3C),
        actionKeyBackground = Color(0xFF00897B),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFF0F0F0),
        textSecondary = Color(0xFF9E9E9E),
        accentColor = Color(0xFF26A69A),
        suggestionBackground = Color(0xFF141414),
        suggestionActive = Color(0xFF26A69A),
        dividerColor = Color(0xFF303030)
    )

    val MidnightBlue = KeyboardColorTheme(
        id = "midnight_blue",
        name = "Midnight Blue",
        isDark = true,
        keyboardBackground = Color(0xFF0F172A),
        keyBackground = Color(0xFF1E293B),
        keyBorderColor = Color(0xFF334155),
        keyPressedBackground = Color(0xFF475569),
        actionKeyBackground = Color(0xFF2563EB),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        accentColor = Color(0xFF38BDF8),
        suggestionBackground = Color(0xFF0B1120),
        suggestionActive = Color(0xFF38BDF8),
        dividerColor = Color(0xFF1E293B)
    )

    val ForestEmerald = KeyboardColorTheme(
        id = "forest_emerald",
        name = "Forest Emerald",
        isDark = true,
        keyboardBackground = Color(0xFF0B1D15),
        keyBackground = Color(0xFF173528),
        keyBorderColor = Color(0xFF234B3B),
        keyPressedBackground = Color(0xFF2D5D4A),
        actionKeyBackground = Color(0xFF1E7051),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFF0FDF4),
        textSecondary = Color(0xFF86EFAC),
        accentColor = Color(0xFF34D399),
        suggestionBackground = Color(0xFF081710),
        suggestionActive = Color(0xFF34D399),
        dividerColor = Color(0xFF1C3F30)
    )

    val DeepPurple = KeyboardColorTheme(
        id = "deep_purple",
        name = "Deep Purple",
        isDark = true,
        keyboardBackground = Color(0xFF170F24),
        keyBackground = Color(0xFF271A3C),
        keyBorderColor = Color(0xFF3E2B5C),
        keyPressedBackground = Color(0xFF503874),
        actionKeyBackground = Color(0xFF7E22CE),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFFD8B4FE),
        accentColor = Color(0xFFC084FC),
        suggestionBackground = Color(0xFF10091C),
        suggestionActive = Color(0xFFC084FC),
        dividerColor = Color(0xFF2F1F47)
    )

    val ClassicLight = KeyboardColorTheme(
        id = "classic_light",
        name = "Classic Light",
        isDark = false,
        keyboardBackground = Color(0xFFECEFF1),
        keyBackground = Color(0xFFFFFFFF),
        keyBorderColor = Color(0xFFCFD8DC),
        keyPressedBackground = Color(0xFFDFE4E7),
        actionKeyBackground = Color(0xFF00796B),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF212121),
        textSecondary = Color(0xFF757575),
        accentColor = Color(0xFF00796B),
        suggestionBackground = Color(0xFFE0E0E0),
        suggestionActive = Color(0xFF00796B),
        dividerColor = Color(0xFFCFD8DC)
    )

    val MintLight = KeyboardColorTheme(
        id = "mint_light",
        name = "Mint Light",
        isDark = false,
        keyboardBackground = Color(0xFFE8F5E9),
        keyBackground = Color(0xFFFFFFFF),
        keyBorderColor = Color(0xFFC8E6C9),
        keyPressedBackground = Color(0xFFDCEDC8),
        actionKeyBackground = Color(0xFF2E7D32),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF1B5E20),
        textSecondary = Color(0xFF558B2F),
        accentColor = Color(0xFF2E7D32),
        suggestionBackground = Color(0xFFDCEDC8),
        suggestionActive = Color(0xFF2E7D32),
        dividerColor = Color(0xFFC8E6C9)
    )

    val RosePink = KeyboardColorTheme(
        id = "rose_pink",
        name = "Rose Pink",
        isDark = true,
        keyboardBackground = Color(0xFF26101B),
        keyBackground = Color(0xFF3B1B2B),
        keyBorderColor = Color(0xFF5A2641),
        keyPressedBackground = Color(0xFF752F55),
        actionKeyBackground = Color(0xFFEC4899),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFFF472B6),
        accentColor = Color(0xFFF472B6),
        suggestionBackground = Color(0xFF1E0C15),
        suggestionActive = Color(0xFFF472B6),
        dividerColor = Color(0xFF4C1D34)
    )

    val Saffron = KeyboardColorTheme(
        id = "saffron",
        name = "Saffron (Bhagwa / Kesari)",
        isDark = true,
        keyboardBackground = Color(0xFF1F140A),
        keyBackground = Color(0xFF332010),
        keyBorderColor = Color(0xFF54351A),
        keyPressedBackground = Color(0xFF6B4321),
        actionKeyBackground = Color(0xFFEA580C),
        actionKeyContent = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFFFBEB),
        textSecondary = Color(0xFFFDBA74),
        accentColor = Color(0xFFF97316),
        suggestionBackground = Color(0xFF180F07),
        suggestionActive = Color(0xFFFB923C),
        dividerColor = Color(0xFF432A15)
    )

    val All = listOf(
        DarkSlateTeal,
        OledBlack,
        CharcoalDark,
        RosePink,
        Saffron,
        MidnightBlue,
        ForestEmerald,
        DeepPurple,
        ClassicLight,
        MintLight
    )

    fun getById(id: String): KeyboardColorTheme {
        return All.find { it.id == id } ?: DarkSlateTeal
    }
}
