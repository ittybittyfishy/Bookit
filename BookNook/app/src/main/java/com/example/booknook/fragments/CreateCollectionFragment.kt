package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.booknook.R
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateCollectionFragment : DialogFragment() {

    // Declaring variables for the EditText and Button UI elements
    private lateinit var collectionNameEditText: EditText
    private lateinit var collectionSummaryEditText: EditText
    private lateinit var createButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI elements by finding them by their respective IDs in the layout
        collectionNameEditText = view.findViewById(R.id.collectionNameEditText)
        collectionSummaryEditText = view.findViewById(R.id.collectionSummaryEditText)
        createButton = view.findViewById(R.id.createButton)

        // Set an OnClickListener to the create button to activate creation
        createButton.setOnClickListener {
            // Get the text entered in the collection name and summary EditText fields
            val collectionName = collectionNameEditText.text.toString()
            val collectionSummary = collectionSummaryEditText.text.toString()

            // Check if both the collection name and summary fields are not empty
            if (collectionName.isNotEmpty() && collectionSummary.isNotEmpty())
            {
                // If valid input, call the function to create a new collection in Firestore
                createCollection(collectionName, collectionSummary)
            }
            else
            {
                // Show a Toast message if any of the fields are empty
                Toast.makeText(activity, "Please enter a collection name and summary", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createCollection(name: String, summary: String)
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null)
        {
            val db = FirebaseFirestore.getInstance()
            val collectionData = hashMapOf(
                "summary" to summary,
                "books" to emptyList<String>()
            )

            db.collection("users").document(userId)
                .update("customCollections.$name", collectionData)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Collection created", Toast.LENGTH_SHORT).show()
                    dismiss() // Close the dialog
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to create collection", Toast.LENGTH_SHORT).show()
                }
        }
    }
}