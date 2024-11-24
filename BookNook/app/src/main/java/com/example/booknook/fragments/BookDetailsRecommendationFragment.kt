package com.example.booknook.fragments

import android.app.AlertDialog
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.Comment
import com.example.booknook.CommentsAdapter
import com.example.booknook.ImageLinks
import com.example.booknook.IndustryIdentifier
import com.example.booknook.Reply
import com.example.booknook.Review
import com.example.booknook.ReviewsAdapter
import com.example.booknook.TemplateReview
import com.example.booknook.VolumeInfo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale


// Veronica Nguyen
class BookDetailsRecommendationFragment : Fragment() {
    private lateinit var personalSummary: EditText
    private lateinit var selectBookButton: Button
    private lateinit var readMoreButton: Button
    private var isDescriptionExpanded = false  // defaults the description to not be expanded
    // List of predefined collections that users can assign books to.
    private val standardCollections = listOf("Select Collection", "Reading", "Finished", "Want to Read", "Dropped", "Remove")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(layout.fragment_book_details_recommendation, container, false)

        // Retrieves data from arguments passed in from the search fragment
        val groupId = arguments?.getString("groupId")
        val bookTitle = arguments?.getString("bookTitle")
        val bookAuthor = arguments?.getString("bookAuthor")
        val bookAuthorsList = arguments?.getStringArrayList("bookAuthorsList")
        val bookImage = arguments?.getString("bookImage")
        val bookRating = arguments?.getFloat("bookRating") ?: 0f
        val isbn = arguments?.getString("bookIsbn")
        val bookDescription = arguments?.getString("bookDescription")
        val bookGenres = arguments?.getStringArrayList("bookGenres")
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user ID

