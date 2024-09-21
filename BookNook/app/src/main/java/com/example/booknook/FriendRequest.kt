package com.example.booknook

// Data class to hold information of a friend request
data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val username: String = "",
    val status: String = "pending"
)