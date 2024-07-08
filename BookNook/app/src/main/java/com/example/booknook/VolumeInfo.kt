package com.example.booknook

//properties that every volume info will have
//property used in bookitem
class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val imageLinks: ImageLinks?
)