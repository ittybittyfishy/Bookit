package com.example.booknook.fragments

import android.os.Bundle
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

class ReviewActivity : Fragment() {

    private lateinit var submitButton: Button
    private lateinit var reviewEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var spoilerCheckbox: CheckBox
    private lateinit var sensitiveCheckbox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_write_review_no_template, container, false)

        // Retrieve views
        submitButton = view.findViewById(R.id.submitReviewButton)
        reviewEditText = view.findViewById(R.id.reviewInput)
        ratingBar = view.findViewById(R.id.myRatingBar)
        spoilerCheckbox = view.findViewById(R.id.spoilerCheckbox)
        sensitiveCheckbox = view.findViewById(R.id.sensitiveTopicsCheckbox)

        // Retrieve the ImageView for displaying the book image
        val bookImageView: ImageView = view.findViewById(R.id.bookImage)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)

        // Retrieve book information from arguments
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val bookIsbn = arguments?.getString("bookIsbn")
        val bookImage = arguments?.getString("bookImage") // Image URL passed in arguments
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        authorTextView.text = bookAuthor  // Update text with the book's author(s)
        bookRatingBar.rating = bookRating // Update stars with rating
        ratingNumberTextView.text = "(${bookRating.toString()})" // Set the rating number text

        // Load the book's image using Glide
        if (!bookImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(R.drawable.placeholder_image) // Add a placeholder image
                .into(bookImageView)
        } else {
            // Optionally set a default or placeholder image if no image URL is available
            bookImageView.setImageResource(R.drawable.placeholder_image)
        }

        // Fetch existing review if it exists
        if (userId != null && bookIsbn != null) {
            val db = FirebaseFirestore.getInstance()
            val bookRef = db.collection("books").document(bookIsbn)

            bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val existingReview = querySnapshot.documents[0].data

                        // Populate the form fields with existing review data
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

        // Handle the submit button click
        submitButton.setOnClickListener {
            val reviewText = reviewEditText.text.toString()
            val rating = ratingBar.rating
            val hasSpoilers = spoilerCheckbox.isChecked
            val hasSensitiveTopics = sensitiveCheckbox.isChecked

            // Save review to Firestore
            saveReview(reviewText, rating, hasSpoilers, hasSensitiveTopics)
        }

        return view
    }

    private fun saveReview(reviewText: String, rating: Float, hasSpoilers: Boolean, hasSensitiveTopics: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val username = user?.displayName

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val bookIsbn = arguments?.getString("bookIsbn")

            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")

                    if (bookIsbn != null) {
                        val reviewData = mapOf(
                            "userId" to userId,
                            "username" to username,
                            "reviewText" to reviewText,
                            "rating" to rating,
                            "hasSpoilers" to hasSpoilers,
                            "hasSensitiveTopics" to hasSensitiveTopics,
                            "timestamp" to FieldValue.serverTimestamp()
                        )

                        // Reference to the specific book's document
                        val bookRef = db.collection("books").document(bookIsbn)

                        // Check if the user has already submitted a review
                        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    // Add a new review if no existing review is found
                                    bookRef.collection("reviews").add(reviewData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                activity,
                                                "Review saved successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            (activity as? MainActivity)?.replaceFragment(
                                                HomeFragment(),
                                                "Home"
                                            )
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                activity,
                                                "Failed to save review",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    // Update existing review
                                    val existingReviewId = querySnapshot.documents[0].id
                                    bookRef.collection("reviews").document(existingReviewId)
                                        .set(reviewData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                activity,
                                                "Review updated successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            (activity as? MainActivity)?.replaceFragment(
                                                HomeFragment(),
                                                "Home"
                                            )
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                activity,
                                                "Failed to update review",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    activity,
                                    "Failed to check existing reviews",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(activity, "Book ISBN not provided", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}