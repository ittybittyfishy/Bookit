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
import com.google.firebase.auth.FirebaseAuth

class BookDetailsFragment : Fragment() {

    private lateinit var writeReviewButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_details, container, false)


        // Retrieves data from arguments passed in
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookImage = arguments?.getString("bookImage")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f

        // Retrieves Ids in the fragment
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)

        authorTextView.text = bookAuthor  // Update text with the book's author(s)
        bookRatingBar.rating = bookRating // Update stars with rating

        // Update the book's image
        if (bookImage != null) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(imageView)
        }

        //Declare button that connects to XML,
        writeReviewButton = view.findViewById(R.id.write_review_button)

        writeReviewButton.setOnClickListener {
            // Handle requests button click
            val noTemplateFragment = ReviewActivity()
            (activity as MainActivity).replaceFragment(noTemplateFragment, "Write a Review")
        }


        return view
    }
}