package com.example.booknook.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.utils.GenreUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

// Yunjong Noh
// This fragment handles the function of writing and storing review data to Firebase (With Template Ver.)
class ReviewActivityTemplate : Fragment() {

    // Declare UI components for submitting reviews
    private lateinit var submitButton: Button
    private lateinit var reviewEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var removeTemplateButton: Button
    private lateinit var ratingPromptText: TextView  // TextView for "Rate it!"
    private var userRating: Float? = null  // Start with null to indicate no rating

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_write_review_with_template, container, false)

        // Initialize UI components
        submitButton = view.findViewById(R.id.submitReviewButton)
        reviewEditText = view.findViewById(R.id.reviewInput)
        ratingBar = view.findViewById(R.id.myRatingBar)
        removeTemplateButton = view.findViewById(R.id.removeTemplateButton)
        ratingPromptText = view.findViewById(R.id.myRatingLabel)  // Initialize the "Rate it!" TextView

        // Retrieve views for displaying the book image and author details
        val bookImageView: ImageView = view.findViewById(R.id.bookImage)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)
        val bookTitleView: TextView = view.findViewById(R.id.bookTitle)

        // Retrieve book information passed through arguments (e.g., from previous screen)
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookAuthorsList = arguments?.getStringArrayList("bookAuthorsList")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val bookIsbn = arguments?.getString("bookIsbn") // Use this to identify the book for the review
        val bookImage = arguments?.getString("bookImage") // Image URL passed in arguments
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user ID

        // Update UI with the book's author and rating info
        authorTextView.text = bookAuthor  // Display the book's author(s)
        bookRatingBar.rating = bookRating // Set rating bar with book rating
        ratingNumberTextView.text = "(${bookRating.toString()})" // Display the numeric rating
        bookTitleView.text = bookTitle // Display the Title of book

        // Load the book's image using Glide (a third-party library for image loading)
        if (!bookImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(bookImage) // Load image URL
                .placeholder(R.drawable.placeholder_image) // Display a placeholder while loading
                .into(bookImageView)
        } else {
            // Set a default image if no image URL is available
            bookImageView.setImageResource(R.drawable.placeholder_image)
        }

        // Declare sub-ratings (checkboxes, rating bars, and text inputs)
        val charactersCheckbox = view.findViewById<CheckBox>(R.id.charactersCheckbox)
        val charactersRatingBar = view.findViewById<RatingBar>(R.id.charactersRatingBar)
        val charactersReviewEditText = view.findViewById<EditText>(R.id.input1)

        val writingCheckbox = view.findViewById<CheckBox>(R.id.writingCheckbox)
        val writingRatingBar = view.findViewById<RatingBar>(R.id.writingRatingBar)
        val writingReviewEditText = view.findViewById<EditText>(R.id.input2)

        val plotCheckbox = view.findViewById<CheckBox>(R.id.plotCheckbox)
        val plotRatingBar = view.findViewById<RatingBar>(R.id.plotRatingBar)
        val plotReviewEditText = view.findViewById<EditText>(R.id.input3)

        val themesCheckbox = view.findViewById<CheckBox>(R.id.themesCheckbox)
        val themesRatingBar = view.findViewById<RatingBar>(R.id.themesRatingBar)
        val themesReviewEditText = view.findViewById<EditText>(R.id.input4)

        val strengthsCheckbox = view.findViewById<CheckBox>(R.id.strengthsCheckbox)
        val strengthsRatingBar = view.findViewById<RatingBar>(R.id.strengthsRatingBar)
        val strengthsReviewEditText = view.findViewById<EditText>(R.id.input5)

        val weaknessesCheckbox = view.findViewById<CheckBox>(R.id.weaknessesCheckbox)
        val weaknessesRatingBar = view.findViewById<RatingBar>(R.id.weaknessesRatingBar)
        val weaknessesReviewEditText = view.findViewById<EditText>(R.id.input6)

        // Fetch the existing review from Firebase if review exists and populate fields
        if (userId != null && bookIsbn != null) {
            val db = FirebaseFirestore.getInstance()

            // Reference to the specific book's document in the "books" collection using the book's ISBN
            val bookRef = db.collection("books").document(bookIsbn)

            bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val reviewData = querySnapshot.documents[0].data

                        // Populate the fields with the fetched data
                        reviewEditText.setText(reviewData?.get("reviewText") as? String ?: "")
                        ratingBar.rating = (reviewData?.get("rating") as? Double)?.toFloat() ?: 0f

                        charactersCheckbox.isChecked =
                            reviewData?.get("charactersChecked") as? Boolean ?: false
                        charactersRatingBar.rating =
                            (reviewData?.get("charactersRating") as? Double)?.toFloat() ?: 0f
                        charactersReviewEditText.setText(
                            reviewData?.get("charactersReview") as? String ?: ""
                        )

                        writingCheckbox.isChecked =
                            reviewData?.get("writingChecked") as? Boolean ?: false
                        writingRatingBar.rating =
                            (reviewData?.get("writingRating") as? Double)?.toFloat() ?: 0f
                        writingReviewEditText.setText(
                            reviewData?.get("writingReview") as? String ?: ""
                        )

                        plotCheckbox.isChecked = reviewData?.get("plotChecked") as? Boolean ?: false
                        plotRatingBar.rating =
                            (reviewData?.get("plotRating") as? Double)?.toFloat() ?: 0f
                        plotReviewEditText.setText(reviewData?.get("plotReview") as? String ?: "")

                        themesCheckbox.isChecked =
                            reviewData?.get("themesChecked") as? Boolean ?: false
                        themesRatingBar.rating =
                            (reviewData?.get("themesRating") as? Double)?.toFloat() ?: 0f
                        themesReviewEditText.setText(
                            reviewData?.get("themesReview") as? String ?: ""
                        )

                        strengthsCheckbox.isChecked =
                            reviewData?.get("strengthsChecked") as? Boolean ?: false
                        strengthsRatingBar.rating =
                            (reviewData?.get("strengthsRating") as? Double)?.toFloat() ?: 0f
                        strengthsReviewEditText.setText(
                            reviewData?.get("strengthsReview") as? String ?: ""
                        )

                        weaknessesCheckbox.isChecked =
                            reviewData?.get("weaknessesChecked") as? Boolean ?: false
                        weaknessesRatingBar.rating =
                            (reviewData?.get("weaknessesRating") as? Double)?.toFloat() ?: 0f
                        weaknessesReviewEditText.setText(
                            reviewData?.get("weaknessesReview") as? String ?: ""
                        )

                    }
                }
                .addOnFailureListener {
                            Toast.makeText(activity, "Failed to load review data", Toast.LENGTH_SHORT).show()
                }
        }

        // Check where the review is stored and navigate to the appropriate fragment
        if (userId != null && bookIsbn != null) {
            checkAndNavigateToCorrectFragment(userId, bookIsbn)
        }

        // --- Itzel Medina ---
        // Disable RatingBar initially, but keep submit button always enabled
        ratingBar.isEnabled = false
        submitButton.isEnabled = true  // Always keep the submit button enabled

        // Activate RatingBar when "Rate it!" prompt is clicked
        ratingPromptText.setOnClickListener { activateRatingBar() }


        //Itzel Medina
        // Reference to the rating value TextView
        val ratingValueTextView = view.findViewById<TextView>(R.id.ratingValue)

        // Handle RatingBar changes
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            userRating = rating
            if (rating > 0) {
                ratingPromptText.text = "My Rating"
                ratingValueTextView.text = String.format("%.1f", rating)  // Update rating value text
            } else {
                ratingValueTextView.text = "0.0"  // Reset rating value text
            }
        }

        // Handle the submit button click to save the review
        // Ensure focus is cleared from EditText fields before retrieving data
        submitButton.setOnClickListener {
            view.clearFocus()
            if (userRating != null) {

                // Capture the main review text and rating
                val reviewText = reviewEditText.text.toString()
                val rating = userRating ?: 0f

                // Capture sub-ratings only when the button is clicked
                val charactersChecked = charactersCheckbox.isChecked
                val charactersRating = charactersRatingBar.rating
                val charactersReview = charactersReviewEditText.text.toString()

                val writingChecked = writingCheckbox.isChecked
                val writingRating = writingRatingBar.rating
                val writingReview = writingReviewEditText.text.toString()

                val plotChecked = plotCheckbox.isChecked
                val plotRating = plotRatingBar.rating
                val plotReview = plotReviewEditText.text.toString()

                val themesChecked = themesCheckbox.isChecked
                val themesRating = themesRatingBar.rating
                val themesReview = themesReviewEditText.text.toString()

                val strengthsChecked = strengthsCheckbox.isChecked
                val strengthsRating = strengthsRatingBar.rating
                val strengthsReview = strengthsReviewEditText.text.toString()

                val weaknessesChecked = weaknessesCheckbox.isChecked
                val weaknessesRating = weaknessesRatingBar.rating
                val weaknessesReview = weaknessesReviewEditText.text.toString()

                // Save all data to Firebase
                saveReview(
                    userId, bookIsbn, reviewText, rating, charactersChecked,
                    charactersRating, charactersReview, writingChecked,
                    writingRating, writingReview, plotChecked,
                    plotRating, plotReview, themesChecked,
                    themesRating, themesReview, strengthsChecked,
                    strengthsRating, strengthsReview, weaknessesChecked,
                    weaknessesRating, weaknessesReview
                )
            } else {
                // --- Itzel Medina ---
                // Display a toast if the user tries to submit without a rating
                Toast.makeText(activity, "You must rate the book before submitting a review!", Toast.LENGTH_SHORT).show()
            }
        }


        //Declare button that connects to XML
        removeTemplateButton.setOnClickListener {
            if (userId != null && bookIsbn != null) {
                // Create a pop-up alert to confirm if user wants to delete their old review
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete Review")  // Sets title of alert
                builder.setMessage(
                    "Removing the template will permanently delete the currently displayed review. " +
                            "Are you sure you want to delete your old review?"
                )  // Alert message

                // If user presses "Yes"
                builder.setPositiveButton("Yes") { dialog, which ->
                    // Delete the old review before switching to the template fragment
                    deleteOldReview(userId, bookIsbn) {
                        // After deletion, prepare the data to switch to the template fragment
                        val reviewActivityFragment = ReviewActivity()
                        val bundle = Bundle() // Bundle to store data that will be transferred to the fragment

                        // Adds data into the bundle
                        bundle.putString("bookTitle", bookTitle)
                        bundle.putStringArrayList("bookAuthorsList", bookAuthorsList)
                        bundle.putString("bookImage", bookImage)
                        bundle.putFloat("bookRating", bookRating)
                        bundle.putString("bookIsbn", bookIsbn)

                        // sets reviewActivityFragment's arguments to the data in bundle
                        reviewActivityFragment.arguments = bundle
                        (activity as? MainActivity)?.replaceFragment(reviewActivityFragment, "Write a Review") // Go back to No template fragment
                    }
                }
                // If user presses "No"
                builder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()  // Dismisses the alert ad does nothing
                }
                builder.create().show()
            }
        }
        return view
    }

    // Itzel Medina
    // Function to activate the RatingBar
    fun activateRatingBar() {
        ratingBar.isEnabled = true
        ratingBar.rating = 0f
        userRating = 0f
        ratingPromptText.text = "My Rating"
    }

    private fun checkAndNavigateToCorrectFragment(userId: String, bookIsbn: String) {
        val db = FirebaseFirestore.getInstance()
        val bookRef = db.collection("books").document(bookIsbn)

        // Query Firestore to check if a review exists and whether it used a template
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                // If a review exists for this user
                if (!querySnapshot.isEmpty) {
                    val existingReview = querySnapshot.documents[0].data
                    val isTemplateUsed = existingReview?.get("isTemplateUsed") as? Boolean ?: false

                    // If the review does not use a template
                    if (!isTemplateUsed) {
                        // Navigate to the no-template fragment
                        val reviewActivityFragment = ReviewActivity()
                        val bundle = Bundle()

                        //retrieve book's data
                        bundle.putString("bookTitle", arguments?.getString("bookTitle"))
                        bundle.putString("bookAuthor", arguments?.getString("bookAuthor"))
                        bundle.putString("bookImage", arguments?.getString("bookImage"))
                        bundle.putFloat("bookRating", arguments?.getFloat("bookRating") ?: 0f)
                        bundle.putString("bookIsbn", bookIsbn)

                        reviewActivityFragment.arguments = bundle
                        (activity as MainActivity).replaceFragment(reviewActivityFragment, "Write a Review")
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to retrieve review", Toast.LENGTH_SHORT).show()
            }
    }

    //Deletion of review with temp
    private fun deleteOldReview(userId: String, bookIsbn: String, onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Generate unique document ID if the book does not have an ISBN
        var isbnToCheck = bookIsbn
        if (isbnToCheck.isNullOrEmpty() || isbnToCheck == "No ISBN") {
            val bookTitle = arguments?.getString("bookTitle")
            val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")
            val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT) ?: "unknown_title"
            val authorsId = bookAuthors?.joinToString("_")?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
            isbnToCheck = "$titleId-$authorsId" // Create bookId based on the title and authors
        }

        // Check if a review exists in Firestore and whether a template was used
        val bookRef = db.collection("books").document(isbnToCheck)

        // Query for the existing review
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingReviewId = querySnapshot.documents[0].id
                    bookRef.collection("reviews").document(existingReviewId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Old review deleted", Toast.LENGTH_SHORT).show()
                            decrementUserReviewNum(userId)  // Decrease the user's review count
                            updateUserAverageRating(userId)
                            onComplete() // Call onComplete after deletion
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to delete old review", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    onComplete() // No existing review, proceed to the next step
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to check existing reviews", Toast.LENGTH_SHORT).show()
            }
    }

    // Function for saving with Template review Data
    private fun saveReview(
        userId: String?, bookIsbn: String?, reviewText: String, rating: Float, charactersChecked: Boolean,
        charactersRating: Float, charactersReview: String, writingChecked: Boolean,
        writingRating: Float, writingReview: String, plotChecked: Boolean,
        plotRating: Float, plotReview: String, themesChecked: Boolean,
        themesRating: Float, themesReview: String, strengthsChecked: Boolean,
        strengthsRating: Float, strengthsReview: String, weaknessesChecked: Boolean,
        weaknessesRating: Float, weaknessesReview: String
    ) {
        //Get the current user from Firebase Auth
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid // Current logged-in user ID

        // Check both userId and bookIsbn are not null before proceeding
        if (userId != null) {
            // initialize Firebase Instance
            val db = FirebaseFirestore.getInstance()
            val bookTitle = arguments?.getString("bookTitle")
            val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")
            var bookIsbn = arguments?.getString("bookIsbn") // Use this to identify the book for the review
            // Yunjong Noh
            // Delivered Genre list
            val rawBookGenres = arguments?.getStringArrayList("bookGenresList") ?: listOf("default genre")

            // If the book has no ISBN, create a unique document ID using the title and authors of the book
            if (bookIsbn.isNullOrEmpty() || bookIsbn == "No ISBN") {
                // Creates title part by replacing all whitespaces with underscores, and making it lowercase
                val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
                    ?: "unknown_title"
                // Creates authors part by combining authors, replacing all whitespaces with underscores, and making it lowercase
                val authorsId =
                    bookAuthors?.joinToString("_")?.replace("\\s+".toRegex(), "_")?.lowercase(
                        Locale.ROOT
                    )
                bookIsbn = "$titleId-$authorsId" // Update bookIsbn with new Id
            }
            // Yunjong Noh
            // Normalize genres using GenreUtils before saving
            val bookGenres = rawBookGenres.map { GenreUtils.normalizeGenre(it) }
            // Add log for checking
            Log.d("ReviewActivity", "Raw genres: $rawBookGenres")
            Log.d("ReviewActivity", "Normalized genres: $bookGenres")

            // Get the user's username from database
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") // Get username if exists

                    // Create a map for review data to save into Firebase
                    val reviewData = mapOf(
                        "userId" to userId,
                        "username" to username,
                        "reviewText" to reviewText,
                        "rating" to rating.toDouble(),
                        "charactersChecked" to charactersChecked,
                        "charactersRating" to charactersRating.toDouble(),
                        "charactersReview" to charactersReview,
                        "writingChecked" to writingChecked,
                        "writingRating" to writingRating.toDouble(),
                        "writingReview" to writingReview,
                        "plotChecked" to plotChecked,
                        "plotRating" to plotRating.toDouble(),
                        "plotReview" to plotReview,
                        "themesChecked" to themesChecked,
                        "themesRating" to themesRating.toDouble(),
                        "themesReview" to themesReview,
                        "strengthsChecked" to strengthsChecked,
                        "strengthsRating" to strengthsRating.toDouble(),
                        "strengthsReview" to strengthsReview,
                        "weaknessesChecked" to weaknessesChecked,
                        "weaknessesRating" to weaknessesRating.toDouble(),
                        "weaknessesReview" to weaknessesReview,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "isTemplateUsed" to true
                    )

                    // Map to store book data
                    val bookData = mapOf(
                        "bookTitle" to bookTitle,
                        "authors" to bookAuthors,
                        // Yunjong Noh
                        // Save normalized genres
                        "genres" to bookGenres
                    )

                    // Reference to the specific book's document in the "books" collection
                    val bookRef = db.collection("books").document(bookIsbn)
                    bookRef.set(bookData, SetOptions.merge()) // Updates database with book details if not in database already

                    // Check if the user has already submitted a review
                    bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.isEmpty) {
                                // If no review exists for this user, add a new one
                                bookRef.collection("reviews").add(reviewData)
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, "Review saved successfully!", Toast.LENGTH_SHORT).show()
                                        incrementUserReviewNum(userId) // Increments the number of reviews field
                                        updateUserAverageRating(userId)
                                        // Yunjong Noh
                                        // Add a notification for the new review (11/10)
                                        bookTitle?.let {
                                            addReviewNotification(userId, it, NotificationType.REVIEW_ADDED)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(activity, "Failed to save review: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // If a review already exists, update it
                                val existingReviewId = querySnapshot.documents[0].id
                                bookRef.collection("reviews").document(existingReviewId).set(reviewData)
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, "Review updated successfully!", Toast.LENGTH_SHORT).show()
                                        updateUserAverageRating(userId)
                                        // Yunjong Noh
                                        // Add a notification for the updated review (11/10)
                                        bookTitle?.let {
                                            addReviewNotification(userId, it, NotificationType.REVIEW_EDIT)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(activity, "Failed to update review", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to check existing reviews", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "Book ISBN or user not provided", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Yunjong Noh
    // Function to add a review notification to Firestore
    private fun addReviewNotification(userId: String, bookTitle: String, notificationType: NotificationType) {
        val db = FirebaseFirestore.getInstance()
        val currentTime = System.currentTimeMillis()
        val expirationTime = currentTime + 10 * 24 * 60 * 60 * 1000 // Notification expiration time: 10 days from now

        // Get the current user ID (this will be the senderId)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch the current user's profile details
        val currentUserDocRef = db.collection("users").document(currentUserId)
        currentUserDocRef.get().addOnSuccessListener { currentUserDoc ->
            if (currentUserDoc.exists()) {
                // Fetch the sender's profile details
                val senderProfileImageUrl = currentUserDoc.getString("profileImageUrl") ?: ""
                val senderUsername = currentUserDoc.getString("username") ?: "Unknown User"

                // Fetch the user's friends and groups to check if they are eligible for the notification
                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Fetch the user's friends (List<Map<String, String>>) and groups (List<String>) data
                        val friends = document.get("friends") as? List<Map<String, String>> ?: emptyList()
                        val joinedGroups = document.get("joinedGroups") as? List<String> ?: emptyList()

                        // Notification message
                        val notificationMessage = "A review for \"$bookTitle\" has been added or updated."

                        // Loop through friends and send notifications to eligible friends
                        friends.forEach { friend ->
                            if (friend["friendId"] == currentUserId) {
                                // Send notification to friend with receiverId as the friend
                                sendNotification(friend["friendId"]!!, notificationMessage, notificationType, expirationTime, currentUserId, friend["friendId"]!!, senderProfileImageUrl, senderUsername)
                            }
                        }

                        // Loop through groups and send notifications to eligible group members
                        joinedGroups.forEach { groupId ->
                            sendNotificationToGroupMembers(groupId, notificationMessage, notificationType, expirationTime, currentUserId, senderProfileImageUrl, senderUsername)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("ReviewNotification", "Failed to retrieve current user data for notification.") // Log any errors fetching current user data
        }
    }
    // Yunjong Noh
    // Function to send notification (with sender's details like profile image and username)
    private fun sendNotification(userId: String, message: String, notificationType: NotificationType, expirationTime: Long, senderId: String, receiverId: String, senderProfileImageUrl: String, senderUsername: String) {
        val db = FirebaseFirestore.getInstance()

        // Skip sending notification if the current user is the sender (userId is the same as currentUserId)
        if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
            Log.d("ReviewNotification", "Notification not sent to the sender (userId = currentUserId).")
            return
        }

        // Create the notification object
        val notification = NotificationItem(
            userId = userId,  // Receiver's ID
            senderId = senderId,  // Sender's ID
            receiverId = receiverId,  // Receiver's ID
            message = message,
            timestamp = System.currentTimeMillis(),
            type = notificationType,
            dismissed = false,
            expirationTime = expirationTime,
            profileImageUrl = senderProfileImageUrl, // Use sender's profile image
            username = senderUsername // Use sender's username
        )

        // Add the notification to the "notifications" collection in Firestore
        db.collection("notifications").add(notification)
            .addOnSuccessListener { documentReference ->
                val notificationId = documentReference.id
                db.collection("notifications").document(notificationId)
                    .update("notificationId", notificationId)
                    .addOnSuccessListener {
                        Log.d("ReviewNotification", "Notification added with ID: $notificationId") // Log success
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReviewNotification", "Error updating notificationId: ${e.message}", e) // Log any errors
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ReviewNotification", "Error adding notification: ${e.message}", e) // Log any errors adding the notification
            }
    }
    // Yunjong Noh
    // Function to send notification to group members (with sender's details like profile image and username)
    private fun sendNotificationToGroupMembers(groupId: String, message: String, notificationType: NotificationType, expirationTime: Long, senderId: String, senderProfileImageUrl: String, senderUsername: String) {
        val db = FirebaseFirestore.getInstance()

        // Fetch group members (assuming the group data contains the members' IDs)
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener { groupDoc ->
                if (groupDoc.exists()) {
                    val groupMembers = groupDoc.get("members") as? List<String> ?: emptyList()

                    // Send notification to each member in the group
                    groupMembers.forEach { memberId ->
                        // Only send notification to non-current users (excluding sender)
                        if (memberId != FirebaseAuth.getInstance().currentUser?.uid) {
                            sendNotification(
                                memberId, message, notificationType, expirationTime, senderId, memberId, senderProfileImageUrl, senderUsername
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReviewNotification", "Error fetching group members: ${e.message}", e)
            }
    }

    // Veronica Nguyen
    // Function updates the average rating of the user
    fun updateUserAverageRating(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)

        // Accesses all collections named "reviews" in database
        db.collectionGroup("reviews")
            .whereEqualTo("userId", userId)  // Finds all documents with the user's id (current user)
            .get()
            .addOnSuccessListener { documents ->
                // Gets all of the user's ratings under reviews
                val userRatings = documents.mapNotNull { it.getDouble("rating") }
                if (userRatings.isNotEmpty()) {
                    // Gets the sum of all of the ratings
                    val ratingsTotalSum = userRatings.sum()
                    // Calculates the user's average rating
                    val averageRating = ratingsTotalSum / userRatings.size
                    // Rounds the average rating to two decimal places
                    val roundedAverageRating =
                        BigDecimal(averageRating).setScale(2, RoundingMode.HALF_UP).toDouble()

                    // Updates the averageRating field in database
                    db.collection("users").document(userId)
                        .update("averageRating", roundedAverageRating)
                }
            }
    }

    // Veronica Nguyen
    // Function to decrement user's number of reviews when a review is deleted
    private fun decrementUserReviewNum(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.update("numReviews", FieldValue.increment(-1))  // decrements the field by 1
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to update review count", Toast.LENGTH_SHORT).show()
            }
    }

    // Veronica Nguyen
    // Function to increment user's number of reviews when a review is added
    private fun incrementUserReviewNum(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.update("numReviews", FieldValue.increment(1))  // increments the field by 1
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to update review count", Toast.LENGTH_SHORT).show()
            }
    }
}
