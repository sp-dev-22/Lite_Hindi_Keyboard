package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.data.ThemePresets
import com.example.keyboard.KeyboardActionListener
import com.example.keyboard.LiteKeyboardRoot
import com.example.ui.screens.ClearDataScreen
import com.example.ui.screens.ClipboardScreen
import com.example.ui.screens.KeyboardHeightScreen
import com.example.ui.screens.ThemePickerScreen
import com.example.ui.theme.LiteHindiKeyboardTheme

enum class AppScreen {
    HOME,
    HEIGHT_SETTINGS,
    THEMES,
    CLIPBOARD,
    CLEAR_DATA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = KeyboardPreferences.getInstance(this)

        // Seed some initial clipboard snippets matching user screenshots if empty
        if (preferences.clipboardItems.value.isEmpty()) {
            preferences.addClipboardItem(
                text = "gemini-3.8-live",
                isPinned = true,
                shortcut = "ge"
            )
            preferences.addClipboardItem(
                text = "gemini-3.5-live-translate-preview",
                isPinned = true,
                shortcut = "gt"
            )
            preferences.addClipboardItem(
                text = "नमस्ते! आप कैसे हैं?",
                isPinned = true,
                shortcut = "namaste"
            )
        }

        setContent {
            val themeId by preferences.themeId.collectAsState()
            val activeTheme = remember(themeId) { ThemePresets.getById(themeId) }

            LiteHindiKeyboardTheme(darkTheme = activeTheme.isDark) {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = AppScreen.HOME
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(
                            preferences = preferences,
                            onNavigate = { currentScreen = it }
                        )

                        AppScreen.HEIGHT_SETTINGS -> KeyboardHeightScreen(
                            preferences = preferences,
                            onBack = { currentScreen = AppScreen.HOME }
                        )

                        AppScreen.THEMES -> ThemePickerScreen(
                            preferences = preferences,
                            onBack = { currentScreen = AppScreen.HOME }
                        )

                        AppScreen.CLIPBOARD -> ClipboardScreen(
                            preferences = preferences,
                            onBack = { currentScreen = AppScreen.HOME }
                        )

                        AppScreen.CLEAR_DATA -> ClearDataScreen(
                            preferences = preferences,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    preferences: KeyboardPreferences,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val themeId by preferences.themeId.collectAsState()
    val activeTheme = remember(themeId) { ThemePresets.getById(themeId) }

    // Test pad live input text and cursor
    var testText by remember { mutableStateOf("ram ") }
    var cursorPosition by remember { mutableIntStateOf(4) }
    var showLiveKeyboard by remember { mutableStateOf(true) }

    // Requirement 7: auto sync clipboard
    val sysClipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager }
    LaunchedEffect(Unit) {
        try {
            val clip = sysClipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                if (!text.isNullOrBlank()) {
                    preferences.addClipboardItem(text, isPinned = false)
                }
            }
        } catch (_: Exception) {}
    }

    // Check system IME status using official InputMethodManager API
    fun isKeyboardEnabled(): Boolean {
        return try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.enabledInputMethodList?.any { it.packageName == context.packageName } ?: false
        } catch (_: Exception) {
            false
        }
    }

