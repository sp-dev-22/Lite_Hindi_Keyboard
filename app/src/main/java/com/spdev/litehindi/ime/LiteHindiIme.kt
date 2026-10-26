package com.spdev.litehindi.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.spdev.litehindi.MainActivity
import com.spdev.litehindi.data.Prefs
import com.spdev.litehindi.data.Themes
import com.spdev.litehindi.engine.HindiEngine
import com.spdev.litehindi.ui.AppTheme
import com.spdev.litehindi.ui.KeyboardListener
import com.spdev.litehindi.ui.KeyboardRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * The actual IME.
 *
 * Design rules:
 *  - The Latin word being typed lives in the editor as a *composing region*
 *    (setComposingText), so the host app knows about it, backspace works,
 *    and nothing is lost when focus moves.
 *  - Only Devanagari (or English) is ever committed.
 *  - No clipboard reading, no network, no analytics, no vibration.
 */
class LiteHindiIme :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val registry = LifecycleRegistry(this)
    private val viewModelStore = ViewModelStore()
    private val savedState = SavedStateRegistryController.create(this)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Bumped for every new input session so the UI drops any half-typed word. */
    private val sessionKey = MutableStateFlow(0)
    private val numericField = MutableStateFlow(false)

    override val lifecycle: Lifecycle get() = registry
    override val viewModelStore: ViewModelStore get() = viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedState.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        // Dictionary is 49 KB of JSON; parse it off the main thread, once.
        scope.launch { HindiEngine.load(this@LiteHindiIme) }
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        val type = info?.inputType ?: 0
        val cls = type and InputType.TYPE_MASK_CLASS
        numericField.value =
            cls == InputType.TYPE_CLASS_NUMBER ||
            cls == InputType.TYPE_CLASS_PHONE ||
            cls == InputType.TYPE_CLASS_DATETIME
        sessionKey.value = sessionKey.value + 1
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (registry.currentState == Lifecycle.State.CREATED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        if (registry.currentState == Lifecycle.State.STARTED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        if (registry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        if (registry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        super.onFinishInputView(finishingInput)
    }

    override fun onCreateInputView(): View {
        val prefs = Prefs.get(this)

        val view = ComposeView(this)
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)

        view.setContent {
            val themeId by prefs.themeId.collectAsState()
            val session by sessionKey.collectAsState()
            val numeric by numericField.collectAsState()

            AppTheme(Themes.byId(themeId)) {
                KeyboardRoot(
                    theme = Themes.byId(themeId),
                    prefs = prefs,
                    listener = connection,
                    sessionKey = session,
                    numericField = numeric
                )
            }
        }
        return view
    }

    override fun onDestroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
        scope.cancel()
        super.onDestroy()
    }

    // ------------------------------------------------------------- input plumbing

    private val connection = object : KeyboardListener {

        override fun commitText(text: String) {
            val ic = currentInputConnection ?: return
            ic.commitText(text, 1)
            ic.finishComposingText()
        }

        override fun setComposing(text: String) {
            currentInputConnection?.setComposingText(text, 1)
        }

        override fun finishComposing() {
            currentInputConnection?.finishComposingText()
        }

        override fun deleteBackward(count: Int) {
            if (count <= 0) return
            val ic = currentInputConnection ?: return
            val selected = ic.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                ic.commitText("", 1)
                return
            }
            ic.deleteSurroundingText(count, 0)
        }

        override fun moveCursor(delta: Int) {
            if (delta == 0) return
            val ic = currentInputConnection ?: return
            val start = selectionStart(ic) ?: return
            val target = (start + delta).coerceAtLeast(0)
            ic.setSelection(target, target)
        }

        override fun onEnter() {
            if (!sendDefaultEditorAction(true)) {
                val ic = currentInputConnection ?: return
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }

        override fun openSettings() {
            val intent = Intent(this@LiteHindiIme, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    /**
     * Cursor movement without faking arrow key events: arrow keys move focus in
     * many apps instead of the caret. getSurroundingText is the reliable path,
     * getExtractedText is the fallback for editors that only support extraction.
     */
    private fun selectionStart(ic: InputConnection): Int? {
        val surrounding = ic.getSurroundingText(1, 1)
        if (surrounding != null) return surrounding.selectionStart
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
        return extracted?.selectionStart
    }
}
