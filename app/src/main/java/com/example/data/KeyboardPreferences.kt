package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class KeyboardPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeId = MutableStateFlow(prefs.getString(KEY_THEME_ID, "slate_teal") ?: "slate_teal")
    val themeId: StateFlow<String> = _themeId.asStateFlow()

    private val _heightScale = MutableStateFlow(prefs.getFloat(KEY_HEIGHT_SCALE, 1.0f))
    val heightScale: StateFlow<Float> = _heightScale.asStateFlow()

    private val _keyTextScale = MutableStateFlow(prefs.getFloat(KEY_KEY_TEXT_SCALE, 1.0f))
    val keyTextScale: StateFlow<Float> = _keyTextScale.asStateFlow()

    private val _bottomSpace = MutableStateFlow(prefs.getBoolean(KEY_BOTTOM_SPACE, true))
    val bottomSpace: StateFlow<Boolean> = _bottomSpace.asStateFlow()

    private val _bottomSpaceDp = MutableStateFlow(prefs.getInt(KEY_BOTTOM_SPACE_DP, 10))
    val bottomSpaceDp: StateFlow<Int> = _bottomSpaceDp.asStateFlow()

    private val _showKeyBorders = MutableStateFlow(prefs.getBoolean(KEY_SHOW_KEY_BORDERS, true))
    val showKeyBorders: StateFlow<Boolean> = _showKeyBorders.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _soundOnKeypress = MutableStateFlow(prefs.getBoolean(KEY_SOUND, false))
    val soundOnKeypress: StateFlow<Boolean> = _soundOnKeypress.asStateFlow()

    private val _hindiTransliteration = MutableStateFlow(prefs.getBoolean(KEY_HINDI_TRANSLITERATION, true))
    val hindiTransliteration: StateFlow<Boolean> = _hindiTransliteration.asStateFlow()

    private val _autoCommitOnSpace = MutableStateFlow(prefs.getBoolean(KEY_AUTO_COMMIT_ON_SPACE, true))
    val autoCommitOnSpace: StateFlow<Boolean> = _autoCommitOnSpace.asStateFlow()

    private val _showNumberRow = MutableStateFlow(prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true))
    val showNumberRow: StateFlow<Boolean> = _showNumberRow.asStateFlow()

    private val _clipboardItems = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val clipboardItems: StateFlow<List<ClipboardItem>> = _clipboardItems.asStateFlow()

    init {
        loadClipboardItems()
    }

    fun setThemeId(id: String) {
        prefs.edit().putString(KEY_THEME_ID, id).apply()
        _themeId.value = id
    }

    fun setHeightScale(scale: Float) {
        prefs.edit().putFloat(KEY_HEIGHT_SCALE, scale).apply()
        _heightScale.value = scale
    }

    fun setKeyTextScale(scale: Float) {
        prefs.edit().putFloat(KEY_KEY_TEXT_SCALE, scale).apply()
        _keyTextScale.value = scale
    }

    fun setBottomSpace(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BOTTOM_SPACE, enabled).apply()
        _bottomSpace.value = enabled
    }

    fun setBottomSpaceDp(dp: Int) {
        prefs.edit().putInt(KEY_BOTTOM_SPACE_DP, dp).apply()
        _bottomSpaceDp.value = dp
    }

    fun setShowKeyBorders(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_KEY_BORDERS, show).apply()
        _showKeyBorders.value = show
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setSoundOnKeypress(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundOnKeypress.value = enabled
    }

    fun setHindiTransliteration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HINDI_TRANSLITERATION, enabled).apply()
        _hindiTransliteration.value = enabled
    }

    fun setAutoCommitOnSpace(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_COMMIT_ON_SPACE, enabled).apply()
        _autoCommitOnSpace.value = enabled
    }

    fun setShowNumberRow(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, show).apply()
        _showNumberRow.value = show
    }

    // --- Clipboard persistence ---
    private fun loadClipboardItems() {
        val raw = prefs.getString(KEY_CLIPBOARD_DATA, null) ?: return
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ClipboardItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ClipboardItem(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false),
                        shortcut = if (obj.has("shortcut") && !obj.isNull("shortcut")) obj.getString("shortcut") else null
                    )
                )
            }
            _clipboardItems.value = list
        } catch (_: Exception) {
            _clipboardItems.value = emptyList()
        }
    }

    private fun saveClipboardItems(list: List<ClipboardItem>) {
        _clipboardItems.value = list
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("timestamp", item.timestamp)
            obj.put("isPinned", item.isPinned)
            obj.put("shortcut", item.shortcut ?: JSONObject.NULL)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CLIPBOARD_DATA, arr.toString()).apply()
    }

    fun addClipboardItem(text: String, isPinned: Boolean = false, shortcut: String? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        // Support up to 50k tokens (~250,000 characters)
        val valueText = if (trimmed.length > 300_000) trimmed.take(300_000) else trimmed
        val current = _clipboardItems.value.toMutableList()
        // Deduplicate recent unpinned
        if (!isPinned) {
            // If already the top item, ignore
            if (current.firstOrNull { !it.isPinned }?.text == valueText) return
            current.removeAll { !it.isPinned && it.text == valueText }
        }
        val item = ClipboardItem(
            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
            text = valueText,
            isPinned = isPinned,
            shortcut = shortcut?.takeIf { it.isNotBlank() }
        )
        current.add(0, item)
        // Requirement 8: Keyboard's clipboard can store up to 20 values
        val pinned = current.filter { it.isPinned }
        val recent = current.filter { !it.isPinned }.take(20)
        saveClipboardItems(pinned + recent)
    }

    fun updateClipboardItem(id: String, text: String, isPinned: Boolean, shortcut: String?) {
        val current = _clipboardItems.value.map {
            if (it.id == id) it.copy(text = text, isPinned = isPinned, shortcut = shortcut?.takeIf { s -> s.isNotBlank() })
            else it
        }
        saveClipboardItems(current)
    }

    fun deleteClipboardItem(id: String) {
        val current = _clipboardItems.value.filterNot { it.id == id }
        saveClipboardItems(current)
    }

    fun clearRecentClipboard() {
        val current = _clipboardItems.value.filter { it.isPinned }
        saveClipboardItems(current)
    }

    fun clearPinnedClipboard() {
        val current = _clipboardItems.value.filterNot { it.isPinned }
        saveClipboardItems(current)
    }

    // --- Typed Words stats & storage ---
    fun getTypedEnglishWordsCount(): Int = prefs.getInt(KEY_COUNT_TYPED_EN, 142)
    fun getTypedHindiWordsCount(): Int = prefs.getInt(KEY_COUNT_TYPED_HI, 386)
    fun getSavedHindiWordsCount(): Int = prefs.getInt(KEY_COUNT_SAVED_HI, 12)

    fun incrementTypedWord(isHindi: Boolean) {
        if (isHindi) {
            val count = prefs.getInt(KEY_COUNT_TYPED_HI, 386) + 1
            prefs.edit().putInt(KEY_COUNT_TYPED_HI, count).apply()
        } else {
            val count = prefs.getInt(KEY_COUNT_TYPED_EN, 142) + 1
            prefs.edit().putInt(KEY_COUNT_TYPED_EN, count).apply()
        }
    }

    fun clearTypedEnglishWords() {
        prefs.edit().putInt(KEY_COUNT_TYPED_EN, 0).apply()
    }

    fun clearTypedHindiWords() {
        prefs.edit().putInt(KEY_COUNT_TYPED_HI, 0).apply()
    }

    fun clearSavedHindiWords() {
        prefs.edit().putInt(KEY_COUNT_SAVED_HI, 0).apply()
    }

    companion object {
        private const val PREFS_NAME = "lite_hindi_keyboard_prefs"
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_HEIGHT_SCALE = "height_scale"
        private const val KEY_KEY_TEXT_SCALE = "key_text_scale"
        private const val KEY_BOTTOM_SPACE = "bottom_space"
        private const val KEY_BOTTOM_SPACE_DP = "bottom_space_dp"
        private const val KEY_SHOW_KEY_BORDERS = "show_key_borders"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SOUND = "sound_keypress"
        private const val KEY_HINDI_TRANSLITERATION = "hindi_transliteration"
        private const val KEY_AUTO_COMMIT_ON_SPACE = "auto_commit_on_space"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_CLIPBOARD_DATA = "clipboard_data"
        private const val KEY_COUNT_TYPED_EN = "count_typed_en"
        private const val KEY_COUNT_TYPED_HI = "count_typed_hi"
        private const val KEY_COUNT_SAVED_HI = "count_saved_hi"

        @Volatile
        private var instance: KeyboardPreferences? = null

        fun getInstance(context: Context): KeyboardPreferences {
            return instance ?: synchronized(this) {
                instance ?: KeyboardPreferences(context).also { instance = it }
            }
        }
    }
}
