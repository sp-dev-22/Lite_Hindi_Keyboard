package com.example.data

data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val shortcut: String? = null
)
