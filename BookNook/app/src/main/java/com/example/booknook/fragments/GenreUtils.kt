// File: GenreUtils.kt
package com.example.booknook.utils

object GenreUtils {
    // Right side is from the API
    private val genreSynonyms = mapOf(
        "non-fiction" to "nonfiction",
        "sci-fi" to "science fiction"
        // Add more synonyms as needed
    )

    /**
     * Normalizes a genre string by trimming whitespace, converting it to lowercase,
     * and resolving any known genre synonyms.
     *
     * @param genre The original genre string.
     * @return A normalized genre string.
     */
    fun normalizeGenre(genre: String): String {
        val normalized = genre.trim().lowercase()

        // Check if the normalized genre is a known synonym
        return genreSynonyms[normalized] ?: normalized
    }
}
