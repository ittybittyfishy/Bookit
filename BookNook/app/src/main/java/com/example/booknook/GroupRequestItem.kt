package com.example.booknook

// Data class representing an individual group request
class GroupRequestItem (
    val id: String = "",
    val groupName: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderUsername: String = "",
    val status: String = "pending"
){}