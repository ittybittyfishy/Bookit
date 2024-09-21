package com.example.booknook.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.BookItem
import com.example.booknook.R.*
import com.google.firebase.auth.FirebaseAuth

// Veronica Nguyen
class BookDetailsFragment : Fragment() {

    private lateinit var writeReviewButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(layout.fragment_book_details, container, false)

        // Retrieves data from arguments passed in from the search fragment
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookImage = arguments?.getString("bookImage")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val isbn = arguments?.getString("bookIsbn")

        // Retrieves Ids in the fragment
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)

        authorTextView.text = bookAuthor  // Update text with the book's author(s)
        bookRatingBar.rating = bookRating // Update stars with rating
        ratingNumberTextView.text = "(${bookRating.toString()})" // Set the rating number text

        // Update the book's image
        if (bookImage != null) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(drawable.placeholder_image)
                .error(drawable.placeholder_image)
                .into(imageView)
        }

        // Yunjong Noh
        //Declare button that connects to XML
        writeReviewButton = view.findViewById(R.id.write_review_button)

        writeReviewButton.setOnClickListener {

            // Handle requests button click
            val reviewActivityFragment = ReviewActivity()
            val bundle = Bundle() // Bundle to store data that will be transferred to the fragment
            // Adds data into the bundle
            bundle.putString("bookTitle", bookTitle)
            bundle.putString("bookAuthor", bookAuthor)
            bundle.putString("bookImage", bookImage)
            bundle.putFloat("bookRating", bookRating)
            bundle.putString("bookIsbn", isbn)

            reviewActivityFragment.arguments = bundle  // sets reviewActivityFragment's arguments to the data in bundle
            (activity as MainActivity).replaceFragment(reviewActivityFragment, "Write a Review")  // Opens a new fragment
        }

        return view
    }
}