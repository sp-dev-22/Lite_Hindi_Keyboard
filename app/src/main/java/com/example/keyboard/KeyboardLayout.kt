package com.example.keyboard

enum class KeyboardMode {
    ALPHA,
    SYMBOLS_1,
    SYMBOLS_2,
    NUMPAD
}

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    MODE_SWITCH,
    HINDI_TOGGLE,
    CLIPBOARD,
    SETTINGS,
    SPACE,
    ACTION
}

data class KeyModel(
    val primary: String,
    val secondary: String? = null,
    val type: KeyType = KeyType.CHARACTER,
    val targetMode: KeyboardMode? = null,
    val weight: Float = 1.0f
)

object KeyboardLayouts {

    // Number Row with Hindi digits superscript (from Screenshots 3 & 4)
    val numberRow: List<KeyModel> = listOf(
        KeyModel("1", "१"),
        KeyModel("2", "२"),
        KeyModel("3", "३"),
        KeyModel("4", "४"),
        KeyModel("5", "५"),
        KeyModel("6", "६"),
        KeyModel("7", "७"),
        KeyModel("8", "८"),
        KeyModel("9", "९"),
        KeyModel("0", "०")
    )

    // QWERTY Layout (from Screenshots 3 & 4)
    val alphaRow1: List<KeyModel> = listOf(
        KeyModel("q", "~"),
        KeyModel("w", "±"),
        KeyModel("e", "×"),
        KeyModel("r", "÷"),
        KeyModel("t", "°"),
        KeyModel("y", "`"),
        KeyModel("u", "´"),
        KeyModel("i", "¯"),
        KeyModel("o", "{"),
        KeyModel("p", "}")
    )

    val alphaRow2: List<KeyModel> = listOf(
        KeyModel("a", "@"),
        KeyModel("s", "#"),
        KeyModel("d", "₹"),
        KeyModel("f", "%"),
        KeyModel("g", "&"),
        KeyModel("h", "*"),
        KeyModel("j", "-"),
        KeyModel("k", "("),
        KeyModel("l", ")")
    )

    val alphaRow3: List<KeyModel> = listOf(
        KeyModel("⇧", type = KeyType.SHIFT, weight = 1.4f),
        KeyModel("z", "!"),
        KeyModel("x", "\""),
        KeyModel("c", "'"),
        KeyModel("v", ":"),
        KeyModel("b", "+"),
        KeyModel("n", "/"),
        KeyModel("m", "?"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.4f)
    )

    // Bottom row for Alpha: ?123, 😊/,, अ (Hindi Toggle), Space, ., Enter
    val alphaRow4: List<KeyModel> = listOf(
        KeyModel("?123", type = KeyType.MODE_SWITCH, targetMode = KeyboardMode.SYMBOLS_1, weight = 1.3f),
        KeyModel(",", weight = 1.0f),
        KeyModel("अ", type = KeyType.HINDI_TOGGLE, weight = 1.1f),
        KeyModel(" ", type = KeyType.SPACE, weight = 3.6f),
        KeyModel(".", weight = 1.0f),
        KeyModel("↵", type = KeyType.ENTER, weight = 1.4f)
    )

    // Symbols Mode 1 (Screenshot 9)
    val sym1Row1: List<KeyModel> = listOf(
        KeyModel("~"), KeyModel("±"), KeyModel("×"), KeyModel("÷"), KeyModel("°"),
        KeyModel("`"), KeyModel("´"), KeyModel("_"), KeyModel("{"), KeyModel("}")
    )

    val sym1Row2: List<KeyModel> = listOf(
        KeyModel("@"), KeyModel("#"), KeyModel("₹"), KeyModel("%"), KeyModel("&"),
        KeyModel("*"), KeyModel("-"), KeyModel("="), KeyModel("("), KeyModel(")")
    )

    val sym1Row3: List<KeyModel> = listOf(
        KeyModel("=\\<", type = KeyType.MODE_SWITCH, targetMode = KeyboardMode.SYMBOLS_2, weight = 1.4f),
        KeyModel("!"), KeyModel("\""), KeyModel("'"), KeyModel(":"), KeyModel("+"),
        KeyModel("/"), KeyModel("?"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.4f)
    )

    val sym1Row4: List<KeyModel> = listOf(
        KeyModel("ABC", type = KeyType.MODE_SWITCH, targetMode = KeyboardMode.ALPHA, weight = 1.3f),
        KeyModel(",", weight = 1.0f),
        KeyModel(" ", type = KeyType.SPACE, weight = 3.2f),
        KeyModel("12 34", type = KeyType.MODE_SWITCH, targetMode = KeyboardMode.NUMPAD, weight = 1.1f),
        KeyModel(".", weight = 1.0f),
        KeyModel("↵", type = KeyType.ENTER, weight = 1.4f)
    )

    // Symbols Mode 2 (Screenshot 10)
    val sym2Row1: List<KeyModel> = listOf(
        KeyModel("$"), KeyModel("£"), KeyModel("€"), KeyModel("¥"), KeyModel("©"),
        KeyModel("®"), KeyModel("^"), KeyModel("π"), KeyModel("["), KeyModel("]")
    )

    val sym2Row2: List<KeyModel> = sym1Row2

    val sym2Row3: List<KeyModel> = listOf(
        KeyModel("?123", type = KeyType.MODE_SWITCH, targetMode = KeyboardMode.SYMBOLS_1, weight = 1.4f),
        KeyModel("¡"), KeyModel("<"), KeyModel(">"), KeyModel(";"), KeyModel("|"),
        KeyModel("\\"), KeyModel("¿"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.4f)
    )

    val sym2Row4: List<KeyModel> = sym1Row4

    // Numpad Mode (Screenshot 11)
    val numpadRow1: List<KeyModel> = listOf(
        KeyModel("1", weight = 1f),
        KeyModel("2", weight = 1f),
        KeyModel("3", weight = 1f),
        KeyModel("ABC", type = KeyType.MODE_SWITCH, targetMode = KeyboardMode.ALPHA, weight = 1f)
    )

    val numpadRow2: List<KeyModel> = listOf(
        KeyModel("4", weight = 1f),
        KeyModel("5", weight = 1f),
        KeyModel("6", weight = 1f),
        KeyModel(",", weight = 1f)
    )

    val numpadRow3: List<KeyModel> = listOf(
        KeyModel("7", weight = 1f),
        KeyModel("8", weight = 1f),
        KeyModel("9", weight = 1f),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1f)
    )

    val numpadRow4: List<KeyModel> = listOf(
        KeyModel(".", weight = 1f),
        KeyModel("0", weight = 1f),
        KeyModel("_", weight = 1f),
        KeyModel("↵", type = KeyType.ENTER, weight = 1f)
    )
}
