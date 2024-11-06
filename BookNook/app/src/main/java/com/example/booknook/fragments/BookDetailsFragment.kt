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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.Comment
import com.example.booknook.CommentsAdapter
import com.example.booknook.ImageLinks
import com.example.booknook.IndustryIdentifier
import com.example.booknook.RecommendationAdapterBookDetails
import com.example.booknook.RecommendationsAdapter
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
class BookDetailsFragment : Fragment() {
    private lateinit var editButton: ImageButton
    private lateinit var personalSummary: EditText
    private lateinit var writeReviewButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var readMoreButton: Button
    private var isDescriptionExpanded = false  // defaults the description to not be expanded
    // List of predefined collections that users can assign books to.
    private val standardCollections = listOf("Select Collection", "Reading", "Finished", "Want to Read", "Dropped", "Remove")
    private val recommendationsList = mutableListOf<Map<String, Any>>() // Initialize an empty list to store recommendation
    private lateinit var recommendationsAdapter: RecommendationAdapterBookDetails

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
        val bookDescription = arguments?.getString("bookDescription")
        val bookGenres = arguments?.getStringArrayList("bookGenres")
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Current logged-in user ID

        // Retrieves Ids in the fragment
        val titleTextView: TextView = view.findViewById(R.id.bookTitle)
        val authorTextView: TextView = view.findViewById(R.id.bookAuthor)
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val bookRatingBar: RatingBar = view.findViewById(R.id.bookRating)
        val ratingNumberTextView: TextView = view.findViewById(R.id.ratingNumber)
        val spinnerSelectCollection: Spinner = view.findViewById(R.id.spinnerSelectCollection)
        val btnAddToCustomCollection: Button = view.findViewById(R.id.btnAddToCustomCollection)
        val descriptionTextView: TextView = view.findViewById(R.id.bookDescription)
        val genreHolder: LinearLayout = view.findViewById(R.id.tagContainer)
        val readingStatus: TextView = view.findViewById(R.id.readingStatus)
        val personalRating: RatingBar = view.findViewById(R.id.personalBookRating)
        val personalRatingNum: TextView = view.findViewById(R.id.personalRatingNumber)
        val addRec: ImageButton = view.findViewById(R.id.addRecommendationButton)
        val recHolder: RecyclerView = view.findViewById(R.id.recommendationsRecyclerView)

        // Initialize RecyclerView for recommendations
        recHolder.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Set up RecommendationsAdapter
        recommendationsAdapter = RecommendationAdapterBookDetails(
            recommendationsList,
            isbn ?: "",  // Provide empty string if isbn is null
            userId ?: "" // Provide empty string if userId is null
        )
        recHolder.adapter = recommendationsAdapter

        // Fetch recommendations
        if (isbn != null) {
            fetchRecommendations(isbn)
        }

        // Opens page to add recommendations when button is pressed
        addRec.setOnClickListener {
            val AddRecommendationBookDetailsFragment = AddRecommendationBookDetailsFragment()
            val bundle = Bundle()
            bundle.putString("isbn", isbn)
            AddRecommendationBookDetailsFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(AddRecommendationBookDetailsFragment, "Add Recommendation", showBackButton = true)
        }

        // Calls views
        editButton = view.findViewById(R.id.edit_summary_button)
        personalSummary = view.findViewById(R.id.personal_summary)
        cancelButton = view.findViewById(R.id.cancel_button)
        saveChangesButton = view.findViewById(R.id.save_changes_button)
        writeReviewButton = view.findViewById(R.id.write_review_button)
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

