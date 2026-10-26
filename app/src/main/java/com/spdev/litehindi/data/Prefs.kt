package com.spdev.litehindi.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for every user setting.
 * Deliberately tiny: SharedPreferences only, no database, no DataStore,
 * no JSON blobs. The keyboard stores nothing about what you type.
 */
class Prefs private constructor(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("lite_hindi_prefs", Context.MODE_PRIVATE)

    private val _themeId = MutableStateFlow(sp.getString(K_THEME, "slate_teal") ?: "slate_teal")
    val themeId: StateFlow<String> = _themeId

    private val _heightScale = MutableStateFlow(sp.getFloat(K_HEIGHT, 1.0f))
    val heightScale: StateFlow<Float> = _heightScale

    private val _keyTextScale = MutableStateFlow(sp.getFloat(K_TEXT, 1.0f))
    val keyTextScale: StateFlow<Float> = _keyTextScale

    private val _bottomSpace = MutableStateFlow(sp.getBoolean(K_BOTTOM, true))
    val bottomSpace: StateFlow<Boolean> = _bottomSpace

    private val _bottomSpaceDp = MutableStateFlow(sp.getInt(K_BOTTOM_DP, 8))
    val bottomSpaceDp: StateFlow<Int> = _bottomSpaceDp

    private val _keyBorders = MutableStateFlow(sp.getBoolean(K_BORDERS, true))
    val keyBorders: StateFlow<Boolean> = _keyBorders

    private val _hindiAuto = MutableStateFlow(sp.getBoolean(K_HINDI_AUTO, true))
    val hindiAuto: StateFlow<Boolean> = _hindiAuto

    private val _showSuggestions = MutableStateFlow(sp.getBoolean(K_SUGGESTIONS, true))
    val showSuggestions: StateFlow<Boolean> = _showSuggestions

    private val _showNumberRow = MutableStateFlow(sp.getBoolean(K_NUMBER_ROW, true))
    val showNumberRow: StateFlow<Boolean> = _showNumberRow

    fun setThemeId(v: String) { _themeId.value = v; sp.edit().putString(K_THEME, v).apply() }
    fun setHeightScale(v: Float) { _heightScale.value = v; sp.edit().putFloat(K_HEIGHT, v).apply() }
    fun setKeyTextScale(v: Float) { _keyTextScale.value = v; sp.edit().putFloat(K_TEXT, v).apply() }
    fun setBottomSpace(v: Boolean) { _bottomSpace.value = v; sp.edit().putBoolean(K_BOTTOM, v).apply() }
    fun setBottomSpaceDp(v: Int) { _bottomSpaceDp.value = v; sp.edit().putInt(K_BOTTOM_DP, v).apply() }
    fun setKeyBorders(v: Boolean) { _keyBorders.value = v; sp.edit().putBoolean(K_BORDERS, v).apply() }
    fun setHindiAuto(v: Boolean) { _hindiAuto.value = v; sp.edit().putBoolean(K_HINDI_AUTO, v).apply() }
    fun setShowSuggestions(v: Boolean) { _showSuggestions.value = v; sp.edit().putBoolean(K_SUGGESTIONS, v).apply() }
    fun setShowNumberRow(v: Boolean) { _showNumberRow.value = v; sp.edit().putBoolean(K_NUMBER_ROW, v).apply() }

    fun resetToDefaults() {
        sp.edit().clear().apply()
        _themeId.value = "slate_teal"
        _heightScale.value = 1.0f
        _keyTextScale.value = 1.0f
        _bottomSpace.value = true
        _bottomSpaceDp.value = 8
        _keyBorders.value = true
        _hindiAuto.value = true
        _showSuggestions.value = true
        _showNumberRow.value = true
    }

    companion object {
        private const val K_THEME = "theme_id"
        private const val K_HEIGHT = "height_scale"
        private const val K_TEXT = "key_text_scale"
        private const val K_BOTTOM = "bottom_space"
        private const val K_BOTTOM_DP = "bottom_space_dp"
        private const val K_BORDERS = "key_borders"
        private const val K_HINDI_AUTO = "hindi_auto"
        private const val K_SUGGESTIONS = "show_suggestions"
        private const val K_NUMBER_ROW = "show_number_row"

        @Volatile
        private var instance: Prefs? = null

        fun get(context: Context): Prefs {
            return instance ?: synchronized(this) {
                instance ?: Prefs(context).also { instance = it }
            }
        }
    }
}
