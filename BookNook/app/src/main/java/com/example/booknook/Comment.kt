package com.example.booknook

import java.util.Date

data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null,
    var replies: List<Reply> = listOf(), // 초기값으로 빈 리스트 설정
    val isbn: String = "",   // 해당 리뷰가 속한 책의 ID
    val reviewId: String = "",  // 각 리뷰의 고유 ID
    var commentId: String = "" // 해당 댓글의 고유 ID
)
