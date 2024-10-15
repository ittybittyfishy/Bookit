package com.example.booknook
import java.util.Date

//Yunjong Noh
// Data class to hold no-template review information
data class Review(
    val userId: String = "",
    val username: String = "",
    val reviewText: String = "",
    val rating: Double = 0.0,
    val hasSpoilers: Boolean = false,  // Boolean, not referencing UI
    val hasSensitiveTopics: Boolean = false,  // Boolean, not referencing UI
    val timestamp: Date? = null,
    val isTemplateUsed: Boolean = false,
    val isbn: String = "",
    val reviewId: String = "",
    var likes: Int = 0,
    var dislikes: Int = 0
)
