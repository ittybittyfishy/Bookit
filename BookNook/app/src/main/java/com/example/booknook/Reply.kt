package com.example.booknook

import java.util.Date

data class Reply(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null
)
