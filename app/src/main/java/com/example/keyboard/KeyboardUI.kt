package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardItem
import com.example.data.KeyboardColorTheme
import com.example.data.KeyboardPreferences
import com.example.engine.HindiTransliterator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

interface KeyboardActionListener {
    fun onCommitText(text: String)
    fun onDeleteBackward()
    fun onDeleteBackward(count: Int) {
        repeat(count) { onDeleteBackward() }
    }
    fun onMoveCursor(delta: Int) {}
    fun onEnter()
    fun onOpenSettings()
}

@Composable
fun LiteKeyboardRoot(
    modifier: Modifier = Modifier,
    theme: KeyboardColorTheme,
    preferences: KeyboardPreferences,
    listener: KeyboardActionListener
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize asset dictionary once
    LaunchedEffect(Unit) {
        HindiTransliterator.initFromContext(context)
    }

    val heightScale by preferences.heightScale.collectAsState()
    val keyTextScale by preferences.keyTextScale.collectAsState()
    val bottomSpace by preferences.bottomSpace.collectAsState()
    val bottomSpaceDp by preferences.bottomSpaceDp.collectAsState()
    val showKeyBorders by preferences.showKeyBorders.collectAsState()
    val hapticEnabled by preferences.hapticFeedback.collectAsState()
    val isHindiTransliteration by preferences.hindiTransliteration.collectAsState()
    val autoCommitOnSpace by preferences.autoCommitOnSpace.collectAsState()
    val showNumberRow by preferences.showNumberRow.collectAsState()
    val clipboardItems by preferences.clipboardItems.collectAsState()

    var keyboardMode by remember { mutableStateOf(KeyboardMode.ALPHA) }
    var isShifted by remember { mutableStateOf(false) }
    var isCapsLock by remember { mutableStateOf(false) }
    var showClipboardOverlay by remember { mutableStateOf(false) }
    var showAddClipDialog by remember { mutableStateOf(false) }
    var editingClipItem by remember { mutableStateOf<ClipboardItem?>(null) }

    // Live composing text buffer (for Latin -> Hindi transliteration)
    var composingBuffer by remember { mutableStateOf("") }
    // Candidate suggestions list
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    // Last auto-committed word for undo bubble (matching Screenshot 8)
    var lastTransliteratedUndo by remember { mutableStateOf<Pair<String, String>?>(null) }

    // State for delete swipe selection
    var deleteSwipeCount by remember { mutableIntStateOf(0) }

    // Update suggestions whenever composingBuffer changes
    LaunchedEffect(composingBuffer, isHindiTransliteration) {
        if (composingBuffer.isNotEmpty() && isHindiTransliteration) {
            suggestions = HindiTransliterator.transliterate(composingBuffer)
        } else {
            suggestions = emptyList()
        }
    }

    fun triggerHaptic() {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun commitSelectedCandidate(word: String, fromSpace: Boolean = false) {
        if (composingBuffer.isNotEmpty()) {
            val latinOriginal = composingBuffer
            listener.onCommitText(word + if (fromSpace) " " else "")
            preferences.incrementTypedWord(isHindi = word != latinOriginal)
            // Prepare undo bubble
            if (word != latinOriginal) {
                lastTransliteratedUndo = Pair(word, latinOriginal)
            } else {
                lastTransliteratedUndo = null
            }
            composingBuffer = ""
            suggestions = emptyList()
        } else {
            listener.onCommitText(word + if (fromSpace) " " else "")
        }
    }

    fun handleSingleDelete() {
        triggerHaptic()
        if (composingBuffer.isNotEmpty()) {
            composingBuffer = composingBuffer.dropLast(1)
        } else {
            val undo = lastTransliteratedUndo
            if (undo != null) {
                listener.onDeleteBackward()
                listener.onCommitText(undo.second)
                lastTransliteratedUndo = null
            } else {
                listener.onDeleteBackward()
            }
        }
    }

    fun handleBatchDelete(count: Int) {
        if (count <= 0) return
        triggerHaptic()
        if (composingBuffer.isNotEmpty()) {
            val remaining = max(0, composingBuffer.length - count)
            val deletedFromBuffer = composingBuffer.length - remaining
            composingBuffer = composingBuffer.take(remaining)
            val excess = count - deletedFromBuffer
            if (excess > 0) {
                listener.onDeleteBackward(excess)
            }
        } else {
            listener.onDeleteBackward(count)
        }
        lastTransliteratedUndo = null
    }

    fun handleKeyPress(key: KeyModel) {
        triggerHaptic()
        when (key.type) {
            KeyType.CHARACTER -> {
                val charToInput = if (key.primary.length == 1 && key.primary[0].isLetter()) {
                    if (isShifted || isCapsLock) key.primary.uppercase() else key.primary.lowercase()
                } else {
                    key.primary
                }

                if (isShifted && !isCapsLock) {
                    isShifted = false
                }

                if (isHindiTransliteration && keyboardMode == KeyboardMode.ALPHA && charToInput.all { it.isLetter() }) {
                    composingBuffer += charToInput
                    lastTransliteratedUndo = null
                } else {
                    if (composingBuffer.isNotEmpty()) {
                        val top = suggestions.firstOrNull() ?: composingBuffer
                        commitSelectedCandidate(top, fromSpace = false)
                    }
                    listener.onCommitText(charToInput)
                    lastTransliteratedUndo = null
                }
            }

            KeyType.SPACE -> {
                if (composingBuffer.isNotEmpty()) {
                    if (isHindiTransliteration && autoCommitOnSpace && suggestions.isNotEmpty()) {
                        val topHindi = suggestions.first()
                        commitSelectedCandidate(topHindi, fromSpace = true)
                    } else {
                        commitSelectedCandidate(composingBuffer, fromSpace = true)
                    }
                } else {
                    listener.onCommitText(" ")
                }
            }

            KeyType.BACKSPACE -> {
                handleSingleDelete()
            }

            KeyType.ENTER -> {
                if (composingBuffer.isNotEmpty()) {
                    val top = suggestions.firstOrNull() ?: composingBuffer
                    commitSelectedCandidate(top, fromSpace = false)
                }
                listener.onEnter()
                lastTransliteratedUndo = null
            }

            KeyType.SHIFT -> {
                if (isCapsLock) {
                    isCapsLock = false
                    isShifted = false
                } else if (isShifted) {
                    isCapsLock = true
                } else {
                    isShifted = true
                }
            }

            KeyType.HINDI_TOGGLE -> {
                val next = !isHindiTransliteration
                preferences.setHindiTransliteration(next)
                if (!next && composingBuffer.isNotEmpty()) {
                    listener.onCommitText(composingBuffer)
                    composingBuffer = ""
                }
            }

            KeyType.MODE_SWITCH -> {
                key.targetMode?.let {
                    keyboardMode = it
                }
            }

            KeyType.CLIPBOARD -> {
                showClipboardOverlay = true
            }

            KeyType.SETTINGS -> {
                listener.onOpenSettings()
            }

            KeyType.ACTION -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.keyboardBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Bar: Suggestion Strip or Action Toolbar
            TopStrip(
                theme = theme,
                composingBuffer = composingBuffer,
                suggestions = suggestions,
                lastTransliteratedUndo = lastTransliteratedUndo,
                isHindiTransliteration = isHindiTransliteration,
                deleteSwipeCount = deleteSwipeCount,
                onCandidateClick = { candidate ->
                    triggerHaptic()
                    commitSelectedCandidate(candidate, fromSpace = false)
                },
                onUndoClick = {
                    triggerHaptic()
                    lastTransliteratedUndo?.let { (_, latin) ->
                        listener.onDeleteBackward()
                        listener.onCommitText(latin)
                        lastTransliteratedUndo = null
                    }
                },
                onClipboardClick = {
                    triggerHaptic()
                    showClipboardOverlay = true
                },
                onSettingsClick = {
                    triggerHaptic()
                    listener.onOpenSettings()
                }
            )

            // Number Row
            if (showNumberRow && keyboardMode != KeyboardMode.NUMPAD) {
                KeyboardRow(
                    keys = KeyboardLayouts.numberRow,
                    theme = theme,
                    heightScale = heightScale,
                    keyTextScale = keyTextScale,
                    showBorders = showKeyBorders,
                    isShifted = false,
                    isCapsLock = false,
                    isHindiTransliteration = isHindiTransliteration,
                    onKeyPress = { handleKeyPress(it) },
                    onKeyLongPress = { key ->
                        triggerHaptic()
                        key.secondary?.let { listener.onCommitText(it) }
                    },
                    onSpaceSwipeMove = { delta ->
                        triggerHaptic()
                        listener.onMoveCursor(delta)
                    },
                    onDeleteSwipe = { count ->
                        deleteSwipeCount = count
                    },
                    onDeleteSwipeRelease = { count ->
                        deleteSwipeCount = 0
                        handleBatchDelete(count)
                    },
                    onDeleteContinuous = {
                        handleSingleDelete()
                    }
                )
            }

            // Keyboard Body depending on current mode
            when (keyboardMode) {
                KeyboardMode.ALPHA -> {
                    KeyboardRow(
                        keys = KeyboardLayouts.alphaRow1,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = isShifted,
                        isCapsLock = isCapsLock,
                        isHindiTransliteration = isHindiTransliteration,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = { key ->
                            triggerHaptic()
                            key.secondary?.let { listener.onCommitText(it) }
                        },
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.alphaRow2,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = isShifted,
                        isCapsLock = isCapsLock,
                        isHindiTransliteration = isHindiTransliteration,
                        horizontalPadding = 12.dp,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = { key ->
                            triggerHaptic()
                            key.secondary?.let { listener.onCommitText(it) }
                        },
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.alphaRow3,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = isShifted,
                        isCapsLock = isCapsLock,
                        isHindiTransliteration = isHindiTransliteration,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = { key ->
                            triggerHaptic()
                            key.secondary?.let { listener.onCommitText(it) }
                        },
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = { count ->
                            deleteSwipeCount = count
                        },
                        onDeleteSwipeRelease = { count ->
                            deleteSwipeCount = 0
                            handleBatchDelete(count)
                        },
                        onDeleteContinuous = {
                            handleSingleDelete()
                        }
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.alphaRow4,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = isHindiTransliteration,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = { key ->
                            triggerHaptic()
                            if (key.type == KeyType.HINDI_TOGGLE) {
                                listener.onOpenSettings()
                            }
                        },
                        onSpaceSwipeMove = { delta ->
                            triggerHaptic()
                            listener.onMoveCursor(delta)
                        },
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                }

                KeyboardMode.SYMBOLS_1 -> {
                    KeyboardRow(
                        keys = KeyboardLayouts.sym1Row1,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.sym1Row2,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.sym1Row3,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = { count -> deleteSwipeCount = count },
                        onDeleteSwipeRelease = { count ->
                            deleteSwipeCount = 0
                            handleBatchDelete(count)
                        },
                        onDeleteContinuous = { handleSingleDelete() }
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.sym1Row4,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = { delta ->
                            triggerHaptic()
                            listener.onMoveCursor(delta)
                        },
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                }

                KeyboardMode.SYMBOLS_2 -> {
                    KeyboardRow(
                        keys = KeyboardLayouts.sym2Row1,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.sym2Row2,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.sym2Row3,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = { count -> deleteSwipeCount = count },
                        onDeleteSwipeRelease = { count ->
                            deleteSwipeCount = 0
                            handleBatchDelete(count)
                        },
                        onDeleteContinuous = { handleSingleDelete() }
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.sym2Row4,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = { delta ->
                            triggerHaptic()
                            listener.onMoveCursor(delta)
                        },
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                }

                KeyboardMode.NUMPAD -> {
                    KeyboardRow(
                        keys = KeyboardLayouts.numpadRow1,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.numpadRow2,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.numpadRow3,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = { count -> deleteSwipeCount = count },
                        onDeleteSwipeRelease = { count ->
                            deleteSwipeCount = 0
                            handleBatchDelete(count)
                        },
                        onDeleteContinuous = { handleSingleDelete() }
                    )
                    KeyboardRow(
                        keys = KeyboardLayouts.numpadRow4,
                        theme = theme,
                        heightScale = heightScale,
                        keyTextScale = keyTextScale,
                        showBorders = showKeyBorders,
                        isShifted = false,
                        isCapsLock = false,
                        isHindiTransliteration = false,
                        onKeyPress = { handleKeyPress(it) },
                        onKeyLongPress = {},
                        onSpaceSwipeMove = {},
                        onDeleteSwipe = {},
                        onDeleteSwipeRelease = {},
                        onDeleteContinuous = {}
                    )
                }
            }

            // Bottom Space for Gesture Chin (Screenshot 12)
            if (bottomSpace) {
                Spacer(modifier = Modifier.height(bottomSpaceDp.dp))
            }
        }

        // Overlay: In-Keyboard Clipboard Manager (Screenshots 5, 6, 7)
        AnimatedVisibility(
            visible = showClipboardOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ClipboardOverlayView(
                theme = theme,
                items = clipboardItems,
                onClose = { showClipboardOverlay = false },
                onSelectItem = { item ->
                    triggerHaptic()
                    listener.onCommitText(item.text)
                    showClipboardOverlay = false
                },
                onAddNewClip = {
                    showAddClipDialog = true
                },
                onEditClip = { item ->
                    editingClipItem = item
                },
                onDeleteClip = { item ->
                    preferences.deleteClipboardItem(item.id)
                }
            )
        }
    }

    // Add / Edit Clip Dialog (Screenshot 7)
    if (showAddClipDialog || editingClipItem != null) {
        val editing = editingClipItem
        var clipContent by remember { mutableStateOf(editing?.text ?: "") }
        var shortcut by remember { mutableStateOf(editing?.shortcut ?: "") }
        var isPinned by remember { mutableStateOf(editing?.isPinned ?: true) }

        AlertDialog(
            onDismissRequest = {
                showAddClipDialog = false
                editingClipItem = null
            },
            title = {
                Text(
                    if (editing != null) "Edit Clip" else "Add Clip",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = clipContent,
                        onValueChange = { clipContent = it },
                        label = { Text("Clip content (up to 50k tokens)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = shortcut,
                        onValueChange = { shortcut = it },
                        label = { Text("Shortcut (Optional)") },
                        supportingText = {
                            Text("When you type the shortcut phrase, clip content will be suggested")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Pin to top",
                            color = theme.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (clipContent.isNotBlank()) {
                            if (editing != null) {
                                preferences.updateClipboardItem(
                                    id = editing.id,
                                    text = clipContent,
                                    isPinned = isPinned,
                                    shortcut = shortcut
                                )
                            } else {
                                preferences.addClipboardItem(
                                    text = clipContent,
                                    isPinned = isPinned,
                                    shortcut = shortcut
                                )
                            }
                        }
                        showAddClipDialog = false
                        editingClipItem = null
                    }
                ) {
                    Text("Save", color = theme.accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddClipDialog = false
                        editingClipItem = null
                    }
                ) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
            containerColor = theme.keyBackground
        )
    }
}

@Composable
private fun TopStrip(
    theme: KeyboardColorTheme,
    composingBuffer: String,
    suggestions: List<String>,
    lastTransliteratedUndo: Pair<String, String>?,
    isHindiTransliteration: Boolean,
    deleteSwipeCount: Int,
    onCandidateClick: (String) -> Unit,
    onUndoClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = theme.suggestionBackground
    ) {
        if (deleteSwipeCount > 0) {
            // Visual feedback when swiping left on delete key
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFFE57373))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Release to delete $deleteSwipeCount | Slide right to cancel",
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else if (composingBuffer.isNotEmpty() && suggestions.isNotEmpty()) {
            // Live Suggestion Bar (Screenshot 4 & 2)
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(suggestions.size) { index ->
                    val word = suggestions[index]
                    val isFirst = index == 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isFirst) theme.suggestionActive.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .clickable { onCandidateClick(word) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word,
                                color = if (isFirst) theme.suggestionActive else theme.textPrimary,
                                fontSize = if (isFirst) 17.sp else 15.sp,
                                fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (index < suggestions.size - 1) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(20.dp)
                                    .padding(horizontal = 2.dp),
                                color = theme.dividerColor
                            )
                        }
                    }
                }
            }
        } else if (lastTransliteratedUndo != null) {
            // Undo pill (Screenshot 8: ↶ ge / ↶ ram)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.keyBackground,
                    border = BorderStroke(1.dp, theme.accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onUndoClick() }
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo transliteration",
                            tint = theme.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = lastTransliteratedUndo.second,
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // Default Action Strip: Clipboard, Settings, Hindi mode badge
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClipboardClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Clipboard",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Status Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isHindiTransliteration) theme.accentColor.copy(alpha = 0.15f) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isHindiTransliteration) theme.accentColor else theme.textSecondary)
                        )
                        Text(
                            text = if (isHindiTransliteration) "Hindi Mode (अ)" else "English Mode",
                            color = if (isHindiTransliteration) theme.accentColor else theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    keys: List<KeyModel>,
    theme: KeyboardColorTheme,
    heightScale: Float,
    keyTextScale: Float,
    showBorders: Boolean,
    isShifted: Boolean,
    isCapsLock: Boolean,
    isHindiTransliteration: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp = 3.dp,
    onKeyPress: (KeyModel) -> Unit,
    onKeyLongPress: (KeyModel) -> Unit,
    onSpaceSwipeMove: (Int) -> Unit,
    onDeleteSwipe: (Int) -> Unit,
    onDeleteSwipeRelease: (Int) -> Unit,
    onDeleteContinuous: () -> Unit
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
            KeyView(
                modifier = Modifier
                    .weight(key.weight)
                    .height(keyHeight),
                key = key,
                theme = theme,
                keyTextScale = keyTextScale,
                showBorder = showBorders,
                isShifted = isShifted,
                isCapsLock = isCapsLock,
                isHindiTransliteration = isHindiTransliteration,
                onClick = { onKeyPress(key) },
                onLongClick = { onKeyLongPress(key) },
                onSpaceSwipeMove = onSpaceSwipeMove,
                onDeleteSwipe = onDeleteSwipe,
                onDeleteSwipeRelease = onDeleteSwipeRelease,
                onDeleteContinuous = onDeleteContinuous
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyView(
    modifier: Modifier = Modifier,
    key: KeyModel,
    theme: KeyboardColorTheme,
    keyTextScale: Float,
    showBorder: Boolean,
    isShifted: Boolean,
    isCapsLock: Boolean,
    isHindiTransliteration: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSpaceSwipeMove: (Int) -> Unit,
    onDeleteSwipe: (Int) -> Unit,
    onDeleteSwipeRelease: (Int) -> Unit,
    onDeleteContinuous: () -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val isActionKey = key.type == KeyType.ENTER
    val isSpecialKey = key.type in listOf(KeyType.SHIFT, KeyType.BACKSPACE, KeyType.MODE_SWITCH)

    val keyBg = when {
        isActionKey -> theme.actionKeyBackground
        isSpecialKey -> theme.keyBackground.copy(alpha = 0.75f)
        else -> theme.keyBackground
    }

    val keyTextClr = when {
        isActionKey -> theme.actionKeyContent
        else -> theme.textPrimary
    }

    val displayLabel = remember(key.primary, isShifted, isCapsLock) {
        if (key.primary.length == 1 && key.primary[0].isLetter()) {
            if (isShifted || isCapsLock) key.primary.uppercase() else key.primary.lowercase()
        } else {
            key.primary
        }
    }

    // Touch modifier specific to key type
    val touchModifier = when (key.type) {
        KeyType.BACKSPACE -> {
            // Requirement 2: Swipe left to select characters/words for delete, swipe right to unselect.
            // Hold delete key to delete continuously.
            var totalDragLeftPx by remember { mutableFloatStateOf(0f) }
            var isDragging by remember { mutableStateOf(false) }
            val thresholdPx = with(density) { 24.dp.toPx() }

            Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragLeftPx = 0f
                        isDragging = false
                    },
                    onDragEnd = {
                        val count = if (totalDragLeftPx > thresholdPx) {
                            ((totalDragLeftPx - thresholdPx) / thresholdPx).toInt() + 1
                        } else 0

                        if (count > 0) {
                            onDeleteSwipeRelease(count)
                        } else {
                            onDeleteSwipe(0)
                            if (!isDragging) {
                                onClick()
                            }
                        }
                        totalDragLeftPx = 0f
                        isDragging = false
                    },
                    onDragCancel = {
                        onDeleteSwipe(0)
                        totalDragLeftPx = 0f
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        isDragging = true
                        // Dragging left means negative dragAmount.x
                        totalDragLeftPx = max(0f, totalDragLeftPx - dragAmount.x)
                        val count = if (totalDragLeftPx > thresholdPx) {
                            ((totalDragLeftPx - thresholdPx) / thresholdPx).toInt() + 1
                        } else 0
                        onDeleteSwipe(count)
                    }
                )
            }.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        // Hold to delete continuously
                        coroutineScope.launch {
                            while (isActive) {
                                onDeleteContinuous()
                                delay(65)
                            }
                        }
                    }
                )
            }
        }

        KeyType.SPACE -> {
            // Requirement 3: Swipe space key horizontally to move cursor easily
            var accumulatedDx by remember { mutableFloatStateOf(0f) }
            var isDragging by remember { mutableStateOf(false) }
            val stepPx = with(density) { 18.dp.toPx() }

            Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        accumulatedDx = 0f
                        isDragging = false
                    },
                    onDragEnd = {
                        if (!isDragging) {
                            onClick()
                        }
                        accumulatedDx = 0f
                        isDragging = false
                    },
                    onDragCancel = {
                        accumulatedDx = 0f
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        isDragging = true
                        accumulatedDx += dragAmount.x
                        if (accumulatedDx >= stepPx) {
                            onSpaceSwipeMove(1)
                            accumulatedDx -= stepPx
                        } else if (accumulatedDx <= -stepPx) {
                            onSpaceSwipeMove(-1)
                            accumulatedDx += stepPx
                        }
                    }
                )
            }.clickable { onClick() }
        }

        else -> {
            Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(keyBg)
            .then(
                if (showBorder) {
                    Modifier.border(
                        BorderStroke(
                            0.75.dp,
                            if (isActionKey) theme.actionKeyBackground else theme.keyBorderColor
                        ),
                        RoundedCornerShape(6.dp)
                    )
                } else Modifier
            )
            .then(touchModifier),
        contentAlignment = Alignment.Center
    ) {
        // Main Key Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (key.type) {
                KeyType.SHIFT -> {
                    Icon(
                        imageVector = if (isCapsLock) Icons.Default.KeyboardCapslock else Icons.Default.ArrowUpward,
                        contentDescription = "Shift",
                        tint = if (isShifted || isCapsLock) theme.accentColor else theme.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                KeyType.BACKSPACE -> {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                KeyType.ENTER -> {
                    Icon(
                        imageVector = Icons.Default.KeyboardReturn,
                        contentDescription = "Enter",
                        tint = theme.actionKeyContent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                KeyType.HINDI_TOGGLE -> {
                    // "अ" key with Green underline indicator when ON (Screenshots 3 & 4)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "अ",
                            color = if (isHindiTransliteration) theme.accentColor else theme.textPrimary,
                            fontSize = (18 * keyTextScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isHindiTransliteration) Color(0xFF4CAF50) else Color.Transparent)
                        )
                    }
                }

                KeyType.SPACE -> {
                    // Spacebar sleek line with subtle cursor guidance indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(theme.textSecondary.copy(alpha = 0.35f))
                        )
                    }
                }

                else -> {
                    Text(
                        text = displayLabel,
                        color = keyTextClr,
                        fontSize = (if (key.primary.length > 2) 13 else 19 * keyTextScale.toInt()).sp,
                        fontWeight = if (key.type == KeyType.CHARACTER) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Secondary superscript character (Requirement 6: no emoji on comma or other keys)
        if (key.secondary != null && key.type != KeyType.SPACE && key.type != KeyType.HINDI_TOGGLE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 4.dp, top = 2.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = key.secondary,
                    color = theme.textSecondary.copy(alpha = 0.85f),
                    fontSize = (9 * keyTextScale).sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ClipboardOverlayView(
    theme: KeyboardColorTheme,
    items: List<ClipboardItem>,
    onClose: () -> Unit,
    onSelectItem: (ClipboardItem) -> Unit,
    onAddNewClip: () -> Unit,
    onEditClip: (ClipboardItem) -> Unit,
    onDeleteClip: (ClipboardItem) -> Unit
) {
    val pinned = items.filter { it.isPinned }
    val recent = items.filter { !it.isPinned }.take(20)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.keyboardBackground)
            .padding(8.dp)
    ) {
        // Header (Screenshot 5)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }
                Text(
                    text = "Clipboard (${items.size}/20)",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onAddNewClip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.keyBackground,
                    contentColor = theme.textPrimary
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(16.dp),
                    tint = theme.accentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Clips Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pinned.isNotEmpty()) {
                items(pinned) { item ->
                    ClipCard(
                        item = item,
                        theme = theme,
                        onClick = { onSelectItem(item) },
                        onEdit = { onEditClip(item) },
                        onDelete = { onDeleteClip(item) }
                    )
                }
            }

            if (recent.isNotEmpty()) {
                items(recent) { item ->
                    ClipCard(
                        item = item,
                        theme = theme,
                        onClick = { onSelectItem(item) },
                        onEdit = { onEditClip(item) },
                        onDelete = { onDeleteClip(item) }
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    Text(
                        text = "Clipboard is empty.\nCopied items will automatically appear here.",
                        color = theme.textSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClipCard(
    item: ClipboardItem,
    theme: KeyboardColorTheme,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = theme.keyBackground,
        border = BorderStroke(0.5.dp, theme.keyBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = theme.accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Supports up to 50k tokens cleanly displayed with ellipsis
            Text(
                text = item.text,
                color = theme.textPrimary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!item.shortcut.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Shortcut: ${item.shortcut}",
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
