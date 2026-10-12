package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearDataScreen(
    preferences: KeyboardPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardItems by preferences.clipboardItems.collectAsState()

    var typedEnCount by remember { mutableIntStateOf(preferences.getTypedEnglishWordsCount()) }
    var typedHiCount by remember { mutableIntStateOf(preferences.getTypedHindiWordsCount()) }
    var savedHiCount by remember { mutableIntStateOf(preferences.getSavedHindiWordsCount()) }

    val pinnedCount = clipboardItems.count { it.isPinned }
    val recentCount = clipboardItems.count { !it.isPinned }

    val enSizeKb = if (typedEnCount > 0) (typedEnCount * 12 / 1024 + 1) else 0
    val hiSizeKb = if (typedHiCount > 0) (typedHiCount * 16 / 1024 + 1) else 0
    val savedHiSizeKb = if (savedHiCount > 0) 1 else 0
    val pinnedSizeKb = if (pinnedCount > 0) 1 else 0
    val recentSizeKb = if (recentCount > 0) 1 else 0
    val totalSizeKb = enSizeKb + hiSizeKb + savedHiSizeKb + pinnedSizeKb + recentSizeKb

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Clear data",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp
                        )
                        Text(
                            "Total ${if (totalSizeKb < 1024) "$totalSizeKb KB" else "${totalSizeKb / 1024} MB"}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Memory & Performance Banner for Infinix Smart 7 HD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF004D40).copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "RAM Speed",
                        tint = Color(0xFF26A69A),
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "Infinix Smart 7 HD (2GB RAM) Optimized",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Keyboard uses < 10 MB RAM (vs 150 MB in Desh Keyboard). Zero background sync, zero ad bloat.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: Typing history
            Text(
                "Typing history on your device",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Item 1: Typed words in English
                    DataRow(
                        title = "Typed words in English",
                        description = "New words saved when you type in English",
                        sizeText = "$enSizeKb KB",
                        isEmpty = enSizeKb == 0,
                        onClear = {
                            preferences.clearTypedEnglishWords()
                            typedEnCount = 0
                            Toast.makeText(context, "English words history cleared", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Item 2: Typed words in Hindi
                    DataRow(
                        title = "Typed words in Hindi",
                        description = "Order of Hindi word suggestions based on your previous choices",
                        sizeText = "$hiSizeKb KB",
                        isEmpty = hiSizeKb == 0,
                        onClear = {
                            preferences.clearTypedHindiWords()
                            typedHiCount = 0
                            Toast.makeText(context, "Hindi suggestions history cleared", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Item 3: Saved words in Hindi
                    DataRow(
                        title = "Saved words in Hindi",
                        description = "Hindi words you've added yourself",
                        sizeText = "$savedHiSizeKb KB",
                        isEmpty = savedHiSizeKb == 0,
                        onClear = {
                            preferences.clearSavedHindiWords()
                            savedHiCount = 0
                            Toast.makeText(context, "Custom words cleared", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Section 2: Clipboard
            Text(
                "Clipboard",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Item 1: Pinned items
                    DataRow(
                        title = "Pinned items on clipboard",
                        description = "Items you've pinned to your clipboard",
                        sizeText = "$pinnedSizeKb KB",
                        isEmpty = pinnedCount == 0,
                        onClear = {
                            preferences.clearPinnedClipboard()
                            Toast.makeText(context, "Pinned clipboard cleared", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Item 2: Recent items
                    DataRow(
                        title = "Recent items on clipboard",
                        description = "Items you've recently copied",
                        sizeText = "$recentSizeKb KB",
                        isEmpty = recentCount == 0,
                        onClear = {
                            preferences.clearRecentClipboard()
                            Toast.makeText(context, "Recent clipboard items cleared", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DataRow(
    title: String,
    description: String,
    sizeText: String,
    isEmpty: Boolean,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                sizeText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isEmpty) {
            Text(
                "Empty",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp)
            )
        } else {
            TextButton(
                onClick = onClear,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    "Clear",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
