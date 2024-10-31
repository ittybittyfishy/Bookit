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
// This fragment handles the function of writing and storing review data to Firebase
class ReviewActivity : Fragment() {

    // Declare UI components for submitting reviews
    private lateinit var submitButton: Button
    private lateinit var reviewEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var spoilerCheckbox: CheckBox
    private lateinit var sensitiveCheckbox: CheckBox
    private lateinit var useTemplateButton: Button

    //itzel medina
    private lateinit var ratingValue: TextView  // TextView to display the current rating value


    // Declare mutable properties for userId and bookIsbn
    private var userId: String? = null
    private var bookIsbn: String? = null

    private var userRating: Float? = null // Start with null to indicate no rating
    private lateinit var ratingPromptText: TextView  // TextView for "Rate it!"

    // Called when the fragment is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_write_review_no_template, container, false)

        // Retrieve UI components from the view
        submitButton = view.findViewById(R.id.submitReviewButton)
        reviewEditText = view.findViewById(R.id.reviewInput)
        ratingBar = view.findViewById(R.id.myRatingBar)
        spoilerCheckbox = view.findViewById(R.id.spoilerCheckbox)
        sensitiveCheckbox = view.findViewById(R.id.sensitiveTopicsCheckbox)
        useTemplateButton = view.findViewById(R.id.useTemplateButton)

        ratingPromptText = view.findViewById(R.id.ratingPromptText)

        //itzel medina
        ratingValue = view.findViewById(R.id.ratingValue)  // Initialize ratingValue TextView


        // Retrieve book information passed through arguments (e.g., from previous screen)
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookAuthorsList = arguments?.getStringArrayList("bookAuthorsList")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val bookIsbn = arguments?.getString("bookIsbn")
        val bookImage = arguments?.getString("bookImage")
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Update UI with the book's author and rating info
        view.findViewById<TextView>(R.id.bookAuthor).text = bookAuthor
        view.findViewById<RatingBar>(R.id.bookRating).rating = bookRating
        view.findViewById<TextView>(R.id.ratingNumber).text = "(${bookRating})"
        view.findViewById<TextView>(R.id.bookTitle).text = bookTitle

        // Load the book's image using Glide (Glide helps to call cache of image)
        val bookImageView = view.findViewById<ImageView>(R.id.bookImage)
        if (!bookImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(R.drawable.placeholder_image)
                .into(bookImageView)
        } else {
            bookImageView.setImageResource(R.drawable.placeholder_image)
        }

        // Fetch existing review if the user has already submitted one for this book
        if (userId != null && bookIsbn != null) {
            val db = FirebaseFirestore.getInstance()

            // For books without ISBN, generate a unique bookIsbn
            var isbnToCheck = bookIsbn
            if (isbnToCheck.isNullOrEmpty() || isbnToCheck == "No ISBN") {
                val bookTitle = arguments?.getString("bookTitle")
                val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")
                val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT) ?: "unknown_title"
                val authorsId = bookAuthors?.joinToString("_")?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
                isbnToCheck = "$titleId-$authorsId" // Generate a unique bookIsbn based on the title and authors
            }
            // Check if a review exists in Firestore and whether a template was used
            val bookRef = db.collection("books").document(isbnToCheck)