    var isImeEnabled by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isImeEnabled = isKeyboardEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Lite Hindi Keyboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Infinix Smart 7 HD (2GB RAM) Edition",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(AppScreen.THEMES) }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable upper section (Setup + Menu)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // IME Activation Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isImeEnabled)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isImeEnabled) Color(0xFF2E7D32) else Color(0xFFC62828)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isImeEnabled) Icons.Default.Check else Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    if (isImeEnabled) "Keyboard Activated in System" else "Step 1: Enable Keyboard in System",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    if (isImeEnabled) "Ready to use in WhatsApp, Chrome, SMS & all apps" else "Enable 'Lite Hindi Keyboard' in device Input settings",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("1. Settings", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.showInputMethodPicker()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("2. Select Default", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Live Typing Test Pad Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Live Typing Test Pad",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row {
                                TextButton(onClick = { testText = "" }) {
                                    Text("Clear", fontSize = 12.sp)
                                }
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(testText))
                                }) {
                                    Text("Copy", fontSize = 12.sp)
                                }
                            }
                        }

                        // Simulated text field displaying typed text with cursor
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(12.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                if (testText.isEmpty()) {
                                    Text(
                                        "यहाँ टाइप करें (जैसे: khata टाइप करके space दबाएँ -> 'खाता ' बन जाएगा)...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 15.sp
                                    )
                                } else {
                                    val safeCursor = cursorPosition.coerceIn(0, testText.length)
                                    val before = testText.take(safeCursor)
                                    val after = testText.drop(safeCursor)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            before,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(20.dp)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Text(
                                            after,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Latin to Hindi transliteration test active",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = { showLiveKeyboard = !showLiveKeyboard }) {
                                Text(if (showLiveKeyboard) "Hide Keyboard" else "Show Keyboard", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Settings Navigation Items (Matching Screenshot 1)
                Text(
                    "Keyboard Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // Keyboard Height (Screenshot 1 & 12)
                        SettingsNavRow(
                            icon = Icons.Default.Height,
                            title = "Keyboard height",
                            subtitle = "Height, key text size, and bottom spacing",
                            onClick = { onNavigate(AppScreen.HEIGHT_SETTINGS) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Themes & Dark Mode
                        SettingsNavRow(
                            icon = Icons.Default.Palette,
                            title = "Themes & Dark Mode",
                            subtitle = "Pure color OLED battery saver & color themes",
                            onClick = { onNavigate(AppScreen.THEMES) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Clipboard
                        SettingsNavRow(
                            icon = Icons.Default.ContentPaste,
                            title = "Clipboard Manager",
                            subtitle = "Pinned phrases and text shortcuts",
                            onClick = { onNavigate(AppScreen.CLIPBOARD) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Clear Data (Screenshot 1 & 2)
                        SettingsNavRow(
                            icon = Icons.Default.CleaningServices,
                            title = "Clear data",
                            subtitle = "Typing history, suggestion cache, and storage",
                            onClick = { onNavigate(AppScreen.CLEAR_DATA) }
                        )
                    }
                }
            }

            // Live Interactive Embedded Keyboard (matching real IME layout)
            AnimatedVisibility(visible = showLiveKeyboard) {
                Surface(
                    color = activeTheme.keyboardBackground,
                    shadowElevation = 8.dp
                ) {
                    LiteKeyboardRoot(
                        theme = activeTheme,
                        preferences = preferences,
                        listener = object : KeyboardActionListener {
                            override fun onCommitText(text: String) {
                                val pos = cursorPosition.coerceIn(0, testText.length)
                                val before = testText.take(pos)
                                val after = testText.drop(pos)
                                testText = before + text + after
                                cursorPosition = pos + text.length
                            }

                            override fun onDeleteBackward() {
                                val pos = cursorPosition.coerceIn(0, testText.length)
                                if (pos > 0) {
                                    val before = testText.take(pos - 1)
                                    val after = testText.drop(pos)
                                    testText = before + after
                                    cursorPosition = pos - 1
                                }
                            }

                            override fun onDeleteBackward(count: Int) {
                                val pos = cursorPosition.coerceIn(0, testText.length)
                                val actualCount = count.coerceAtMost(pos)
                                if (actualCount > 0) {
                                    val before = testText.take(pos - actualCount)
                                    val after = testText.drop(pos)
                                    testText = before + after
                                    cursorPosition = pos - actualCount
                                }
                            }

                            override fun onMoveCursor(delta: Int) {
                                cursorPosition = (cursorPosition + delta).coerceIn(0, testText.length)
                            }

                            override fun onEnter() {
                                onCommitText("\n")
                            }

                            override fun onOpenSettings() {
                                onNavigate(AppScreen.HEIGHT_SETTINGS)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
