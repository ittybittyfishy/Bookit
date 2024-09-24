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

            // Listen for real-time updates to the user's document
            db.collection("users").document(userId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e("CustomCollectionTab", "Listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val customCollections =
                            documentSnapshot.get("customCollections") as? Map<String, Map<String, Any>>
                        if (customCollections != null) {
                            // Map the Firestore data to your local model
                            val collectionList = customCollections.map { entry ->
                                val collectionName = entry.key
                                val collectionData = entry.value
                                val summary = collectionData["summary"] as? String ?: ""

                                // Convert the books data from Firestore to a list of BookItemCollection
                                val booksData =
                                    collectionData["books"] as? List<Map<String, Any>> ?: listOf()
                                val books = mapBooks(booksData)

                                CollectionCustomItem(collectionName, books, summary)
                            }

                            // Sort the collection list alphabetically by collection name
                            val sortedCollectionList = collectionList.sortedBy { it.collectionName }

                            // Update the adapter with the new collection list
                            customCollectionAdapter = CollectionCustomAdapter(sortedCollectionList)
                            recyclerView.adapter = customCollectionAdapter
                            customCollectionAdapter.notifyDataSetChanged()
                        }
                    }
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
            val pages = (bookMap["pages"] as?  Long ?: 0).toInt()

            BookItemCollection(title, authors, imageLink, pages)
        }.sortedBy { it.title }
    }


}