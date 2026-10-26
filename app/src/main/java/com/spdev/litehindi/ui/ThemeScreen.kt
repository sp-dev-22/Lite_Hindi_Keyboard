package com.spdev.litehindi.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spdev.litehindi.data.KeyboardTheme
import com.spdev.litehindi.data.Prefs
import com.spdev.litehindi.data.Themes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(prefs: Prefs, onBack: () -> Unit) {
    val currentId by prefs.themeId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Themes", fontWeight = FontWeight.SemiBold, fontSize = 19.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("‹", fontSize = 24.sp) }
                },
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
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    "Every background is drawn in code — gradients and subtle patterns. " +
                        "No wallpaper images, so no extra APK weight and no decode cost.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // weight(1f) so the grid never pushes past the screen
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(Themes.All) { theme ->
                    ThemeCard(theme, theme.id == currentId) { prefs.setThemeId(theme.id) }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(theme: KeyboardTheme, selected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.bgBottom),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) theme.accent else theme.keyBorder
        ),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .themedBackground(theme)
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("राम", color = theme.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("ram", color = theme.textSecondary, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(theme.keyBg)
                                    .border(0.5.dp, theme.keyBorder, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f).height(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(theme.keyBg)
                        )
                        Box(
                            modifier = Modifier
                                .weight(2.6f).height(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(theme.keyBg)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.1f).height(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(theme.actionBg)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    theme.name,
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(theme.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = theme.bgBottom, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
