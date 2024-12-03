package com.example.booknook.fragments

import android.content.Context
import android.content.SharedPreferences
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
import com.example.booknook.MainActivity


class HomeFragment : Fragment() {

    //itzel medina
    private lateinit var refreshButton: Button
    private lateinit var reloadimage: ImageView


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
    private lateinit var messageTextView4: TextView


    // Add this variable inside the HomeFragment class to track the number of rated books
    private var ratedBooksCount = 0

    // Add this variable inside the HomeFragment class
    private var fourthBookShown = false


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

    //itzel medina
    private lateinit var expandButton: ImageButton
    private lateinit var booksContainer: LinearLayout



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

        // Initialize Book 1 views
        bookCoverImageView1 = view.findViewById(R.id.bookCoverImageView1)
        bookTitleTextView1 = view.findViewById(R.id.bookTitleTextView1)
        bookAuthorsTextView1 = view.findViewById(R.id.bookAuthorsTextView1)
        dislikeButton1 = view.findViewById(R.id.dislikeButton1)
        likeButton1 = view.findViewById(R.id.likeButton1)
        messageTextView1 = view.findViewById(R.id.messageTextView1)
        buttonContainer1 = view.findViewById(R.id.buttonContainer1)

        // Initialize Book 2 views
        bookCoverImageView2 = view.findViewById(R.id.bookCoverImageView2)
        bookTitleTextView2 = view.findViewById(R.id.bookTitleTextView2)
        bookAuthorsTextView2 = view.findViewById(R.id.bookAuthorsTextView2)
        dislikeButton2 = view.findViewById(R.id.dislikeButton2)
        likeButton2 = view.findViewById(R.id.likeButton2)
        messageTextView2 = view.findViewById(R.id.messageTextView2)
        buttonContainer2 = view.findViewById(R.id.buttonContainer2)

        // Initialize Book 3 views
        bookCoverImageView3 = view.findViewById(R.id.bookCoverImageView3)
        bookTitleTextView3 = view.findViewById(R.id.bookTitleTextView3)
        bookAuthorsTextView3 = view.findViewById(R.id.bookAuthorsTextView3)
        dislikeButton3 = view.findViewById(R.id.dislikeButton3)
        likeButton3 = view.findViewById(R.id.likeButton3)
        messageTextView3 = view.findViewById(R.id.messageTextView3)
        buttonContainer3 = view.findViewById(R.id.buttonContainer3)

        // Initialize Book 4 views
        bookCoverImageView4 = view.findViewById(R.id.bookCoverImageView4)
        bookTitleTextView4 = view.findViewById(R.id.bookTitleTextView4)
        bookAuthorsTextView4 = view.findViewById(R.id.bookAuthorsTextView4)
        messageTextView4 = view.findViewById(R.id.messageTextView4)


        //itzel medina
        // Initialize Expand Button and Books Container
        expandButton = view.findViewById(R.id.expandButton)
        booksContainer = view.findViewById(R.id.booksContainer)

        //itzel medina
        // Retrieve the expanded/collapsed state from SharedPreferences
        val isExpanded = getSharedPreferences()?.getBoolean("isBooksContainerExpanded", true) ?: true
        setBooksContainerVisibility(isExpanded, animate = false)
        updateExpandButtonIcon(isExpanded)

        // Set up click listener for Expand Button
        expandButton.setOnClickListener {
            val currentlyExpanded = booksContainer.visibility == View.VISIBLE
            val newExpandedState = !currentlyExpanded
            setBooksContainerVisibility(newExpandedState, animate = true)
            updateExpandButtonIcon(newExpandedState)
            // Save the new state
            getSharedPreferences()?.edit()?.putBoolean("isBooksContainerExpanded", newExpandedState)?.apply()

        }

        //books clickable
        // Set click listeners for Book 1 ImageView
        bookCoverImageView1.setOnClickListener {
            val book = getBookItem(1) // Method to retrieve BookItem for Book 1
            book?.let {
                navigateToBookDetails(it)
            }
        }

// Repeat for Book 2, Book 3, and Book 4
        bookCoverImageView2.setOnClickListener {
            val book = getBookItem(2)
            book?.let {
                navigateToBookDetails(it)
            }
        }

