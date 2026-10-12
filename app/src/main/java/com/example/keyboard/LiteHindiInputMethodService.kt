package com.example.keyboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.KeyboardPreferences
import com.example.data.ThemePresets

class LiteHindiInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var clipboardManager: ClipboardManager? = null
    private var clipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Requirement 7: copy clipboard's data into keyboard's clipboard automatically
        val prefs = KeyboardPreferences.getInstance(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            syncSystemClipboard(prefs)
        }
        clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
    }

    private fun syncSystemClipboard(prefs: KeyboardPreferences) {
        try {
            val clip = clipboardManager?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val text = item?.coerceToText(this)?.toString()?.trim()
                if (!text.isNullOrBlank()) {
                    prefs.addClipboardItem(text, isPinned = false)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val prefs = KeyboardPreferences.getInstance(this)
        syncSystemClipboard(prefs)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val windowDecorView = window?.window?.decorView
        if (windowDecorView != null) {
            windowDecorView.setViewTreeLifecycleOwner(this)
            windowDecorView.setViewTreeViewModelStoreOwner(this)
            windowDecorView.setViewTreeSavedStateRegistryOwner(this)
        }

        val prefs = KeyboardPreferences.getInstance(this)

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                val themeId = prefs.themeId.value
                val theme = ThemePresets.getById(themeId)

                LiteKeyboardRoot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    theme = theme,
                    preferences = prefs,
                    listener = object : KeyboardActionListener {
                        override fun onCommitText(text: String) {
                            currentInputConnection?.commitText(text, 1)
                        }

                        override fun onDeleteBackward() {
                            val conn = currentInputConnection ?: return
                            val selected = conn.getSelectedText(0)
                            if (!selected.isNullOrEmpty()) {
                                conn.commitText("", 1)
                            } else {
                                conn.deleteSurroundingText(1, 0)
                            }
                        }

                        // Requirement 2: Swipe left delete batch
                        override fun onDeleteBackward(count: Int) {
                            if (count <= 0) return
                            val conn = currentInputConnection ?: return
                            val selected = conn.getSelectedText(0)
                            if (!selected.isNullOrEmpty()) {
                                conn.commitText("", 1)
                            } else {
                                conn.deleteSurroundingText(count, 0)
                            }
                        }

                        // Requirement 3: Swipe space key to move cursor
                        override fun onMoveCursor(delta: Int) {
                            if (delta < 0) {
                                repeat(-delta) {
                                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
                                }
                            } else if (delta > 0) {
                                repeat(delta) {
                                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
                                }
                            }
                        }

                        override fun onEnter() {
                            val conn = currentInputConnection ?: return
                            sendDefaultEditorAction(true)
                        }

                        override fun onOpenSettings() {
                            val intent = Intent(this@LiteHindiInputMethodService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            }
                            startActivity(intent)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clipChangedListener?.let {
            clipboardManager?.removePrimaryClipChangedListener(it)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
