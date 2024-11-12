package com.example.booknook.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookItemCollection
import com.example.booknook.CollectionAdapter
import com.example.booknook.CollectionCustomAdapter
import com.example.booknook.CollectionCustomItem
import com.example.booknook.CollectionItem
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomCollectionTab : Fragment() {

    // Declare UI elements
    private lateinit var overviewButton: Button
    private lateinit var makeCollectionButton: Button
    private lateinit var db: FirebaseFirestore
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val customCollectionList = mutableListOf<CollectionCustomItem>() // mutable list to hold collection (initally empty)
    private lateinit var recyclerView: RecyclerView
    private lateinit var customCollectionAdapter: CollectionCustomAdapter
    private lateinit var sortSpinner: Spinner

    // Called when the fragment's view is being created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_custom_collection_tab, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Setup RecyclerView with a LinearLayoutManager (vertical scrolling)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the collection adapter with the collection list (empty at first)
        customCollectionAdapter = CollectionCustomAdapter(customCollectionList) { book ->
            fetchBookDetailsFromCustomCollections(book, requireContext())
        }
        recyclerView.adapter = customCollectionAdapter

        // Return the view to be displayed
        return view
    }

    // Called after the view is created and UI elements are initialized
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons and spinner
        overviewButton = view.findViewById(R.id.overview_button)
        makeCollectionButton = view.findViewById(R.id.make_collection_button)
        sortSpinner = view.findViewById(R.id.sortBooks)

        // Set click listener to switch to the overview (standard collections)
        overviewButton.setOnClickListener {
            val collectionFragment = CollectionFragment()
            (activity as MainActivity).replaceFragment(collectionFragment, "My Books")
        }

        // Set click listener to open a dialog for creating a new custom collection
        makeCollectionButton.setOnClickListener {
            val createCollection = CreateCollectionFragment()
            createCollection.show(parentFragmentManager, "CreateCollectionDialog")
        }

        // Setup sorting spinner
        setupSortSpinner()

        // Fetch the custom collections
        fetchCustomCollections()
    }

    // Setup the spinner for sorting options and handle the selection event
    private fun setupSortSpinner() {
        // Create an ArrayAdapter using the string array and custom spinner item layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.book_sort_options,
            R.layout.item_collections_spinner_layout  // Custom layout for the spinner display
        )
        // Apply the adapter to the spinner
        adapter.setDropDownViewResource(R.layout.item_collections_spinner_dropdown) // The layout for dropdown items
        sortSpinner.adapter = adapter

        // Handle selection changes as before
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSortOption = parent.getItemAtPosition(position).toString()
                sortBooks(selectedSortOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action if nothing is selected
            }
        }
    }

    // Function to fetch custom collections from Firestore
    private fun fetchCustomCollections() {
        userId?.let {
            // Access the user's document in the "users" collection
            db.collection("users").document(it)
                .addSnapshotListener { documentSnapshot, error ->
                    // Handle errors during data retrieval
                    if (error != null) {
                        Log.e("CustomCollectionTab", "Listen failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    // If data is retrieved successfully, process the document snapshot
                    documentSnapshot?.let { doc ->
                        if (doc.exists()) {
                            // Get the custom collections from Firestore (as a map of collections)
                            val customCollections = doc.get("customCollections") as? Map<String, Map<String, Any>>
                            // If collections exist, map them to the local list
                            customCollections?.let {
                                // Convert each Firestore collection entry to a CollectionCustomItem
                                val collectionList = customCollections.map { entry ->
                                    val collectionName = entry.key
                                    val collectionData = entry.value
                                    val summary = collectionData["summary"] as? String ?: ""


                                    // Get the list of books in the collection from Firestore
                                    val booksData = collectionData["books"] as? List<Map<String, Any>> ?: listOf()
                                    val books = mapBooks(booksData)

                                    // Return a CollectionCustomItem for this collection
                                    CollectionCustomItem(collectionName, books, summary)
                                }

                                // Clear the existing custom collections and add the new list (sorted by collection name)
                                customCollectionList.clear()  // Clear existing list
                                customCollectionList.addAll(collectionList.sortedBy { it.collectionName }) // Sort the list
                                // Notify the adapter that the data has changed so it can refresh the UI
                                customCollectionAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
        } ?: Log.e("CustomCollectionTab", "User ID is null, unable to fetch collections")
    }

    // Function to map book data from Firestore into a list of BookItemCollection objects
    private fun mapBooks(booksData: List<Map<String, Any>>): List<BookItemCollection> {
        return booksData.map { bookMap ->
            val title = bookMap["title"] as? String ?: "Unknown title" // Book title (default to "Unknown title" if null)
            val authors = bookMap["authors"] as? List<String> ?: listOf("Unknown author") // Authors (default to "Unknown author")
            val imageLink = bookMap["imageLink"] as? String ?: "" // URL for the book's image
            val pages = (bookMap["pages"] as? Long ?: 0).toInt() // Number of pages or chapters (default to 0
            val tags = bookMap["tags"] as List<String> ?: emptyList() // Tags associated with the book (default to empty)
            val genres = bookMap["genres"] as? List<String> ?: listOf("Unknown genre") // Genres (default to "Unknown genre")

            // Return a BookItemCollection object with the mapped data
            BookItemCollection(title, authors, imageLink, pages, tags, genres)
        }.sortedBy { it.title } // Sort books alphabetically by title
    }

    // Function to sort books within each custom collection based on the selected option
    private fun sortBooks(sortOption: String) {
        // Iterate over each custom collection and apply sorting
        customCollectionList.forEach { collection ->
            collection.books = when (sortOption) {
                // Sort books by title (A-Z or Z-A)
                "Title (A-Z)" -> collection.books.sortedBy { it.title }
                "Title (Z-A)" -> collection.books.sortedByDescending { it.title }
                // Sort books by the first author (A-Z or Z-A)
                "Author (A-Z)" -> collection.books.sortedBy { it.authors.firstOrNull() ?: "Unknown Author" }
                "Author (Z-A)" -> collection.books.sortedByDescending { it.authors.firstOrNull() ?: "Unknown Author" }
                // Sort books by the number of chapters/pages read (ascending or descending)
                "Chapter's Read (Ascending)" -> collection.books.sortedBy { it.pages }
                "Chapter's Read (Descending)" -> collection.books.sortedByDescending { it.pages }
                // Default case (no sorting applied)
                else -> collection.books
            }
        }
        // Notify the adapter that the sorted data has changed, so the UI can be updated
        customCollectionAdapter.notifyDataSetChanged()  // Notify adapter after sorting
    }

    private fun fetchBookDetailsFromCustomCollections(book: BookItemCollection, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Fetch the user document
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    // Fetch custom collections
                    val customCollections = document.get("customCollections") as? Map<String, Any>
                    if (customCollections != null) {
                        for ((collectionName, collectionData) in customCollections) {
                            val booksInCollection = (collectionData as? Map<String, Any>)?.get("books") as? List<Map<String, Any>>

                            booksInCollection?.let { books ->
                                for (existingBook in books) {
                                    // Check if this book matches the clicked book
                                    if (existingBook["title"] == book.title &&
                                        (existingBook["authors"] as? List<*>) == book.authors
                                    ) {
                                        // Book found, prepare the bundle
                                        val bookDetailsFragment = BookDetailsFragment()
                                        val bundle = Bundle()

                                        // Title and Authors
                                        bundle.putString("bookTitle", existingBook["title"] as? String ?: "")
                                        bundle.putString("bookAuthor", (existingBook["authors"] as? List<*>)?.joinToString(", ") ?: "")
                                        bundle.putStringArrayList("bookAuthorsList", ArrayList(existingBook["authors"] as? List<String> ?: emptyList()))

                                        // Image Link
                                        bundle.putString("bookImage", existingBook["imageLink"] as? String)

                                        // Handling Rating
                                        val rating = when (val ratingValue = existingBook["rating"]) {
                                            is Double -> ratingValue.toFloat()
                                            is Long -> ratingValue.toFloat()
                                            else -> 0f
                                        }
                                        bundle.putFloat("bookRating", rating)

                                        // ISBN
                                        bundle.putString("bookIsbn", existingBook["isbn"] as? String ?: "No ISBN")

                                        // Handling Description
                                        val description = existingBook["description"] as? String ?: "No Description"
                                        bundle.putString("bookDescription", description)

                                        // Genres
                                        bundle.putStringArrayList("bookGenres", ArrayList(existingBook["genres"] as? List<String> ?: emptyList()))

                                        bookDetailsFragment.arguments = bundle

                                        // Navigate to BookDetailsFragment
                                        (context as MainActivity).replaceFragment(bookDetailsFragment, "Book Details", showBackButton = true)
                                        return@addOnSuccessListener // Exit after navigating to avoid redundant processing
                                    }
                                }
                            }
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching custom collections: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}