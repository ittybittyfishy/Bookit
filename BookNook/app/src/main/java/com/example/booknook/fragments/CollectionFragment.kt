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
import com.example.booknook.CollectionAdapter
import com.example.booknook.CollectionItem
import com.example.booknook.VolumeInfo
import com.example.booknook.ImageLinks
import com.example.booknook.IndustryIdentifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CollectionFragment : Fragment(){

    private lateinit var myCollectionButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var collectionAdapter: CollectionAdapter
    private val collectionList = mutableListOf<CollectionItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_collection, container, false)
        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        collectionAdapter = CollectionAdapter(collectionList)
        recyclerView.adapter = collectionAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        myCollectionButton = view.findViewById(R.id.my_collection_button)

        // Set listeners
        myCollectionButton.setOnClickListener {
            // Handle account button click
            val customcollectionFragment = CustomCollectionTab()
            (activity as MainActivity).replaceFragment(customcollectionFragment, "My Books")
        }

        fetchCollection()

    }

    private fun fetchCollection() {
        userId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val standardCollections = document.get("standardCollections") as? Map<String, List<Map<String, Any>>>
                        if (standardCollections != null) {
                            collectionList.clear() // Clear existing data
                            for ((name, books) in standardCollections) {
                                val bookItems = books.map { book ->
                                    val imageLinkMap = book["imageLink"] as? Map<String, String>
                                    val imageLinks = imageLinkMap?.let { ImageLinks(thumbnail = it["thumbnail"]) }

                                    BookItem(VolumeInfo(
                                        title = book["title"] as? String ?: "",
                                        authors = book["authors"] as? List<String> ?: listOf("Unknown Author"),
                                        imageLinks = imageLinks,
                                        averageRating = (book["averageRating"] as? Number)?.toFloat() ?: 0.0f,
                                        categories = book["categories"] as? List<String> ?: listOf("Unknown Genre")
                                    ))
                                }
                                collectionList.add(CollectionItem(name, bookItems))
                            }
                            collectionAdapter.notifyDataSetChanged()
                    } else {
                            Log.d("CollectionFragment", "No standardCollections found")
                        }
                    } else {
                        Log.d("CollectionFragment", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("CollectionFragment", "get failed with ", exception)
                }
        }
    }

    private fun mapToBookItem(data: Map<String, Any>): BookItem {
        val title = data["title"] as? String ?: ""
        val authors = data["authors"] as? List<String> ?: emptyList()
        val imageLink = data["imageLink"] as? ImageLinks?
        val bookInfo = VolumeInfo(title, authors, imageLink)
        return BookItem(bookInfo)
    }

}