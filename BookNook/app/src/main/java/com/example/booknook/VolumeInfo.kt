package com.example.booknook

data class VolumeInfo(
    val title: String, // Title of the book
    val authors: List<String>?, // List of authors
    val imageLinks: ImageLinks?, // Links to book cover images
    val industryIdentifiers: List<IndustryIdentifier>?, // List of industry identifiers (e.g., ISBN)
    val averageRating: Float?, // Average rating of the book
    val categories: List<String>? // List of genres/categories of the book
)


// Example of IndustryIdentifier class to handle identifiers like ISBN
data class IndustryIdentifier(
    val type: String, // Type of identifier (e.g., "ISBN_10", "ISBN_13")
    val identifier: String // The identifier value
)
