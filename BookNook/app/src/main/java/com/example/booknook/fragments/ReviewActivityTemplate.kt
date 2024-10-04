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

// Yunjong Noh
// This fragment handles the function of writing and storing review data to Firebase (With Template Ver.)
class ReviewActivityTemplate : Fragment() {

    // Declare UI components for submitting reviews
    private lateinit var submitButton: Button
    private lateinit var reviewEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var removeTemplateButton: Button

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
            val bookRef = db.collection("books").document(bookIsbn)

            bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val reviewData = querySnapshot.documents[0].data

                        // Populate the fields with the fetched data
                        reviewEditText.setText(reviewData?.get("reviewText") as? String ?: "")
                        ratingBar.rating = (reviewData?.get("rating") as? Double)?.toFloat() ?: 0f

                        charactersCheckbox.isChecked = reviewData?.get("charactersChecked") as? Boolean ?: false
                        charactersRatingBar.rating = (reviewData?.get("charactersRating") as? Double)?.toFloat() ?: 0f
                        charactersReviewEditText.setText(reviewData?.get("charactersReview") as? String ?: "")

                        writingCheckbox.isChecked = reviewData?.get("writingChecked") as? Boolean ?: false
                        writingRatingBar.rating = (reviewData?.get("writingRating") as? Double)?.toFloat() ?: 0f
                        writingReviewEditText.setText(reviewData?.get("writingReview") as? String ?: "")

                        plotCheckbox.isChecked = reviewData?.get("plotChecked") as? Boolean ?: false
                        plotRatingBar.rating = (reviewData?.get("plotRating") as? Double)?.toFloat() ?: 0f
                        plotReviewEditText.setText(reviewData?.get("plotReview") as? String ?: "")

                        themesCheckbox.isChecked = reviewData?.get("themesChecked") as? Boolean ?: false
                        themesRatingBar.rating = (reviewData?.get("themesRating") as? Double)?.toFloat() ?: 0f
                        themesReviewEditText.setText(reviewData?.get("themesReview") as? String ?: "")

                        strengthsCheckbox.isChecked = reviewData?.get("strengthsChecked") as? Boolean ?: false
                        strengthsRatingBar.rating = (reviewData?.get("strengthsRating") as? Double)?.toFloat() ?: 0f
                        strengthsReviewEditText.setText(reviewData?.get("strengthsReview") as? String ?: "")

                        weaknessesCheckbox.isChecked = reviewData?.get("weaknessesChecked") as? Boolean ?: false
                        weaknessesRatingBar.rating = (reviewData?.get("weaknessesRating") as? Double)?.toFloat() ?: 0f
                        weaknessesReviewEditText.setText(reviewData?.get("weaknessesReview") as? String ?: "")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to load review data", Toast.LENGTH_SHORT).show()
                }
        }

        // Handle the submit button click to save the review
        submitButton.setOnClickListener {
            // Ensure focus is cleared from EditText fields before retrieving data
            view.clearFocus()

            // Capture the main review text and rating
            val reviewText = reviewEditText.text.toString()
            val rating = ratingBar.rating

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
                userId, bookIsbn, reviewText, rating,
                charactersChecked, charactersRating, charactersReview,
                writingChecked, writingRating, writingReview,
                plotChecked, plotRating, plotReview,
                themesChecked, themesRating, themesReview,
                strengthsChecked, strengthsRating, strengthsReview,
                weaknessesChecked, weaknessesRating, weaknessesReview
            )
        }

        //Declare button that connects to XML
        removeTemplateButton.setOnClickListener {

            // Handle requests button click
            val reviewActivityFragment = ReviewActivity()
            val bundle = Bundle() // Bundle to store data that will be transferred to the fragment
            // Adds data into the bundle
            bundle.putString("bookTitle", bookTitle)
            bundle.putString("bookAuthor", bookAuthor)
            bundle.putString("bookImage", bookImage)
            bundle.putFloat("bookRating", bookRating)
            bundle.putString("bookIsbn", bookIsbn)

            reviewActivityFragment.arguments = bundle  // sets reviewActivityFragment's arguments to the data in bundle
            (activity as? MainActivity)?.replaceFragment(reviewActivityFragment, "Write a Review") // Go back to No template fragment
        }

        return view
    }

    //function for saving with Template review Data
    //Define variable types
    private fun saveReview(
        userId: String?, bookIsbn: String?, reviewText: String, rating: Float,
        charactersChecked: Boolean, charactersRating: Float, charactersReview: String,
        writingChecked: Boolean, writingRating: Float, writingReview: String,
        plotChecked: Boolean, plotRating: Float, plotReview: String,
        themesChecked: Boolean, themesRating: Float, themesReview: String,
        strengthsChecked: Boolean, strengthsRating: Float, strengthsReview: String,
        weaknessesChecked: Boolean, weaknessesRating: Float, weaknessesReview: String
    ) {
        //Get the current user from Firebase Auth
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.displayName ?: "Anonymous"

        // Check both userId and bookIsbn are not null before proceeding
        if (userId != null && bookIsbn != null) {
            // initialize Firebase Instance
            val db = FirebaseFirestore.getInstance()
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
                "timestamp" to FieldValue.serverTimestamp()
            )

            // Reference to the specific book's document in the "books" collection
            val bookRef = db.collection("books").document(bookIsbn)

            // Check if the user has already submitted a review by querying reviews collection with the userId
            bookRef.collection("reviews").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // If no review exists for this user, add a new one
                        bookRef.collection("reviews").add(reviewData)
                            .addOnSuccessListener {
                                // Show success message and navigate back to the Home fragment
                                Toast.makeText(activity, "Review saved successfully!", Toast.LENGTH_SHORT).show()
                                // Optionally clear input fields here if needed before navigating back
                                (activity as? MainActivity)?.replaceFragment(HomeFragment(), "Home")
                            }
                            .addOnFailureListener { e ->
                                // If saving the review fails, display an error message
                                Toast.makeText(activity, "Failed to save review: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // If a review already exists, update it with the new data
                        val existingReviewId = querySnapshot.documents[0].id
                        bookRef.collection("reviews").document(existingReviewId).set(reviewData)
                            .addOnSuccessListener {
                                // Show success message for review update
                                Toast.makeText(activity, "Review updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                // If updating the review fails, display an error message
                                Toast.makeText(activity, "Failed to update review", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    // If querying for the existing review fails, display an error message
                    Toast.makeText(activity, "Failed to check existing reviews", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If userId or bookIsbn is null, display an error message
            Toast.makeText(activity, "Book ISBN or user not provided", Toast.LENGTH_SHORT).show()
        }
    }
}