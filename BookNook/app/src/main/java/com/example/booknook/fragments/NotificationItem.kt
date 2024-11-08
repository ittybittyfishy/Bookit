package com.example.booknook.fragments

// Yunjong Noh

data class NotificationItem(
    val notificationId: String = "", // Firestore document ID
    val userId: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val type: NotificationType = NotificationType.FRIEND_REQUEST,
    val dismissed: Boolean = false
)

enum class NotificationType {
    FRIEND_REQUEST,
    GROUP_JOIN_REQUEST,
    GROUP_MESSAGES,
    FRIEND_STARTED_BOOK,
    FRIEND_FINISHED_BOOK,
    REVIEW_REPLY,
    REVIEW_ADDED
}