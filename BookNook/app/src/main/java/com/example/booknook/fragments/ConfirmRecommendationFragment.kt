package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


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

        val groupId = arguments?.getString("groupId")
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
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            searchBookRecommendationFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(searchBookRecommendationFragment, "Search")
        }

        // Handles click of "Confirm Book" button
        confirmBookButton.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            // Book recommendation information
            val recommendation = hashMapOf(
                "image" to bookImage,
                "title" to bookTitle,
                "authors" to bookAuthor
            )

            if (groupId != null) {
                // Adds the book under recommendations subcollection under groups in database
                db.collection("groups").document(groupId)
                    .collection("recommendations")
                    .whereEqualTo("title", bookTitle)
                    .whereEqualTo("authors", bookAuthor)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        // If the recommendation hasn't been added previously
                        if (querySnapshot.isEmpty) {
                            // Adds the book recommendation
                            db.collection("groups").document(groupId)
                                .collection("recommendations")
                                .add(recommendation)
                                .addOnSuccessListener { documentReference ->
                                    val recommendationId = documentReference.id
                                    // Adds the recommendationId as a field
                                    documentReference.update("recommendationId", recommendationId)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Added book to recommendations", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error adding recommendation", e)
                                }
                        // Tells user that the book has already been recommended
                        } else {
                            Toast.makeText(activity, "This book has already been recommended", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        return view
    }

}