package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth

class CustomCollectionTab : Fragment() {

    private lateinit var overviewButton: Button
    private lateinit var makeCollectionButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_collection_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        overviewButton = view.findViewById(R.id.overview_button)
        makeCollectionButton = view.findViewById(R.id.make_collection_button)

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
    }

}