package com.example.booknook

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val username: String = "",
    val status: String = "pending"
)