package com.example.booknook.fragments

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

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

    // Called when the fragment is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (no-template layout used initially)
        val view = inflater.inflate(R.layout.fragment_write_review_no_template, container, false)

        // Retrieve UI components from the view
        submitButton = view.findViewById(R.id.submitReviewButton)
        reviewEditText = view.findViewById(R.id.reviewInput)
        ratingBar = view.findViewById(R.id.myRatingBar)
        spoilerCheckbox = view.findViewById(R.id.spoilerCheckbox)
        sensitiveCheckbox = view.findViewById(R.id.sensitiveTopicsCheckbox)
        useTemplateButton = view.findViewById(R.id.useTemplateButton)

        // Retrieve views for displaying the book image and author details
        val bookImageView: ImageView = view.findViewById(R.id.bookImage)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)
        val bookTitleView: TextView = view.findViewById(R.id.bookTitle)

        // Retrieve book information passed through arguments (e.g., from previous screen)
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val bookIsbn = arguments?.getString("bookIsbn") // Use this to identify the book for the review
        val bookImage = arguments?.getString("bookImage") // Image URL passed in arguments
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user ID

        // Update UI with the book's author and rating info
        authorTextView.text = bookAuthor  // Display the book's author(s)
        bookRatingBar.rating = bookRating // Set rating bar with book rating
        ratingNumberTextView.text = "(${bookRating.toString()})" // Display the numeric rating
        bookTitleView.text = bookTitle //Display the Title of book

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

        // Fetch existing review if the user has already submitted one for this book
        if (userId != null && bookIsbn != null) {
            val db = FirebaseFirestore.getInstance()
            val bookRef = db.collection("books").document(bookIsbn)

            // Query the database to see if the user already submitted a review for this book
            bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Populate the form fields with existing review data if a review is found
                        val existingReview = querySnapshot.documents[0].data
                        reviewEditText.setText(existingReview?.get("reviewText") as? String ?: "")
                        ratingBar.rating = (existingReview?.get("rating") as? Double)?.toFloat() ?: 0f
                        spoilerCheckbox.isChecked = existingReview?.get("hasSpoilers") as? Boolean ?: false
                        sensitiveCheckbox.isChecked = existingReview?.get("hasSensitiveTopics") as? Boolean ?: false
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

        // Handle the submit button click to save the review
        submitButton.setOnClickListener {
            // Get user input from the form
            val reviewText = reviewEditText.text.toString()
            val rating = ratingBar.rating
            val hasSpoilers = spoilerCheckbox.isChecked
            val hasSensitiveTopics = sensitiveCheckbox.isChecked

            // Save the review to Firestore
            saveReview(reviewText, rating, hasSpoilers, hasSensitiveTopics, false)
        }

        // Yunjong Noh
        //Declare button that connects to XML
        useTemplateButton.setOnClickListener {
            if (userId != null && bookIsbn != null) {
                // Delete the old review before switching to the template fragment
                deleteOldReview(userId, bookIsbn) {
                    // After deletion, prepare the data to switch to the template fragment
                    val reviewActivityTemplateFragment = ReviewActivityTemplate()
                    val bundle = Bundle() // Bundle to store data that will be transferred to the fragment

                    // Add data to the bundle
                    bundle.putString("bookTitle", bookTitle)
                    bundle.putString("bookAuthor", bookAuthor)
                    bundle.putString("bookImage", bookImage)
                    bundle.putFloat("bookRating", bookRating)
                    bundle.putString("bookIsbn", bookIsbn)

                    reviewActivityTemplateFragment.arguments = bundle // Set arguments for the fragment

                    // Replace the current fragment with the template fragment
                    (activity as MainActivity).replaceFragment(reviewActivityTemplateFragment, "Write a Review")
                }
            }
        }

        return view
    }

    private fun checkAndNavigateToCorrectFragment(userId: String, bookIsbn: String) {
        val db = FirebaseFirestore.getInstance()
        val bookRef = db.collection("books").document(bookIsbn)

        // Query Firestore to check if a review exists and whether it used a template
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingReview = querySnapshot.documents[0].data
                    val isTemplateUsed = existingReview?.get("isTemplateUsed") as? Boolean ?: false

                    if (isTemplateUsed) {
                        // Navigate to the with-template fragment
                        val reviewActivityTemplateFragment = ReviewActivityTemplate()
                        val bundle = Bundle()

                        bundle.putString("bookTitle", arguments?.getString("bookTitle"))
                        bundle.putString("bookAuthor", arguments?.getString("bookAuthor"))
                        bundle.putString("bookImage", arguments?.getString("bookImage"))
                        bundle.putFloat("bookRating", arguments?.getFloat("bookRating") ?: 0f)
                        bundle.putString("bookIsbn", bookIsbn)

                        reviewActivityTemplateFragment.arguments = bundle
                        (activity as MainActivity).replaceFragment(reviewActivityTemplateFragment, "Write a Review")
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to retrieve review", Toast.LENGTH_SHORT).show()
            }
    }


    private fun deleteOldReview(userId: String, bookIsbn: String, onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val bookRef = db.collection("books").document(bookIsbn)

        // Query for the existing review
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingReviewId = querySnapshot.documents[0].id
                    bookRef.collection("reviews").document(existingReviewId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Old review deleted", Toast.LENGTH_SHORT).show()
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

    // Function to save the review data to Firestore
    private fun saveReview(reviewText: String, rating: Float, hasSpoilers: Boolean, hasSensitiveTopics: Boolean, isTemplateUsed: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser // Get current user
        val userId = user?.uid // Get user ID

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            var bookIsbn = arguments?.getString("bookIsbn") // Retrieve the book's ISBN from arguments
            val bookTitle = arguments?.getString("bookTitle")
            val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")

            if (bookIsbn.isNullOrEmpty()) {
                // Creates a new document for the book and uses Firestore ID if it doesn't have an ISBN
                val newBookRef = db.collection("books").document()
                bookIsbn = newBookRef.id // Use the Firestore ID as the unique identifier for the book
            }

            // Retrieve the user's username from Firestore (for storing with the review)
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") // Get username if exists

                    if (bookIsbn != null) {
                        // Create a map for the review data to store in Firestore
                        val reviewData = mapOf(
                            "userId" to userId,
                            "username" to username,
                            "reviewText" to reviewText,
                            "rating" to rating,
                            "hasSpoilers" to hasSpoilers,
                            "hasSensitiveTopics" to hasSensitiveTopics,
                            "timestamp" to FieldValue.serverTimestamp(), // Use Firestore timestamp
                            "isTemplateUsed" to isTemplateUsed
                        )

                        // Map to store book data
                        val bookData = mapOf(
                            "bookTitle" to bookTitle,
                            "authors" to bookAuthors,
                            "bookIsbn" to bookIsbn
                        )

                        // Reference to the specific book's document in Firestore
                        val bookRef = db.collection("books").document(bookIsbn)
                        bookRef.set(bookData, SetOptions.merge())  // Updates database with book details if not in database already

                        // Check if the user has already submitted a review
                        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    // No existing review: Add a new review
                                    bookRef.collection("reviews").add(reviewData)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Review saved successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(activity, "Failed to save review", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Update the existing review
                                    val existingReviewId = querySnapshot.documents[0].id
                                    bookRef.collection("reviews").document(existingReviewId)
                                        .set(reviewData) // Update review data
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Review updated successfully!", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(activity, "Book ISBN not provided", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}