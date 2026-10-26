package com.spdev.litehindi.data

import androidx.compose.ui.graphics.Color

/**
 * Background decoration drawn with code only — no PNG, no JPG, no wallpaper files.
 */
enum class BgPattern { NONE, DOTS, DIAGONAL, GLOW }

data class KeyboardTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val pattern: BgPattern,
    val keyBg: Color,
    val keyBorder: Color,
    val keyPressed: Color,
    val actionBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val stripBg: Color
)

object Themes {

    val SlateTeal = KeyboardTheme(
        id = "slate_teal", name = "Slate Teal", isDark = true,
        bgTop = Color(0xFF16211F), bgBottom = Color(0xFF0B1211),
        pattern = BgPattern.GLOW,
        keyBg = Color(0xFF1E2D2A), keyBorder = Color(0xFF2B403C), keyPressed = Color(0xFF33514C),
        actionBg = Color(0xFF2E6B61), textPrimary = Color(0xFFF2F7F6), textSecondary = Color(0xFF8CA39E),
        accent = Color(0xFF4DD0C4), stripBg = Color(0xFF101A18)
    )

    val OledBlack = KeyboardTheme(
        id = "oled_black", name = "OLED Black", isDark = true,
        bgTop = Color(0xFF000000), bgBottom = Color(0xFF000000),
        pattern = BgPattern.NONE,
        keyBg = Color(0xFF131313), keyBorder = Color(0xFF242424), keyPressed = Color(0xFF2C2C2C),
        actionBg = Color(0xFF2E7D32), textPrimary = Color(0xFFFFFFFF), textSecondary = Color(0xFF8A8A8A),
        accent = Color(0xFF66BB6A), stripBg = Color(0xFF070707)
    )

    val Charcoal = KeyboardTheme(
        id = "charcoal", name = "Charcoal", isDark = true,
        bgTop = Color(0xFF1C1C1C), bgBottom = Color(0xFF121212),
        pattern = BgPattern.NONE,
        keyBg = Color(0xFF2A2A2A), keyBorder = Color(0xFF383838), keyPressed = Color(0xFF3E3E3E),
        actionBg = Color(0xFF00796B), textPrimary = Color(0xFFF0F0F0), textSecondary = Color(0xFF9E9E9E),
        accent = Color(0xFF26A69A), stripBg = Color(0xFF161616)
    )

    val Midnight = KeyboardTheme(
        id = "midnight", name = "Midnight Blue", isDark = true,
        bgTop = Color(0xFF16233F), bgBottom = Color(0xFF0A1120),
        pattern = BgPattern.DOTS,
        keyBg = Color(0xFF1E2C4A), keyBorder = Color(0xFF2E4166), keyPressed = Color(0xFF3A5183),
        actionBg = Color(0xFF2F5BD0), textPrimary = Color(0xFFF4F7FF), textSecondary = Color(0xFF95A6C4),
        accent = Color(0xFF6BA8FF), stripBg = Color(0xFF101A2E)
    )

    val Forest = KeyboardTheme(
        id = "forest", name = "Forest Emerald", isDark = true,
        bgTop = Color(0xFF12301F), bgBottom = Color(0xFF07160E),
        pattern = BgPattern.DIAGONAL,
        keyBg = Color(0xFF1A3B29), keyBorder = Color(0xFF26543A), keyPressed = Color(0xFF2F6A49),
        actionBg = Color(0xFF1E7A50), textPrimary = Color(0xFFF0FDF4), textSecondary = Color(0xFF8FD3A8),
        accent = Color(0xFF4ADE80), stripBg = Color(0xFF0D2417)
    )

    val Indigo = KeyboardTheme(
        id = "indigo", name = "Indigo Night", isDark = true,
        bgTop = Color(0xFF241A3D), bgBottom = Color(0xFF110A1E),
        pattern = BgPattern.GLOW,
        keyBg = Color(0xFF312350), keyBorder = Color(0xFF463469), keyPressed = Color(0xFF57407F),
        actionBg = Color(0xFF6D3FC4), textPrimary = Color(0xFFF6F2FF), textSecondary = Color(0xFFBBA8D9),
        accent = Color(0xFFB388FF), stripBg = Color(0xFF1A1130)
    )

