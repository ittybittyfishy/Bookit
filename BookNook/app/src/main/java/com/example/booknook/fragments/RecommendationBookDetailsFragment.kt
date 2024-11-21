package com.example.booknook.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.BookItem
import com.example.booknook.Comment
import com.example.booknook.CommentsAdapter
import com.example.booknook.ImageLinks
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.RecommendationAdapterBookDetails
import com.example.booknook.Review
import com.example.booknook.ReviewsAdapter
import com.example.booknook.TemplateReview
import com.example.booknook.VolumeInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.Locale

class RecommendationBookDetailsFragment : Fragment() {

    private lateinit var bookImage: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var bookAuthor: TextView
    private lateinit var bookDescription: TextView
    private lateinit var readMoreButton: Button
    private lateinit var bookRatingBar: RatingBar
    private lateinit var ratingNumber: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_recommendation_book_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        bookImage = view.findViewById(R.id.bookImage)
        bookTitle = view.findViewById(R.id.bookTitle)
        bookAuthor = view.findViewById(R.id.bookAuthor)
        bookDescription = view.findViewById(R.id.bookDescription)
        readMoreButton = view.findViewById(R.id.readMoreButton)
        bookRatingBar = view.findViewById(R.id.bookRating)
        ratingNumber = view.findViewById(R.id.ratingNumber)

        // Retrieve data from the bundle
        val bundle = arguments
        val imageUrl = bundle?.getString("bookImage")
        val title = bundle?.getString("bookTitle")
        val author = bundle?.getString("bookAuthor")
        val description = bundle?.getString("bookDescription")
        val bookAvgRating = bundle?.getFloat("bookRating") ?: 0f

        // Populate views
        Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.placeholder_image).into(bookImage)
        bookTitle.text = title ?: "Unknown Title"
        bookAuthor.text = author ?: "Unknown Author"
        bookDescription.text = description ?: "No description available"
        bookRatingBar.rating = bookAvgRating // Update stars with rating
        ratingNumber.text = "(${bookAvgRating.toString()})" // Update the book rating

        // Set up "Read More" button for long descriptions
        readMoreButton.setOnClickListener {
            bookDescription.maxLines = if (bookDescription.maxLines == 6) Int.MAX_VALUE else 6
            readMoreButton.text = if (bookDescription.maxLines == 6) "Read More" else "Show Less"
        }
    }
}