package com.example.booknook.fragments

import android.app.AlertDialog
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
        bookIsbn = arguments?.getString("bookIsbn")
        val bookImage = arguments?.getString("bookImage")
        userId = FirebaseAuth.getInstance().currentUser?.uid

        // Update UI with the book's author and rating info
        view.findViewById<TextView>(R.id.bookAuthor).text = bookAuthor
        view.findViewById<RatingBar>(R.id.bookRating).rating = bookRating
        view.findViewById<TextView>(R.id.ratingNumber).text = "(${bookRating})"
        view.findViewById<TextView>(R.id.bookTitle).text = bookTitle

        // Load the book's image using Glide
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
        userId?.let { uid ->
            bookIsbn?.let { isbn ->
                val db = FirebaseFirestore.getInstance()
                val bookRef = db.collection("books").document(isbn)

                bookRef.collection("reviews").whereEqualTo("userId", uid).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
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
        }

        // Disable RatingBar and submit button initially
        ratingBar.isEnabled = false
        submitButton.isEnabled = false

        // Activate RatingBar when "Rate it!" prompt is clicked
        ratingPromptText.setOnClickListener { activateRatingBar() }


        //Itzel Medina
        // Handle RatingBar changes
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating > 0) {
                userRating = rating
                ratingPromptText.text = "My Rating"
                ratingValue.text = rating.toString()  // Update the TextView with the current rating
                submitButton.isEnabled = true
            } else {
                userRating = null
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
            userId?.let { uid ->
                bookIsbn?.let { isbn ->
                    // Confirmation alert before switching to template
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Delete Review")
                        .setMessage("Switching to a review template will permanently delete the currently displayed review. Are you sure?")
                        .setPositiveButton("Yes") { _, _ ->
                            deleteOldReview(uid, isbn) {
                                val templateFragment = ReviewActivityTemplate()
                                val bundle = Bundle().apply {
                                    putString("bookTitle", bookTitle)
                                    putStringArrayList("bookAuthorsList", bookAuthorsList)
                                    putString("bookImage", bookImage)
                                    putFloat("bookRating", bookRating)
                                    putString("bookIsbn", isbn)
                                }
                                templateFragment.arguments = bundle
                                (activity as MainActivity).replaceFragment(templateFragment, "Write a Review")
                            }
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
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

    // Save review data to Firestore
    private fun saveReview(reviewText: String, rating: Float, hasSpoilers: Boolean, hasSensitiveTopics: Boolean, isTemplateUsed: Boolean) {
        userId?.let { uid ->
            bookIsbn?.let { isbn ->
                val db = FirebaseFirestore.getInstance()
                val bookRef = db.collection("books").document(isbn)

                db.collection("users").document(uid).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username")
                        val reviewData = mapOf(
                            "userId" to uid,
                            "username" to username,
                            "reviewText" to reviewText,
                            "rating" to rating,
                            "hasSpoilers" to hasSpoilers,
                            "hasSensitiveTopics" to hasSensitiveTopics,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "isTemplateUsed" to isTemplateUsed
                        )

                        val bookData = mapOf(
                            "bookTitle" to arguments?.getString("bookTitle"),
                            "authors" to arguments?.getStringArrayList("bookAuthorsList")
                        )

                        bookRef.set(bookData, SetOptions.merge())
                        bookRef.collection("reviews").whereEqualTo("userId", uid).get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    bookRef.collection("reviews").add(reviewData)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Review saved successfully!", Toast.LENGTH_SHORT).show()
                                            incrementUserReviewNum(uid)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(activity, "Failed to save review", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    val existingReviewId = querySnapshot.documents[0].id
                                    bookRef.collection("reviews").document(existingReviewId)
                                        .set(reviewData)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Review updated successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(activity, "Failed to update review", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun deleteOldReview(userId: String, bookIsbn: String, onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val bookRef = db.collection("books").document(bookIsbn)

        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val existingReviewId = querySnapshot.documents[0].id
                    bookRef.collection("reviews").document(existingReviewId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Old review deleted", Toast.LENGTH_SHORT).show()
                            decrementUserReviewNum(userId)
                            onComplete()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to delete old review", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    onComplete()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to check existing reviews", Toast.LENGTH_SHORT).show()
            }
    }

    private fun incrementUserReviewNum(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("numReviews", FieldValue.increment(1))
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to update review count", Toast.LENGTH_SHORT).show()
            }
    }

    private fun decrementUserReviewNum(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("numReviews", FieldValue.increment(-1))
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to update review count", Toast.LENGTH_SHORT).show()
            }
    }
}
