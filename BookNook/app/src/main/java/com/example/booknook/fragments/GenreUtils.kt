// File: GenreUtils.kt
package com.example.booknook.utils

// right side is from api, left side is genre-preferencen
// TO-DO: juvenile fiction and comic books need to be performed
object GenreUtils {
    private val genreSynonyms = mapOf(
        "non-fiction" to listOf("nonfiction"),
        "sci-fi" to listOf("science fiction"),
        // Yunjong Noh
        // A map of genres to a list of their synonyms
        // This helps in standardizing genres that might be referred to by different names
        // ex) Maps " A " genre to a list containing its synonym " A "
        "action" to listOf("activities"),
        "adult" to listOf("young adult fiction", "adulthood"),
        "adventure" to listOf("adventure stories", "adventure"),
        "children" to listOf("children's stories", "child welfare", "children", "child development"),
        "comedy" to listOf("comedy", "humor", "parody", "parodies"),
        "crime" to listOf("criminal statistics", "criminal procedure", "true crime", "crime"),
        "drama" to listOf("drama", "american drama", "english drama", "european drama", "drama in education"),
        "fantasy" to listOf("fantasy fiction", "urban fantasy"),
        "fiction" to listOf("fiction", "historical fiction", "romantic fiction", "literary fiction"),
        "historical" to listOf("history", "historical fiction", "world history", "united states"),
        "horror" to listOf("horror films", "horror tales", "haunted houses", "occultism"),
        "lgbtq+" to listOf("homosexuality", "gays", "gender identity", "lgbtq+", "lgbt", "gender studies" ),
        "manga" to listOf("graphic novels", "comics", "comic books, strips, etc", "card games"),
        "comic books" to listOf("graphic novels", "comic books", "manga", "cartoons", "cartoons and comics", "caricature", "caricatures and cartoons"),
        "mature" to listOf("adulthood", "emotional maturity"),
        "mystery" to listOf("detective and mystery stories", "mystery", "magic tricks", "magic"),
        "nonfiction" to listOf("nonfiction", "self help", "philosophy", "philosophers"),
        "poetry" to listOf("poetry", "poetics", "language arts", "verse", "poems"),
        "psychological" to listOf("psychology", "mental health", "mental health personnel", "relationship"),
        "romance" to listOf("prose romances", "erotic literature", "love", "relationships"),
        "school life" to listOf("education", "affective education", "young adult nonfiction", "study aids"),
        "juvenile fiction" to listOf("juvenile fiction", "children's literature", "young adult fiction", "kid's books" ),
        "science" to listOf("computers", "science", "technology", "engineering", "experiments", "chemistry", "biology"),
        "science fiction" to listOf("science fiction", "hard science fiction", "androids", "life on other planets"),
        "biography" to listOf("biography", "autobiography", "presidents"),
        "sports" to listOf("sports", "sports for women", "athletes", "fitness"),
        "supernatural" to listOf("supernatural", "spirit", "spiritualism"),
        "suspense" to listOf("suspense", "thriller", "crime suspense"),
        "thriller" to listOf("thriller", "psychological thriller", "crime thriller", "mystery thriller", "suspense thriller"),
        "tragedy" to listOf("tragedy", "tragic fiction", "dramatic tragedy", "literary tragedy", "classic tragedy")


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
        // Yunjong Noh, Modified on 10/29/24 for fit to genre synonyms
        return genreSynonyms.entries.find { (_, synonyms) -> normalized in synonyms }?.key ?: normalized
    }
}
