package com.spdev.litehindi.engine

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Roman (Latin) -> Devanagari transliteration.
 *
 * Two layers:
 *   1. A curated dictionary (assets/hindi_dict.json, ~1300 roman keys) for accuracy
 *      on real words: "khata" -> खाता, "main" -> मैं, "nahi" -> नहीं.
 *   2. A rule-based phonetic converter for anything the dictionary does not know.
 *
 * Performance notes (this runs on every keystroke of a 2 GB phone):
 *   - All lookup tables are top-level `val`s, built once. Nothing is allocated
 *     per call except the output StringBuilder.
 *   - No substring() anywhere: matching uses String.startsWith(prefix, index).
 *   - Dictionary loads once, off the main thread.
 */
object HindiEngine {

    private var dict: HashMap<String, Array<String>>? = null

    @Volatile
    private var loading = false

    /** Small built-in set so the very first keystrokes are already accurate. */
    private val CORE = HashMap<String, Array<String>>(48).apply {
        put("hai", arrayOf("है"))
        put("hain", arrayOf("हैं"))
        put("main", arrayOf("मैं"))
        put("mein", arrayOf("में"))
        put("nahi", arrayOf("नहीं"))
        put("nahin", arrayOf("नहीं"))
        put("aap", arrayOf("आप"))
        put("aur", arrayOf("और"))
        put("ek", arrayOf("एक"))
        put("se", arrayOf("से"))
        put("ke", arrayOf("के"))
        put("ka", arrayOf("का"))
        put("ki", arrayOf("की"))
        put("ko", arrayOf("को"))
        put("ye", arrayOf("ये"))
        put("yeh", arrayOf("यह"))
        put("wo", arrayOf("वो"))
        put("woh", arrayOf("वह"))
        put("hum", arrayOf("हम"))
        put("tum", arrayOf("तुम"))
        put("kya", arrayOf("क्या"))
        put("kaise", arrayOf("कैसे"))
        put("theek", arrayOf("ठीक"))
        put("acha", arrayOf("अच्छा"))
        put("bahut", arrayOf("बहुत"))
        put("ghar", arrayOf("घर"))
        put("pani", arrayOf("पानी"))
        put("namaste", arrayOf("नमस्ते"))
        put("dhanyavad", arrayOf("धन्यवाद"))
        put("kripya", arrayOf("कृपया"))
        put("ram", arrayOf("राम"))
        put("bharat", arrayOf("भारत"))
        put("desh", arrayOf("देश"))
        put("hindi", arrayOf("हिंदी"))
    }

