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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.BookItem
import com.example.booknook.R
import com.example.booknook.BookResponse
import com.example.booknook.api.GoogleBooksApiService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
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

    // Yunjong Noh
    // RecyclerView for notifications
    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var updatesAdapter: UpdatesAdapter
    private val notificationList = mutableListOf<NotificationItem>()

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

        // Yunjong Noh
        // Initialize RecyclerView for notifications
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        updatesAdapter = UpdatesAdapter(notificationList) { notificationId ->
            dismissNotification(notificationId) // Firestore에서 알림 해제 함수 호출
        }
        notificationsRecyclerView.adapter = updatesAdapter

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

                    // Load recommendations if needed or if no stored data is available
                    if (shouldFetchRecommendations(uid) || loadRecommendationsFromPreferences(uid) == null) {
                        fetchRecommendedBooksFromGoogle(uid)
                        saveLastFetchDate(uid) // Save the current date as the last fetch date
                    } else {
                        loadRecommendationsFromPreferences(uid)?.let { recommendations ->
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
        // Yunjong Noh
        // Initialize UpdatesAdapter with the dismiss callback
        updatesAdapter = UpdatesAdapter(notificationList) { notificationId ->
            // Call the function to dismiss the notification in Firestore
            dismissNotification(notificationId)
        }
        notificationsRecyclerView.adapter = updatesAdapter

        // Fetch and display notifications once and add real-time listener for updates
        fetchNotifications()
        listenToNotifications()

        deleteExpiredNotifications() // Call the function to delete expired notifications
    }

    // Yunjong Noh
    // Function to delete expired notifications
    private fun deleteExpiredNotifications() {
        val db = FirebaseFirestore.getInstance() // Initialize Firestore instance
        val now = System.currentTimeMillis() // Get current time

        // Fetch all notifications from Firestore
        db.collection("notifications").get()
            .addOnSuccessListener { result ->
                // Iterate through each notification
                for (document in result) {
                    val expirationTime = document.getLong("expirationTime") ?: 0L
                    if (expirationTime <= now) {
                        // Delete the notification if it has expired
                        db.collection("notifications").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("HomeFragment", "Expired notification deleted: ${document.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("HomeFragment", "Error deleting expired notification: ${e.message}", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching notifications: ${e.message}", e)
            }
    }


    // Yunjong Noh
    // Fetch notifications from Firestore
    private fun fetchNotifications() {
        db.collection("notifications").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                Log.d("HomeFragment", "No notifications found in Firestore.")
            } else {
                // Check for new, non-dismissed notifications
                val existingIds = notificationList.map { it.notificationId }.toSet()
                // Iterate over each document in the result
                for (document in result) {
                    // Convert the document to a NotificationItem object
                    val notification = document.toObject(NotificationItem::class.java)
                    if (!notification.dismissed && !existingIds.contains(notification.notificationId)) {
                        // Fetch user details and update notification
                        db.collection("users").document(notification.userId).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    // Set the profile image URL and username for the notification
                                    notification.profileImageUrl = userDoc.getString("profileImageUrl") ?: ""
                                    notification.username = userDoc.getString("username") ?: "Unknown User"
                                    // Add the notification to the list and update the adapter
                                    notificationList.add(notification)
                                    updatesAdapter.notifyDataSetChanged()
                                } else {
                                    Log.d("HomeFragment", "User not found for userId: ${notification.userId}")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("HomeFragment", "Error fetching user info: ${e.message}", e)
                            }
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("HomeFragment", "Error fetching notifications: ${e.message}", e)
        }
    }

    // Yunjong Noh
    // Dismiss the notification by updating Firestore
    private fun dismissNotification(notificationId: String) {
        db.collection("notifications").document(notificationId)
            .update("dismissed", true)
            .addOnSuccessListener {
                Log.d("HomeFragment", "Notification dismissed successfully.")

                // Find and remove the notification from the notification list
                val index = notificationList.indexOfFirst { it.notificationId == notificationId }
                if (index != -1) { // If the notification exists in the list
                    notificationList.removeAt(index) // Remove the notification from the list
                    updatesAdapter.notifyItemRemoved(index) // Reflect the changes in the RecyclerView
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error dismissing notification: ${e.message}", e)
            }
    }
    // Yunjong Noh
    // Function to listen for real-time updates to notifications
    private fun listenToNotifications() {
        db.collection("notifications")
            .whereEqualTo("userId", auth.currentUser?.uid) // Filter notifications for the current user
            .whereEqualTo("isDismissed", false) // Only listen to non-dismissed notifications
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeFragment", "Listen failed.", e) // Log if there is an error
                    return@addSnapshotListener
                }

                // Iterate through the document changes
                for (docChange in snapshots!!.documentChanges) {
                    val notification = docChange.document.toObject(NotificationItem::class.java)
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            // Handle newly added notifications
                            Log.d("HomeFragment", "New notification: ${notification.message}")
                            notificationList.add(notification)
                            updatesAdapter.notifyDataSetChanged()
                            showInAppNotification(notification.message) // Show in-app notification
                        }
                        // Handling for checking log
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("HomeFragment", "Notification modified: ${notification.message}")
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d("HomeFragment", "Notification removed: ${notification.message}")
                        }
                    }
                }
            }
    }
    // Yunjong Noh
    // Function to display an in-app notification using Snackbar
    private fun showInAppNotification(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() // Show message in a Snackbar
        }
    }



    // Yunjong Noh
    // Check if 24 hours have passed since the last fetch
    private fun shouldFetchRecommendations(userId: String): Boolean {
        // Access SharedPreferences to retrieve the timestamp of the last fetch
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val lastFetchTime = sharedPreferences.getLong("lastRecommendationFetchTime_$userId", 0L)

        // Get the current time in milliseconds
        val currentTime = Calendar.getInstance().timeInMillis

        // Define 24 hours in milliseconds
        val oneDayInMillis = 24 * 60 * 60 * 1000 // 24 hours

        // Return true if 24 hours have passed since the last fetch, false otherwise
        val shouldFetch = (currentTime - lastFetchTime) >= oneDayInMillis
        Log.d("shouldFetchRecommendations", "Should fetch for user $userId: $shouldFetch")
        return shouldFetch
    }

    // Yunjong Noh
    // Save the current date as the last fetch date
    private fun saveLastFetchDate(userId: String) {
        // Access SharedPreferences in edit mode to store the current timestamp
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Store the current time in milliseconds as the "lastRecommendationFetchTime"
        editor.putLong("lastRecommendationFetchTime_$userId", Calendar.getInstance().timeInMillis)

        // Apply the changes to SharedPreferences
        editor.apply()
    }

    // Yunjong Noh
    // Convert the list of BookItem to JSON and store it in SharedPreferences
    private fun saveRecommendationsToPreferences(userId: String, recommendations: List<BookItem>) {
        // Access SharedPreferences in edit mode to store the recommendations
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert the list of BookItem to a JSON string using Gson
        val gson = Gson()
        val json = gson.toJson(recommendations)

        // Store the JSON string as "recommendations" in SharedPreferences
        editor.putString("recommendations_$userId", json)
        Log.d("saveRecommendations", "Saving recommendations for user $userId: $json")

        // Apply the changes to SharedPreferences
        editor.apply()
    }

    // Yunjong Noh
    // Load the stored JSON recommendations from SharedPreferences and convert them back to a list of BookItem
    private fun loadRecommendationsFromPreferences(userId: String): List<BookItem>? {
        // Access SharedPreferences to retrieve the stored recommendations JSON
        val sharedPreferences = requireContext().getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE)
        val gson = Gson()

        // Retrieve the JSON string representing the list of recommendations
        val json = sharedPreferences.getString("recommendations_$userId", null)
        Log.d("loadRecommendations", "Loaded recommendations for user $userId: $json")
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
                Log.d("fetchRecommendedBooks", "User $uid genrePreferences: $genrePreferences")
                Log.d("fetchRecommendedBooks", "User $uid topGenres: $topGenres")
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
        val apiKey = "Api here" // Google Books API key
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
            saveRecommendationsToPreferences(auth.currentUser?.uid ?: "", books)
            // Update the first book's info
            val book1 = books[0] // Get the first book from the list
            bookTitleTextView1.text = book1.volumeInfo.title // Set the book title in the corresponding TextView
            // Display the book authors or a default text if no authors are available
            bookAuthorsTextView1.text = book1.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"

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