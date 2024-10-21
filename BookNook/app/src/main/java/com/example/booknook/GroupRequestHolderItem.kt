package com.example.booknook

// Data class representing a holder for group request information
class GroupRequestHolderItem (
    val groupId: String,
    val groupName: String,
    val requests: MutableList<GroupRequestItem>
){
}