            // Query the database to see if the user already submitted a review for this book
            bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Populate the form fields with existing review data if a review is found
                        val existingReview = querySnapshot.documents[0].data
                        reviewEditText.setText(existingReview?.get("reviewText") as? String ?: "")
                        ratingBar.rating =
                            (existingReview?.get("rating") as? Double)?.toFloat() ?: 0f
                        spoilerCheckbox.isChecked =
                            existingReview?.get("hasSpoilers") as? Boolean ?: false
                        sensitiveCheckbox.isChecked =
                            existingReview?.get("hasSensitiveTopics") as? Boolean ?: false
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to retrieve existing review", Toast.LENGTH_SHORT).show()
                }
        }

        // Check where the review is stored and navigate to the appropriate fragment
        if (userId != null && bookIsbn != null) {
            checkAndNavigateToCorrectFragment(userId, bookIsbn)
        }

        // Disable RatingBar and submit button initially
        ratingBar.isEnabled = false
        submitButton.isEnabled = false

        // Activate RatingBar when "Rate it!" prompt is clicked
        ratingPromptText.setOnClickListener { activateRatingBar() }


        //Itzel Medina
        // Handle RatingBar changes
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            userRating = rating
            if (rating >= 0) {
                ratingPromptText.text = "My Rating"
                ratingValue.text = rating.toString()  // Update the TextView with the current rating
                submitButton.isEnabled = true
            } else {
                ratingPromptText.text = "Rate it!"
                ratingValue.text = "0.0"  // Reset the TextView if rating is zero
                submitButton.isEnabled = false
            }
        }


        // Itzel Medina - Ensure the submit button is always enabled (Modified Code)
        // Remove the line that disables the submit button initially
        submitButton.isEnabled = true  // Always enable the submit button

        // Itzel Medina - Handle the submit button click (Modified Code)
        submitButton.setOnClickListener {
            if (userRating != null) {
                // User has provided a rating, proceed with the review submission
                val reviewText = reviewEditText.text.toString()
                val rating = userRating ?: 0f
                val hasSpoilers = spoilerCheckbox.isChecked
                val hasSensitiveTopics = sensitiveCheckbox.isChecked

                // Save the review to Firestore
                saveReview(reviewText, rating, hasSpoilers, hasSensitiveTopics, false)
            } else {
                // Show a toast if the user tries to submit without a rating
                Toast.makeText(activity, "You must rate the book before submitting a review!", Toast.LENGTH_SHORT).show()
            }
        }


        // Handle the useTemplateButton click
        useTemplateButton.setOnClickListener {
            if (userId != null && bookIsbn != null) {
                // Create a pop-up alert to confirm if user wants to delete their old review
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete Review")  // Sets title of alert
                builder.setMessage(
                    "Switching to a review template will permanently delete the currently displayed review. " +
                            "Are you sure you want to delete your old review?"
                )  // Alert message
                // If user presses "Yes"
                builder.setPositiveButton("Yes") { dialog, which ->
                    // Delete the old review before switching to the template fragment
                    deleteOldReview(userId, bookIsbn) {
                        // After deletion, prepare the data to switch to the template fragment
                        val reviewActivityTemplateFragment = ReviewActivityTemplate()
                        val bundle = Bundle() // Bundle to store data that will be transferred to the fragment

                        // Add data to the bundle
                        bundle.putString("bookTitle", bookTitle)
                        bundle.putStringArrayList("bookAuthorsList", bookAuthorsList)
                        bundle.putString("bookImage", bookImage)
                        bundle.putFloat("bookRating", bookRating)
                        bundle.putString("bookIsbn", bookIsbn)

                        reviewActivityTemplateFragment.arguments = bundle // Set arguments for the fragment
                        (activity as MainActivity).replaceFragment(reviewActivityTemplateFragment, "Write a Review"
                        )
                    }
                }
                // If user presses "No"
                builder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()  // Dismisses the alert and does nothing
                }
                builder.create().show()
            }
        }
        return view
    }

    // Activate RatingBar
    private fun activateRatingBar() {
        ratingBar.isEnabled = true
        ratingBar.rating = 0f
        userRating = 0f
        ratingPromptText.text = "My Rating"
    }

    private fun checkAndNavigateToCorrectFragment(userId: String, bookIsbn: String) {
        val db = FirebaseFirestore.getInstance()

        // If the book does not have an ISBN, generate a unique document ID
        var isbnToCheck = bookIsbn
        if (isbnToCheck.isNullOrEmpty() || isbnToCheck == "No ISBN") {
            val bookTitle = arguments?.getString("bookTitle")
            val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")
            val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT) ?: "unknown_title"
            val authorsId = bookAuthors?.joinToString("_")?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
            isbnToCheck = "$titleId-$authorsId" // Generate a unique bookId based on the title and authors
        }

        // Check if a review exists in Firestore and whether a template was used
        val bookRef = db.collection("books").document(isbnToCheck)
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                // If a review exists for this user
                if (!querySnapshot.isEmpty) {
                    val existingReview = querySnapshot.documents[0].data
                    val isTemplateUsed = existingReview?.get("isTemplateUsed") as? Boolean ?: false

                    // If the review does not use a template
                    if (isTemplateUsed) {
                        // Navigate to the fragment that uses a template for the review
                        val reviewActivityTemplateFragment = ReviewActivityTemplate()
                        val bundle = Bundle()

                        //retrieve book's data
                        bundle.putString("bookTitle", arguments?.getString("bookTitle"))
                        bundle.putString("bookAuthor", arguments?.getString("bookAuthor"))
                        bundle.putString("bookImage", arguments?.getString("bookImage"))
                        bundle.putFloat("bookRating", arguments?.getFloat("bookRating") ?: 0f)
                        bundle.putString("bookIsbn", isbnToCheck)

                        reviewActivityTemplateFragment.arguments = bundle
                        (activity as MainActivity).replaceFragment(
                            reviewActivityTemplateFragment,
                            "Write a Review"
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to retrieve review.", Toast.LENGTH_SHORT).show()
            }
    }

    //Deletion of review no temp (even no-isbn)
    private fun deleteOldReview(userId: String, bookIsbn: String?, onComplete: () -> Unit) {
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

        val bookRef = db.collection("books").document(isbnToCheck)

        // Delete the old review
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingReviewId = querySnapshot.documents[0].id
                    bookRef.collection("reviews").document(existingReviewId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Old review deleted.", Toast.LENGTH_SHORT).show()
                            decrementUserReviewNum(userId) // Decrease the user's review count
                            updateUserAverageRating(userId)
                            onComplete()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to delete review.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    onComplete() // If no review exists, just complete
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to retrieve reviews.", Toast.LENGTH_SHORT).show()
            }
    }

    // Save review data to Firestore (No Template)
    private fun saveReview(
        reviewText: String,
        rating: Float,
        hasSpoilers: Boolean,
        hasSensitiveTopics: Boolean,
        isTemplateUsed: Boolean
    ) {
        // Get the current user from Firebase Auth
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid // Current logged-in user ID

        // Ensure userId and bookIsbn are not null using let
        if (userId != null) {
            // Initialize Firebase Instance
            val db = FirebaseFirestore.getInstance()
            val bookTitle = arguments?.getString("bookTitle")
            val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")
            var bookIsbn = arguments?.getString("bookIsbn") // Use this to identify the book for the review
            val rawBookGenres = arguments?.getStringArrayList("bookGenresList") ?: listOf("default genre") // 전달된 장르 리스트

            // If the book has no ISBN, create a unique document ID using the title and authors of the book
            if (bookIsbn.isNullOrEmpty() || bookIsbn == "No ISBN") {
                // Create title part by replacing all whitespaces with underscores, and making it lowercase
                val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
                    ?: "unknown_title"
                // Create authors part by combining authors, replacing all whitespaces with underscores, and making it lowercase
                val authorsId = bookAuthors?.joinToString("_")?.replace("\\s+".toRegex(), "_")
                    ?.lowercase(Locale.ROOT)
                bookIsbn = "$titleId-$authorsId" // Update bookIsbn with new Id
            }

            // Normalize genres using GenreUtils before saving
            val bookGenres = rawBookGenres.map { GenreUtils.normalizeGenre(it) }
            // 로그 추가
            Log.d("ReviewActivity", "Raw genres: $rawBookGenres")
            Log.d("ReviewActivity", "Normalized genres: $bookGenres")

            // Get the user's username from database
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") // Get username if exists

                    if (bookIsbn != null) {
                        // Create a map for the review data to save into Firebase
                        val reviewData = mapOf(
                            "userId" to userId,
                            "username" to username,
                            "reviewText" to reviewText,
                            "rating" to rating,
                            "hasSpoilers" to hasSpoilers,
                            "hasSensitiveTopics" to hasSensitiveTopics,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "isTemplateUsed" to isTemplateUsed // isTemplateUsed will be false for no-template reviews
                        )

                        // Map to store book data
                        val bookData = mapOf(
                            "bookTitle" to bookTitle,
                            "authors" to bookAuthors,
                            "genres" to bookGenres // Save normalized genres
                        )

                        // Reference to the specific book's document in the "books" collection using the book's ISBN
                        val bookRef = db.collection("books").document(bookIsbn)
                        bookRef.set(bookData, SetOptions.merge())  // Updates database with book details if not in database already

                        // Check if the user has already submitted a review by querying reviews collection with the userId
                        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    // If no review exists for this user, add a new one
                                    bookRef.collection("reviews").add(reviewData)
                                        .addOnSuccessListener {
                                            // Show success message
                                            Toast.makeText(requireActivity(), "Review saved successfully!", Toast.LENGTH_SHORT).show()
                                            // Increment the number of reviews field for the user
                                            incrementUserReviewNum(userId)
                                            updateUserAverageRating(userId)
                                        }
                                        .addOnFailureListener {
                                            // If saving the review fails, display an error message
                                            Toast.makeText(requireActivity(), "Failed to save review", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // If a review already exists, update it with the new data
                                    val existingReviewId = querySnapshot.documents[0].id
                                    bookRef.collection("reviews").document(existingReviewId)
                                        .set(reviewData)
                                        .addOnSuccessListener {
                                            // Show success message for review update
                                            Toast.makeText(requireActivity(), "Review updated successfully!", Toast.LENGTH_SHORT).show()
                                            updateUserAverageRating(userId)
                                        }
                                        .addOnFailureListener {
                                            // If updating the review fails, display an error message
                                            Toast.makeText(requireActivity(), "Failed to update review", Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }.addOnFailureListener {
                                // If querying for the existing review fails, display an error message
                                Toast.makeText(requireActivity(), "Failed to check existing reviews", Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // If userId or bookIsbn is null, display an error message
                        Toast.makeText(activity, "Book ISBN or user not provided", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
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