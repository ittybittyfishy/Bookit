package com.example.booknook.fragments

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

    private lateinit var overviewButton: Button
    private lateinit var makeCollectionButton: Button
    private lateinit var db: FirebaseFirestore
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val customCollectionList = mutableListOf<CollectionCustomItem>() // mutable list to hold collection (initally empty)
    private lateinit var recyclerView: RecyclerView
    private lateinit var customCollectionAdapter: CollectionCustomAdapter
    private lateinit var sortSpinner: Spinner


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_custom_collection_tab, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView and Adapter (initialize adapter only once)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        customCollectionAdapter = CollectionCustomAdapter(customCollectionList)  // Initialize with mutable list
        recyclerView.adapter = customCollectionAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons and spinner
        overviewButton = view.findViewById(R.id.overview_button)
        makeCollectionButton = view.findViewById(R.id.make_collection_button)
        sortSpinner = view.findViewById(R.id.sortBooks)

        // Set listeners
        overviewButton.setOnClickListener {
            val collectionFragment = CollectionFragment()
            (activity as MainActivity).replaceFragment(collectionFragment, "My Books")
        }

        makeCollectionButton.setOnClickListener {
            val createCollection = CreateCollectionFragment()
            createCollection.show(parentFragmentManager, "CreateCollectionDialog")
        }

        // Setup spinner listener (do this once, outside of data fetching)
        setupSortSpinner()

        // Fetch the custom collections
        fetchCustomCollections()
    }

    private fun setupSortSpinner() {
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

    private fun fetchCustomCollections() {
        userId?.let {
            db.collection("users").document(it)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e("CustomCollectionTab", "Listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    documentSnapshot?.let { doc ->
                        if (doc.exists()) {
                            val customCollections = doc.get("customCollections") as? Map<String, Map<String, Any>>
                            customCollections?.let {
                                // Map Firestore data to your local model
                                val collectionList = customCollections.map { entry ->
                                    val collectionName = entry.key
                                    val collectionData = entry.value
                                    val summary = collectionData["summary"] as? String ?: ""

                                    // Convert books data from Firestore
                                    val booksData = collectionData["books"] as? List<Map<String, Any>> ?: listOf()
                                    val books = mapBooks(booksData)

                                    CollectionCustomItem(collectionName, books, summary)
                                }

                                // Update the data in the adapter and notify the changes
                                customCollectionList.clear()  // Clear existing list
                                customCollectionList.addAll(collectionList.sortedBy { it.collectionName }) // Sort the list
                                customCollectionAdapter.notifyDataSetChanged()  // Notify adapter of changes
                            }
                        }
                    }
                }
        } ?: Log.e("CustomCollectionTab", "User ID is null, unable to fetch collections")
    }

    private fun mapBooks(booksData: List<Map<String, Any>>): List<BookItemCollection> {
        return booksData.map { bookMap ->
            val title = bookMap["title"] as? String ?: "Unknown title"
            val authors = bookMap["authors"] as? List<String> ?: listOf("Unknown author")
            val imageLink = bookMap["imageLink"] as String
            val pages = (bookMap["pages"] as? Long ?: 0).toInt()

            BookItemCollection(title, authors, imageLink, pages)
        }.sortedBy { it.title }
    }

    private fun sortBooks(sortOption: String) {
        customCollectionList.forEach { collection ->
            collection.books = when (sortOption) {
                "Title (A-Z)" -> collection.books.sortedBy { it.title }
                "Title (Z-A)" -> collection.books.sortedByDescending { it.title }
                "Author (A-Z)" -> collection.books.sortedBy { it.authors.firstOrNull() ?: "Unknown Author" }
                "Author (Z-A)" -> collection.books.sortedByDescending { it.authors.firstOrNull() ?: "Unknown Author" }
                "Chapter's Read (Ascending)" -> collection.books.sortedBy { it.pages }
                "Chapter's Read (Descending)" -> collection.books.sortedByDescending { it.pages }
                else -> collection.books
            }
        }
        customCollectionAdapter.notifyDataSetChanged()  // Notify adapter after sorting
    }
}