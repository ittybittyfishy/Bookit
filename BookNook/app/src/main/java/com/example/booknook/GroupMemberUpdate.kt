package com.example.booknook
import com.google.firebase.Timestamp

// Data class to store information for a group member's update
data class GroupMemberUpdate(
    val updateId : String = "",
    val userId: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val type: String = "",
    val dismissedBy: List<String> = emptyList(),
    val bookTitle: String? = null,
    val bookAuthors: String? = null,
    val bookRating: Float? = null,
    val bookImage: String? = null,
    val rating: Float? = null,
    val reviewText: String = "",
    val hasSpoilers: Boolean = false,
    val hasSensitiveTopics: Boolean = false,
    val charactersChecked: Boolean = false,
    val charactersRating: Float? = null,
    val charactersReview: String = "",
    val writingChecked: Boolean = false,
    val writingRating: Float? = null,
    val writingReview: String = "",
    val plotChecked: Boolean = false,
    val plotRating: Float? = null,
    val plotReview: String = "",
    val themesChecked: Boolean = false,
    val themesRating: Float? = null,
    val themesReview: String = "",
    val strengthsChecked: Boolean = false,
    val strengthsRating: Float? = null,
    val strengthsReview: String = "",
    val weaknessesChecked: Boolean = false,
    val weaknessesRating: Float? = null,
    val weaknessesReview: String = "",
    val timestamp: Timestamp? = null
)
