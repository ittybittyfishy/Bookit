package com.example.booknook

import java.util.Date

data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null,
    val replies: List<Reply> = emptyList(), // Replies 리스트 추가
    var isbn: String = "",   // 해당 리뷰가 속한 책의 ID
    var reviewId: String = "",  // 각 리뷰의 고유 ID
    var commentId: String = "" // 해당 댓글의 고유 ID
)
