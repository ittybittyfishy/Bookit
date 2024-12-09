package com.example.booknook
import java.util.Date

//Yunjong Noh
// Data class to hold with-template review information
data class TemplateReview(
    val userId: String = "",
    val username: String = "",
    val reviewText: String = "",
    val rating: Double = 0.0,
    val charactersRating: Double? = null,
    val charactersReview: String? = "",
    val writingRating: Double? = null,
    val writingReview: String? = "",
    val plotRating: Double? = null,
    val plotReview: String? = "",
    val themesRating: Double? = null,
    val themesReview: String? = "",
    val strengthsRating: Double? = null,
    val strengthsReview: String? = "",
    val weaknessesRating: Double? = null,
    val weaknessesReview: String? = "",
    val timestamp: Date? = null,
    val isTemplateUsed: Boolean = true,
    val isbn: String = "",
    val reviewId: String = "",
    var likes: Int = 0,
    var dislikes: Int = 0
)