package com.example.booknook

import java.util.Date

// Data class to hold group reply information
data class GroupReply(
    val userId: String = "",
    val username: String = "",
    val replyText: String = "",
    val timestamp: Date? = null
)
