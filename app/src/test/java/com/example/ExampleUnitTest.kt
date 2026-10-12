package com.example

import com.example.engine.HindiTransliterator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testHindiTransliteratorRam() {
    val suggestions = HindiTransliterator.transliterate("ram")
    assertTrue(suggestions.isNotEmpty())
    assertEquals("राम", suggestions[0])
    assertTrue(suggestions.contains("ram"))
  }

  @Test
  fun testHindiTransliteratorKhata() {
    val suggestions = HindiTransliterator.transliterate("khata")
    assertTrue(suggestions.isNotEmpty())
    assertEquals("खाता", suggestions[0])
    assertTrue(suggestions.contains("khata"))
  }

  @Test
  fun testHindiTransliteratorSaja() {
    val suggestions = HindiTransliterator.transliterate("saja")
    assertTrue(suggestions.isNotEmpty())
    assertTrue(suggestions[0] == "सज़ा" || suggestions[0] == "सजा")
  }
}
