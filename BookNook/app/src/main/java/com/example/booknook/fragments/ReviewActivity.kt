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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_write_review_no_template, container, false)

        // Retrieve views
        submitButton = view.findViewById(R.id.submitReviewButton)
        reviewEditText = view.findViewById(R.id.reviewInput)
        ratingBar = view.findViewById(R.id.myRatingBar)
        spoilerCheckbox = view.findViewById(R.id.spoilerCheckbox)
        sensitiveCheckbox = view.findViewById(R.id.sensitiveTopicsCheckbox)

        // Retrieves data from arguments passed in
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookImage = arguments?.getString("bookImage")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f

        // Retrieves Ids in the fragment
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)

        // Apply book data to views
        authorTextView.text = bookAuthor  // Update text with the book's author(s)
        bookRatingBar.rating = bookRating // Update stars with rating

        // Load the book's image
        if (bookImage != null) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(imageView)
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
        val db = FirebaseFirestore.getInstance()
        val review = hashMapOf(
            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
            "reviewText" to reviewText,
            "rating" to rating,
            "hasSpoilers" to hasSpoilers,
            "hasSensitiveTopics" to hasSensitiveTopics,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("reviews")
            .add(review)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Review saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error saving review: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToBookDetailsFragment() {
        // Assuming you're using MainActivity for fragment transactions
        val bookDetailsFragment = BookDetailsFragment()

        // If needed, pass data back to the BookDetailsFragment using a bundle
        val bundle = Bundle()
        bundle.putString("bookAuthor", arguments?.getString("bookAuthor"))
        bundle.putString("bookImage", arguments?.getString("bookImage"))
        bundle.putFloat("bookRating", arguments?.getFloat("bookRating") ?: 0f)
        bookDetailsFragment.arguments = bundle

        // Replace the current fragment with BookDetailsFragment
        (activity as MainActivity).replaceFragment(bookDetailsFragment, "Book Details")
    }
}