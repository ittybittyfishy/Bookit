package com.example.booknook.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import android.widget.LinearLayout // If not already imported
import android.widget.Toast


class HomeFragment : Fragment() {

    //testing
    private lateinit var refreshButton: Button

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

    //work review 4 itzel medina
    private lateinit var dislikeButton1: ImageButton
    private lateinit var likeButton1: ImageButton
    private lateinit var messageTextView1: TextView
    private lateinit var buttonContainer1: View

    private lateinit var dislikeButton2: ImageButton
    private lateinit var likeButton2: ImageButton
    private lateinit var messageTextView2: TextView
    private lateinit var buttonContainer2: View

    private lateinit var dislikeButton3: ImageButton
    private lateinit var likeButton3: ImageButton
    private lateinit var messageTextView3: TextView
    private lateinit var buttonContainer3: View

    // Book 4 UI components
    private lateinit var bookCoverImageView4: ImageView
    private lateinit var bookTitleTextView4: TextView
    private lateinit var bookAuthorsTextView4: TextView
    private lateinit var dislikeButton4: ImageButton
    private lateinit var likeButton4: ImageButton
    private lateinit var messageTextView4: TextView
    private lateinit var buttonContainer4: View

    // Variables to store genres for each book
    private var genreBook1: String? = null
    private var genreBook2: String? = null
    private var genreBook3: String? = null

    // Yunjong Noh
    // RecyclerView for notifications
    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var updatesAdapter: UpdatesAdapter
    private val notificationList = mutableListOf<NotificationItem>()

    //work review 4 itzel medina
    // Variable to store genre for Book 4
    private var genreBook4: String? = null


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

        //work review 4
        // Initialize Like and Dislike buttons
        likeButton2 = view.findViewById<ImageButton>(R.id.likeButton2)
        dislikeButton2 = view.findViewById<ImageButton>(R.id.dislikeButton2)
        likeButton3 = view.findViewById<ImageButton>(R.id.likeButton3)
        dislikeButton3 = view.findViewById<ImageButton>(R.id.dislikeButton3)

        // Initialize Message TextViews
        messageTextView1 = view.findViewById<TextView>(R.id.messageTextView1)
        messageTextView2 = view.findViewById<TextView>(R.id.messageTextView2)
        messageTextView3 = view.findViewById<TextView>(R.id.messageTextView3)

        // Initialize Book 1 views
        dislikeButton1 = view.findViewById(R.id.dislikeButton1)
        likeButton1 = view.findViewById(R.id.likeButton1)
        messageTextView1 = view.findViewById(R.id.messageTextView1)
        buttonContainer1 = view.findViewById(R.id.buttonContainer1)

        // Initialize Book 2 views
        dislikeButton2 = view.findViewById(R.id.dislikeButton2)
        likeButton2 = view.findViewById(R.id.likeButton2)
        messageTextView2 = view.findViewById(R.id.messageTextView2)
        buttonContainer2 = view.findViewById(R.id.buttonContainer2)

        // Initialize Book 3 views
        dislikeButton3 = view.findViewById(R.id.dislikeButton3)
        likeButton3 = view.findViewById(R.id.likeButton3)
        messageTextView3 = view.findViewById(R.id.messageTextView3)
        buttonContainer3 = view.findViewById(R.id.buttonContainer3)

        //work review 4 itzel medina
        // Initialize Book 4 views
        bookCoverImageView4 = view.findViewById(R.id.bookCoverImageView4)
        bookTitleTextView4 = view.findViewById(R.id.bookTitleTextView4)
        bookAuthorsTextView4 = view.findViewById(R.id.bookAuthorsTextView4)
        dislikeButton4 = view.findViewById(R.id.dislikeButton4)
        likeButton4 = view.findViewById(R.id.likeButton4)
        messageTextView4 = view.findViewById(R.id.messageTextView4)
        buttonContainer4 = view.findViewById(R.id.buttonContainer4)


