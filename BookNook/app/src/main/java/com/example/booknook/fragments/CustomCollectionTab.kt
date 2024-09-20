package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_custom_collection_tab, container, false)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize Adapter with empty collection list
        customCollectionAdapter = CollectionCustomAdapter(customCollectionList)
        recyclerView.adapter = customCollectionAdapter

        Log.d("CustomCollectionTab", "onCreateView called")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CustomCollectionTab", "onViewCreated called, initializing RecyclerView")

        // Initialize buttons
        overviewButton = view.findViewById(R.id.overview_button)
        makeCollectionButton = view.findViewById(R.id.make_collection_button)

        // Initialize RecyclerView with layout manager and adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        customCollectionAdapter = CollectionCustomAdapter(emptyList())
        recyclerView.adapter = customCollectionAdapter

        // Set listeners
        overviewButton.setOnClickListener {
            // Handle account button click
            val collectionFragment = CollectionFragment()
            (activity as MainActivity).replaceFragment(collectionFragment, "My Books")
        }

        makeCollectionButton.setOnClickListener{
            val createCollection = CreateCollectionFragment()
            createCollection.show(parentFragmentManager, "CreateCollectionDialog")

        }
        // Fetch the custom collections
        fetchCustomCollections()
    }

    // Fetch the custom collections and update the RecyclerView
    private fun fetchCustomCollections() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            Log.d("CustomCollectionTab", "Fetching custom collections for user: $userId")

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val customCollections =
                            document.get("customCollections") as? Map<String, Map<String, Any>>
                        if (customCollections != null) {
                            Log.d(
                                "CustomCollectionTab",
                                "Custom collections retrieved: $customCollections"
                            )

                            // Transform the customCollections map into a list of CollectionCustomItem
                            val collectionList = customCollections.map { entry ->
                                val collectionName = entry.key
                                val collectionData = entry.value
                                val summary = collectionData["summary"] as? String ?: ""

                                // Convert the books data from Firestore to a list of BookItemCollection
                                val booksData =
                                    collectionData["books"] as? List<Map<String, Any>> ?: listOf()
                                val books = mapBooks(booksData)

                                Log.d(
                                    "CustomCollectionTab",
                                    "Collection name: $collectionName, Summary: $summary, Books: $books"
                                )

                                CollectionCustomItem(collectionName, books, summary)
                            }

                            Log.d(
                                "CustomCollectionTab",
                                "Updating RecyclerView with collection list"
                            )

                            // Update the adapter with the retrieved collections
                            customCollectionAdapter = CollectionCustomAdapter(collectionList)
                            recyclerView.adapter = customCollectionAdapter
                            customCollectionAdapter.notifyDataSetChanged()

                            // If no collections exist, log and handle empty state
                            if (collectionList.isEmpty()) {
                                Log.d(
                                    "CustomCollectionTab",
                                    "No custom collections found, showing default empty state"
                                )
                                // Handle empty state here, such as showing a message
                            }
                        } else {
                            Log.d("CustomCollectionTab", "No custom collections found for user")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CustomCollectionTab", "Failed to fetch custom collections: ${e.message}")
                }
        } else {
            Log.e("CustomCollectionTab", "User ID is null, unable to fetch collections")
        }
    }
    private fun mapBooks(booksData: List<Map<String, Any>>): List<BookItemCollection> {
        return booksData.map { bookMap ->
            val title = bookMap["title"] as? String ?: "Unknown title"
            val authors = bookMap["authors"] as? List<String> ?: listOf("Unknown author")
            val imageLink = bookMap["imageLink"] as String
            val pages = bookMap["pages"] as? Int ?: 0

            BookItemCollection(title, authors, imageLink, pages)
        }
    }


}