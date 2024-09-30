// File: GenreUtils.kt
package com.example.booknook.utils

object GenreUtils {
    /**
     * Normalizes a genre string by trimming whitespace and converting it to lowercase.
     *
     * @param genre The original genre string.
     * @return A normalized genre string.
     */
    fun normalizeGenre(genre: String): String {
        return genre.trim().lowercase()
    }
}