        // Set up click listeners for Book 1
        dislikeButton1.setOnClickListener {
            onDislikeClicked(buttonContainer1, messageTextView1, "You disliked this book!")
        }

        likeButton1.setOnClickListener {
            onLikeClicked(buttonContainer1, messageTextView1, "You liked this book!")
        }

        // Set up click listeners for Book 2
        dislikeButton2.setOnClickListener {
            onDislikeClicked(buttonContainer2, messageTextView2, "You disliked this book!")
        }

        likeButton2.setOnClickListener {
            onLikeClicked(buttonContainer2, messageTextView2, "You liked this book!")
        }

        // Set up click listeners for Book 3
        dislikeButton3.setOnClickListener {
            onDislikeClicked(buttonContainer3, messageTextView3, "You disliked this book!")
        }

        likeButton3.setOnClickListener {
            onLikeClicked(buttonContainer3, messageTextView3, "You liked this book!")
        }

        // Yunjong Noh
        // Initialize RecyclerView for notifications
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        updatesAdapter = UpdatesAdapter(notificationList) { notificationId ->
            dismissNotification(notificationId) // Call Notification Release Function in Firestore
        }
        notificationsRecyclerView.adapter = updatesAdapter

        //Itzel medina work review 4
        // Set up click listeners for Book 4
        dislikeButton4.setOnClickListener {
            onDislikeClicked(buttonContainer4, messageTextView4, "You disliked this book!")
        }

        likeButton4.setOnClickListener {
            onLikeClicked(buttonContainer4, messageTextView4, "You liked this book!")
        }

        //testing
        // Initialize Refresh Button
        refreshButton = view.findViewById(R.id.refreshButton)