    val Wine = KeyboardTheme(
        id = "wine", name = "Wine Rose", isDark = true,
        bgTop = Color(0xFF31161F), bgBottom = Color(0xFF170A0F),
        pattern = BgPattern.NONE,
        keyBg = Color(0xFF42202B), keyBorder = Color(0xFF5C2E3D), keyPressed = Color(0xFF733A4C),
        actionBg = Color(0xFFB03060), textPrimary = Color(0xFFFFF1F4), textSecondary = Color(0xFFDCA3B4),
        accent = Color(0xFFFF7AA2), stripBg = Color(0xFF241016)
    )

    val Saffron = KeyboardTheme(
        id = "saffron", name = "Kesari", isDark = true,
        bgTop = Color(0xFF34200C), bgBottom = Color(0xFF180E05),
        pattern = BgPattern.DOTS,
        keyBg = Color(0xFF432A11), keyBorder = Color(0xFF5C3A18), keyPressed = Color(0xFF744A21),
        actionBg = Color(0xFFD2691E), textPrimary = Color(0xFFFFF8EC), textSecondary = Color(0xFFE0B584),
        accent = Color(0xFFFFA94D), stripBg = Color(0xFF261707)
    )

    val ClassicLight = KeyboardTheme(
        id = "classic_light", name = "Classic Light", isDark = false,
        bgTop = Color(0xFFEDF1F3), bgBottom = Color(0xFFE2E8EB),
        pattern = BgPattern.NONE,
        keyBg = Color(0xFFFFFFFF), keyBorder = Color(0xFFCFD8DC), keyPressed = Color(0xFFDCE3E6),
        actionBg = Color(0xFF00796B), textPrimary = Color(0xFF1B2426), textSecondary = Color(0xFF6B7B80),
        accent = Color(0xFF00796B), stripBg = Color(0xFFE6EBEE)
    )

    val MintLight = KeyboardTheme(
        id = "mint_light", name = "Mint Light", isDark = false,
        bgTop = Color(0xFFE9F7EE), bgBottom = Color(0xFFD6EFE0),
        pattern = BgPattern.GLOW,
        keyBg = Color(0xFFFFFFFF), keyBorder = Color(0xFFC3E3D0), keyPressed = Color(0xFFD3EBDC),
        actionBg = Color(0xFF2E7D32), textPrimary = Color(0xFF14301C), textSecondary = Color(0xFF5C7D68),
        accent = Color(0xFF2E7D32), stripBg = Color(0xFFDFF3E7)
    )

    val Paper = KeyboardTheme(
        id = "paper", name = "Paper Grain", isDark = false,
        bgTop = Color(0xFFF7F2E7), bgBottom = Color(0xFFEFE7D6),
        pattern = BgPattern.DOTS,
        keyBg = Color(0xFFFFFDF8), keyBorder = Color(0xFFDED3BC), keyPressed = Color(0xFFEAE0CC),
        actionBg = Color(0xFF8D6E63), textPrimary = Color(0xFF3A2F22), textSecondary = Color(0xFF857A66),
        accent = Color(0xFFB07D3B), stripBg = Color(0xFFF2EAD9)
    )

    val SkyLight = KeyboardTheme(
        id = "sky_light", name = "Sky Light", isDark = false,
        bgTop = Color(0xFFE8F2FB), bgBottom = Color(0xFFD3E6F7),
        pattern = BgPattern.DIAGONAL,
        keyBg = Color(0xFFFFFFFF), keyBorder = Color(0xFFC2D8EC), keyPressed = Color(0xFFD2E3F3),
        actionBg = Color(0xFF1565C0), textPrimary = Color(0xFF11212E), textSecondary = Color(0xFF5A7387),
        accent = Color(0xFF1976D2), stripBg = Color(0xFFDCEAF8)
    )

    val All: List<KeyboardTheme> = listOf(
        SlateTeal, OledBlack, Charcoal, Midnight, Forest, Indigo, Wine, Saffron,
        ClassicLight, MintLight, Paper, SkyLight
    )

    fun byId(id: String): KeyboardTheme = All.firstOrNull { it.id == id } ?: SlateTeal
}
