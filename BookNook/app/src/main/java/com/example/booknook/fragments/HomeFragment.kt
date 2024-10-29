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
    // Retrieve user data from Firestore, including top genres and average rating
    private fun fetchRecommendedBooksFromGoogle(uid: String) {
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val topGenres = document.get("topGenres") as? List<String> ?: listOf() // Get user's top genres
                val avgRating = document.get("avgRating") as? Double ?: 3.0 // Get user's average rating

                // If top genres exist, fetch books from Google Books API
                if (topGenres.isNotEmpty()) {
                    performGoogleBooksSearch(topGenres, avgRating)
                } else {
                    println("No top genres available for user.")
                }
            } else {
                println("No user data found.")
            }
        }.addOnFailureListener { e ->
            println("Error retrieving top genres: ${e.message}")
        }
    }

    // Yunjong Noh
    // Perform Google Books API search based on user's Top genres
    private fun performGoogleBooksSearch(genres: List<String>, avgRating: Double) {
        val apiKey = "give me api code" // API key for Google Books API
        val genreBooksMap = mutableMapOf<String, MutableList<BookItem>>() // Store books by genre
        val recommendedBookIds = mutableSetOf<String>() // Track book IDs to avoid duplicates
        var apiCallsCompleted = 0 // Counter to track how many API calls are done

        Log.d("HomeFragment", "Top genres for the user: $genres")

        // For each genre, make a Google Books API request with randomized startIndex
        genres.forEach { genre ->
            genreBooksMap[genre] = mutableListOf() // Initialize list for each genre
            // Randomize the starting index for each genre request to get different results on each refresh
            val randomStartIndex = Random.nextInt(0, 1000) // Start index up to 1000 for more variety
            fetchBooksForGenreWithRetry(genre, apiKey, genreBooksMap, recommendedBookIds, randomStartIndex, 0) {
                apiCallsCompleted++
                if (apiCallsCompleted == genres.size) {
                    val finalBooks = genreBooksMap.values.flatten().shuffled().take(3) // Shuffle and pick up to 3 books
                    if (finalBooks.isNotEmpty()) {
                        displayRecommendedBooks(finalBooks) // Update the UI with selected books
                    } else {
                        println("No unique books found.") // Log if no books are found
                    }
                }
            }
        }
    }

    // Yunjong Noh
    // Recursive function to handle retries with a random startIndex
    private fun fetchBooksForGenreWithRetry(
        genre: String, // Genre to search for
        apiKey: String, // API key for Google Books API
        genreBooksMap: MutableMap<String, MutableList<BookItem>>, // Map to store books by genre
        recommendedBookIds: MutableSet<String>, // Set of recommended book IDs to avoid duplicates
        startIndex: Int, // Start index for pagination, randomized for variety
        attempt: Int, // Current retry attempt
        onComplete: () -> Unit // Callback for when the process is complete
    ) {
        if (attempt >= 3) { // Exit if max retries reached
            Log.d("HomeFragment", "Max retries reached for genre '$genre'.")
            onComplete() // Call completion callback
            return
        }

        val query = "subject:$genre" // Search query for the genre
        val call = GoogleBooksApiService.create().searchBooks(query, startIndex, 20, apiKey) // API call for books

        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) { // Check if response is successful
                    val books = response.body()?.items ?: emptyList() // Get list of books or empty if none
                    Log.d("HomeFragment", "Books retrieved for genre '$genre' at startIndex $startIndex: ${books.size}")

                    if (books.isNotEmpty()) { // If books are found
                        val highRatedBooks = books.filter { book -> // Filter high-rated books not yet recommended
                            val rating = book.volumeInfo.averageRating ?: return@filter false
                            rating >= 4.0 && !recommendedBookIds.contains(book.id)
                        }

                        if (highRatedBooks.isNotEmpty()) { // Add high-rated book if available
                            genreBooksMap[genre]?.add(highRatedBooks.first())
                            recommendedBookIds.add(highRatedBooks.first().id)
                        } else { // Otherwise, add the best-rated book found
                            val bestRatedBook = books.maxByOrNull { it.volumeInfo.averageRating?.toDouble() ?: -1.0 }
                            bestRatedBook?.let { book ->
                                genreBooksMap[genre]?.add(book)
                                recommendedBookIds.add(book.id)
                            }
                        }
                        onComplete() // Complete if books were added
                    } else { // If no books found, retry with incremented startIndex
                        Log.d("HomeFragment", "No books found for genre '$genre' at startIndex $startIndex. Retrying with a new startIndex...")
                        val newStartIndex = startIndex + 20 // Increase startIndex for variety
                        fetchBooksForGenreWithRetry(genre, apiKey, genreBooksMap, recommendedBookIds, newStartIndex, attempt + 1, onComplete) // Retry with updated parameters
                    }
                } else { // Log error if response unsuccessful
                    Log.d("HomeFragment", "Google Books API Error for genre '$genre': ${response.errorBody()?.string()}")
                    onComplete()
                }
            }
            // handling control at catlog
            override fun onFailure(call: Call<BookResponse>, t: Throwable) { // Handle API call failure
                Log.d("HomeFragment", "Google Books API Failure for genre '$genre': ${t.message}")
                onComplete()
            }
        })
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