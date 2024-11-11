package com.example.booknook.fragments

// Yunjong Noh
// Data class representing a notification item
data class NotificationItem(
    val notificationId: String = "", // Unique ID for the notification
    val userId: String = "", // ID of the user associated with the notification
    val senderId: String = "", // ID of the sender of the notification
    val receiverId: String = "", // ID of the receiver of the notification (newly added)
    val message: String = "", // Message content of the notification
    val timestamp: Long = 0L, // Timestamp when the notification was created
    val type: NotificationType = NotificationType.FRIEND_REQUEST, // Type of the notification (default)
    val dismissed: Boolean = false, // Indicates if the notification has been dismissed
    val expirationTime: Long = 0L, // Expiration time for the notification
    var profileImageUrl: String = "",
    var username: String = ""
)

// Enum class representing different types of notifications
enum class NotificationType {
    FRIEND_REQUEST, // Notification for a friend request
    GROUP_JOIN_REQUEST, // Notification for a group join request
    GROUP_MESSAGES, // Notification for group messages
    FRIEND_STARTED_BOOK, // Notification when a friend starts a book
    FRIEND_FINISHED_BOOK, // Notification when a friend finishes a book
    REVIEW_REPLY, // Notification for a reply to a review
    REVIEW_EDIT, // Notification when a review is edited
    REVIEW_ADDED // Notification when a review is added
}
