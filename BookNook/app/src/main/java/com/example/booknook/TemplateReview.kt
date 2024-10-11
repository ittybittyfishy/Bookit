package com.example.booknook
import java.util.Date

//Yunjong Noh
// Data class to hold with-template review information
data class TemplateReview(
    val userId: String = "",
    val username: String = "",
    val reviewText: String = "",
    val rating: Double = 0.0,
    val charactersRating: Double = 0.0,
    val charactersReview: String? = "",
    val writingRating: Double = 0.0,
    val writingReview: String? = "",
    val plotRating: Double = 0.0,
    val plotReview: String? = "",
    val themesRating: Double = 0.0,
    val themesReview: String? = "",
    val strengthsRating: Double = 0.0,
    val strengthsReview: String? = "",
    val weaknessesRating: Double = 0.0,
    val weaknessesReview: String? = "",
    val timestamp: Date? = null,
    val isTemplateUsed: Boolean = true
)