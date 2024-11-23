package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.firestore.FirebaseFirestore

class ConfirmRecommendationBookDetailsFragment : Fragment() {

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

        // Get arguments passed through a bundle
        val isbn = arguments?.getString("isbn")
        val recIsbn = arguments?.getString("recIsbn")
        val bookImage = arguments?.getString("bookImage")
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookAuthorList= arguments?.getStringArrayList("bookAuthorsList")
        val bookdesc = arguments?.getString("bookDescription")
        val bookGenres = arguments?.getStringArrayList("bookGenres")
        val bookAvgRating = arguments?.getFloat("bookRating")

        // initialize XML elements
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val titleTextView: TextView = view.findViewById(R.id.bookTitleText)
        val authorsTextView: TextView = view.findViewById(R.id.bookAuthorsText)

        // initialize buttons
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
            val SearchBookRecommendationBookDetailsFragment = SearchBookRecommendationBookDetailsFragment()
            val bundle = Bundle()
            bundle.putString("isbn", isbn) // Passing the ISBN to the next fragment.
            SearchBookRecommendationBookDetailsFragment.arguments = bundle
            // Replacing the current fragment with the search fragment allowing the next page to use a back button
            (activity as MainActivity).replaceFragment(SearchBookRecommendationBookDetailsFragment, "Recommendation Search", showBackButton = false)
        }

        // Handles click of "Confirm Book" button
        confirmBookButton.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            if (isbn != null) {
                Log.d("ConfirmRecommendation", "Searching for ISBN: $isbn")

                // Reference to the book document
                val bookRef = db.collection("books").document(isbn)

                // Check if the book exists in the database
                bookRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Reference to the recommendations collection
                        val recommendationsRef = bookRef.collection("recommendations")

                        // Query to check if the recommendation already exists
                        recommendationsRef
                            .whereEqualTo("title", bookTitle)
                            .whereEqualTo("authors", bookAuthor)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    // Recommendation does not exist; add new recommendation
                                    val newRecommendation = hashMapOf(
                                        "recIsbn" to recIsbn,
                                        "image" to bookImage,
                                        "title" to bookTitle,
                                        "authors" to bookAuthor,
                                        "authorsList" to bookAuthorList,
                                        "description" to bookdesc,
                                        "genres" to bookGenres,
                                        "bookRating" to bookAvgRating,
                                        "numUpvotes" to 1 // Initialize upvotes to 1
                                    )
                                    recommendationsRef.add(newRecommendation)
                                        .addOnSuccessListener { documentReference ->
                                            documentReference.update("recommendationId", documentReference.id)
                                            Log.d("Firestore", "Recommendation added with ID: ${documentReference.id}")
                                            Toast.makeText(activity, "Added book to recommendations", Toast.LENGTH_SHORT).show()
                                            val SearchFragment = SearchFragment()
                                            (activity as MainActivity).replaceFragment(SearchFragment, "Search")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("Firestore", "Error adding recommendation", e)
                                        }
                                } else {
                                    // Recommendation exists; notify the user
                                    Toast.makeText(activity, "This book has already been recommended", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error checking recommendation", e)
                            }
                    } else {
                        Log.w("Firestore", "Book with ISBN $isbn does not exist")
                        Toast.makeText(activity, "Book does not exist in database", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }

}