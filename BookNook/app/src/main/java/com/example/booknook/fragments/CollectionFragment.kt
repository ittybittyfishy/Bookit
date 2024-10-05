package com.example.booknook.fragments

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
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

        // initalize spinner
        sortSpinner = view.findViewById(R.id.sortBooks)

        // Fetch the user's collections and books from Firestore
        fetchCollectionsAndBooks()

    }

    private fun setupSortSpinner() {
        // Set listener for spinner selection
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSortOption = parent.getItemAtPosition(position).toString()
                sortBooks(selectedSortOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No-op
            }
        }
    }

    // function to get users collection
    private fun fetchCollectionsAndBooks() {
        userId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val standardCollections = document.get("standardCollections") as? Map<String, Any>
                        if (standardCollections != null) {
                            collectionList.clear()

                            // Define the desired order
                            val desiredOrder = listOf("Want to Read", "Reading", "Finished", "Dropped")

                            // Loop through collections in desired order
                            for (name in desiredOrder) {
                                val books = standardCollections[name]
                                if (books is List<*>) {
                                    val bookItems = books.mapNotNull { book ->
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
                                            null
                                        }
                                    }
                                    collectionList.add(CollectionItem(name, bookItems))
                                }
                            }

                            collectionAdapter.notifyDataSetChanged()

                            // Set up the spinner after the data is fetched
                            setupSortSpinner()

                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("CollectionFragment", "get failed with ", exception)
                }
        }
    }

    // Function to sort the books based on the selected option
    private fun sortBooks(sortOption: String) {
        for (collectionBook in collectionList) {
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
}