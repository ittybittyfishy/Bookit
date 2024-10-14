package com.example.booknook

import java.util.Date

//Yunjong Noh
//Data class to hold Comment information
data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null,
    var replies: List<Reply> = listOf(),
    val isbn: String = "",
    val reviewId: String = "",
    var commentId: String = ""
)
