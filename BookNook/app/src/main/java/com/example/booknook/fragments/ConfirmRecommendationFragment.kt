package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R


class ConfirmRecommendationFragment : Fragment() {

    private lateinit var changeBookButton: Button
    private lateinit var confirmBookButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_confirm_recommendation, container, false)

        val bookImage = arguments?.getString("bookImage")
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")

        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val titleTextView: TextView = view.findViewById(R.id.bookTitleText)
        val authorsTextView: TextView = view.findViewById(R.id.bookAuthorsText)

        changeBookButton = view.findViewById(R.id.changeBookButton)
        confirmBookButton = view.findViewById(R.id.confirmBookButton)

        titleTextView.text = bookTitle
        authorsTextView.text = bookAuthor

        // Update the book's image
        if (bookImage != null) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(imageView)
        }

        // Handles click of "Change Book" button
        changeBookButton.setOnClickListener {
            // Takes user back to search page
            val searchBookRecommendationFragment = SearchBookRecommendationFragment()
            (activity as MainActivity).replaceFragment(searchBookRecommendationFragment, "Search")
        }

        // Handles click of "Confirm Book" button
        confirmBookButton.setOnClickListener {
            // Takes user back to recommendations page
            val groupRecommendationsFragment = GroupRecommendationsFragment()
            (activity as MainActivity).replaceFragment(groupRecommendationsFragment, "Search")


        }

        return view
    }

}