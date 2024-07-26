package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booknook.MainActivity
import com.example.booknook.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookItem
import com.example.booknook.VolumeInfo
import com.example.booknook.ImageLinks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CollectionFragment : Fragment() {

    private lateinit var myCollectionButton: Button

    private lateinit var recyclerView: RecyclerView
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collection, container, false)
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
    }
}