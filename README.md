# Lite Hindi Keyboard

A deliberately small Android keyboard: type Latin, get Devanagari. Built as a
light alternative to heavy keyboards on a 2 GB phone (Infinix Smart 7 HD class).

## What it does

| Feature | Status |
|---|---|
| Roman → Devanagari auto transliteration | yes |
| Plain English typing | yes |
| Long-press backspace to keep deleting | yes |
| Swipe left on backspace to select-and-delete N characters | yes |
| Swipe on the space bar to move the cursor | yes |
| Symbols pages + long-press symbols on letter keys | yes |
| 12 colour themes with code-drawn gradients/patterns | yes |
| Emoji, stickers, clipboard manager, vibration, wallpaper images, next-word prediction | **no, by design** |

## Why it is small

* No Firebase, no Gemini, no Retrofit/OkHttp, no Room, no image assets.
* The whole keyboard layout is ~1 KB of Kotlin (`ui/Keys.kt`).
* Transliteration tables are built once; the hot path allocates one StringBuilder.
* The dictionary (49 KB JSON) is parsed once, off the main thread.
* Nothing you type is stored anywhere. `allowBackup=false`.

## Project layout

```
app/src/main/java/com/spdev/litehindi/
  MainActivity.kt          settings app shell + home screen
  ime/LiteHindiIme.kt      the InputMethodService (composing text, cursor, delete)
  engine/HindiEngine.kt    transliteration rules + dictionary lookup
  data/Prefs.kt            all settings, SharedPreferences only
  data/Themes.kt           the 12 themes (colours only)
  ui/KeyboardRoot.kt       keyboard composable + gesture wiring
  ui/KeyView.kt            one key: look, tap, long-press, hold, swipe
  ui/Keys.kt               the key layouts
  ui/Background.kt         code-drawn gradient/pattern backgrounds
  ui/ThemeScreen.kt        theme picker
  ui/SizeScreen.kt         height / text size / toggles
  ui/AppTheme.kt           Material3 wrapper for the settings app
app/src/main/assets/hindi_dict.json   roman -> devanagari candidates
```

## Build

No local Android SDK needed:

1. Push to `fix/cleanup` (or run the workflow manually).
2. `.github/workflows/build.yml` builds a debug APK on GitHub's runners.
3. Download the `lite-hindi-debug-apk` artifact and install it.

Locally, if you ever get a PC: `gradle assembleDebug`.

## Known limitation

Word-final `n`/`m` is kept as a consonant (`main` → मैन) because a blanket
anusvara rule breaks words like `din` (दिन). The dictionary supplies the correct
form for common words (मैं, हैं). Add entries to `hindi_dict.json` to improve.
