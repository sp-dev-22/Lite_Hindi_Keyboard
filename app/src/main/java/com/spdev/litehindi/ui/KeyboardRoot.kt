package com.spdev.litehindi.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spdev.litehindi.data.KeyboardTheme
import com.spdev.litehindi.data.Prefs
import com.spdev.litehindi.engine.HindiEngine

/** Everything the keyboard needs from the IME (or from the settings test pad). */
interface KeyboardListener {
    fun commitText(text: String)
    fun setComposing(text: String)
    fun finishComposing()
    fun deleteBackward(count: Int)
    fun moveCursor(delta: Int)
    fun onEnter()
    fun openSettings()
}

@Composable
fun KeyboardRoot(
    theme: KeyboardTheme,
    prefs: Prefs,
    listener: KeyboardListener,
    sessionKey: Int = 0,
    numericField: Boolean = false
) {
    val heightScale by prefs.heightScale.collectAsState()
    val textScale by prefs.keyTextScale.collectAsState()
    val bottomSpace by prefs.bottomSpace.collectAsState()
    val bottomSpaceDp by prefs.bottomSpaceDp.collectAsState()
    val borders by prefs.keyBorders.collectAsState()
    val hindiAuto by prefs.hindiAuto.collectAsState()
    val showSuggestions by prefs.showSuggestions.collectAsState()
    val showNumberRow by prefs.showNumberRow.collectAsState()

    var mode by remember { mutableStateOf(KMode.ALPHA) }
    var shifted by remember { mutableStateOf(false) }
    var caps by remember { mutableStateOf(false) }
    var buffer by remember { mutableStateOf("") }
    var undo by remember { mutableStateOf<Pair<String, String>?>(null) }
    var swipeCount by remember { mutableIntStateOf(0) }

    // Candidates are derived synchronously from the buffer, so a fast typist
    // never commits the previous keystroke's suggestion.
    val candidates = remember(buffer, hindiAuto) {
        if (hindiAuto && buffer.isNotEmpty()) HindiEngine.suggest(buffer) else emptyList()
    }

    LaunchedEffect(sessionKey) {
        buffer = ""
        undo = null
        shifted = false
        caps = false
        mode = if (numericField) KMode.NUM else KMode.ALPHA
    }

    fun updateBuffer(next: String) {
        buffer = next
        if (next.isEmpty()) listener.finishComposing() else listener.setComposing(next)
    }

    fun commitWord(word: String, addSpace: Boolean) {
        if (buffer.isEmpty()) {
            listener.commitText(if (addSpace) " " else "")
            return
        }
        val latin = buffer
        listener.commitText(if (addSpace) "$word " else word)
        undo = if (word != latin) Pair(word, latin) else null
        buffer = ""
    }

    fun deleteOnce() {
        if (buffer.isNotEmpty()) {
            updateBuffer(buffer.dropLast(1))
            undo = null
            return
        }
        val pending = undo
        if (pending != null) {
            listener.deleteBackward(pending.first.length)
            listener.commitText(pending.second)
            undo = null
        } else {
            listener.deleteBackward(1)
        }
    }

    fun deleteMany(count: Int) {
        if (count <= 0) return
        if (buffer.isNotEmpty()) {
            val keep = (buffer.length - count).coerceAtLeast(0)
            val deletedFromBuffer = buffer.length - keep
            updateBuffer(buffer.take(keep))
            val rest = count - deletedFromBuffer
            if (rest > 0) listener.deleteBackward(rest)
        } else {
            listener.deleteBackward(count)
        }
        undo = null
    }

    fun pressKey(key: Key) {
        when (key.type) {
            KType.CHAR -> {
                val ch = if ((shifted || caps) && key.label.length == 1) key.label.uppercase() else key.label
                if (shifted && !caps) shifted = false
                val isLetter = ch.length == 1 && ch[0].isLetter()
                if (hindiAuto && mode == KMode.ALPHA && isLetter) {
                    updateBuffer(buffer + ch.lowercase())
                } else {
                    if (buffer.isNotEmpty()) commitWord(candidates.firstOrNull() ?: buffer, false)
                    listener.commitText(ch)
                    undo = null
                }
            }

            KType.SPACE -> {
                if (buffer.isEmpty()) {
                    listener.commitText(" ")
                } else {
                    commitWord(candidates.firstOrNull() ?: buffer, true)
                }
            }

            KType.ENTER -> {
                if (buffer.isNotEmpty()) commitWord(candidates.firstOrNull() ?: buffer, false)
                listener.onEnter()
                undo = null
            }

            KType.BACK -> deleteOnce()

            KType.SHIFT -> {
                if (caps) {
                    caps = false
                    shifted = false
                } else if (shifted) {
                    caps = true
                } else {
                    shifted = true
                }
            }

            KType.HINDI -> {
                val next = !hindiAuto
                prefs.setHindiAuto(next)
                if (!next && buffer.isNotEmpty()) {
                    listener.commitText(buffer)
                    buffer = ""
                }
                undo = null
            }

            KType.MODE -> {
                key.target?.let { mode = it }
                shifted = false
                caps = false
            }
        }
    }

    fun actionsFor(key: Key): KeyActions {
        val common = KeyActions(
            tap = { pressKey(key) },
            longPress = {
                if (key.sub != null) {
                    if (buffer.isNotEmpty()) commitWord(candidates.firstOrNull() ?: buffer, false)
                    listener.commitText(key.sub)
                    undo = null
                }
            }
        )
        return when (key.type) {
            KType.BACK -> KeyActions(
                tap = { deleteOnce() },
                holdTick = { deleteOnce() },
                swipePreview = { swipeCount = it },
                swipeCommit = { n ->
                    swipeCount = 0
                    deleteMany(n)
                }
            )

            KType.SPACE -> KeyActions(
                tap = { pressKey(key) },
                cursorStep = { delta -> if (buffer.isEmpty()) listener.moveCursor(delta) }
            )

            else -> common
        }
    }

    Column(modifier = Modifier.fillMaxWidth().themedBackground(theme)) {

        // ---- top strip: delete-swipe counter / spelling candidates / undo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(theme.stripBg)
        ) {
            when {
                swipeCount > 0 -> Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Release to delete $swipeCount  •  slide back to cancel",
                        color = theme.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                showSuggestions && candidates.isNotEmpty() -> LazyRow(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(candidates.size) { index ->
                        val word = candidates[index]
                        val first = index == 0
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (first) theme.accent.copy(alpha = 0.18f) else theme.keyBg.copy(alpha = 0f))
                                .clickable { commitWord(word, false) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = word,
                                color = if (first) theme.accent else theme.textPrimary,
                                fontSize = if (first) 17.sp else 15.sp,
                                fontWeight = if (first) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                undo != null -> Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.keyBg)
                            .clickable {
                                val pending = undo
                                if (pending != null) {
                                    listener.deleteBackward(pending.first.length)
                                    listener.commitText(pending.second)
                                    undo = null
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "↶ ${undo?.second ?: ""}",
                            color = theme.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (showNumberRow && mode != KMode.NUM) {
            KeyRow(Keys.numberRow, theme, heightScale, textScale, borders, shifted, caps, hindiAuto, ::actionsFor)
        }

        when (mode) {
            KMode.ALPHA -> {
                KeyRow(Keys.row1, theme, heightScale, textScale, borders, shifted, caps, hindiAuto, ::actionsFor)
                KeyRow(Keys.row2, theme, heightScale, textScale, borders, shifted, caps, hindiAuto, ::actionsFor, 12.dp)
                KeyRow(Keys.row3, theme, heightScale, textScale, borders, shifted, caps, hindiAuto, ::actionsFor)
                KeyRow(Keys.row4, theme, heightScale, textScale, borders, false, false, hindiAuto, ::actionsFor)
            }

            KMode.SYM1 -> {
                KeyRow(Keys.sym1Row1, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.sym1Row2, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.sym1Row3, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.sym1Row4, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
            }

            KMode.SYM2 -> {
                KeyRow(Keys.sym2Row1, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.sym1Row2, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.sym2Row3, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.sym1Row4, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
            }

            KMode.NUM -> {
                KeyRow(Keys.numRow1, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.numRow2, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.numRow3, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
                KeyRow(Keys.numRow4, theme, heightScale, textScale, borders, false, false, false, ::actionsFor)
            }
        }

        if (bottomSpace) Spacer(modifier = Modifier.height(bottomSpaceDp.dp))
    }
}

@Composable
private fun KeyRow(
    keys: List<Key>,
    theme: KeyboardTheme,
    heightScale: Float,
    textScale: Float,
    borders: Boolean,
    shifted: Boolean,
    caps: Boolean,
    hindiOn: Boolean,
    actionsFor: (Key) -> KeyActions,
    horizontalPadding: androidx.compose.ui.unit.Dp = 3.dp
) {
    val keyHeight = (46 * heightScale).dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { key ->
            Box(modifier = Modifier.weight(key.weight).height(keyHeight)) {
                KeyButton(
                    key = key,
                    theme = theme,
                    keyHeight = keyHeight,
                    textScale = textScale,
                    showBorder = borders,
                    shifted = shifted,
                    caps = caps,
                    hindiOn = hindiOn,
                    actions = actionsFor(key)
                )
            }
        }
    }
}