        // Set up the ArrayAdapter for the spinner
        val collectionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            standardCollections
        )
        collectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSelectCollection.adapter = collectionAdapter

        // Handle spinner item selection
        spinnerSelectCollection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 5) {
                    if (bookTitle != null) {
                        if (bookAuthor != null) {
                            removeBookFromStandardCollection(requireContext(), bookTitle, bookAuthor)
                        }
                    }
                } else if (position != 0) {
                    val selectedCollection = standardCollections[position]
                    if (bookTitle != null) {
                        if (bookAuthor != null) {
                            if (bookGenres != null) {
                                saveBookToCollection(
                                    requireContext(),
                                    bookTitle,
                                    bookAuthor,
                                    bookImage,
                                    selectedCollection,
                                    bookGenres
                                )
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when nothing is selected if necessary
            }
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

        // Olivia Fishbough
        // Get standard collection that book is in, if any
        if (bookTitle != null && bookAuthorsList != null) {
            findBookCollection(requireContext(), bookTitle, bookAuthorsList.joinToString(", ")) { collection ->
                if (collection != null) {
                    // Set text in reading status to collection name
                    readingStatus.text = "$collection"
                    readingStatus.visibility = View.VISIBLE
                } else {
                    // If the book is not in a collection just say N/A
                    readingStatus.text = "N/A"
                    readingStatus.visibility = View.VISIBLE
                }
            }
        }

        // Olivia Fishbough
        // Load in Book Genres
        if (!bookGenres.isNullOrEmpty()) {
            for (genre in bookGenres) {
                // Create a new TextView for each genre
                val genreTextView = TextView(requireContext()).apply {
                    text = genre
                    setBackgroundResource(R.drawable.friend_username_border) // Set the background drawable
                    setPadding(10, 10, 10, 10) //
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    textSize = 14f
                }

                // Add the genreTextView to the genreHolder LinearLayout
                genreHolder.addView(genreTextView)
            }
        }


            // Create a BookItem object
        val bookId = arguments?.getString("bookId") ?: "Unknown ID" // You can adjust this based on your data source
        val book = volumeInfo?.let { BookItem(id = bookId, volumeInfo = it) }

        // Handle the "Add to Custom Collection" button click event.
        btnAddToCustomCollection.setOnClickListener {
            if (book != null) {
                showCustomCollectionDialog(requireContext(), book)
            } // Show a dialog to select custom collection
        }

        // Display the "Read more" button for the book description if it's too long
        if (descriptionTextView.maxLines == 6) {
            readMoreButton.visibility = View.VISIBLE
        } else {
            readMoreButton.visibility = View.GONE
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

        // Load in users rating for book if it exists
        if (isbn != null) {
            loadUserRatingForBook(isbn, personalRating, personalRatingNum)
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
            bundle.putStringArrayList("bookGenresList", bookGenres) // Add Genre list

            reviewActivityFragment.arguments = bundle  // sets reviewActivityFragment's arguments to the data in bundle
            (activity as MainActivity).replaceFragment(reviewActivityFragment, "Write a Review", showBackButton = true)  // Opens a new fragment
        }
        //Yunjong Noh
        // Check if the ISBN is not null("?" statement) and then fetch reviews
        isbn?.let {
            fetchReviews(it)  // Call the fetchReviews method and pass the ISBN
        }

        return view
    }

    // Olivia Fishbough
    // Function to Load recommendations
    private fun fetchRecommendations(bookId: String) {
        val db = FirebaseFirestore.getInstance()
        val recommendationsRef = db.collection("books").document(bookId).collection("recommendations")

        recommendationsRef.get()
            .addOnSuccessListener { documents ->
                recommendationsList.clear() // Clear old data before adding new
                for (document in documents) {
                    val recommendationData = document.data
                    recommendationsList.add(recommendationData)
                }
                recommendationsAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                Log.e("BookDetailsFragment", "Error fetching recommendations: ${exception.message}")
            }
    }

    // Olivia Fishbough
    // Function to retrieve users rating for a book
    private fun loadUserRatingForBook(bookId: String, ratingBar: RatingBar, ratingTextView: TextView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val bookRef = db.collection("books").document(bookId)

        // Retrieve the list of reviews for this book
        bookRef.collection("reviews").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userReview = querySnapshot.documents[0]
                    val userRating = userReview.getDouble("rating") ?: 0.0

                    // Set the retrieved rating to the RatingBar and TextView
                    ratingBar.rating = userRating.toFloat()
                    ratingTextView.text = "($userRating)"
                } else {
                    // If no rating is found, set default display
                    ratingBar.rating = 0f
                    ratingTextView.text = "(N/A)"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load rating: ${e.message}", Toast.LENGTH_SHORT).show()
                ratingBar.rating = 0f
                ratingTextView.text = "(N/A)"
            }
    }

    // Olivia Fishbough
    // Finds the book if it is saved in a collection
    private fun findBookCollection(
        context: Context,
        title: String,
        authors: String,
        callback: (String?) -> Unit // Callback to return the collection name or null if not found
    ) {
        // Get the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Check if user is logged in
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Begin a Firestore transaction to access user data
            db.runTransaction { transaction ->
                // Get a snapshot of the user's document
                val snapshot = transaction.get(userDocRef)

                // Loop through standard collections to find the book
                for (collection in standardCollections.drop(1).take(4)) { // Skip "Select Collection" and "Remove"
                    // Get the list of books in the current collection, if any
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                    booksInCollection?.let {
                        // Check each book in the collection to see if it matches the title and authors provided
                        for (existingBook in it) {
                            if (existingBook["title"] == title &&
                                existingBook["authors"] == authors.split(", ").map { it.trim() }
                            ) {
                                // If a match is found, return the collection name
                                return@runTransaction collection // Return collection name
                            }
                        }
                    }
                }
                null // Return null if book is not in any collection
            }.addOnSuccessListener { collection ->
                // Pass the collection name to the callback if found, otherwise pass null
                callback(collection as? String)
            }.addOnFailureListener { e ->
                // Display an error message if the transaction fails
                Toast.makeText(context, "Failed to find book collection: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        } else {
            callback(null) // Return null if user is not logged in
        }
    }


    // Method to save the book to a selected collection in Firestore.
    private fun saveBookToCollection(
        context: Context,
        title: String,
        authors: String,
        bookImage: String?,
        newCollectionName: String,
        genres: List<String>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID
        if (userId != null) {
            val db = FirebaseFirestore.getInstance() // Reference to Firestore

            // Create a map of the book's details to be saved.
            val book = hashMapOf(
                "title" to title,
                "authors" to authors.split(", "),
                "imageLink" to bookImage,
                "genres" to genres
            )

            val userDocRef = db.collection("users").document(userId) // Reference to the user's document

            // Firestore transaction to update the database.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef) // Get current document

                // Loop through standard collections and remove the book from old collections.
                for (collection in standardCollections) {
                    if (collection != "Select Collection" && collection != newCollectionName) {
                        val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                        booksInCollection?.let {
                            for (existingBook in it) {
                                if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                    transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))
                                    break
                                }
                            }
                        }
                    }
                }

                // Add the book to the new collection.
                transaction.update(userDocRef, "standardCollections.$newCollectionName", FieldValue.arrayUnion(book))
                null
            }.addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when adding book to default collection
                Toast.makeText(context, context.getString(R.string.book_added_to_collection, newCollectionName), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, context.getString(R.string.failed_to_add_book, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to remove a book from standard collections.
    private fun removeBookFromStandardCollection(
        context: Context,
        title: String,
        authors: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Firestore transaction to remove the book from collections.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Loop through the standard collections and remove the book.
                for (collection in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                    booksInCollection?.let {
                        for (existingBook in it) {
                            if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))
                                break
                            }
                        }
                    }
                }
                null
            }.addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when removing book from standard collection
                Toast.makeText(context, "Book removed from standard collection", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to remove book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to add a book to a custom collection in Firestore.
    private fun addBookToCustomCollection(userId: String, book: BookItem, collectionName: String, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Prepare book data for the collection.
        val bookData = hashMapOf(
            "title" to book.volumeInfo.title,
            "authors" to book.volumeInfo.authors,
            "imageLink" to book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
            "pages" to 0,
            "tags" to emptyList<String>(),
            "genres" to (book.volumeInfo.categories ?: listOf("Unknown Genre"))
        )

        // Update Firestore to add the book to the custom collection.
        db.collection("users").document(userId)
            .update("customCollections.$collectionName.books", FieldValue.arrayUnion(bookData))
            .addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when adding book to custom collection
                Toast.makeText(context, "Book added to $collectionName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add book to $collectionName", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to remove a book from custom collections.
    private fun removeBookFromCustomCollections(userId: String, book: BookItem, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Get book title and authors for comparison.
        val bookTitle = book.volumeInfo.title
        val bookAuthors = book.volumeInfo.authors

        // Fetch custom collections and remove the book from all of them.
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val customCollections = document.get("customCollections") as? Map<String, Any>

                if (!customCollections.isNullOrEmpty()) {
                    val batch = db.batch()

                    // Loop through each custom collection.
                    for (collectionName in customCollections.keys) {
                        val collectionRef = db.collection("users").document(userId)
                        val books = (customCollections[collectionName] as? Map<String, Any>)?.get("books") as? List<Map<String, Any>>

                        // Filter and remove the matching book from each collection.
                        if (!books.isNullOrEmpty()) {
                            val booksToRemove = books.filter { bookMap ->
                                val title = bookMap["title"] as? String
                                val authors = bookMap["authors"] as? List<String>
                                title == bookTitle && authors == bookAuthors
                            }

                            // Update Firestore.
                            for (bookToRemove in booksToRemove) {
                                batch.update(collectionRef, "customCollections.$collectionName.books", FieldValue.arrayRemove(bookToRemove))
                            }
                        }
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Book removed from all custom collections.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to remove book from custom collections.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "No custom collections found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading custom collections.", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to show a dialog where the user can select a custom collection.
    private fun showCustomCollectionDialog(context: Context, book: BookItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Fetch the user's custom collections and identify collections the book is already in.
            userDocRef.get().addOnSuccessListener { document ->
                val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
                val collectionsBookIsIn = mutableListOf<String>()

                // Identify collections containing the book.
                customCollections?.forEach { (collectionName, collectionData) ->
                    val booksInCollection = collectionData["books"] as? List<Map<String, Any>>
                    booksInCollection?.forEach { bookData ->
                        if (bookData["title"] == book.volumeInfo.title &&
                            bookData["authors"] == book.volumeInfo.authors
                        ) {
                            collectionsBookIsIn.add(collectionName)
                        }
                    }
                }

                // Check if there are any custom collections to display.
                if (!customCollections.isNullOrEmpty()) {
                    val customCollectionNames = customCollections.keys.toMutableList()
                    customCollectionNames.add("Remove from ALL Custom Collections")

                    // Filter out collections the book is already in
                    val filteredCollections = customCollectionNames.filterNot { it in collectionsBookIsIn }
                    val displayCollectionNames = filteredCollections.toMutableList()

                    // Show a message if all collections contain the book
                    if (displayCollectionNames.isEmpty()) {
                        Toast.makeText(context, "Book is already in all custom collections.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Build and show the dialog with custom collections.
                    AlertDialog.Builder(context)
                        .setTitle("Select Custom Collection")
                        .setItems(displayCollectionNames.toTypedArray()) { dialog, which ->
                            val selectedCollectionName = displayCollectionNames[which]

                            // Check if the user selected the option to remove from custom collections.
                            if (selectedCollectionName == "Remove from ALL Custom Collections") {
                                removeBookFromCustomCollections(userId, book, context) // Remove the book from custom collections
                                calculateTopGenres(userId, context)  // Calculate top genres when removing book from custom collections
                            } else {
                                // Add the book to the selected custom collection.
                                addBookToCustomCollection(userId, book, selectedCollectionName, context) // Add the book to the selected custom collection
                            }
                        }
                        .setNegativeButton("Cancel", null) // Cancel button to dismiss the dialog
                        .show() // Display the dialog
                } else {
                    // Inform the user if no custom collections are found.
                    Toast.makeText(context, "No custom collections found.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Handle failure to load custom collections.
                Toast.makeText(context, "Error loading custom collections.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Veronica Nguyen
    // Function to get the user's top 3 genres
    fun calculateTopGenres(userId: String, context: Context) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()  // Gets users collection
            .addOnSuccessListener { document ->
                val genreCount = mutableMapOf<String, Int>() // Map to count number of times a genre appears

                // Gets user's standard collections
                val standardCollections = document.get("standardCollections") as? Map<String, List<Map<String, Any>>>
                // Loops through each standard collection
                standardCollections?.values?.forEach { bookList ->
                    // Loops through each book in a collection
                    bookList.forEach { book ->
                        val genres = book["genres"] as? List<String> ?: listOf("Unknown Genre")  // Retrieves genres from a book
                        // Loops through each genre of a book
                        genres.forEach { genre ->
                            if (genre != "Unknown Genre") { // Excludes "Unknown Genre" from top genres calculation
                                // Sets default number of a genre's occurrence to 0 and retrieves its current count
                                // Increments the count of that genre by 1
                                genreCount[genre] = genreCount.getOrDefault(genre, 0) + 1
                            }
                        }
                    }
                }

                // Gets user's custom collections
                val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
                // Loops through each custom collection
                customCollections?.forEach { (_, collectionData) ->  //  Ignores key parameter of lambda expression
                    val books = collectionData["books"] as? List<Map<String, Any>>  // Gets the books in collection
                    // Loops through each book in a collection
                    books?.forEach { book ->
                        val genres = book["genres"] as? List<String> ?: listOf("Unknown Genre")  // Retrieves genres from a book
                        // Loops through each genre of a book
                        genres.forEach { genre ->
                            if (genre != "Unknown Genre") { // Excludes "Unknown Genre" from top genres calculation
                                // Sets default number of a genre's occurrence to 0 and retrieves its current count
                                // Increments the count of that genre by 1
                                genreCount[genre] = genreCount.getOrDefault(genre, 0) + 1
                            }
                        }
                    }
                }

                // Sort genres by count in descending order and take the top 3
                val topGenres = genreCount.entries.sortedByDescending { it.value }.take(3).map { it.key }

                // Update user's topGenres field in Firestore
                db.collection("users").document(userId).update("topGenres", topGenres)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Top genres updated: $topGenres", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update top genres: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to retrieve collections: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
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