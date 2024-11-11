package com.example.booknook.fragments

import android.content.Context
import android.media.Image
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
import com.example.booknook.MainActivity
import com.example.booknook.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookAdapter
import com.example.booknook.BookItem
import com.example.booknook.BookItemCollection
import com.example.booknook.CollectionAdapter
import com.example.booknook.CollectionItem
import com.example.booknook.VolumeInfo
import com.example.booknook.ImageLinks
import com.example.booknook.IndustryIdentifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//This class displays and manages a users book collection
class  CollectionFragment : Fragment(){

    //Declare UI elements
    private lateinit var myCollectionButton: Button // Button takes you to cutsom collections
    private lateinit var recyclerView: RecyclerView // displays list of collections
    private lateinit var db: FirebaseFirestore
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var collectionAdapter: CollectionAdapter // manages and displays collection data in a scrollabe envrioment
    private val collectionList = mutableListOf<CollectionItem>() // mutable list to hold collection (initally empty)
    private lateinit var sortSpinner: Spinner

    //Layout, called when fragments view is being created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // takes the xml and brings it to view
        val view = inflater.inflate(R.layout.fragment_collection, container, false)

        //initialize database
        db = FirebaseFirestore.getInstance()

        //Setup recycler view and its layout manager(which handles positioning items)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the collectionAdapter with the empty collectionList and set it to the recyclerview
        collectionAdapter = CollectionAdapter(collectionList) { book ->
            // When a book is clicked, first find which collection it belongs to
            findBookCollection(requireContext(), book.title, book.authors.joinToString(", ")) { collectionName ->
                if (collectionName != null) {
                    // If collection is found, fetch the book details from Firestore
                    fetchBookDetailsFromCollection(book, requireContext())
                } else {
                    Toast.makeText(context, "Book not found in any collection.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        recyclerView.adapter = collectionAdapter


        // returns view that was created so it can be displayed
        return view
    }

    // function is called after the view has been created and all views have been initialized
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        myCollectionButton = view.findViewById(R.id.my_collection_button)

        // Set listeners
        myCollectionButton.setOnClickListener {
            // Changes fragment to custom collections
            val customcollectionFragment = CustomCollectionTab()
            (activity as MainActivity).replaceFragment(customcollectionFragment, "My Books")

        }

        // initalize spinner for sorting options
        sortSpinner = view.findViewById(R.id.sortBooks)

        // Set up the sort spinner with selection listener
        setupSortSpinner()

        // Fetch the user's collections and books from Firestore
        fetchCollectionsAndBooks()

    }

    // Set up the sort spinner with selection listener
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

    // Function to fetch user's collections from Firestore
    private fun fetchCollectionsAndBooks() {
        userId?.let { uid ->
            // Fetch user document from Firestore
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val standardCollections = document.get("standardCollections") as? Map<String, Any>
                        if (standardCollections != null) {
                            collectionList.clear() // Clear previous data

                            // Define the desired order
                            val desiredOrder = listOf("Want to Read", "Reading", "Finished", "Dropped")

                            // Loop through collections in desired order
                            for (name in desiredOrder) {
                                val books = standardCollections[name] // Get books for each collection
                                if (books is List<*>) {
                                    val bookItems = books.mapNotNull { book ->
                                        // Create BookItemCollection objects from the fetched data
                                        if (book is Map<*, *>) {
                                            BookItemCollection(
                                                title = book["title"] as? String ?: "",
                                                authors = book["authors"] as? List<String> ?: listOf("Unknown Author"),
                                                imageLink = book["imageLink"] as? String ?: "",
                                                pages = (book["pages"] as? Long ?: 0).toInt(),
                                                tags = book["tags"] as? List<String> ?: emptyList(),
                                                genres = book["genres"] as? List<String> ?: listOf("Unknown Genre")  // Veronica Nguyen
                                            )
                                        } else {
                                            null // Return null if not a valid Map
                                        }
                                    }
                                    collectionList.add(CollectionItem(name, bookItems)) // Add collection item to the list
                                }
                            }
                            // Notify the adapter to update the UI
                            collectionAdapter.notifyDataSetChanged()

                            // Set up the spinner after the data is fetched
                            setupSortSpinner()

                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("CollectionFragment", "get failed with ", exception) // Log failure to fetch data
                }
        }
    }

    // Function to sort the books based on the selected option
    private fun sortBooks(sortOption: String) {
        for (collectionBook in collectionList) {
            // Sort books based on the selected sorting option
            collectionBook.books = when (sortOption) {
                "Title (A-Z)" -> collectionBook.books.sortedBy { it.title }
                "Title (Z-A)" -> collectionBook.books.sortedByDescending { it.title }
                "Author (A-Z)" -> collectionBook.books.sortedBy { it.authors.firstOrNull() ?: "Unknown Author" }
                "Author (Z-A)" -> collectionBook.books.sortedByDescending { it.authors.firstOrNull() ?: "Unknown Author" }
                "Chapter's Read (Ascending)" -> collectionBook.books.sortedBy { it.pages }
                "Chapter's Read (Descending)" -> collectionBook.books.sortedByDescending { it.pages }
                else -> collectionBook.books // default
            }
        }

        // Notify the adapter to update the UI
        collectionAdapter.notifyDataSetChanged()
    }

    private fun findBookCollection(
        context: Context,
        title: String,
        authors: String?,
        callback: (String?) -> Unit // Callback to return collection name
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Loop through standard collections to find the book
                for (collection in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                    booksInCollection?.let {
                        for (existingBook in it) {
                            if (existingBook["title"] == title &&
                                existingBook["authors"] == (authors?.split(", ") ?: listOf("Unknown Author"))
                            ) {
                                return@runTransaction collection // Return the collection name
                            }
                        }
                    }
                }
                null // Return null if not found in any collection
            }.addOnSuccessListener { collection ->
                callback(collection as? String) // Pass the collection name to the callback
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to find book collection: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(null) // Pass null if there's an error
            }
        } else {
            callback(null) // Return null if user is not logged in
        }
    }

    private fun fetchBookDetailsFromCollection(book: BookItemCollection, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val standardCollections = document.get("standardCollections") as? Map<String, Any>
                    if (standardCollections != null) {
                        // Loop through each standard collection to find the book
                        for (collectionName in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                            val booksInCollection = standardCollections[collectionName] as? List<Map<String, Any>>

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
                Toast.makeText(context, "Error fetching collections: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}