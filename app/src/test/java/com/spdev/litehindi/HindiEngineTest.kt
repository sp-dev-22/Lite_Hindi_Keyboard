package com.spdev.litehindi

import com.spdev.litehindi.engine.HindiEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for the transliteration rules. No Android, no device needed.
 * These run in CI with `gradle testDebugUnitTest`.
 */
class HindiEngineTest {

    @Test
    fun basicWords() {
        assertEquals("राम", HindiEngine.phonetic("raam"))
        assertEquals("खाता", HindiEngine.phonetic("khata"))
        assertEquals("है", HindiEngine.phonetic("hai"))
        assertEquals("नमस्ते", HindiEngine.phonetic("namaste"))
        assertEquals("भरत", HindiEngine.phonetic("bharat"))
        assertEquals("दिन", HindiEngine.phonetic("din"))
    }

    @Test
    fun anusvaraBeforeConsonant() {
        assertEquals("संत", HindiEngine.phonetic("sant"))
        assertEquals("चंद", HindiEngine.phonetic("chand"))
    }

    @Test
    fun digraphs() {
        assertEquals("छत", HindiEngine.phonetic("chhat"))
        assertEquals("शहर", HindiEngine.phonetic("shahar"))
    }

    @Test
    fun emptyInput() {
        assertEquals("", HindiEngine.phonetic(""))
    }

    @Test
    fun suggestionsKeepEnglishOption() {
        val list = HindiEngine.suggest("khata")
        assertEquals("खाता", list.first())
        assertTrue(list.contains("khata"))
    }

    @Test
    fun suggestionsForKnownWord() {
        val list = HindiEngine.suggest("ram")
        assertTrue(list.contains("राम"))
        assertTrue(list.contains("ram"))
    }
}
