package com.example.booknook.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.BookItem
import com.example.booknook.R
import com.example.booknook.BookResponse
import com.example.booknook.api.GoogleBooksApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.random.Random

class HomeFragment : Fragment() {
    // declare UI componets
    private lateinit var loggedInTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Yunjong Noh
    // Define UI componets of 3 book's recommendation
    private lateinit var bookCoverImageView1: ImageView
    private lateinit var bookTitleTextView1: TextView
    private lateinit var bookAuthorsTextView1: TextView

    private lateinit var bookCoverImageView2: ImageView
    private lateinit var bookTitleTextView2: TextView
    private lateinit var bookAuthorsTextView2: TextView

    private lateinit var bookCoverImageView3: ImageView
    private lateinit var bookTitleTextView3: TextView
    private lateinit var bookAuthorsTextView3: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize FirebaseAuth and FirebaseFirestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        loggedInTextView = view.findViewById(R.id.loggedInTextView)

        bookCoverImageView1 = view.findViewById(R.id.bookCoverImageView1)
        bookTitleTextView1 = view.findViewById(R.id.bookTitleTextView1)
        bookAuthorsTextView1 = view.findViewById(R.id.bookAuthorsTextView1)

        bookCoverImageView2 = view.findViewById(R.id.bookCoverImageView2)
        bookTitleTextView2 = view.findViewById(R.id.bookTitleTextView2)
        bookAuthorsTextView2 = view.findViewById(R.id.bookAuthorsTextView2)

        bookCoverImageView3 = view.findViewById(R.id.bookCoverImageView3)
        bookTitleTextView3 = view.findViewById(R.id.bookTitleTextView3)
        bookAuthorsTextView3 = view.findViewById(R.id.bookAuthorsTextView3)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get users UID
        val userId = auth.currentUser?.uid

        // If user is logged in, fetch username from Firebase
        userId?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")
                    // Set the text to display "logged in as username"
                    loggedInTextView.text = "Logged in as\n$username"

