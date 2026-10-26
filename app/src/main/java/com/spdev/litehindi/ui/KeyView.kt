package com.spdev.litehindi.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spdev.litehindi.data.KeyboardTheme
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.max

/** Callbacks a key can trigger. Grouped so the composable signature stays short. */
class KeyActions(
    val tap: () -> Unit = {},
    val longPress: () -> Unit = {},
    val holdTick: () -> Unit = {},
    val swipePreview: (Int) -> Unit = {},
    val swipeCommit: (Int) -> Unit = {},
    val cursorStep: (Int) -> Unit = {}
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyButton(
    key: Key,
    theme: KeyboardTheme,
    keyHeight: Dp,
    textScale: Float,
    showBorder: Boolean,
    shifted: Boolean,
    caps: Boolean,
    hindiOn: Boolean,
    actions: KeyActions
) {
    var pressed by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val slopPx = with(density) { 14.dp.toPx() }
    val stepPx = with(density) { 26.dp.toPx() }

    val shape = RoundedCornerShape(6.dp)

    val isEnter = key.type == KType.ENTER
    val isSpecial = key.type == KType.SHIFT || key.type == KType.BACK || key.type == KType.MODE

    val keyBg = when {
        pressed -> theme.keyPressed
        isEnter -> theme.actionBg
        isSpecial -> theme.keyBg.copy(alpha = 0.72f)
        else -> theme.keyBg
    }

    val gesture: Modifier = when (key.type) {
        // ---- backspace: tap = 1 char, hold = repeat, swipe left = select N chars
        KType.BACK -> Modifier.pointerInput("back") {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                pressed = true
                var dragging = false
                var holding = false
                var count = 0
                var lifted = false
                val holdDelay = viewConfiguration.longPressTimeoutMillis
                val tick = 55L

                while (true) {
                    val event = withTimeoutOrNull(if (holding) tick else holdDelay) { awaitPointerEvent() }
                    if (event == null) {
                        if (!dragging) {
                            holding = true
                            actions.holdTick()
                        }
                        continue
                    }
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.changedToUpIgnoreConsumed()) {
                        change.consume()
                        lifted = true
                        break
                    }
                    val dx = change.position.x - down.position.x
                    if (!dragging && !holding && abs(dx) > slopPx) dragging = true
                    if (dragging) {
                        change.consume()
                        val newCount = max(0, ((abs(dx) - slopPx) / stepPx).toInt() + 1)
                        if (newCount != count) {
                            count = newCount
                            actions.swipePreview(count)
                        }
                    }
                }

                pressed = false
                if (dragging) {
                    actions.swipePreview(0)
                    actions.swipeCommit(count)
                } else if (!holding && lifted) {
                    actions.tap()
                }
            }
        }

        // ---- space: tap = space, horizontal drag = move cursor
        KType.SPACE -> Modifier.pointerInput("space") {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                pressed = true
                var acc = 0f
                var moved = false

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.changedToUpIgnoreConsumed()) {
                        change.consume()
                        break
                    }
                    change.consume()
                    acc += change.positionChange().x
                    while (acc >= stepPx) {
                        actions.cursorStep(1)
                        acc -= stepPx
                        moved = true
                    }
                    while (acc <= -stepPx) {
                        actions.cursorStep(-1)
                        acc += stepPx
                        moved = true
                    }
                }

                pressed = false
                if (!moved) actions.tap()
            }
        }

        // ---- everything else: tap + long press for the symbol on the key
        else -> Modifier.combinedClickable(
            onClick = { actions.tap() },
            onLongClick = { actions.longPress() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(keyBg)
            .then(
                if (showBorder) Modifier.border(BorderStroke(0.75.dp, theme.keyBorder), shape)
                else Modifier
            )
            .then(gesture),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (key.type) {
                KType.SHIFT -> Text(
                    text = if (caps) "⇪" else "⇧",
                    color = if (shifted || caps) theme.accent else theme.textPrimary,
                    fontSize = (19 * textScale).sp,
                    fontWeight = FontWeight.Bold
                )

                KType.BACK -> Text(
                    text = "⌫",
                    color = theme.textPrimary,
                    fontSize = (18 * textScale).sp
                )

                KType.ENTER -> Text(
                    text = "↵",
                    color = theme.textPrimary,
                    fontSize = (18 * textScale).sp,
                    fontWeight = FontWeight.Bold
                )

                KType.HINDI -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hindiOn) "अ" else "A",
                        color = if (hindiOn) theme.accent else theme.textPrimary,
                        fontSize = (18 * textScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .width(14.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (hindiOn) theme.accent else theme.textSecondary.copy(alpha = 0.25f))
                    )
                }

                KType.SPACE -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(theme.textSecondary.copy(alpha = 0.35f))
                    )
                }

                else -> Text(
                    text = if (key.label.length == 1 && key.label[0].isLetter()) {
                        if (shifted || caps) key.label.uppercase() else key.label
                    } else key.label,
                    color = theme.textPrimary,
                    fontSize = (if (key.label.length > 2) 13f else 19f * textScale).sp,
                    fontWeight = if (key.type == KType.CHAR) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Long-press symbol hint, top-right, like a phone dialpad
        if (key.sub != null && key.type != KType.SPACE && key.type != KType.HINDI) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 4.dp, top = 2.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = key.sub,
                    color = theme.textSecondary.copy(alpha = 0.8f),
                    fontSize = (9 * textScale).sp
                )
            }
        }
    }
}
