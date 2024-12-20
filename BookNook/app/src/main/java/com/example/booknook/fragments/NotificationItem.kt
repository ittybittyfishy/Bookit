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
    var profileImageUrl: String = "", // Retrieve Profile image
    var username: String = "" // to show user name
)

// Enum class representing different types of notifications
enum class NotificationType {
    FRIEND_REQUEST, // Notification for a friend request (done)
    GROUP_JOIN_REQUEST, // Notification for a group join request (done)
    GROUP_MESSAGES, // Notification for group messages
    FRIEND_STARTED_BOOK, // Notification when a friend starts a book (done)
    FRIEND_FINISHED_BOOK, // Notification when a friend finishes a book (done)
    REVIEW_REPLY, // Notification for a reply to a review (done)
    REVIEW_EDIT, // Notification when a review is edited (done)
    REVIEW_ADDED, // Notification when a review is added (done)
}