        // Set up click listener for Refresh Button
        refreshButton.setOnClickListener {
            onRefreshButtonClicked()
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get users UID
        val userId = auth.currentUser?.uid

        //work review 4
        // Set up Like and Dislike button listeners
        setupButtonListeners()


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
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("userId", currentUserId) // Only fetch notifications for the current user
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("HomeFragment", "No notifications found for current user.")
                } else {
                    // Check for new, non-dismissed notifications
                    val existingIds = notificationList.map { it.notificationId }.toSet()
                    // Iterate over each document in the result
                    for (document in result) {
                        val notification = document.toObject(NotificationItem::class.java)
                        if (!notification.dismissed && !existingIds.contains(notification.notificationId)) {
                            // Fetch user details for sender (not receiver)
                            db.collection("users").document(notification.senderId).get()
                                .addOnSuccessListener { senderDoc ->
                                    if (senderDoc.exists()) {
                                        // Set the profile image URL and username for the sender (not receiver)
                                        notification.profileImageUrl = senderDoc.getString("profileImageUrl") ?: ""
                                        notification.username = senderDoc.getString("username") ?: "Unknown User"

                                        // Add the notification to the list and update the adapter
                                        notificationList.add(notification)
                                        updatesAdapter.notifyDataSetChanged()
                                    } else {
                                        Log.d("HomeFragment", "Sender not found for senderId: ${notification.senderId}")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("HomeFragment", "Error fetching sender info: ${e.message}", e)
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching notifications: ${e.message}", e)
            }
    }

    // Yunjong Noh
    // Dismiss the notification by updating Firestore
    private fun dismissNotification(notificationId: String) {
        // Only allow dismissing notifications for the current user
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
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("userId", currentUserId) // Only listen to notifications for the current user
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
                            // Fetch sender's details
                            db.collection("users").document(notification.senderId).get()
                                .addOnSuccessListener { senderDoc ->
                                    if (senderDoc.exists()) {
                                        notification.profileImageUrl = senderDoc.getString("profileImageUrl") ?: ""
                                        notification.username = senderDoc.getString("username") ?: "Unknown User"
                                        notificationList.add(notification)
                                        updatesAdapter.notifyDataSetChanged()
                                    } else {
                                        Log.d("HomeFragment", "Sender not found for senderId: ${notification.senderId}")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("HomeFragment", "Error fetching sender info: ${e.message}", e)
                                }
                        }
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



    //testing
    /**
     * Handles the Refresh Recommendations button click.
     */
    private fun onRefreshButtonClicked() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch new recommendations from Google Books API
        fetchRecommendedBooksFromGoogle(userId)

        // Update the last fetch date to current time to prevent immediate refetching (optional)
        saveLastFetchDate(userId)

        // Optionally, clear existing recommendations before fetching new ones
        // clearExistingRecommendations(userId)
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
                                val finalBooks = genreBooksMap.values.flatten().take(4) // Take up to 3 books total
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
        if (books.isNotEmpty()) {
            // Save the list of recommended books to SharedPreferences for later use
            saveRecommendationsToPreferences(auth.currentUser?.uid ?: "", books)

            // ---------------------
            // Display Book 1 Details
            // ---------------------
            val book1 = books.getOrNull(0) // Safely get the first book or null
            // Set the book title, defaulting to "Unknown Title" if null
            bookTitleTextView1.text = book1?.volumeInfo?.title ?: "Unknown Title"
            // Set the book authors, defaulting to "Unknown Author" if null
            bookAuthorsTextView1.text =
                book1?.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"

            // Extract the primary genre, defaulting to "Various Genres" if null
            genreBook1 = book1?.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"

            // Reset the message TextView visibility and text
            messageTextView1.visibility = View.GONE
            messageTextView1.text = "Message will appear here"

            // Prepare the thumbnail URL, replacing "http://" with "https://" for security
            val thumbnail1 =
                book1?.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
            Log.d("HomeFragment", "Book 1 Image URL: $thumbnail1")

            // Load the book cover image using Glide, or use a placeholder if the thumbnail is null
            if (book1 != null) {
                Glide.with(this)
                    .load(thumbnail1 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true) // Skip memory cache for fresh loading
                    .into(bookCoverImageView1)
            } else {
                // If book1 is null, set a default placeholder image
                bookCoverImageView1.setImageResource(R.drawable.placeholder_image)
            }

            // ---------------------
            // Display Book 2 Details
            // ---------------------
            val book2 = books.getOrNull(1) // Safely get the second book or null
            if (book2 != null) {
                // Set the book title, defaulting to "Unknown Title" if null
                bookTitleTextView2.text = book2.volumeInfo?.title ?: "Unknown Title"
                // Set the book authors, defaulting to "Unknown Author" if null
                bookAuthorsTextView2.text =
                    book2.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"

                // Extract the primary genre, defaulting to "Various Genres" if null
                genreBook2 = book2.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"

                // Reset the message TextView visibility and text
                messageTextView2.visibility = View.GONE
                messageTextView2.text = "Message will appear here"

                // Prepare the thumbnail URL, replacing "http://" with "https://" for security
                val thumbnail2 =
                    book2.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 2 Image URL: $thumbnail2")

                // Load the book cover image using Glide, or use a placeholder if the thumbnail is null
                Glide.with(this)
                    .load(thumbnail2 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true) // Skip memory cache for fresh loading
                    .into(bookCoverImageView2)
            } else {
                // Hide the entire Book 2 layout if the book is not available
                view?.findViewById<LinearLayout>(R.id.bookItem2)?.visibility = View.GONE
            }

            // ---------------------
            // Display Book 3 Details
            // ---------------------
            val book3 = books.getOrNull(2) // Safely get the third book or null
            if (book3 != null) {
                // Set the book title, defaulting to "Unknown Title" if null
                bookTitleTextView3.text = book3.volumeInfo?.title ?: "Unknown Title"
                // Set the book authors, defaulting to "Unknown Author" if null
                bookAuthorsTextView3.text =
                    book3.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"

                // Extract the primary genre, defaulting to "Various Genres" if null
                genreBook3 = book3.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"

                // Reset the message TextView visibility and text
                messageTextView3.visibility = View.GONE
                messageTextView3.text = "Message will appear here"

                // Prepare the thumbnail URL, replacing "http://" with "https://" for security
                val thumbnail3 =
                    book3.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 3 Image URL: $thumbnail3")

                // Load the book cover image using Glide, or use a placeholder if the thumbnail is null
                Glide.with(this)
                    .load(thumbnail3 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true) // Skip memory cache for fresh loading
                    .into(bookCoverImageView3)
            } else {
                // Hide the entire Book 3 layout if the book is not available
                view?.findViewById<LinearLayout>(R.id.bookItem3)?.visibility = View.GONE
            }

            //work review 4 itzel medina
            // ---------------------
            // Display Book 4 Details
            // ---------------------
            val book4 = books.getOrNull(3) // Safely get the fourth book or null
            if (book4 != null) {
                bookTitleTextView4.text = book4.volumeInfo?.title ?: "Unknown Title"
                bookAuthorsTextView4.text =
                    book4.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"
                genreBook4 = book4.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"

                // Reset the message TextView visibility and text
                messageTextView4.visibility = View.GONE
                messageTextView4.text = "Message will appear here"

                // Prepare the thumbnail URL, replacing "http://" with "https://" for security
                val thumbnail4 =
                    book4.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 4 Image URL: $thumbnail4")

                // Load the book cover image using Glide, or use a placeholder if the thumbnail is null
                Glide.with(this)
                    .load(thumbnail4 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true) // Skip memory cache for fresh loading
                    .into(bookCoverImageView4)

                // Ensure the fourth book's layout is visible
                view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.VISIBLE
            } else {
                // Hide the entire Book 4 layout if the book is not available
                view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.GONE
            }
        }
    }

    //work review 4 itzel medina
    /**
     * Set up Like and Dislike button listeners for all three books.
     */
    private fun setupButtonListeners() {
        // Like Button 1
        likeButton1.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(0),
                feedback = "like",
                genre = genreBook1,
                messageTextView = messageTextView1,
                likeButton = likeButton1,
                dislikeButton = dislikeButton1
            )
        }

        // Dislike Button 1
        dislikeButton1.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(0),
                feedback = "dislike",
                genre = genreBook1,
                messageTextView = messageTextView1,
                likeButton = likeButton1,
                dislikeButton = dislikeButton1
            )
        }

