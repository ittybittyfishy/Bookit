package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.BookItem
import com.example.booknook.R
import com.example.booknook.BookResponse
import com.example.booknook.api.GoogleBooksApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    private lateinit var bookRating1: RatingBar
    private lateinit var ratingNumber1: TextView
    private lateinit var ratingsCountTextView1: TextView

    private lateinit var bookCoverImageView2: ImageView
    private lateinit var bookTitleTextView2: TextView
    private lateinit var bookAuthorsTextView2: TextView
    private lateinit var bookRating2: RatingBar
    private lateinit var ratingNumber2: TextView
    private lateinit var ratingsCountTextView2: TextView

    private lateinit var bookCoverImageView3: ImageView
    private lateinit var bookTitleTextView3: TextView
    private lateinit var bookAuthorsTextView3: TextView
    private lateinit var bookRating3: RatingBar
    private lateinit var ratingNumber3: TextView
    private lateinit var ratingsCountTextView3: TextView

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
        bookRating1 = view.findViewById(R.id.bookRating1)
        ratingNumber1 = view.findViewById(R.id.ratingNumber1)
        ratingsCountTextView1 = view.findViewById(R.id.ratingsCountTextView1)

        bookCoverImageView2 = view.findViewById(R.id.bookCoverImageView2)
        bookTitleTextView2 = view.findViewById(R.id.bookTitleTextView2)
        bookAuthorsTextView2 = view.findViewById(R.id.bookAuthorsTextView2)
        bookRating2 = view.findViewById(R.id.bookRating2)
        ratingNumber2 = view.findViewById(R.id.ratingNumber2)
        ratingsCountTextView2 = view.findViewById(R.id.ratingsCountTextView2)

        bookCoverImageView3 = view.findViewById(R.id.bookCoverImageView3)
        bookTitleTextView3 = view.findViewById(R.id.bookTitleTextView3)
        bookAuthorsTextView3 = view.findViewById(R.id.bookAuthorsTextView3)
        bookRating3 = view.findViewById(R.id.bookRating3)
        ratingNumber3 = view.findViewById(R.id.ratingNumber3)
        ratingsCountTextView3 = view.findViewById(R.id.ratingsCountTextView3)

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
                    fetchRecommendedBooksFromGoogle(uid)
                } else {
                    loggedInTextView.text = "Username not found"
                }
            }.addOnFailureListener { exception ->
                loggedInTextView.text = "Error: ${exception.message}"
            }
        }
    }

    // Yunjong Noh
    // Retrieve user data from Firestore, including top genres and rating
    private fun fetchRecommendedBooksFromGoogle(uid: String) {
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Retrieve both genrePreferences and topGenres from Firestore
                val genrePreferences = document.get("genrePreferences") as? List<String> ?: listOf() // User's genre preferences
                val topGenres = document.get("topGenres") as? List<String> ?: listOf() // Get user's top genres
                val avgRating = document.get("avgRating") as? Double ?: 3.0 // Get user's average rating

                // Combine the two lists into a single list of genres, removing duplicates
                val combinedGenres = (genrePreferences + topGenres).distinct() // Remove duplicates

                // If the combined genre list is not empty, perform a search with Google Books API
                if (combinedGenres.isNotEmpty()) {
                    performGoogleBooksSearch(combinedGenres, avgRating)
                } else {
                    println("No genre preferences or top genres available for user.")
                }
            } else {
                println("No user data found.")
            }
        }.addOnFailureListener { e ->
            // Print error message if there’s an issue retrieving genre preferences or top genres
            println("Error retrieving genre preferences or top genres: ${e.message}")
        }
    }

    // Yunjong Noh
    // Perform Google Books API search based on user's Top genres
    private fun performGoogleBooksSearch(genres: List<String>, avgRating: Double) {
        val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM" // API key for Google Books API
        val genreBooksMap = mutableMapOf<String, MutableList<BookItem>>() // Store books by genre
        val recommendedBookIds = mutableSetOf<String>() // Track book IDs to avoid duplicates
        var apiCallsCompleted = 0 // Counter to track how many API calls are done

        Log.d("HomeFragment", "Combined recommendation for the user (genrePreferences + topGenres): $genres")

        // For each genre, make a single Google Books API request
        genres.forEach { genre ->
            genreBooksMap[genre] = mutableListOf() // Initialize list for each genre

            // Generate a random startIndex between 0 and 100 for varied search results
            val randomStartIndex = Random.nextInt(0, 100)

            // Nested function to fetch books for a specific genre with an attempt counter
            fun fetchBooksForGenre(attempt: Int) {
                val query = "subject:$genre" // Build query for the current genre
                val call = GoogleBooksApiService.create().searchBooks(query, randomStartIndex, 20, apiKey) // Single API call per genre with random startIndex

                call.enqueue(object : Callback<BookResponse> {
                    override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                        if (response.isSuccessful) {
                            // Retrieve the list of books from the response
                            val books = response.body()?.items ?: emptyList()
                            Log.d("HomeFragment", "Books retrieved for genre '$genre': ${books.size}")

                            // Add unique books to the genreBooksMap up to 1 per genre
                            books.firstOrNull { !recommendedBookIds.contains(it.id) }?.let { book ->
                                genreBooksMap[genre]?.add(book)
                                recommendedBookIds.add(book.id) // Mark the book ID as added to avoid duplicates
                            }

                            // Increment the completed API calls counter
                            apiCallsCompleted++
                            // Once all API calls are complete, gather the results and display recommendations
                            if (apiCallsCompleted == genres.size) {
                                val finalBooks = genreBooksMap.values.flatten().take(3) // Select up to 3 books
                                if (finalBooks.isNotEmpty()) {
                                    displayRecommendedBooks(finalBooks) // Display final recommendations
                                } else {
                                    Log.d("HomeFragment", "No books found across all genres.")
                                }
                            }
                        } else {
                            // Log an error if the response is unsuccessful
                            Log.d("HomeFragment", "Google Books API Error for genre '$genre': ${response.errorBody()?.string()}")
                            handleApiRetryOrFailure(attempt) // Handle retry or failure logic
                        }
                    }

                    override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                        // Log a message if the API call fails
                        Log.d("HomeFragment", "Google Books API Failure for genre '$genre': ${t.message}")
                        handleApiRetryOrFailure(attempt) // Handle retry or failure logic
                    }

                    // Function to retry the API call or mark the call as complete on failure
                    private fun handleApiRetryOrFailure(attempt: Int) {
                        if (attempt < 1) { // Only retry once to prevent excessive API calls
                            fetchBooksForGenre(attempt + 1) // Retry by increasing the attempt counter
                        } else {
                            // Mark as complete if retry limit is reached
                            apiCallsCompleted++
                            if (apiCallsCompleted == genres.size) {
                                val finalBooks = genreBooksMap.values.flatten().take(3) // Select up to 3 books
                                if (finalBooks.isNotEmpty()) {
                                    displayRecommendedBooks(finalBooks)
                                } else {
                                    Log.d("HomeFragment", "No books found across all genres.")
                                }
                            }
                        }
                    }
                })
            }

            // initializes first attempt to fetch books for genre
            fetchBooksForGenre(0)
        }
    }

    // Yunjong Noh
    // Display the recommended books in the UI
    private fun displayRecommendedBooks(books: List<BookItem>) {
        if (books.isNotEmpty()) {
            // Update the first book's info
            val book1 = books[0] // Get the first book from the list
            bookTitleTextView1.text = book1.volumeInfo.title // Set the book title in the corresponding TextView
            bookAuthorsTextView1.text = book1.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author" // Display the book authors or a default text if no authors are available

            // Set the book rating and rating count
            val rating1 = book1.volumeInfo.averageRating ?: 0.0
            val ratingsCount1 = book1.volumeInfo.ratingsCount ?: 0
            bookRating1.rating = rating1.toFloat()
            ratingNumber1.text = "($rating1)"
            ratingsCountTextView1.text = "$ratingsCount1 ratings"

            val thumbnail1 = book1.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://") // Get the thumbnail URL and ensure it uses HTTPS instead of HTTP
            // Load the book cover image using Glide
            // If the thumbnail URL is null, load a default placeholder image
            Log.d("HomeFragment", "Book 1 Image URL: $thumbnail1") // Log the image URL for debugging
            Glide.with(this)
                .load(thumbnail1 ?: R.drawable.placeholder_image) // If the URL is null, use a default image
                .skipMemoryCache(true) // avoid cache for fresh load
                .into(bookCoverImageView1) // Display the image in the ImageView for the first book

            // Update the second book's info, ALL logic is the same as First one.
            val book2 = books.getOrNull(1)
            if (book2 != null) {
                bookTitleTextView2.text = book2.volumeInfo.title
                bookAuthorsTextView2.text = book2.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"

                val rating2 = book2.volumeInfo.averageRating ?: 0.0
                val ratingsCount2 = book2.volumeInfo.ratingsCount ?: 0
                bookRating2.rating = rating2.toFloat()
                ratingNumber2.text = "($rating2)"
                ratingsCountTextView2.text = "$ratingsCount2 ratings"

                val thumbnail2 = book2.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 2 Image URL: $thumbnail2")
                Glide.with(this)
                    .load(thumbnail2 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true)
                    .into(bookCoverImageView2)
            }

            // Update the third book's info, ALL logic is the same as First one.
            val book3 = books.getOrNull(2)
            if (book3 != null) {
                bookTitleTextView3.text = book3.volumeInfo.title
                bookAuthorsTextView3.text = book3.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"

                val rating3 = book3.volumeInfo.averageRating ?: 0.0
                val ratingsCount3 = book3.volumeInfo.ratingsCount ?: 0
                bookRating3.rating = rating3.toFloat()
                ratingNumber3.text = "($rating3)"
                ratingsCountTextView3.text = "$ratingsCount3 ratings"

                val thumbnail3 = book3.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 3 Image URL: $thumbnail3")
                Glide.with(this)
                    .load(thumbnail3 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true)
                    .into(bookCoverImageView3)
            }
        }
    }
}