package com.example.booknook

import java.util.Date

data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null
)
