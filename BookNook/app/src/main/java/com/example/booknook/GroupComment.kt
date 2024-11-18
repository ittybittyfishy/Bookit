package com.example.booknook

import com.google.firebase.firestore.FieldValue
import java.util.Date

// Data class to hold group comment information
data class GroupComment(
    val userId: String = "",
    val username: String = "",
    val commentText: String = "",
    val timestamp: Date? = null,
    var commentId: String = ""
)