        // Like Button 2
        likeButton2.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(1),
                feedback = "like",
                genre = genreBook2,
                messageTextView = messageTextView2,
                likeButton = likeButton2,
                dislikeButton = dislikeButton2
            )
        }

        // Dislike Button 2
        dislikeButton2.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(1),
                feedback = "dislike",
                genre = genreBook2,
                messageTextView = messageTextView2,
                likeButton = likeButton2,
                dislikeButton = dislikeButton2
            )
        }

        // Like Button 3
        likeButton3.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(2),
                feedback = "like",
                genre = genreBook3,
                messageTextView = messageTextView3,
                likeButton = likeButton3,
                dislikeButton = dislikeButton3
            )
        }

        // Dislike Button 3
        dislikeButton3.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(2),
                feedback = "dislike",
                genre = genreBook3,
                messageTextView = messageTextView3,
                likeButton = likeButton3,
                dislikeButton = dislikeButton3
            )
        }

        // Like Button 4
        likeButton4.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(3),
                feedback = "like",
                genre = genreBook4,
                messageTextView = messageTextView4,
                likeButton = likeButton4,
                dislikeButton = dislikeButton4
            )
        }

        // Dislike Button 4
        dislikeButton4.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(3),
                feedback = "dislike",
                genre = genreBook4,
                messageTextView = messageTextView4,
                likeButton = likeButton4,
                dislikeButton = dislikeButton4
            )
        }

    }

    //work review 4 itzel medina
    /**
     * Retrieves the book ID based on the position.
     * Ensure that the books list is accessible and not null.
     */
    private fun getBookId(position: Int): String? {
        val recommendations = loadRecommendationsFromPreferences(auth.currentUser?.uid ?: "") ?: return null
        return recommendations.getOrNull(position)?.id
    }


    //work review 4 itzel medina
    /**
     * Handles the Like button click.
     *
     * @param buttonContainer The container holding the Like and Dislike buttons.
     * @param messageTextView The TextView to display the message.
     * @param message The message to display.
     */
    private fun onLikeClicked(buttonContainer: View, messageTextView: TextView, message: String) {
        buttonContainer.visibility = View.GONE
        messageTextView.text = message
        messageTextView.visibility = View.VISIBLE
    }

    /**
     * Handles the Dislike button click.
     *
     * @param buttonContainer The container holding the Like and Dislike buttons.
     * @param messageTextView The TextView to display the message.
     * @param message The message to display.
     */
    private fun onDislikeClicked(buttonContainer: View, messageTextView: TextView, message: String) {
        buttonContainer.visibility = View.GONE
        messageTextView.text = message
        messageTextView.visibility = View.VISIBLE
    }

    //work review 4 itzel medina
    private fun handleUserFeedback(
        bookId: String?,
        feedback: String,
        genre: String?,
        messageTextView: TextView,
        likeButton: ImageButton,
        dislikeButton: ImageButton
    ) {
        if (bookId == null) {
            Log.e("HomeFragment", "Book ID is null. Cannot record feedback.")
            return
        }

        val userId = auth.currentUser?.uid ?: return

        // Prevent action if genre is null
        if (genre == null) {
            Log.e("HomeFragment", "Genre is null. Cannot display message.")
            return
        }

        // Create feedback data
        val feedbackData = mapOf(
            "bookId" to bookId,
            "feedback" to feedback,
            "genre" to genre,
            "timestamp" to Calendar.getInstance().time
        )

        // Save feedback to Firestore under the user's document
        db.collection("users").document(userId).collection("bookFeedback").add(feedbackData)
            .addOnSuccessListener {
                Log.d("HomeFragment", "Feedback recorded: $feedback for book $bookId")
                // Update the UI message based on feedback
                val message = if (feedback == "like") {
                    "More $genre books like this will be recommended.."
                } else {
                    "This $genre books will no longer be recommended."
                }
                messageTextView.text = message
                messageTextView.visibility = View.VISIBLE

                likeButton.visibility = View.INVISIBLE
                dislikeButton.visibility = View.INVISIBLE

                // Update genre feedback counts
                updateGenreFeedback(userId, genre, feedback)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error recording feedback: ${e.message}", e)
                messageTextView.text = "Failed to record feedback."
                messageTextView.visibility = View.VISIBLE
            }
    }

    /**
     * Updates the genre feedback counts in Firestore.
     *
     * @param userId The UID of the user.
     * @param genre The genre to update.
     * @param feedback The feedback type: "like" or "dislike".
     */
    private fun updateGenreFeedback(userId: String, genre: String, feedback: String) {
        val genreFeedbackDoc = db.collection("users").document(userId).collection("genreFeedback").document(genre)
        genreFeedbackDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentLikes = document.getLong("likes") ?: 0
                val currentDislikes = document.getLong("dislikes") ?: 0

                // Update likes and dislikes based on feedback
                val updatedLikes = when (feedback) {
                    "like" -> currentLikes + 1
                    "dislike" -> (currentLikes - 1).coerceAtLeast(0) // Decrement likes but not below 0
                    else -> currentLikes
                }
                val updatedDislikes = if (feedback == "dislike") currentDislikes + 1 else currentDislikes

                // Update the counts in Firestore
                genreFeedbackDoc.update(
                    "likes", updatedLikes,
                    "dislikes", updatedDislikes
                ).addOnSuccessListener {
                    Log.d("HomeFragment", "Updated genre feedback for $genre: Likes=$updatedLikes, Dislikes=$updatedDislikes")
                    // Determine which genre to recommend based on updated feedback
                    determineFourthRecommendation(userId)
                }.addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error updating genre feedback: ${e.message}", e)
                }
            } else {
                // If the genre document doesn't exist, create it with initial counts
                val initialLikes = if (feedback == "like") 1 else 0
                val initialDislikes = if (feedback == "dislike") 1 else 0
                genreFeedbackDoc.set(mapOf(
                    "likes" to initialLikes,
                    "dislikes" to initialDislikes
                )).addOnSuccessListener {
                    Log.d("HomeFragment", "Initialized genre feedback for $genre: Likes=$initialLikes, Dislikes=$initialDislikes")
                    // After initialization, determine recommendation
                    determineFourthRecommendation(userId)
                }.addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error initializing genre feedback: ${e.message}", e)
                }
            }
        }.addOnFailureListener { e -> Log.e("HomeFragment", "Error retrieving genre feedback: ${e.message}", e)
        }
    }

    //work review 4 itzel medina
    private fun determineFourthRecommendation(userId: String) {
        db.collection("users").document(userId)
            .collection("genreFeedback").get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Create a map of genres with their like counts
                    val genreLikes = mutableMapOf<String, Long>()
                    querySnapshot.documents.forEach { doc ->
                        val genre = doc.id
                        val likes = doc.getLong("likes") ?: 0L
                        genreLikes[genre] = likes
                    }

                    // Find the genre with the highest likes
                    val topGenre = genreLikes.maxByOrNull { it.value }?.key
                    if (topGenre != null && genreLikes[topGenre]!! > 0) {
                        fetchAndDisplayPreferredGenreBook(userId, topGenre)
                    } else {
                        // No likes yet; use a default genre
                        fetchAndDisplayPreferredGenreBook(userId, "Fiction")
                    }
                } else {
                    // No feedback found; use a default genre
                    fetchAndDisplayPreferredGenreBook(userId, "Fiction")
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching genre feedback for recommendation: ${e.message}", e)
            }
    }


    //work review 4 itzel medina
    /**
     * Fetches a book from the preferred genre and updates the fourth recommendation.
     *
     * @param userId The UID of the user.
     * @param preferredGenre The genre to fetch the book from.
     */
    private fun fetchAndDisplayPreferredGenreBook(userId: String, preferredGenre: String) {
        // Replace with your actual Google Books API key management approach
        val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM"
        val query = "subject:$preferredGenre"
        val randomStartIndex = Random.nextInt(0, 30)
        val call = GoogleBooksApiService.create().searchBooks(query, randomStartIndex, 1, apiKey)

        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.items ?: emptyList()
                    if (books.isNotEmpty()) {
                        val preferredBook = books[0]
                        updateFourthRecommendation(preferredBook)
                    } else {
                        Log.d("HomeFragment", "No books found for genre $preferredGenre")
                        // Optionally, set a placeholder or hide the fourth book
                        view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.GONE
                    }
                } else {
                    Log.d("HomeFragment", "Google Books API Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.d("HomeFragment", "Google Books API Failure: ${t.message}")
            }
        })
    }

    /**
     * Updates the fourth book's UI with the preferred book details.
     *
     * @param book The BookItem to display.
     */
    private fun updateFourthRecommendation(book: BookItem) {
        // Update title and authors
        bookTitleTextView4.text = book.volumeInfo?.title ?: "Unknown Title"
        bookAuthorsTextView4.text = book.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"

        // Update genre
        genreBook4 = book.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"

        // Reset the message TextView visibility and text
        messageTextView4.visibility = View.GONE
        messageTextView4.text = "Message will appear here"

        // Prepare the thumbnail URL, replacing "http://" with "https://" for security
        val thumbnail4 = book.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
        Log.d("HomeFragment", "Book 4 Image URL: $thumbnail4")

        // Load the book cover image using Glide, or use a placeholder if the thumbnail is null
        Glide.with(this)
            .load(thumbnail4 ?: R.drawable.placeholder_image)
            .skipMemoryCache(true) // Skip memory cache for fresh loading
            .into(bookCoverImageView4)

        // Ensure the fourth book's layout is visible
        view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.VISIBLE
    }

}