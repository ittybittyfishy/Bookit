package com.example.booknook.fragments

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


class CollectionFragment : Fragment(), BookAdapter.RecyclerViewEvent{

    private lateinit var myCollectionButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var db: FirebaseFirestore
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var collectionAdapter: CollectionAdapter
    private val collectionList = mutableListOf<CollectionItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_collection, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize adapter with the collection list
        collectionAdapter = CollectionAdapter(collectionList)
        recyclerView.adapter = collectionAdapter

        db = FirebaseFirestore.getInstance()

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

        fetchCollectionNames()

    }

    private fun fetchCollectionNames() {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Extract the collection names
                        val standardCollections = document.get("standardCollections") as? Map<String, Any>
                        standardCollections?.keys?.forEach { collectionName ->
                            collectionList.add(CollectionItem(collectionName))
                        }
                        // Notify adapter about data changes
                        collectionAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("CollectionFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("CollectionFragment", "get failed with ", exception)
                }
        }
    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }
}