package com.example.booknook

data class VolumeInfo(
    val title: String = "", // Title of the book
    val authors: List<String>? = listOf("Unknown Author"), // List of authors
    val imageLinks: ImageLinks? = null, // Links to book cover images
    val industryIdentifiers: List<IndustryIdentifier>? = emptyList(), // List of industry identifiers (e.g., ISBN)
    val averageRating: Float? = 0.0f, // Average rating of the book
    val categories: List<String>? = listOf("Unknown Genre"), // List of genres/categories of the book
    val language: String = "" // Language of the book (e.g., "en" for English, "fr" for French)
)



// Example of IndustryIdentifier class to handle identifiers like ISBN
data class IndustryIdentifier(
    val type: String, // Type of identifier (e.g., "ISBN_10", "ISBN_13")
    val identifier: String // The identifier value
)
