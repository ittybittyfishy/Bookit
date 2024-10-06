package com.example.booknook.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.BookItem
import com.example.booknook.R.*
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale


// Veronica Nguyen
class BookDetailsFragment : Fragment() {
    private lateinit var editButton: ImageButton
    private lateinit var personalSummary: EditText
    private lateinit var writeReviewButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveChangesButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(layout.fragment_book_details, container, false)

        // Retrieves data from arguments passed in from the search fragment
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookAuthorsList = arguments?.getStringArrayList("bookAuthorsList")
        val bookImage = arguments?.getString("bookImage")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val isbn = arguments?.getString("bookIsbn")
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user ID

        // Retrieves Ids in the fragment
        val titleTextView: TextView = view.findViewById(R.id.bookTitle)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)

        // Calls views
        editButton = view.findViewById(R.id.edit_summary_button)
        personalSummary = view.findViewById(R.id.personal_summary)
        cancelButton = view.findViewById(R.id.cancel_button)
        saveChangesButton = view.findViewById(R.id.save_changes_button)
        writeReviewButton = view.findViewById(R.id.write_review_button)

        titleTextView.text = bookTitle
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

        // Handles click of edit personal summary button
        editButton.setOnClickListener {
            // Allows user to now type in box
            personalSummary.isFocusable = true
            personalSummary.isFocusableInTouchMode = true
            personalSummary.requestFocus()
            // Makes the keyboard pop up
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(personalSummary, InputMethodManager.SHOW_IMPLICIT)

            // Makes cancel and save changes button visible
            cancelButton.visibility = View.VISIBLE
            saveChangesButton.visibility = View.VISIBLE
        }

        // Handles click of cancel button
        cancelButton.setOnClickListener {
            personalSummary.isFocusable = false
            personalSummary.isFocusableInTouchMode = false
            cancelButton.visibility = View.GONE
            saveChangesButton.visibility = View.GONE
        }

        // Handles click of save changes button
        saveChangesButton.setOnClickListener {
            val summaryText = personalSummary.text.toString()
            saveSummary(summaryText)

            // Hide the buttons
            cancelButton.visibility = View.GONE
            saveChangesButton.visibility = View.GONE
        }

        // Fetch existing summary if the user has already submitted one for this book
        if (userId != null && isbn != null) {
            val db = FirebaseFirestore.getInstance()
            val bookRef = db.collection("books").document(isbn)

            // Checks if the user already submitted a summary for this book
            bookRef.collection("summaries").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Loads in the summary data if a summary is found
                        val existingSummary = querySnapshot.documents[0].data
                        personalSummary.setText(existingSummary?.get("summaryText") as? String ?: "")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to retrieve existing summary", Toast.LENGTH_SHORT).show()
                }
        }

        // Yunjong Noh
        //Declare button that connects to XML
        writeReviewButton.setOnClickListener {

            // Handle requests button click
            val reviewActivityFragment = ReviewActivity()
            val bundle = Bundle() // Bundle to store data that will be transferred to the fragment
            // Adds data into the bundle
            bundle.putString("bookTitle", bookTitle)
            bundle.putString("bookAuthor", bookAuthor)
            bundle.putStringArrayList("bookAuthorsList", bookAuthorsList)
            bundle.putString("bookImage", bookImage)
            bundle.putFloat("bookRating", bookRating)
            bundle.putString("bookIsbn", isbn)

            reviewActivityFragment.arguments = bundle  // sets reviewActivityFragment's arguments to the data in bundle
            (activity as MainActivity).replaceFragment(reviewActivityFragment, "Write a Review")  // Opens a new fragment
        }

        return view
    }

    // Function to save the personal summary into databases
    private fun saveSummary(summaryText: String) {
        val user = FirebaseAuth.getInstance().currentUser // Gets current user
        val userId = user?.uid // Gets user id

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            var bookIsbn = arguments?.getString("bookIsbn") // Retrieve the book's ISBN from arguments
            val bookTitle = arguments?.getString("bookTitle")
            val bookAuthors = arguments?.getStringArrayList("bookAuthorsList")

            // If the book has no ISBN, create a unique document ID using the title and authors of the book
            if (bookIsbn.isNullOrEmpty() || bookIsbn == "No ISBN") {
                // Creates title part by replacing all whitespaces with underscores, and making it lowercase
                val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT) ?: "unknown_title"
                // Creates authors part by combining authors, replacing all whitespaces with underscores, and making it lowercase
                val authorsId = bookAuthors?.joinToString("_")?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
                bookIsbn = "$titleId-$authorsId" // Update bookIsbn with new Id
            }

            // Reference to the specific book's document
            val bookRef = db.collection("books").document(bookIsbn)

            // Get the user's username from database
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") // Get username if exists

                    // Create a map for the summary data
                    val summaryData = mapOf(
                        "userId" to userId,
                        "username" to username,
                        "summaryText" to summaryText,
                        "timestamp" to FieldValue.serverTimestamp() // Use Firestore timestamp
                    )

                    // Map to store book data
                    val bookData = mapOf(
                        "bookTitle" to bookTitle,
                        "authors" to bookAuthors
                    )

                    bookRef.set(bookData, SetOptions.merge())  // Updates database with book details if not in database already

                    // Check if the user has already submitted a summary
                    bookRef.collection("summaries").whereEqualTo("userId", userId).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.isEmpty) {
                                // Add a new summary if one doesn't exist
                                bookRef.collection("summaries").add(summaryData)
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, "Summary saved successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(activity, "Failed to save summary", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Updates the existing summary
                                val existingSummaryId = querySnapshot.documents[0].id
                                bookRef.collection("summaries").document(existingSummaryId)
                                    .set(summaryData) // Update summary data
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, "Summary updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(activity, "Failed to update summary", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to check existing summaries", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Doesn't allow user to click on box after saving changes
        personalSummary.isFocusable = false
        personalSummary.isFocusableInTouchMode = false
    }

}