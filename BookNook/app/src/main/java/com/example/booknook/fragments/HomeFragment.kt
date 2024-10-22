package com.example.booknook.fragments

import android.os.Bundle
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
        val apiKey = "Give ur api password"
        val allBooks = mutableListOf<BookItem>() // Store all books found
        val recommendedBookIds = mutableSetOf<String>() // Track book IDs to avoid duplicates
        var apiCallsCompleted = 0 // Counter to track how many API calls are done

        // For each genre, make an Google books API request
        genres.forEach { genre ->
            val startIndex = Random.nextInt(0, 100) // Randomly set a start index to get different results
            val query = "subject:$genre" // Build query for the current genre
            //Make a request based on gerne, returns randomly 8 books using API
            val call = GoogleBooksApiService.create().searchBooks(query, startIndex, 8, apiKey)

            // Handle the API response
            call.enqueue(object : Callback<BookResponse> {
                override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                    if (response.isSuccessful) {
                        // Get the list of books from the response
                        val books = response.body()?.items ?: emptyList()

                        // Add only new books to the list
                        val uniqueBooks = books.filter { book ->
                            val isNewBook = !recommendedBookIds.contains(book.id) // Check if it's a new book
                            if (isNewBook) {
                                recommendedBookIds.add(book.id) // Track the new book ID
                            }
                            isNewBook // Add if it's a new book
                        }
                        allBooks.addAll(uniqueBooks) // Add unique books to the main list
                    } else {
                        println("Google Books API Error: ${response.errorBody()?.string()}")
                    }

                    // Track how many API calls have completed
                    apiCallsCompleted++

                    // Once all API calls are complete, randomly select 3 books to display
                    if (apiCallsCompleted == genres.size) {
                        if (allBooks.isNotEmpty()) {
                            // Randomly pick 3 books
                            val finalBooks = allBooks.shuffled().take(3) // Shuffle and pick 3 books
                            displayRecommendedBooks(finalBooks) // Update the UI
                        } else {
                            println("No unique books found.")
                        }
                    }
                }
                // Yunjong Noh
                // Handle API call failure for in case of network API request fails
                override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                    println("Google Books API Failure: ${t.message}")
                    apiCallsCompleted++ // Increment the count of completed API calls

                    // Check if all API calls are completed
                    if (apiCallsCompleted == genres.size) {
                        // If books were retrieved, randomly select 3 to display
                        if (allBooks.isNotEmpty()) {
                            val finalBooks = allBooks.shuffled().take(3) // Pick 3 random books
                            displayRecommendedBooks(finalBooks) // Update the UI with selected books
                        } else {
                            println("No unique books found.") //error handler
                        }
                    }
                }
            })
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
