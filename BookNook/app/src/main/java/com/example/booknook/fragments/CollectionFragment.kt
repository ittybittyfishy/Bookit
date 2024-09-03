package com.example.booknook.fragments

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
class CollectionFragment : Fragment(){

    //Declare UI elements
    private lateinit var myCollectionButton: Button // Button takes you to cutsom collections
    private lateinit var recyclerView: RecyclerView // displays list of collections
    private lateinit var db: FirebaseFirestore
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var collectionAdapter: CollectionAdapter // manages and displays collection data in a scrollabe envrioment
    private val collectionList = mutableListOf<CollectionItem>() // mutable list to hold collection (initally empty)

    //Layout, called when fragmetns view is being created
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
        collectionAdapter = CollectionAdapter(collectionList)
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

        // Fetch the user's collections and books from Firestore
        fetchCollectionsAndBooks()

    }

    // function to get users collection
    private fun fetchCollectionsAndBooks() {
        // check if user is logged in
        userId?.let { uid ->
            // Access the Firestore collection for the user and retrieve their document
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    // If the document is successfully retrieved
                    if (document != null) {
                        // Get the standard collections from the document,
                        val standardCollections = document.get("standardCollections") as? Map<String, Any>
                        if (standardCollections != null) {
                            collectionList.clear() // Clear the existing data
                            // Loop through each collection in the standardCollections map
                            for ((name, books) in standardCollections) {
                                // Check if the books are in a list format
                                if (books is List<*>) {
                                    // Map each book in the list to a BookItemCollection object
                                    val bookItems = books.mapNotNull { book ->
                                        if (book is Map<*, *>) {
                                            BookItemCollection(
                                                title = book["title"] as? String ?: "",
                                                authors = book["authors"] as? List<String> ?: listOf("Unknown Author"),
                                                imageLink = book["imageLink"] as? String ?: ""
                                            )
                                        } else {
                                            null // Ignore invalid books
                                        }
                                    }
                                    // Add the collection to the list with its name and associated books
                                    collectionList.add(CollectionItem(name, bookItems))
                                }
                            }

                            // Notify the adapter that data has changed so it updates recyle view
                            collectionAdapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Log an error message
                    Log.d("CollectionFragment", "get failed with ", exception)
                }
        }
    }

    // Function to make sure that the book collection is being caled correctly (debugging)
    private fun fetchAndLogCollections() {
        // Check if userId is not null, meaning a user is logged in
        userId?.let { uid ->
            // Access the Firestore collection for the user and retrieve their document
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val standardCollections = document.get("standardCollections") as? Map<String, Any>
                        if (standardCollections != null) {
                            // Loop through each collection in the standardCollections map
                            for ((collectionName, books) in standardCollections) {
                                // Log the collection name for debugging
                                Log.d("CollectionFragment", "Collection: $collectionName")
                                if (books is List<*>) {
                                    // Loop through each book in the collection
                                    for (book in books) {
                                        if (book is Map<*, *>) {
                                            // Log each key-value pair in the book map
                                            for ((key, value) in book) {
                                                Log.d("CollectionFragment", "Key: $key, Value: $value")
                                            }
                                        } else {
                                            // Log an unexpected format message if the book data isn't as expected
                                            Log.d("CollectionFragment", "Unexpected book format: ${book?.javaClass}")
                                        }
                                    }
                                } else {
                                    // Log an unexpected type message if the books variable isn't a list
                                    Log.d("CollectionFragment", "Unexpected type for books: ${books?.javaClass}")
                                }
                            }
                        } else {
                            // Log a message if no standard collections are found
                            Log.d("CollectionFragment", "No standardCollections found")
                        }
                    } else {
                        // Log a message if the document doesn't exist (likely an error)
                        Log.d("CollectionFragment", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    // Log an error message if something goes wrong when retrieving the document
                    Log.d("CollectionFragment", " failed with ", exception)
                }
        }
    }
}