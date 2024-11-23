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

    // Declare UI elements for book details
    private lateinit var bookImage: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var bookAuthor: TextView
    private lateinit var bookDescription: TextView
    private lateinit var readMoreButton: Button
    private lateinit var bookRatingBar: RatingBar
    private lateinit var ratingNumber: TextView
    // Predefined collections for user organization of books
    private val standardCollections = listOf("Select Collection", "Reading", "Finished", "Want to Read", "Dropped", "Remove")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_recommendation_book_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views to corresponding layout element
        bookImage = view.findViewById(R.id.bookImage)
        bookTitle = view.findViewById(R.id.bookTitle)
        bookAuthor = view.findViewById(R.id.bookAuthor)
        bookDescription = view.findViewById(R.id.bookDescription)
        readMoreButton = view.findViewById(R.id.readMoreButton)
        bookRatingBar = view.findViewById(R.id.bookRating)
        ratingNumber = view.findViewById(R.id.ratingNumber)

        val wantToReadButton: Button = view.findViewById(R.id.wantToRead)

        // Retrieve book details passed to the fragment through a bundl
        val bundle = arguments
        val imageUrl = bundle?.getString("bookImage")
        val title = bundle?.getString("bookTitle")
        val author = bundle?.getString("bookAuthor")
        val description = bundle?.getString("bookDescription")
        val bookAvgRating = bundle?.getFloat("bookRating") ?: 0f
        val bookGenres = bundle?.getStringArrayList("bookGenres")
        val bookAuthorsList = bundle?.getStringArrayList("bookAuthorsList")
        val bookIsbn = bundle?.getString("bookIsbn")

        // Populate UI elements with book data
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

        // Handle "Want to Read" button click to save the book in the user's collection
        wantToReadButton.setOnClickListener {
            // Call the saveBookToCollection method to save the book to "Want to Read"
            saveBookToCollection(
                context = requireContext(),
                title = title ?: "Unknown Title",
                authors = author ?: "Unknown Author",
                bookImage = imageUrl,
                newCollectionName = "Want to Read", // This is the collection name
                genres = bookGenres,
                description = description,
                rating = bookAvgRating,
                isbn = bookIsbn,
                bookAuthorsList = bookAuthorsList ?: listOf()
            )
        }
    }

    // Saves a book to a specified collection for the user
    private fun saveBookToCollection(
        context: Context,
        title: String,
        authors: String,
        bookImage: String?,
        newCollectionName: String,
        genres: List<String>?,
        description: String?,
        rating: Float?,
        isbn: String?,
        bookAuthorsList: List<String>?
    ) {
        // Get the current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID
        if (userId != null) {
            val db = FirebaseFirestore.getInstance() // Reference to Firestore

            // Create a map of the book's details to be saved.
            val book = hashMapOf(
                "title" to title,
                "authors" to bookAuthorsList,
                "authorsList" to bookAuthorsList,
                "imageLink" to bookImage,
                "genres" to genres,
                "description" to description,
                "rating" to rating,
                "isbn" to isbn
            )

            val userDocRef = db.collection("users").document(userId)

            // Firestore transaction to move the book between collections
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Remove the book from old collections if it exists
                for (collection in standardCollections) {
                    if (collection != "Select Collection" && collection != newCollectionName) {
                        val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                        booksInCollection?.let {
                            for (existingBook in it) {
                                if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                    transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))

                                    // Decrement numBooksRead if the book was in the "Finished" collection
                                    if (collection == "Finished") {
                                        transaction.update(userDocRef, "numBooksRead", FieldValue.increment(-1))
                                    }
                                    break
                                }
                            }
                        }
                    }
                }

                // Add the book to the new collection
                transaction.update(userDocRef, "standardCollections.$newCollectionName", FieldValue.arrayUnion(book))
            }.addOnSuccessListener {
                Toast.makeText(context, context.getString(R.string.book_added_to_collection, newCollectionName), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, context.getString(R.string.failed_to_add_book, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}