        bookCoverImageView3.setOnClickListener {
            val book = getBookItem(3)
            book?.let {
                navigateToBookDetails(it)
            }
        }

        bookCoverImageView4.setOnClickListener {
            val book = getBookItem(4)
            book?.let {
                navigateToBookDetails(it)
            }
        }



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

        //itzel medina
        // Initialize Refresh Button
        refreshButton = view.findViewById(R.id.refreshButton)

        reloadimage = view.findViewById(R.id.reload)

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

        // Load the fourthBookShown flag
        fourthBookShown = getSharedPreferences()?.getBoolean("fourthBookShown", false) ?: false


        // Initialize ratedBooksCount
        ratedBooksCount = getSharedPreferences()?.getInt("ratedBooksCount", 0) ?: 0


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


    //itzel medina work review 4
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

        // Update the last fetch date to current time to prevent immediate refetching
        saveLastFetchDate(userId)

        // Reset UI elements for Books 1-3
        messageTextView1.visibility = View.GONE
        buttonContainer1.visibility = View.VISIBLE
        likeButton1.visibility = View.VISIBLE
        dislikeButton1.visibility = View.VISIBLE

        messageTextView2.visibility = View.GONE
        buttonContainer2.visibility = View.VISIBLE
        likeButton2.visibility = View.VISIBLE
        dislikeButton2.visibility = View.VISIBLE

        messageTextView3.visibility = View.GONE
        buttonContainer3.visibility = View.VISIBLE
        likeButton3.visibility = View.VISIBLE
        dislikeButton3.visibility = View.VISIBLE

