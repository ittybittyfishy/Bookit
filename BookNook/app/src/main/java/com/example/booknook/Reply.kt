package com.example.booknook

import java.util.Date

//Yunjong Noh
// Data class to hold reply information
data class Reply(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null
)
