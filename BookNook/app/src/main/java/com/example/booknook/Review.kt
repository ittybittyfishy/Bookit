package com.example.booknook
import java.util.Date

//Yunjong Noh
// Data class to hold no-template review information
data class Review(
    val userId: String = "",
    val username: String = "",
    val reviewText: String = "",
    val rating: Double = 0.0,
    val hasSpoilers: Boolean = false,
    val hasSensitiveTopics: Boolean = false,
    val timestamp: Date? = null,
    val isTemplateUsed: Boolean = false
)