        // Hide the fourth book layout and related views
        view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.basedOnYourInputTextView)?.visibility = View.GONE

        // Hide the refresh button after it is used
        refreshButton.visibility = View.GONE
        reloadimage.visibility = View.GONE
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
        //itzel medina
        val userId = auth.currentUser?.uid ?: return
        fetchDismissedBookIds(userId) { dismissedIds ->
            val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM" // Google Books API key
            val genreBooksMap =
                mutableMapOf<String, MutableList<BookItem>>() // Map to store books by genre
            val recommendedBookIds =
                mutableSetOf<String>() // Set to track book IDs to avoid duplicates
            var apiCallsCompleted = 0 // Counter to track how many API calls have been completed

            Log.d(
                "HomeFragment",
                "Combined recommendation for the user (genrePreferences + topGenres): $genres"
            )

            // For each genre, make a single Google Books API request
            genres.forEach { genre ->
                genreBooksMap[genre] =
                    mutableListOf() // Initialize a list for each genre in the map

                // Generate a random starting index between 0 and 30 to get varied search results
                val randomStartIndex = Random.nextInt(0, 30)

                // Nested function to fetch books for a specific genre with an attempt counter for retries
                fun fetchBooksForGenre(attempt: Int) {
                    val query =
                        "subject:$genre" // Google Books API query parameter for subject/genre
                    val call = GoogleBooksApiService.create().searchBooks(
                        query,
                        randomStartIndex,
                        20,
                        apiKey
                    ) // API call to search books

                    // Handle the API response
                    call.enqueue(object : Callback<BookResponse> {
                        override fun onResponse(
                            call: Call<BookResponse>,
                            response: Response<BookResponse>
                        ) {
                            if (response.isSuccessful) {
                                // Retrieve the list of books from the response
                                val books = response.body()?.items ?: emptyList()
                                Log.d(
                                    "HomeFragment",
                                    "Books retrieved for genre '$genre': ${books.size}"
                                )

                                // Add unique books to the genreBooksMap, limiting to 1 book per genre
                                books.firstOrNull { !recommendedBookIds.contains(it.id) }
                                    ?.let { book ->
                                        genreBooksMap[genre]?.add(book) // Add book to the map for the genre
                                        recommendedBookIds.add(book.id) // Track book ID to avoid duplicates
                                    }

                                apiCallsCompleted++ // Increment the API call completion counter
                                if (apiCallsCompleted == genres.size) { // Check if all genre searches are completed
                                    val finalBooks = genreBooksMap.values.flatten()
                                        .take(3) // Take up to 3 books total
                                    if (finalBooks.isNotEmpty()) {
                                        displayRecommendedBooks(finalBooks) // Display the final book recommendations
                                    } else {
                                        Log.d("HomeFragment", "No books found across all genres.")
                                    }
                                }
                            } else {
                                // Handle API error response and retry if necessary
                                Log.d(
                                    "HomeFragment",
                                    "Google Books API Error for genre '$genre': ${
                                        response.errorBody()?.string()
                                    }"
                                )
                                handleApiRetryOrFailure(attempt)
                            }
                        }

                        override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                            // Log failure and retry if needed
                            Log.d(
                                "HomeFragment",
                                "Google Books API Failure for genre '$genre': ${t.message}"
                            )
                            handleApiRetryOrFailure(attempt)
                        }

                        // Nested function to handle retry or failure logic
                        private fun handleApiRetryOrFailure(attempt: Int) {
                            if (attempt < 5) { // Limit to 5 retry attempts
                                val delay = (1000 * Math.pow(
                                    2.0,
                                    attempt.toDouble()
                                )).toLong() // Exponential backoff (1s, 2s, 4s, 8s, etc.)
                                Log.d(
                                    "HomeFragment",
                                    "Retrying in ${delay / 1000}s for genre '$genre' (attempt $attempt)"
                                )
                                Handler(Looper.getMainLooper()).postDelayed({
                                    fetchBooksForGenre(
                                        attempt + 1
                                    )
                                }, delay)
                            } else {
                                // Final attempt failed; increment completed counter
                                apiCallsCompleted++
                                if (apiCallsCompleted == genres.size) { // Check if all genre searches are completed
                                    val finalBooks = genreBooksMap.values.flatten()
                                        .take(3) // Take up to 3 books total
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
    }

    // Yunjong Noh
    // Display the recommended books in the UI
    /**
     * Function to display the recommended books in the UI.
     * Handles up to four books and updates the respective UI components.
     */
    /**
     * Modified function to display the recommended books in the UI.
     * Handles up to three books and hides the fourth book initially.
     */
    private fun displayRecommendedBooks(books: List<BookItem>) {

        val userId = auth.currentUser?.uid ?: return

        // Reset ratedBooksCount and fourthBookShown
        ratedBooksCount = 0
        getSharedPreferences()?.edit()?.putInt("ratedBooksCount", ratedBooksCount)?.apply()
        fourthBookShown = false
        getSharedPreferences()?.edit()?.putBoolean("fourthBookShown", fourthBookShown)?.apply()
        Log.d("HomeFragment", "ratedBooksCount and fourthBookShown reset.")

        // Save recommended books to Firestore
        books.forEach { book ->
            db.collection("users").document(userId).collection("recommendedBooks").document(book.id)
                .set(
                    mapOf("bookId" to book.id)
                ).addOnSuccessListener {
                    Log.d("HomeFragment", "Book ${book.id} added to recommendedBooks.")
                }.addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error saving recommended book ${book.id}: ${e.message}", e)
                }
        }

        if (books.isNotEmpty()) {
            // Save the list of recommended books to SharedPreferences for later use
            saveRecommendationsToPreferences(userId, books)

            // ---------------------
            // Display Book 1 Details
            // ---------------------
            val book1 = books.getOrNull(0)
            bookTitleTextView1.text = book1?.volumeInfo?.title ?: "Unknown Title"
            bookAuthorsTextView1.text = book1?.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"
            genreBook1 = book1?.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"
            messageTextView1.visibility = View.GONE
            messageTextView1.text = "Message will appear here"

            // Reset buttonContainer1 to visible
            buttonContainer1.visibility = View.VISIBLE

            // **Reset Like and Dislike Buttons to Visible**
            likeButton1.visibility = View.VISIBLE
            dislikeButton1.visibility = View.VISIBLE

            val thumbnail1 = book1?.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
            Log.d("HomeFragment", "Book 1 Image URL: $thumbnail1")

            if (book1 != null) {
                Glide.with(this)
                    .load(thumbnail1 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true)
                    .into(bookCoverImageView1)
            } else {
                bookCoverImageView1.setImageResource(R.drawable.placeholder_image)
            }

            // ---------------------
            // Display Book 2 Details
            // ---------------------
            val book2 = books.getOrNull(1)
            if (book2 != null) {
                bookTitleTextView2.text = book2.volumeInfo?.title ?: "Unknown Title"
                bookAuthorsTextView2.text = book2.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"
                genreBook2 = book2.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"
                messageTextView2.visibility = View.GONE
                messageTextView2.text = "Message will appear here"

                // Reset buttonContainer2 to visible
                buttonContainer2.visibility = View.VISIBLE

                // **Reset Like and Dislike Buttons to Visible**
                likeButton2.visibility = View.VISIBLE
                dislikeButton2.visibility = View.VISIBLE

                val thumbnail2 = book2.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 2 Image URL: $thumbnail2")

                Glide.with(this)
                    .load(thumbnail2 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true)
                    .into(bookCoverImageView2)
            } else {
                view?.findViewById<LinearLayout>(R.id.bookItem2)?.visibility = View.GONE
            }

            // ---------------------
            // Display Book 3 Details
            // ---------------------
            val book3 = books.getOrNull(2)
            if (book3 != null) {
                bookTitleTextView3.text = book3.volumeInfo?.title ?: "Unknown Title"
                bookAuthorsTextView3.text = book3.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"
                genreBook3 = book3.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"
                messageTextView3.visibility = View.GONE
                messageTextView3.text = "Message will appear here"

                // Reset buttonContainer3 to visible
                buttonContainer3.visibility = View.VISIBLE

                // **Reset Like and Dislike Buttons to Visible**
                likeButton3.visibility = View.VISIBLE
                dislikeButton3.visibility = View.VISIBLE

                val thumbnail3 = book3.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
                Log.d("HomeFragment", "Book 3 Image URL: $thumbnail3")

                Glide.with(this)
                    .load(thumbnail3 ?: R.drawable.placeholder_image)
                    .skipMemoryCache(true)
                    .into(bookCoverImageView3)
            } else {
                view?.findViewById<LinearLayout>(R.id.bookItem3)?.visibility = View.GONE
            }

            // Hide the fourth book layout initially
            view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.GONE
            // Hide the "Based on your input" TextView initially
            view?.findViewById<TextView>(R.id.basedOnYourInputTextView)?.visibility = View.GONE
        }
    }



    //work review 4 itzel medina
    /**
     * Set up Like and Dislike button listeners for all three books.
     */
    private fun setupButtonListeners() {
        // Like Button 1 (Position 0)
        likeButton1.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(0),
                feedback = "like",
                genre = genreBook1,
                messageTextView = messageTextView1,
                likeButton = likeButton1,
                dislikeButton = dislikeButton1,
                position = 0
            )
        }

        // Dislike Button 1 (Position 0)
        dislikeButton1.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(0),
                feedback = "dislike",
                genre = genreBook1,
                messageTextView = messageTextView1,
                likeButton = likeButton1,
                dislikeButton = dislikeButton1,
                position = 0
            )
        }

        // Like Button 2 (Position 1)
        likeButton2.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(1),
                feedback = "like",
                genre = genreBook2,
                messageTextView = messageTextView2,
                likeButton = likeButton2,
                dislikeButton = dislikeButton2,
                position = 1
            )
        }

        // Dislike Button 2 (Position 1)
        dislikeButton2.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(1),
                feedback = "dislike",
                genre = genreBook2,
                messageTextView = messageTextView2,
                likeButton = likeButton2,
                dislikeButton = dislikeButton2,
                position = 1
            )
        }

        // Like Button 3 (Position 2)
        likeButton3.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(2),
                feedback = "like",
                genre = genreBook3,
                messageTextView = messageTextView3,
                likeButton = likeButton3,
                dislikeButton = dislikeButton3,
                position = 2
            )
        }

        // Dislike Button 3 (Position 2)
        dislikeButton3.setOnClickListener {
            handleUserFeedback(
                bookId = getBookId(2),
                feedback = "dislike",
                genre = genreBook3,
                messageTextView = messageTextView3,
                likeButton = likeButton3,
                dislikeButton = dislikeButton3,
                position = 2
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
    /**
     * Handles user feedback for a book.
     *
     * @param bookId The ID of the book.
     * @param feedback The type of feedback ("like" or "dislike").
     * @param genre The genre of the book.
     * @param messageTextView The TextView to display messages.
     * @param likeButton The Like button.
     * @param dislikeButton The Dislike button.
     * @param position The position of the book (0, 1, 2, 3).
     */
    private fun handleUserFeedback(
        bookId: String?,
        feedback: String,
        genre: String?,
        messageTextView: TextView,
        likeButton: ImageButton,
        dislikeButton: ImageButton,
        position: Int
    ) {
        if (bookId == null) {
            Log.e("HomeFragment", "Book ID is null. Cannot record feedback.")
            return
        }

        val userId = auth.currentUser?.uid ?: return

        if (genre == null) {
            Log.e("HomeFragment", "Genre is null. Cannot display message.")
            return
        }

        // Save book to dismissedBooks
        db.collection("users").document(userId).collection("dismissedBooks").document(bookId).set(
            mapOf(
                "bookId" to bookId,
                "feedback" to feedback,
                "timestamp" to Calendar.getInstance().time
            )
        ).addOnSuccessListener {
            Log.d("HomeFragment", "Book $bookId added to dismissedBooks with feedback: $feedback")
        }.addOnFailureListener { e ->
            Log.e("HomeFragment", "Error adding book to dismissedBooks: ${e.message}", e)
        }

        // Save feedback to Firestore
        db.collection("users").document(userId).collection("bookFeedback").add(
            mapOf(
                "bookId" to bookId,
                "feedback" to feedback,
                "genre" to genre,
                "timestamp" to Calendar.getInstance().time
            )
        ).addOnSuccessListener {
            Log.d("HomeFragment", "Feedback recorded: $feedback for book $bookId")

            // Update the UI message based on feedback
            val message = if (feedback == "like") {
                "$genre books will now be recommended more."
            } else {
                "$genre books will be recommended less now."
            }
            messageTextView.text = message
            messageTextView.visibility = View.VISIBLE

            likeButton.visibility = View.INVISIBLE
            dislikeButton.visibility = View.INVISIBLE

            // Increment the rated books count only for the first three books
            if (position < 3) {
                incrementRatedBooksCount()
                Log.d("HomeFragment", "RatedBooksCount after increment: $ratedBooksCount")

                // Check if all three books have been rated and the fourth book hasn't been shown yet
                if (ratedBooksCount >= 3 && !fourthBookShown) {
                    Log.d("HomeFragment", "All three books rated. Fetching fourth recommendation.")
                    view?.findViewById<TextView>(R.id.basedOnYourInputTextView)?.visibility = View.VISIBLE
                    determineFourthRecommendation(userId)
                }
            }

            // Update genre feedback counts
            updateGenreFeedback(userId, genre, feedback)
        }.addOnFailureListener { e ->
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

                val updatedLikes = when (feedback) {
                    "like" -> currentLikes + 1
                    "dislike" -> (currentLikes - 1).coerceAtLeast(0) // Decrement likes but not below 0
                    else -> currentLikes
                }
                val updatedDislikes = if (feedback == "dislike") currentDislikes + 1 else currentDislikes

                genreFeedbackDoc.update(
                    "likes", updatedLikes,
                    "dislikes", updatedDislikes
                ).addOnSuccessListener {
                    Log.d("HomeFragment", "Updated genre feedback for $genre: Likes=$updatedLikes, Dislikes=$updatedDislikes")
                    // **Do not call determineFourthRecommendation here**
                    // If you need to fetch recommendations based on updated feedback, ensure it's controlled
                }.addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error updating genre feedback: ${e.message}", e)
                }
            } else {
                val initialLikes = if (feedback == "like") 1 else 0
                val initialDislikes = if (feedback == "dislike") 1 else 0
                genreFeedbackDoc.set(mapOf(
                    "likes" to initialLikes,
                    "dislikes" to initialDislikes
                )).addOnSuccessListener {
                    Log.d("HomeFragment", "Initialized genre feedback for $genre: Likes=$initialLikes, Dislikes=$initialDislikes")
                    // **Do not call determineFourthRecommendation here**
                }.addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error initializing genre feedback: ${e.message}", e)
                }
            }
        }.addOnFailureListener { e ->
            Log.e("HomeFragment", "Error retrieving genre feedback: ${e.message}", e)
        }
    }

    //work review 4 itzel medina
    /**
     * Determines the fourth recommendation based on genre feedback.
     */
    /**
     * Function to determine and fetch the fourth recommendation based on user feedback.
     */
    private fun determineFourthRecommendation(userId: String) {
        if (fourthBookShown) {
            Log.d("HomeFragment", "Fourth book has already been shown. Skipping fetch.")
            return
        }

        Log.d("HomeFragment", "Determining fourth recommendation for user: $userId")
        db.collection("users").document(userId)
            .collection("genreFeedback").get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val genreLikes = querySnapshot.documents.associate {
                        it.id to (it.getLong("likes") ?: 0L)
                    }

                    val topGenre = genreLikes.maxByOrNull { it.value }?.key
                    Log.d("determineFourthRecommendation", "Top genre: $topGenre")
                    if (topGenre != null && genreLikes[topGenre]!! > 0) {
                        fetchAndDisplayFourthRecommendation(userId, topGenre)
                    } else {
                        Log.d("determineFourthRecommendation", "No genres with likes found. Using default genre.")
                        fetchAndDisplayFourthRecommendation(userId, "Fiction")
                    }
                } else {
                    Log.d("determineFourthRecommendation", "No genre feedback found. Using default genre.")
                    fetchAndDisplayFourthRecommendation(userId, "Fiction")
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching genre feedback for recommendation: ${e.message}", e)
            }
    }

    /**
     * Fetches a book from the preferred genre and updates the fourth recommendation.
     *
     * @param userId The UID of the user.
     * @param preferredGenre The genre to fetch the book from.
     */
    private fun fetchAndDisplayFourthRecommendation(userId: String, preferredGenre: String) {
        Log.d("fetchAndDisplayFourthRecommendation", "Fetching book for genre: $preferredGenre")
        val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM" // Ensure this is your valid API key
        val query = "subject:$preferredGenre"
        val randomStartIndex = Random.nextInt(0, 30)
        val call = GoogleBooksApiService.create().searchBooks(query, randomStartIndex, 1, apiKey)

        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.items ?: emptyList()
                    Log.d("fetchAndDisplayFourth", "Books retrieved: ${books.size}")
                    if (books.isNotEmpty()) {
                        val fourthBook = books[0]
                        displayFourthBook(fourthBook)
                    } else {
                        Log.d("fetchAndDisplayFourth", "No books found for genre $preferredGenre")
                        Toast.makeText(requireContext(), "No additional recommendations available.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("fetchAndDisplayFourth", "Google Books API Error: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Failed to fetch additional recommendation.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.e("fetchAndDisplayFourth", "Google Books API Failure: ${t.message}")
                Toast.makeText(requireContext(), "Failed to fetch additional recommendation.", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Displays the fourth book in the UI with the "Based on your input" text.
     *
     * @param book The BookItem to display as the fourth recommendation.
     */
    private fun displayFourthBook(book: BookItem) {
        if (fourthBookShown) {
            Log.d("HomeFragment", "Fourth book has already been shown. Skipping display.")
            return
        }

        Log.d("HomeFragment", "Displaying fourth book: ${book.volumeInfo?.title}")
        // Set the "Based on your input" TextView to visible
        view?.findViewById<TextView>(R.id.basedOnYourInputTextView)?.visibility = View.VISIBLE

        // Set the fourth book details
        bookTitleTextView4.text = book.volumeInfo?.title ?: "Unknown Title"
        bookAuthorsTextView4.text = book.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"
        genreBook4 = book.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"
        messageTextView4.visibility = View.GONE
        messageTextView4.text = "Message will appear here"


        val thumbnail4 = book.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
        Log.d("HomeFragment", "Book 4 Image URL: $thumbnail4")

        Glide.with(this)
            .load(thumbnail4 ?: R.drawable.placeholder_image)
            .skipMemoryCache(true)
            .into(bookCoverImageView4)

        // Make the fourth book's layout visible
        view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.VISIBLE

        Log.d("HomeFragment", "Fourth book displayed successfully.")

        // Save the fourth book to SharedPreferences
        val userId = auth.currentUser?.uid ?: return
        saveFourthRecommendationToPreferences(userId, book)

        // Set the flag after successful display
        markFourthBookAsShown()
    }


    //work review 4 itzel medina
    /**
     * Fetches a book from the preferred genre and updates the fourth recommendation.
     *
     * @param userId The UID of the user.
     * @param preferredGenre The genre to fetch the book from.
     */
    private fun fetchAndDisplayPreferredGenreBook(userId: String, preferredGenre: String) {
        Log.d("fetchAndDisplayPreferredGenreBook", "Fetching book for genre: $preferredGenre")
        val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM" // Google Books API key
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
                        Log.d("fetchAndDisplayPreferredGenreBook", "No books found for genre $preferredGenre")
                        view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.GONE
                    }
                } else {
                    Log.e("fetchAndDisplayPreferredGenreBook", "API Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Log.e("fetchAndDisplayPreferredGenreBook", "API Failure: ${t.message}")
            }
        })
    }


    /**
     * Updates the fourth book's UI with the preferred book details.
     *
     * @param book The BookItem to display.
     */
    private fun updateFourthRecommendation(book: BookItem) {
        bookTitleTextView4.text = book.volumeInfo?.title ?: "Unknown Title"
        bookAuthorsTextView4.text = book.volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author"
        genreBook4 = book.volumeInfo?.categories?.firstOrNull() ?: "Various Genres"

        val thumbnail4 = book.volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://")
        Log.d("updateFourthRecommendation", "Updating fourth book: $thumbnail4")

        Glide.with(this)
            .load(thumbnail4 ?: R.drawable.placeholder_image)
            .into(bookCoverImageView4)

        // Ensure the fourth book's layout is visible
        view?.findViewById<LinearLayout>(R.id.bookItem4)?.visibility = View.VISIBLE
    }

    //itzel medina
    private fun getSharedPreferences(): SharedPreferences? {
        return context?.getSharedPreferences("booknook_prefs", Context.MODE_PRIVATE)
    }


    //itzel medina
    private fun markFourthBookAsShown() {
        getSharedPreferences()?.edit()?.putBoolean("fourthBookShown", true)?.apply()

    }

    //itzel medina
    private fun incrementRatedBooksCount() {
        ratedBooksCount += 1
        getSharedPreferences()?.edit()?.putInt("ratedBooksCount", ratedBooksCount)?.apply()
    }

    //itzel medina
    /**
     * Sets the visibility of the booksContainer with optional animation.
     *
     * @param isVisible Boolean indicating whether the container should be visible.
     * @param animate Boolean indicating whether to animate the visibility change.
     */
    private fun setBooksContainerVisibility(isVisible: Boolean, animate: Boolean) {
        if (animate) {
            if (isVisible) {
                booksContainer.visibility = View.VISIBLE
                booksContainer.animate().alpha(1.0f).setDuration(300).start()
            } else {
                booksContainer.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .withEndAction { booksContainer.visibility = View.GONE }
                    .start()
            }
        } else {
            booksContainer.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    /**
     * Updates the expandButton icon based on the expanded state.
     *
     * @param isExpanded Boolean indicating whether the container is expanded.
     */
    private fun updateExpandButtonIcon(isExpanded: Boolean) {
        if (isExpanded) {
            expandButton.setImageResource(R.drawable.collapse_button) // Up arrow
        } else {
            expandButton.setImageResource(R.drawable.expand_button) // Down arrow
        }
    }

    //itzel medina
    private fun fetchDismissedBookIds(userId: String, callback: (Set<String>) -> Unit) {
        db.collection("users").document(userId).collection("dismissedBooks")
            .get()
            .addOnSuccessListener { documents ->
                val dismissedIds = documents.map { it.id }.toSet()
                callback(dismissedIds)
            }
            .addOnFailureListener { e ->
                Log.e("fetchDismissedBookIds", "Error fetching dismissed books: ${e.message}", e)
                callback(emptySet())
            }
    }

    //books clickable itzel medina
    private fun getBookItem(position: Int): BookItem? {
        val userId = auth.currentUser?.uid ?: return null
        val recommendations = loadRecommendationsFromPreferences(userId) ?: listOf()

        return when (position) {
            1, 2, 3 -> recommendations.getOrNull(position - 1) // Books 1-3
            4 -> loadFourthRecommendationFromPreferences(userId) // Book 4
            else -> null
        }
    }

    //make book clickable
    private fun loadFourthRecommendationFromPreferences(userId: String): BookItem? {
        val sharedPreferences = context?.getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE) ?: return null
        val json = sharedPreferences.getString("fourthRecommendation_$userId", null)
        return if (json != null) {
            Gson().fromJson(json, BookItem::class.java)
        } else {
            null
        }
    }



    //books clickable itzel medina
    private fun navigateToBookDetails(book: BookItem) {
        // Create a new instance of BookDetailsFragment
        val bookDetailsFragment = BookDetailsFragment()

        // Create a Bundle to pass data
        val bundle = Bundle().apply {
            putString("bookId", book.id)
            putString("bookTitle", book.volumeInfo?.title)
            putString("bookAuthor", book.volumeInfo?.authors?.joinToString(", "))
            putStringArrayList("bookAuthorsList", ArrayList(book.volumeInfo?.authors))
            putString("bookImage", book.volumeInfo?.imageLinks?.thumbnail)
            putFloat("bookRating", book.volumeInfo?.averageRating?.toFloat() ?: 0f)
            putString("bookIsbn", getIsbnFromBook(book))
            putString("bookDescription", book.volumeInfo?.description)
            putStringArrayList("bookGenres", ArrayList(book.volumeInfo?.categories))
            // Add any other necessary data
        }

        // Set the arguments on the fragment
        bookDetailsFragment.arguments = bundle

        // Replace the current fragment with BookDetailsFragment
        (activity as MainActivity).replaceFragment(bookDetailsFragment, "Book Details", showBackButton = true)
    }

    //books clickable itzel medina
    private fun getIsbnFromBook(book: BookItem): String? {
        val industryIdentifiers = book.volumeInfo?.industryIdentifiers
        return industryIdentifiers?.firstOrNull { it.type == "ISBN_13" }?.identifier
            ?: industryIdentifiers?.firstOrNull { it.type == "ISBN_10" }?.identifier
    }

    //to make book clickable itzel medina
    private fun saveFourthRecommendationToPreferences(userId: String, book: BookItem) {
        val sharedPreferences = context?.getSharedPreferences("BookNookPreferences", Context.MODE_PRIVATE) ?: return
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(book)
        editor.putString("fourthRecommendation_$userId", json)
        editor.apply()
    }





}