package com.spdev.litehindi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spdev.litehindi.data.Prefs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeScreen(prefs: Prefs, onBack: () -> Unit) {
    val height by prefs.heightScale.collectAsState()
    val textScale by prefs.keyTextScale.collectAsState()
    val bottomSpace by prefs.bottomSpace.collectAsState()
    val bottomSpaceDp by prefs.bottomSpaceDp.collectAsState()
    val borders by prefs.keyBorders.collectAsState()
    val hindiAuto by prefs.hindiAuto.collectAsState()
    val suggestions by prefs.showSuggestions.collectAsState()
    val numberRow by prefs.showNumberRow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Size & layout", fontWeight = FontWeight.SemiBold, fontSize = 19.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Text("‹", fontSize = 24.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Keyboard height", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "${(height * 100).roundToInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = height,
                        onValueChange = { prefs.setHeightScale(it) },
                        valueRange = 0.8f..1.3f,
                        // 0.8 / 0.9 / 1.0 / 1.1 / 1.2 / 1.3 — 1.0 (default) is reachable
                        steps = 4
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Key text size", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "${(textScale * 100).roundToInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = textScale,
                        onValueChange = { prefs.setKeyTextScale(it) },
                        valueRange = 0.85f..1.25f,
                        steps = 3
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SwitchRow("Bottom space", "Extra chin below the keyboard for gesture navigation", bottomSpace) {
                        prefs.setBottomSpace(it)
                    }
                    if (bottomSpace) {
                        Slider(
                            value = bottomSpaceDp.toFloat(),
                            onValueChange = { prefs.setBottomSpaceDp(it.roundToInt()) },
                            valueRange = 4f..28f,
                            steps = 5
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SwitchRow("Key borders", "Thin outline around every key", borders) { prefs.setKeyBorders(it) }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SwitchRow("Roman → Devanagari", "Auto-convert the Latin word when you press space", hindiAuto) { prefs.setHindiAuto(it) }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SwitchRow("Spelling strip", "Show spelling options for the word being typed (not next-word prediction)", suggestions) { prefs.setShowSuggestions(it) }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SwitchRow("Number row", "Show 1–0 above the letters", numberRow) { prefs.setShowNumberRow(it) }
                }
            }

            Text(
                "Tip: long-press any letter to type the small symbol shown on its top-right corner.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
