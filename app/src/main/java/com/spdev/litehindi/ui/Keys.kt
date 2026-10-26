package com.spdev.litehindi.ui

enum class KMode { ALPHA, SYM1, SYM2, NUM }

enum class KType { CHAR, SHIFT, BACK, ENTER, MODE, HINDI, SPACE }

data class Key(
    val label: String,
    val sub: String? = null,
    val type: KType = KType.CHAR,
    val target: KMode? = null,
    val weight: Float = 1f
)

/**
 * Every row is a plain List<Key>. No XML, no JSON, no parsing at runtime —
 * the whole layout is ~1 KB of code, which is the point of a "lite" keyboard.
 */
object Keys {

    val numberRow = listOf(
        Key("1", "!"), Key("2", "@"), Key("3", "#"), Key("4", "$"), Key("5", "%"),
        Key("6", "^"), Key("7", "&"), Key("8", "*"), Key("9", "("), Key("0", ")")
    )

    val row1 = listOf(
        Key("q", "~"), Key("w", "`"), Key("e", "|"), Key("r", "•"), Key("t", "°"),
        Key("y", "±"), Key("u", "×"), Key("i", "÷"), Key("o", "{"), Key("p", "}")
    )

    val row2 = listOf(
        Key("a", "@"), Key("s", "#"), Key("d", "₹"), Key("f", "%"), Key("g", "&"),
        Key("h", "*"), Key("j", "-"), Key("k", "("), Key("l", ")")
    )

    val row3 = listOf(
        Key("⇧", type = KType.SHIFT, weight = 1.4f),
        Key("z", "!"), Key("x", "\""), Key("c", "'"), Key("v", ":"), Key("b", ";"),
        Key("n", "/"), Key("m", "?"),
        Key("⌫", type = KType.BACK, weight = 1.4f)
    )

    val row4 = listOf(
        Key("?123", type = KType.MODE, target = KMode.SYM1, weight = 1.3f),
        Key(",", weight = 1f),
        Key("अ", type = KType.HINDI, weight = 1.1f),
        Key(" ", type = KType.SPACE, weight = 3.6f),
        Key(".", weight = 1f),
        Key("↵", type = KType.ENTER, weight = 1.4f)
    )

    val sym1Row1 = listOf(
        Key("~"), Key("`"), Key("|"), Key("•"), Key("°"),
        Key("±"), Key("×"), Key("÷"), Key("{"), Key("}")
    )

    val sym1Row2 = listOf(
        Key("@"), Key("#"), Key("₹"), Key("%"), Key("&"),
        Key("*"), Key("-"), Key("="), Key("("), Key(")")
    )

    val sym1Row3 = listOf(
        Key("=\\<", type = KType.MODE, target = KMode.SYM2, weight = 1.4f),
        Key("!"), Key("\""), Key("'"), Key(":"), Key(";"), Key("+"), Key("/"), Key("?"),
        Key("⌫", type = KType.BACK, weight = 1.4f)
    )

    val sym1Row4 = listOf(
        Key("ABC", type = KType.MODE, target = KMode.ALPHA, weight = 1.3f),
        Key(",", weight = 1f),
        Key(" ", type = KType.SPACE, weight = 3.2f),
        Key("12", type = KType.MODE, target = KMode.NUM, weight = 1.1f),
        Key(".", weight = 1f),
        Key("↵", type = KType.ENTER, weight = 1.4f)
    )

    val sym2Row1 = listOf(
        Key("$"), Key("£"), Key("€"), Key("¥"), Key("©"),
        Key("®"), Key("^"), Key("π"), Key("["), Key("]")
    )

    val sym2Row3 = listOf(
        Key("?123", type = KType.MODE, target = KMode.SYM1, weight = 1.4f),
        Key("¡"), Key("<"), Key(">"), Key("~"), Key("|"), Key("\\"), Key("¿"),
        Key("⌫", type = KType.BACK, weight = 1.4f)
    )

    val numRow1 = listOf(Key("1"), Key("2"), Key("3"), Key("ABC", type = KType.MODE, target = KMode.ALPHA))
    val numRow2 = listOf(Key("4"), Key("5"), Key("6"), Key(",", type = KType.CHAR))
    val numRow3 = listOf(Key("7"), Key("8"), Key("9"), Key("⌫", type = KType.BACK))
    val numRow4 = listOf(
        Key(".", weight = 1f),
        Key("0", weight = 1f),
        Key(" ", type = KType.SPACE, weight = 1f),
        Key("↵", type = KType.ENTER, weight = 1f)
    )
}
