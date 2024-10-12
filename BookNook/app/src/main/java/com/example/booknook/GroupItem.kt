package com.example.booknook


// For Holding Group Information
data class GroupItem(
    val id: String = "",
    val createdBy: String = "",
    val groupName: String = "",
    val tags: List<String> = emptyList(),
    val private: Boolean = false,
    val bannerImg: String? = null,
    val members: List<String> = emptyList()
)