    /** Call once, from a background thread. Safe to call repeatedly. */
    fun load(context: Context) {
        if (dict != null || loading) return
        loading = true
        try {
            val text = context.assets.open("hindi_dict.json").use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            }
            val json = JSONObject(text)
            val map = HashMap<String, Array<String>>(json.length() * 2)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = json.optJSONArray(k) ?: continue
                val list = ArrayList<String>(arr.length())
                for (i in 0 until arr.length()) {
                    val w = arr.optString(i, "")
                    if (w.isNotEmpty()) list.add(w)
                }
                if (list.isNotEmpty()) map[k] = list.toTypedArray()
            }
            dict = map
        } catch (_: Throwable) {
            dict = HashMap()
        } finally {
            loading = false
        }
    }

    fun isReady(): Boolean = dict != null

    // ---------------------------------------------------------------- consonants

    /** Multi-letter consonants, longest first so "chh" wins over "ch". */
    private val DIGRAPHS: Array<Pair<String, String>> = arrayOf(
        "ksh" to "क्ष्",
        "chh" to "छ्",
        "sh" to "श्",
        "ch" to "च्",
        "kh" to "ख्",
        "gh" to "घ्",
        "jh" to "झ्",
        "th" to "थ्",
        "dh" to "ध्",
        "ph" to "फ्",
        "bh" to "भ्",
        "ny" to "ञ्",
        "tr" to "त्र्",
        "gy" to "ज्ञ्"
    )

    private fun singleConsonant(c: Char): String? = when (c) {
        'k' -> "क्"; 'g' -> "ग्"; 'c' -> "च्"; 'j' -> "ज्"
        't' -> "त्"; 'd' -> "द्"; 'n' -> "न्"; 'p' -> "प्"
        'b' -> "ब्"; 'm' -> "म्"; 'y' -> "य्"; 'r' -> "र्"
        'l' -> "ल्"; 'v', 'w' -> "व्"; 's' -> "स्"; 'h' -> "ह्"
        'z' -> "ज़्"; 'f' -> "फ़्"; 'q' -> "क़्"; 'x' -> "क्स्"
        else -> null
    }

    private fun isConsonantAt(s: String, i: Int): Boolean {
        if (i >= s.length) return false
        if (singleConsonant(s[i]) != null) return true
        for (d in DIGRAPHS) if (s.startsWith(d.first, i)) return true
        return false
    }

    // -------------------------------------------------------------------- vowels

    private const val MATRA_A = ""
    private const val MATRA_AA = "ा"

    // ------------------------------------------------------------------- convert

    /**
     * Rule-based roman -> Devanagari. Allocation-light: one StringBuilder.
     */
    fun phonetic(raw: String): String {
        val s = raw.lowercase()
        val n = s.length
        val sb = StringBuilder(n + 8)
        var i = 0
        var prevConsonant = false

        while (i < n) {
            // 1. multi-letter consonant
            var digraph: String? = null
            var digraphLen = 0
            for (d in DIGRAPHS) {
                if (s.startsWith(d.first, i)) {
                    digraph = d.second
                    digraphLen = d.first.length
                    break
                }
            }
            if (digraph != null) {
                sb.append(digraph)
                prevConsonant = true
                i += digraphLen
                continue
            }

            val ch = s[i]
            val cons = singleConsonant(ch)

            // 2. consonant — but n/m before another consonant becomes anusvara
            if (cons != null) {
                if ((ch == 'n' || ch == 'm') && !prevConsonant && isConsonantAt(s, i + 1)) {
                    sb.append('ं')
                    prevConsonant = false
                    i++
                    continue
                }
                sb.append(cons)
                prevConsonant = true
                i++
                continue
            }

            // 3. vowels (two-letter first)
            var matra: String? = null
            var independent: String? = null
            var vLen = 0

            if (i + 1 < n) {
                val c2 = s[i + 1]
                if (ch == 'a' && c2 == 'a') { matra = "ा"; independent = "आ"; vLen = 2 }
                else if (ch == 'a' && c2 == 'i') { matra = "ै"; independent = "ऐ"; vLen = 2 }
                else if (ch == 'a' && c2 == 'u') { matra = "ौ"; independent = "औ"; vLen = 2 }
                else if (ch == 'e' && c2 == 'e') { matra = "ी"; independent = "ई"; vLen = 2 }
                else if (ch == 'i' && c2 == 'i') { matra = "ी"; independent = "ई"; vLen = 2 }
                else if (ch == 'o' && c2 == 'o') { matra = "ू"; independent = "ऊ"; vLen = 2 }
                else if (ch == 'u' && c2 == 'u') { matra = "ू"; independent = "ऊ"; vLen = 2 }
                else if (ch == 'o' && c2 == 'u') { matra = "ौ"; independent = "औ"; vLen = 2 }
            }
            if (vLen == 0) {
                when (ch) {
                    'a' -> { matra = MATRA_A; independent = "अ"; vLen = 1 }
                    'i' -> { matra = "ि"; independent = "इ"; vLen = 1 }
                    'u' -> { matra = "ु"; independent = "उ"; vLen = 1 }
                    'e' -> { matra = "े"; independent = "ए"; vLen = 1 }
                    'o' -> { matra = "ो"; independent = "ओ"; vLen = 1 }
                }
            }

            if (vLen > 0) {
                if (prevConsonant) {
                    if (sb.isNotEmpty() && sb[sb.length - 1] == '्') sb.deleteCharAt(sb.length - 1)
                    var m = matra ?: MATRA_A
                    // A word-final single "a" after a consonant reads as "aa"
                    // in Hinglish typing: khata -> खाता, saja -> सजा.
                    if (m.isEmpty() && vLen == 1 && ch == 'a' && i == n - 1) m = MATRA_AA
                    sb.append(m)
                } else {
                    sb.append(independent ?: "")
                }
                prevConsonant = false
                i += vLen
                continue
            }

            // 4. anything else passes through (digits, punctuation)
            sb.append(ch)
            prevConsonant = false
            i++
        }

        if (sb.isNotEmpty() && sb[sb.length - 1] == '्') sb.deleteCharAt(sb.length - 1)
        return sb.toString()
    }

    // ------------------------------------------------------------------ suggest

    /**
     * Candidate list for the word being typed.
     * Order: dictionary hits, rule-based form, spelling variants, then the raw
     * Latin word so English typing is always one tap away.
     */
    fun suggest(latin: String, max: Int = 4): List<String> {
        val key = latin.trim().lowercase()
        if (key.isEmpty()) return emptyList()

        val out = ArrayList<String>(max + 1)

        CORE[key]?.let { for (w in it) if (out.size < max) out.add(w) }
        dict?.get(key)?.let { for (w in it) if (out.size < max && !out.contains(w)) out.add(w) }

        val phonetic = phonetic(key)
        if (phonetic.isNotEmpty() && out.size < max && !out.contains(phonetic)) out.add(phonetic)

        if (out.size < max) {
            val alt = variant(key)
            if (alt != null) {
                dict?.get(alt)?.let { for (w in it) if (out.size < max && !out.contains(w)) out.add(w) }
            }
        }

        if (out.size < max && !out.contains(key)) out.add(key)
        return out
    }

    /** Common Hinglish spelling drift: aa/a, ee/i, oo/u. */
    private fun variant(k: String): String? = when {
        k.contains("aa") -> k.replace("aa", "a")
        k.contains("ee") -> k.replace("ee", "i")
        k.contains("oo") -> k.replace("oo", "u")
        k.contains('a') -> k.replace("a", "aa")
        k.contains('i') -> k.replace("i", "ee")
        k.contains('u') -> k.replace("u", "oo")
        else -> null
    }
}
