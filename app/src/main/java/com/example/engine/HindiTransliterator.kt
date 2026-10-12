package com.example.engine

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

object HindiTransliterator {

    // In-memory cache of dictionary loaded from assets/hindi_dict.json
    private var assetDictionary: Map<String, List<String>>? = null

    // Fallback frequent Hinglish -> Hindi word dictionary for instant O(1) matching
    private val frequentDictionary: Map<String, List<String>> = mapOf(
        "khata" to listOf("खाता", "ख़तम", "खता", "खटा"),
        "khaata" to listOf("खाता", "खता"),
        "khatm" to listOf("ख़त्म", "खत्म", "ख़तम"),
        "khatam" to listOf("ख़तम", "खत्म", "ख़त्म"),
        "ram" to listOf("राम", "रमेश", "रम", "क्रम", "रामा"),
        "raam" to listOf("राम", "रामा"),
        "saja" to listOf("सज़ा", "सजा"),
        "saza" to listOf("सज़ा", "सजा"),
        "aam" to listOf("आम"),
        "namaste" to listOf("नमस्ते", "नमस्तेजी", "नमन"),
        "kya" to listOf("क्या", "कया", "ख्याल"),
        "hai" to listOf("है", "हैं", "हो"),
        "hain" to listOf("हैं", "है", "हैन"),
        "ho" to listOf("हो", "होंगे", "होगा"),
        "aap" to listOf("आप", "आपका", "आपकी", "अपने"),
        "aapka" to listOf("आपका", "आपकी", "आपके"),
        "aapki" to listOf("आपकी", "आपका", "आपके"),
        "aapke" to listOf("आपके", "आपका", "आपकी"),
        "kaise" to listOf("कैसे", "कैसा", "कैसी"),
        "kaisa" to listOf("कैसा", "कैसे", "कैसी"),
        "kaisi" to listOf("कैसी", "कैसा", "कैसे"),
        "main" to listOf("मैं", "में", "मैन"),
        "mein" to listOf("में", "मैं"),
        "mera" to listOf("मेरा", "मेरी", "मेरे"),
        "meri" to listOf("मेरी", "मेरा", "मेरे"),
        "mere" to listOf("मेरे", "मेरा", "मेरी"),
        "naam" to listOf("नाम", "नामक", "नामित"),
        "theek" to listOf("ठीक", "ठेका"),
        "thik" to listOf("ठीक", "ठिकाना"),
        "hoon" to listOf("हूँ", "हूँ।"),
        "hun" to listOf("हूँ", "हुन"),
        "bharat" to listOf("भारत", "भारतीय"),
        "desh" to listOf("देश", "देशी", "विदेश"),
        "smart" to listOf("स्मार्ट", "स्मार्टनेस"),
        "phone" to listOf("फ़ोन", "फोन"),
        "fon" to listOf("फ़ोन", "फोन"),
        "acha" to listOf("अच्छा", "अच्छी", "अच्छे"),
        "achha" to listOf("अच्छा", "अच्छी", "अच्छे"),
        "achchha" to listOf("अच्छा", "अच्छी", "अच्छे"),
        "achhe" to listOf("अच्छे", "अच्छा", "अच्छी"),
        "achi" to listOf("अच्छी", "अच्छा", "अच्छे"),
        "bhai" to listOf("भाई", "भाईसाहब"),
        "bhaai" to listOf("भाई"),
        "karo" to listOf("करो", "करें", "करते"),
        "karna" to listOf("करना", "करने", "करनी"),
        "karta" to listOf("करता", "करते", "करती"),
        "karti" to listOf("करती", "करता", "करते"),
        "karte" to listOf("करते", "करता", "करती"),
        "kare" to listOf("करे", "करें", "करेंगे"),
        "karen" to listOf("करें", "करे", "करेंगे"),
        "karenge" to listOf("करेंगे", "करूँगा"),
        "aur" to listOf("और", "औरों"),
        "nahi" to listOf("नहीं", "नही"),
        "nahin" to listOf("नहीं", "नही"),
        "na" to listOf("ना", "न"),
        "bahut" to listOf("बहुत", "बहोत"),
        "bohot" to listOf("बहुत"),
        "bhot" to listOf("बहुत"),
        "kam" to listOf("कम", "काम"),
        "kaam" to listOf("काम", "कम"),
        "baat" to listOf("बात", "बातें", "बातचीत"),
        "baaten" to listOf("बातें", "बात"),
        "batit" to listOf("बातचीत"),
        "shukriya" to listOf("शुक्रिया"),
        "dhanyawad" to listOf("धन्यवाद"),
        "dhanyavaad" to listOf("धन्यवाद"),
        "ghar" to listOf("घर", "घरेलू"),
        "dost" to listOf("दोस्त", "दोस्ती"),
        "dosti" to listOf("दोस्ती", "दोस्त"),
        "khana" to listOf("खाना", "खाने"),
        "khaana" to listOf("खाना", "खाने"),
        "pani" to listOf("पानी"),
        "paani" to listOf("पानी"),
        "pina" to listOf("पीना", "पीने"),
        "peena" to listOf("पीना", "पीने"),
        "samay" to listOf("समय"),
        "din" to listOf("दिन", "दैनिक"),
        "raat" to listOf("रात", "रातों"),
        "subah" to listOf("सुबह"),
        "shaam" to listOf("शाम"),
        "sham" to listOf("शाम"),
        "aaj" to listOf("आज", "आजकल"),
        "kal" to listOf("कल"),
        "parson" to listOf("परसों"),
        "kahan" to listOf("कहाँ", "कहा"),
        "kahaan" to listOf("कहाँ"),
        "kab" to listOf("कब", "कभी"),
        "kyon" to listOf("क्यों", "क्योंकि"),
        "kyu" to listOf("क्यों"),
        "kyun" to listOf("क्यों"),
        "kyunki" to listOf("क्योंकि"),
        "kaun" to listOf("कौन", "कोई"),
        "kitna" to listOf("कितना", "कितने", "कितनी"),
        "kitne" to listOf("कितने", "कितना"),
        "kitni" to listOf("कितनी", "कितना"),
        "sab" to listOf("सब", "सभी"),
        "sabhi" to listOf("सभी", "सब"),
        "kuch" to listOf("कुछ"),
        "kisi" to listOf("किसी"),
        "koi" to listOf("कोई"),
        "zaroor" to listOf("ज़रूर", "जरूर"),
        "jarur" to listOf("ज़रूर", "जरूर"),
        "pyar" to listOf("प्यार", "प्यारा", "प्यारी"),
        "pyaar" to listOf("प्यार"),
        "khushi" to listOf("ख़ुशी", "खुशी"),
        "sach" to listOf("सच", "सच्चा"),
        "soch" to listOf("सोच", "सोचना"),
        "lekin" to listOf("लेकिन"),
        "magar" to listOf("मगर"),
        "agar" to listOf("अगर"),
        "toh" to listOf("तो"),
        "to" to listOf("तो"),
        "bhi" to listOf("भी"),
        "sirf" to listOf("सिर्फ़", "सिर्फ"),
        "sirph" to listOf("सिर्फ", "सिर्फ़"),
        "pehle" to listOf("पहले", "पहला"),
        "pahle" to listOf("पहले", "पहला"),
        "pehla" to listOf("पहला", "पहले", "पहली"),
        "baad" to listOf("बाद", "बादल"),
        "abhi" to listOf("अभी"),
        "hamesha" to listOf("हमेशा"),
        "kabhi" to listOf("कभी"),
        "yaad" to listOf("याद", "यादें"),
        "zindagi" to listOf("ज़िंदगी", "जिंदगी"),
        "duniya" to listOf("दुनिया"),
        "apna" to listOf("अपना", "अपनी", "अपने"),
        "apne" to listOf("अपने", "अपना", "अपनी"),
        "apni" to listOf("अपनी", "अपना", "अपने"),
        "hum" to listOf("हम", "हमारा", "हमें"),
        "ham" to listOf("हम", "हमारा"),
        "tum" to listOf("तुम", "तुम्हारा"),
        "woh" to listOf("वह", "वो"),
        "wo" to listOf("वो", "वह"),
        "yeh" to listOf("यह", "ये"),
        "ye" to listOf("ये", "यह"),
        "unka" to listOf("उनका", "उनकी", "उनके"),
        "inka" to listOf("इनका", "इनकी", "इनके"),
        "mujhe" to listOf("मुझे"),
        "tujhe" to listOf("तुझे"),
        "usse" to listOf("उसे", "उससे"),
        "isse" to listOf("इसे", "इससे"),
        "humein" to listOf("हमें"),
        "unhein" to listOf("उन्हें"),
        "hoga" to listOf("होगा", "होगी", "होंगे"),
        "hogi" to listOf("होगी", "होगा"),
        "honge" to listOf("होंगे"),
        "raha" to listOf("रहा", "रही", "रहे"),
        "rahi" to listOf("रही", "रहा", "रहे"),
        "rahe" to listOf("रहे", "रहा", "रही"),
        "chahiye" to listOf("चाहिए", "चाहते"),
        "sakta" to listOf("सकता", "सकते", "सकती"),
        "sakti" to listOf("सकती", "सकता"),
        "sakte" to listOf("सकते", "सकता"),
        "likho" to listOf("लिखो", "लिखना"),
        "likhna" to listOf("लिखना"),
        "padho" to listOf("पढ़ो", "पढ़ना"),
        "parh" to listOf("पढ़", "पढ़ना"),
        "dekho" to listOf("देखो", "देखना"),
        "suno" to listOf("सुनो", "सुनना"),
        "bolo" to listOf("बोलो", "बोलना"),
        "samjho" to listOf("समझो", "समझना"),
        "kripya" to listOf("कृपया"),
        "madad" to listOf("मदद"),
        "batao" to listOf("बताओ", "बताना"),
        "bataao" to listOf("बताओ"),
        "bata" to listOf("बता", "बताओ"),
        "chalo" to listOf("चलो", "चलना"),
        "aao" to listOf("आओ", "आना"),
        "jao" to listOf("जाओ", "जाना"),
        "ruk" to listOf("रुक", "रुको"),
        "ruko" to listOf("रुको"),
        "pasand" to listOf("पसंद"),
        "badhiya" to listOf("बढ़िया"),
        "speed" to listOf("स्पीड"),
        "fast" to listOf("फ़ास्ट", "फास्ट"),
        "slow" to listOf("स्लो"),
        "keyboard" to listOf("कीबोर्ड"),
        "type" to listOf("टाइप"),
        "typing" to listOf("टाइपिंग"),
        "word" to listOf("वर्ड", "शब्द"),
        "shabd" to listOf("शब्द"),
        "bhasha" to listOf("भाषा"),
        "hindi" to listOf("हिंदी", "हिन्दी"),
        "english" to listOf("इंग्लिश", "अंग्रेज़ी"),
        "number" to listOf("नंबर", "संख्या"),
        "battery" to listOf("बैटरी"),
        "save" to listOf("सेव"),
        "ramesh" to listOf("रमेश", "राम"),
        "shyam" to listOf("श्याम"),
        "mohan" to listOf("मोहन"),
        "krishna" to listOf("कृष्ण", "कृष्णा"),
        "sita" to listOf("सीता"),
        "gita" to listOf("गीता")
    )