                    // Yunjong Noh
                    // Load recommendations from the past 24 hours, if available
                    if (shouldFetchRecommendations()) {
                        fetchRecommendedBooksFromGoogle(uid)
                        saveLastFetchDate() // Save the current date as the last fetch date
                    } else {
                        loadRecommendationsFromPreferences()?.let { recommendations ->
                            displayRecommendedBooks(recommendations) // Display stored recommendations
                            Log.d("HomeFragment", "Stored recommendations loaded.")
                        }
                    }
                } else {
                    loggedInTextView.text = "Username not found"
                }
            }.addOnFailureListener { exception ->
                loggedInTextView.text = "Error: ${exception.message}"
            }
        }
    }

    // Yunjong Noh
    // Check if 24 hours have passed since the last fetch
    private fun shouldFetchRecommendations(): Boolean {
        // Access SharedPreferences to retrieve the timestamp of the last fetch
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val lastFetchTime = sharedPreferences.getLong("lastRecommendationFetchTime", 0L)

        // Get the current time in milliseconds
        val currentTime = Calendar.getInstance().timeInMillis

        // Define 24 hours in milliseconds
        val oneDayInMillis = 24 * 60 * 60 * 1000 // 24 hours

        // Return true if 24 hours have passed since the last fetch, false otherwise
        return (currentTime - lastFetchTime) >= oneDayInMillis
    }

    // Yunjong Noh
    // Save the current date as the last fetch date
    private fun saveLastFetchDate() {
        // Access SharedPreferences in edit mode to store the current timestamp
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Store the current time in milliseconds as the "lastRecommendationFetchTime"
        editor.putLong("lastRecommendationFetchTime", Calendar.getInstance().timeInMillis)

        // Apply the changes to SharedPreferences
        editor.apply()
    }

    // Yunjong Noh
    // Convert the list of BookItem to JSON and store it in SharedPreferences
    private fun saveRecommendationsToPreferences(recommendations: List<BookItem>) {
        // Access SharedPreferences in edit mode to store the recommendations
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert the list of BookItem to a JSON string using Gson
        val gson = Gson()
        val json = gson.toJson(recommendations)

        // Store the JSON string as "recommendations" in SharedPreferences
        editor.putString("recommendations", json)

        // Apply the changes to SharedPreferences
        editor.apply()
    }

    // Yunjong Noh
    // Load the stored JSON recommendations from SharedPreferences and convert them back to a list of BookItem
    private fun loadRecommendationsFromPreferences(): List<BookItem>? {
        // Access SharedPreferences to retrieve the stored recommendations JSON
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val gson = Gson()

        // Retrieve the JSON string representing the list of recommendations
        val json = sharedPreferences.getString("recommendations", null)

        // If JSON data exists, convert it back to a list of BookItem using Gson
        return if (json != null) {
            // Define the type for deserialization (List<BookItem>)
            val type = object : TypeToken<List<BookItem>>() {}.type

            // Deserialize the JSON string back into a List<BookItem>
            gson.fromJson(json, type)
        } else {
            // If no JSON data was found, return null
            null
        }
    }

    // Yunjong Noh
    // Retrieve user data from Firestore, including top genres, rating, and high-rated review genres
    private fun fetchRecommendedBooksFromGoogle(uid: String) {
        val db = FirebaseFirestore.getInstance()

        // Step 1: Fetch user's genrePreferences and topGenres
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val genrePreferences = document.get("genrePreferences") as? List<String> ?: listOf() // User's genre preferences
                val topGenres = document.get("topGenres") as? List<String> ?: listOf() // User's top genres
                val avgRating = document.get("avgRating") as? Double ?: 3.0 // User's average rating

                // Log the retrieved preferences and top genres
                Log.d("fetchRecommendedBooks", "Genre Preferences: $genrePreferences")
                Log.d("fetchRecommendedBooks", "Top Genres: $topGenres")
                Log.d("fetchRecommendedBooks", "Average Rating: $avgRating")

                // Combine genrePreferences and topGenres, removing duplicates
                // Use mutable set for adding high-rated genres
                val combinedGenres = (genrePreferences + topGenres).toMutableSet()
                Log.d("fetchRecommendedBooks", "Initial Combined Genres (Preferences + Top Genres): $combinedGenres")

                // Step 2: Fetch high-rated books and genres directly from books collection
                db.collection("books")
                    .get()
                    .addOnSuccessListener { booksSnapshot ->
                        if (booksSnapshot.isEmpty) {
                            Log.w("fetchRecommendedBooks", "No books found in the collection.")
                        } else {
                            booksSnapshot.forEach { bookDocument ->
                                //get the data of each isbn from the books collection
                                Log.d("fetchRecommendedBooks", "Checking book: ${bookDocument.id}")

                                // Step 2.1: Check if this book has any high-rated reviews
                                bookDocument.reference.collection("reviews")
                                    .whereGreaterThanOrEqualTo("rating", 4.0) // Filter only high-rated reviews
                                    .get()
                                    .addOnSuccessListener { reviewsSnapshot ->
                                        if (!reviewsSnapshot.isEmpty) {
                                            // If there are high-rated reviews, add the book's genres to combinedGenres
                                            val genres = bookDocument.get("genres") as? List<String> ?: emptyList()
                                            if (genres.isNotEmpty()) {
                                                combinedGenres.addAll(genres)
                                                Log.d("fetchRecommendedBooks", "High-rated genres added from book ${bookDocument.id}: $genres")
                                            }
                                        } else {
                                            Log.d("fetchRecommendedBooks", "No high-rated reviews for book: ${bookDocument.id}")
                                        }

                                        // Step 3: After all books are processed,
                                        // perform the Google Books search with the final genre list
                                        if (bookDocument == booksSnapshot.documents.last()) { // Check if last book is processed
                                            val genreList = combinedGenres.toList()
                                            if (genreList.isNotEmpty()) {
                                                Log.d("fetchRecommendedBooks", "Final genre list for search: $genreList")
                                                performGoogleBooksSearch(genreList, avgRating) //search books from api
                                            } else {
                                                Log.w("fetchRecommendedBooks", "No genres available for recommendations.")
                                            }
                                        }
                                    }
                                    // error handler
                                    .addOnFailureListener { e ->
                                        Log.e("fetchRecommendedBooks", "Error retrieving reviews for book ${bookDocument.id}: ${e.message}", e)
                                    }
                            }
                        }
                    }// error handler
                    .addOnFailureListener { e ->
                        Log.e("fetchRecommendedBooks", "Error retrieving books collection: ${e.message}", e)
                    }
            } else { // error handler
                Log.w("fetchRecommendedBooks", "No user data found.")
            } // error handler
        }.addOnFailureListener { e ->
            Log.e("fetchRecommendedBooks", "Error retrieving user data: ${e.message}", e)
        }
    }

    // Yunjong Noh
    // Function to perform a Google Books API search based on the user's top genres
    private fun performGoogleBooksSearch(genres: List<String>, avgRating: Double) {
        val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM" // Google Books API key
        val genreBooksMap = mutableMapOf<String, MutableList<BookItem>>() // Map to store books by genre
        val recommendedBookIds = mutableSetOf<String>() // Set to track book IDs to avoid duplicates
        var apiCallsCompleted = 0 // Counter to track how many API calls have been completed

        Log.d("HomeFragment", "Combined recommendation for the user (genrePreferences + topGenres): $genres")

        // For each genre, make a single Google Books API request
        genres.forEach { genre ->
            genreBooksMap[genre] = mutableListOf() // Initialize a list for each genre in the map

            // Generate a random starting index between 0 and 30 to get varied search results
            val randomStartIndex = Random.nextInt(0, 30)

            // Nested function to fetch books for a specific genre with an attempt counter for retries
            fun fetchBooksForGenre(attempt: Int) {
                val query = "subject:$genre" // Google Books API query parameter for subject/genre
                val call = GoogleBooksApiService.create().searchBooks(query, randomStartIndex, 20, apiKey) // API call to search books

                // Handle the API response
                call.enqueue(object : Callback<BookResponse> {
                    override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                        if (response.isSuccessful) {
                            // Retrieve the list of books from the response
                            val books = response.body()?.items ?: emptyList()
                            Log.d("HomeFragment", "Books retrieved for genre '$genre': ${books.size}")

                            // Add unique books to the genreBooksMap, limiting to 1 book per genre
                            books.firstOrNull { !recommendedBookIds.contains(it.id) }?.let { book ->
                                genreBooksMap[genre]?.add(book) // Add book to the map for the genre
                                recommendedBookIds.add(book.id) // Track book ID to avoid duplicates
                            }

                            apiCallsCompleted++ // Increment the API call completion counter
                            if (apiCallsCompleted == genres.size) { // Check if all genre searches are completed
                                val finalBooks = genreBooksMap.values.flatten().take(3) // Take up to 3 books total
                                if (finalBooks.isNotEmpty()) {
                                    displayRecommendedBooks(finalBooks) // Display the final book recommendations
                                } else {
                                    Log.d("HomeFragment", "No books found across all genres.")
                                }
                            }
                        } else {
                            // Handle API error response and retry if necessary
                            Log.d("HomeFragment", "Google Books API Error for genre '$genre': ${response.errorBody()?.string()}")
                            handleApiRetryOrFailure(attempt)
                        }
                    }

                    override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                        // Log failure and retry if needed
                        Log.d("HomeFragment", "Google Books API Failure for genre '$genre': ${t.message}")
                        handleApiRetryOrFailure(attempt)
                    }

                    // Nested function to handle retry or failure logic
                    private fun handleApiRetryOrFailure(attempt: Int) {
                        if (attempt < 5) { // Limit to 5 retry attempts
                            val delay = (1000 * Math.pow(2.0, attempt.toDouble())).toLong() // Exponential backoff (1s, 2s, 4s, 8s, etc.)
                            Log.d("HomeFragment", "Retrying in ${delay / 1000}s for genre '$genre' (attempt $attempt)")
                            Handler(Looper.getMainLooper()).postDelayed({ fetchBooksForGenre(attempt + 1) }, delay)
                        } else {
                            // Final attempt failed; increment completed counter
                            apiCallsCompleted++
                            if (apiCallsCompleted == genres.size) { // Check if all genre searches are completed
                                val finalBooks = genreBooksMap.values.flatten().take(3) // Take up to 3 books total
                                if (finalBooks.isNotEmpty()) {
                                    displayRecommendedBooks(finalBooks) // Display the final book recommendations
                                } else {
                                    Log.d("HomeFragment", "No books found across all genres.")
                                }
                            }
                        }
                    }
                })
            }

            // Start the first attempt to fetch books for the current genre
            fetchBooksForGenre(0)
        }
    }

    // Yunjong Noh
    // Display the recommended books in the UI
    private fun displayRecommendedBooks(books: List<BookItem>) {
        // Save non-empty list of recommended books to SharedPreferences to reuse within 24 hours
        if (books.isNotEmpty()) {
            saveRecommendationsToPreferences(books)
            // Update the first book's info
            val book1 = books[0] // Get the first book from the list
            bookTitleTextView1.text =
                book1.volumeInfo.title // Set the book title in the corresponding TextView
            bookAuthorsTextView1.text = book1.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author" // Display the book authors or a default text if no authors are available

            val thumbnail1 = book1.volumeInfo.imageLinks?.thumbnail?.replace(
                "http://",
                "https://"
            ) // Get the thumbnail URL and ensure it uses HTTPS instead of HTTP
            // Load the book cover image using Glide
            // If the thumbnail URL is null, load a default placeholder image
            Log.d(
                "HomeFragment",
                "Book 1 Image URL: $thumbnail1"
            ) // Log the image URL for debugging
            if (isAdded && activity != null) {
                Glide.with(this)
                    .load(
                        thumbnail1 ?: R.drawable.placeholder_image
                    ) // If the URL is null, use a default image
                    .skipMemoryCache(true) // avoid cache for fresh load
                    .into(bookCoverImageView1) // Display the image in the ImageView for the first book
            }

            // Update the second book's info, ALL logic is the same as First one.
            val book2 = books.getOrNull(1)
            if (book2 != null) {
                bookTitleTextView2.text = book2.volumeInfo.title
                bookAuthorsTextView2.text =
                    book2.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"


                val thumbnail2 =
                    book2.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 2 Image URL: $thumbnail2")
                if (isAdded && activity != null) {
                    Glide.with(this)
                        .load(thumbnail2 ?: R.drawable.placeholder_image)
                        .skipMemoryCache(true)
                        .into(bookCoverImageView2)
                }
            }

            // Update the third book's info, ALL logic is the same as First one.
            val book3 = books.getOrNull(2)
            if (book3 != null) {
                bookTitleTextView3.text = book3.volumeInfo.title
                bookAuthorsTextView3.text =
                    book3.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"


                val thumbnail3 =
                    book3.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 3 Image URL: $thumbnail3")
                if (isAdded && activity != null) {
                    Glide.with(this)
                        .load(thumbnail3 ?: R.drawable.placeholder_image)
                        .skipMemoryCache(true)
                        .into(bookCoverImageView3)
                }
            }
        }
    }
}