        // Retrieves Ids in the fragment
        val titleTextView: TextView = view.findViewById(R.id.bookTitle)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)
        val descriptionTextView: TextView = view.findViewById(R.id.bookDescription)

        // Calls views
        personalSummary = view.findViewById(R.id.personal_summary)
        selectBookButton = view.findViewById(R.id.selectBookButton)
        readMoreButton = view.findViewById(R.id.readMoreButton)

        titleTextView.text = bookTitle
        authorTextView.text = bookAuthor  // Update text with the book's author(s)
        bookRatingBar.rating = bookRating // Update stars with rating
        ratingNumberTextView.text = "(${bookRating.toString()})" // Set the rating number text
        descriptionTextView.text = bookDescription

        // Update the book's image
        if (bookImage != null) {
            Glide.with(this)
                .load(bookImage)
                .placeholder(drawable.placeholder_image)
                .error(drawable.placeholder_image)
                .into(imageView)
        }

        // Create VolumeInfo object from the data
        val volumeInfo = bookTitle?.let {
            VolumeInfo(
                title = it,
                authors = bookAuthorsList,
                categories = bookGenres,
                imageLinks = bookImage?.let { ImageLinks(it) }
            )
        }

        // Create a BookItem object
        val bookId = arguments?.getString("bookId") ?: "Unknown ID" // You can adjust this based on your data source
        val book = volumeInfo?.let { BookItem(id = bookId, volumeInfo = it) }


        // Display the "Read more" button for the book description if it's too long
        if (descriptionTextView.maxLines == 6) {
            readMoreButton.visibility = View.VISIBLE
        } else {
            readMoreButton.visibility = View.GONE
        }

        // Veronica Nguyen
        // Handles click of "Select Book" button for recommendation
        selectBookButton.setOnClickListener {
            // Takes user to the confirm page to confirm their book for recommendation
            val confirmRecommendationFragment = ConfirmRecommendationFragment()
            val bundle = Bundle().apply {
                putString("groupId", groupId)
                putString("bookImage", volumeInfo?.imageLinks?.thumbnail?.replace("http://", "https://"))
                putString("bookTitle", volumeInfo?.title)
                putString("bookAuthor", volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author")
                putFloat("bookRating", volumeInfo?.averageRating ?: 0f)
            }
            confirmRecommendationFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(confirmRecommendationFragment, "Add Recommendation")
        }

        // Handles click of the read more button
        readMoreButton.setOnClickListener {
            // If the description isn't expanded
            if (isDescriptionExpanded) {
                descriptionTextView.maxLines = 6  // Show only 6 lines of the description
                descriptionTextView.ellipsize = TextUtils.TruncateAt.END  // Truncates the end and adds "..."
                readMoreButton.text = "Read more"  // Button displays as "Read more"
            } else {
                descriptionTextView.maxLines = Int.MAX_VALUE  // Expands the whole description
                descriptionTextView.ellipsize = null  // Removes ellipses
                readMoreButton.text = "Read less"  // Button displays as "Read less"
            }
            isDescriptionExpanded = !isDescriptionExpanded  // Switches state of variable after it's been clicked
        }

        // Fetch existing summary if the user has already submitted one for this book
        if (userId != null && isbn != null) {
            val db = FirebaseFirestore.getInstance()

            var bookIsbn = isbn
            // If the book has no ISBN, create a unique document ID using the title and authors of the book
            if (bookIsbn.isNullOrEmpty() || bookIsbn == "No ISBN") {
                // Creates title part by replacing all whitespaces with underscores, and making it lowercase
                val titleId = bookTitle?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT) ?: "unknown_title"
                // Creates authors part by combining authors, replacing all whitespaces with underscores, and making it lowercase
                val authorsId = bookAuthorsList?.joinToString("_")?.replace("\\s+".toRegex(), "_")?.lowercase(Locale.ROOT)
                bookIsbn = "$titleId-$authorsId" // Update bookIsbn with new Id
            }

            val bookRef = db.collection("books").document(bookIsbn)

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


        //Yunjong Noh
        // Check if the ISBN is not null("?" statement) and then fetch reviews
        isbn?.let {
            fetchReviews(it)  // Call the fetchReviews method and pass the ISBN
        }

        return view
    }

    // Yunjong Noh
    // Function to fetch reviews for a specific book from Firestore using its ISBN
    private fun fetchReviews(isbn: String) {
        // Reference to the reviews collection of the specified book in Firestore
        val reviewsRef = FirebaseFirestore.getInstance()
            .collection("books")
            .document(isbn)
            .collection("reviews")

        // Fetch all reviews for the specified book
        reviewsRef.get()
            .addOnSuccessListener { documents ->
                val reviewsList = mutableListOf<Any>()
                for (document in documents) {
                    val isTemplateUsed = document.getBoolean("isTemplateUsed") ?: false
                    if (isTemplateUsed) {
                        // Convert the document to a TemplateReview object and copy relevant fields
                        val templateReview = document.toObject(TemplateReview::class.java).copy(
                            reviewId = document.id, // Set the review ID
                            isbn = isbn // Set the ISBN
                        )
                        reviewsList.add(templateReview) // Add the template review to the list
                        Log.d("fetchReviews", "Fetched TemplateReview - ISBN: $isbn, ReviewID: ${document.id}") // Log the fetched template review
                        // Fetch comments for the template review
                        fetchComments(isbn, document.id)
                    } else {
                        val review = document.toObject(Review::class.java).copy(
                            reviewId = document.id,
                            isbn = isbn
                        )
                        reviewsList.add(review)
                        Log.d("fetchReviews", "Fetched Review - ISBN: $isbn, ReviewID: ${document.id}") // Log the fetched regular review
                        // Fetch comments for the regular review
                        fetchComments(isbn, document.id)
                    }
                }
                setupRecyclerView(reviewsList) // Set up the RecyclerView with the fetched reviews
            }
            .addOnFailureListener { exception ->
                Log.e("BookDetailsFragment", "Error fetching reviews", exception)
            }
    }

    // Yunjong Noh
    // Function to fetch comments for a specific review from Firestore
    private fun fetchComments(isbn: String, reviewId: String) {
        // Reference to the comments collection for the specified review in Firestore
        val commentsRef = FirebaseFirestore.getInstance()
            .collection("books")
            .document(isbn)
            .collection("reviews")
            .document(reviewId)
            .collection("comments")

        // Fetch all comments for the specified review
        commentsRef.get()
            .addOnSuccessListener { documents ->
                val commentsList = mutableListOf<Comment>()
                for (document in documents) { // Iterate through each comment document
                    // Convert the document to a Comment object and add the comment ID
                    val comment = document.toObject(Comment::class.java).apply {
                        commentId = document.id // Add the comment ID
                    }
                    commentsList.add(comment) // Add the comment to the list
                }
                // Update the UI with the comments list
                setupCommentsRecyclerView(commentsList) // Set up the RecyclerView for comments
            }
            .addOnFailureListener { exception ->
                Log.e("fetchComments", "Error fetching comments", exception)
            }
    }

    // Yunjong Noh
    // Function to set up the RecyclerView and bind it with the fetched reviews
    private fun setupRecyclerView(reviews: List<Any>) {
        // Find the RecyclerView UI element in the layout
        val recyclerView = view?.findViewById<RecyclerView>(R.id.reviewsRecyclerView)
        // Set up the RecyclerView to use a vertical LinearLayoutManager
        recyclerView?.layoutManager = LinearLayoutManager(context)
        // Set the adapter to show fetched reviews in the RecyclerView
        recyclerView?.adapter = ReviewsAdapter(reviews) // Bind the reviews to the adapter
    }

    // Yunjong Noh
    // Function to set up the RecyclerView for comments
    private fun setupCommentsRecyclerView(comments: List<Comment>) {
        // Find the RecyclerView UI element for comments
        val recyclerView = view?.findViewById<RecyclerView>(R.id.commentsRecyclerView)
        // Create an adapter for the comments
        val commentsAdapter = CommentsAdapter(comments)
        // Set the layout manager for the comments RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(context)
        // Bind the comments to the adapter
        recyclerView?.adapter = commentsAdapter
    }

}