    // User-saved custom words (in-memory fast cache, updated from prefs)
    private val userCustomWords = mutableMapOf<String, List<String>>()

    fun initFromContext(context: Context) {
        if (assetDictionary != null) return
        try {
            context.assets.open("hindi_dict.json").use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val sb = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = reader.readLine()
                }
                val json = JSONObject(sb.toString())
                val map = mutableMapOf<String, List<String>>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val arr = json.getJSONArray(k)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                    map[k] = list
                }
                assetDictionary = map
            }
        } catch (_: Exception) {}
    }

    fun setUserWords(words: Map<String, List<String>>) {
        userCustomWords.clear()
        userCustomWords.putAll(words)
    }

    /**
     * Given Latin input text, returns list of candidate suggestions.
     * Order of suggestions:
     * 1. Primary Hindi transliteration (e.g. "खाता" for "khata", "राम" for "ram")
     * 2. Original Latin word (e.g. "khata")
     * 3+ Alternate candidates (e.g. "ख़तम", "खता", "खटा")
     */
    fun transliterate(rawInput: String): List<String> {
        val input = rawInput.trim().lowercase(Locale.ROOT)
        if (input.isEmpty()) return emptyList()

        val results = linkedSetOf<String>()

        // 1. Check user custom words first
        userCustomWords[input]?.let { results.addAll(it) }

        // 2. Check asset dictionary (loaded from user's JSON)
        assetDictionary?.get(input)?.let { dictResults ->
            results.addAll(dictResults)
        }

        // 3. Check hardcoded frequent dictionary
        frequentDictionary[input]?.let { dictResults ->
            results.addAll(dictResults)
        }

        // 4. Try vowel normalized variants (e.g., single 'a' to 'aa' or double 'aa' to 'a')
        if (results.isEmpty() || results.size < 3) {
            val aaVariant = if (!input.contains("aa") && input.contains("a")) {
                input.replace("a", "aa")
            } else if (input.contains("aa")) {
                input.replace("aa", "a")
            } else null

            if (aaVariant != null) {
                assetDictionary?.get(aaVariant)?.let { results.addAll(it) }
                frequentDictionary[aaVariant]?.let { results.addAll(it) }
            }
        }

        // 5. Algorithmic rule-based phonetic transliteration
        val algorithmicHindi = phoneticTransliterate(input)
        if (algorithmicHindi.isNotEmpty()) {
            results.add(algorithmicHindi)
        }

        // Also check if ends with 'a': add open-aa candidate
        if (input.endsWith("a") && !algorithmicHindi.endsWith("ा")) {
            val openAa = algorithmicHindi + "ा"
            results.add(openAa)
        }

        val hindiCandidates = results.toMutableList()
        if (hindiCandidates.isEmpty()) {
            hindiCandidates.add(algorithmicHindi)
        }

        // Final list construction:
        // Position 0: Top Hindi candidate (e.g. "खाता" for "khata", "राम" for "ram")
        // Position 1: Typed Latin word (e.g. "khata", "ram")
        // Position 2+: Remaining alternatives
        val finalCandidates = mutableListOf<String>()
        val primaryHindi = hindiCandidates.firstOrNull() ?: input
        finalCandidates.add(primaryHindi)

        // Exact typed Latin word at index 1
        finalCandidates.add(rawInput)

        for (cand in hindiCandidates.drop(1)) {
            if (!finalCandidates.contains(cand) && cand != rawInput) {
                finalCandidates.add(cand)
            }
            if (finalCandidates.size >= 5) break
        }

        return finalCandidates
    }

    /**
     * Fast, zero-allocation phonetic Latin-to-Devanagari converter
     */
    fun phoneticTransliterate(input: String): String {
        val sb = StringBuilder()
        var i = 0
        val len = input.length

        // Consonant mappings
        val multiConsonants = mapOf(
            "ksha" to "क्ष", "ksh" to "क्ष्",
            "gya" to "ज्ञ", "gy" to "ज्ञ्",
            "tra" to "त्र", "tr" to "त्र्",
            "shh" to "ष्", "sh" to "श्",
            "chh" to "छ्", "ch" to "च्",
            "kh" to "ख्", "gh" to "घ्",
            "jh" to "झ्", "th" to "थ्",
            "dh" to "ध्", "ph" to "फ्",
            "bh" to "भ्", "ng" to "ङ्",
            "ny" to "ञ्"
        )

        val singleConsonants = mapOf(
            'k' to "क्", 'g' to "ग्",
            'c' to "च्", 'j' to "ज्",
            't' to "त्", 'd' to "द्",
            'n' to "न्", 'p' to "प्",
            'b' to "ब्", 'm' to "म्",
            'y' to "य्", 'r' to "र्",
            'l' to "ल्", 'v' to "व्",
            'w' to "व्", 's' to "स्",
            'h' to "ह्", 'z' to "ज़्",
            'f' to "फ़्", 'q' to "क़्",
            'x' to "क्स"
        )

        val vowelMatras = mapOf(
            "aa" to "ा", "ee" to "ी", "oo" to "ू",
            "ai" to "ै", "au" to "ौ",
            "a" to "", "i" to "ि", "u" to "ु",
            "e" to "े", "o" to "ो"
        )

        val independentVowels = mapOf(
            "aa" to "आ", "ee" to "ई", "oo" to "ऊ",
            "ai" to "ऐ", "au" to "औ",
            "a" to "अ", "i" to "इ", "u" to "उ",
            "e" to "ए", "o" to "ओ"
        )

        var lastWasConsonant = false

        while (i < len) {
            val rem4 = if (i + 4 <= len) input.substring(i, i + 4) else null
            val rem3 = if (i + 3 <= len) input.substring(i, i + 3) else null
            val rem2 = if (i + 2 <= len) input.substring(i, i + 2) else null
            val ch = input[i]

            // Check multi-consonants
            if (rem4 != null && multiConsonants.containsKey(rem4)) {
                sb.append(multiConsonants[rem4])
                lastWasConsonant = true
                i += 4
                continue
            }
            if (rem3 != null && multiConsonants.containsKey(rem3)) {
                sb.append(multiConsonants[rem3])
                lastWasConsonant = true
                i += 3
                continue
            }
            if (rem2 != null && multiConsonants.containsKey(rem2)) {
                sb.append(multiConsonants[rem2])
                lastWasConsonant = true
                i += 2
                continue
            }

            // Check single consonants
            if (singleConsonants.containsKey(ch)) {
                sb.append(singleConsonants[ch])
                lastWasConsonant = true
                i++
                continue
            }

            // Check vowels
            var matchedVowelLen = 0
            var matra: String? = null
            var indVowel: String? = null

            if (rem2 != null && vowelMatras.containsKey(rem2)) {
                matchedVowelLen = 2
                matra = vowelMatras[rem2]
                indVowel = independentVowels[rem2]
            } else if (vowelMatras.containsKey(ch.toString())) {
                matchedVowelLen = 1
                val vKey = ch.toString()
                matra = vowelMatras[vKey]
                indVowel = independentVowels[vKey]

                // If single 'a' is at the very end of the word after a consonant,
                // in conversational Hinglish it represents 'ा' (e.g. khata, saja, mara, bacha)
                if (vKey == "a" && i == len - 1 && lastWasConsonant) {
                    matra = "ा"
                }
            }

            if (matchedVowelLen > 0) {
                if (lastWasConsonant) {
                    if (sb.isNotEmpty() && sb[sb.length - 1] == '्') {
                        sb.deleteCharAt(sb.length - 1)
                    }
                    if (!matra.isNullOrEmpty()) {
                        sb.append(matra)
                    }
                } else {
                    sb.append(indVowel ?: "")
                }
                lastWasConsonant = false
                i += matchedVowelLen
                continue
            }

            // Anusvara
            if (ch == 'n' || ch == 'm') {
                if (lastWasConsonant) {
                    sb.append('ं')
                    lastWasConsonant = false
                    i++
                    continue
                }
            }

            // Passthrough
            sb.append(ch)
            lastWasConsonant = false
            i++
        }

        // Clean trailing halant
        if (sb.isNotEmpty() && sb[sb.length - 1] == '्') {
            sb.deleteCharAt(sb.length - 1)
        }

        return sb.toString()
    }
}
