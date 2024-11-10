package com.example.booknook.fragments

class NotificationItem (
    val userId: String,
    val senderId: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    var isDismissed: Boolean = false
)

enum class NotificationType {
    FRIEND_REQUEST, GROUP_JOIN_REQUEST, GROUP_MESSAGES, FRIEND_STARTED_BOOK, FRIEND_FINISHED_BOOK, REVIEW_REPLY
}
