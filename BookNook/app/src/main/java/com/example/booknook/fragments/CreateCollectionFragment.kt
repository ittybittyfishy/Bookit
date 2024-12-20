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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateCollectionFragment : DialogFragment() {

    // Declaring variables to hold the text inputs and button from the layout
    private lateinit var collectionNameEditText: EditText
    private lateinit var collectionSummaryEditText: EditText
    private lateinit var createButton: Button

    // This code is a dialog fragment so you need to specify how it appears on screen
    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    // infalte layout xml
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

    // This function is used to save the new collection to Firestore
    private fun createCollection(name: String, summary: String)
    {
        // Get the ID of the current user who is logged in
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null)
        {
            val db = FirebaseFirestore.getInstance()
            // Create a map (like a dictionary) with the collection's summary and an empty list of books
            val collectionData = hashMapOf(
                "summary" to summary, // Add the summary of the collection
                "books" to mutableListOf<String>() // Add the summary of the collection// Start with no books in the collection (empty list)
            )
            // Update the user's document in Firestore, adding the new custom collection under their account
            db.collection("users").document(userId)
                .update("customCollections.$name", collectionData) // Add the collection using its name as a key
                .addOnSuccessListener {
                    db.collection("users").document(userId)
                        .update("numCollections", FieldValue.increment(1))
                    // If the collection was successfully created, show a confirmation message
                    Toast.makeText(activity, "Collection created", Toast.LENGTH_SHORT).show()
                    dismiss() // Close the dialog
                }
                .addOnFailureListener {
                    // If something goes wrong while creating the collection, show an error message
                    Toast.makeText(activity, "Failed to create collection", Toast.LENGTH_SHORT).show()
                }
        }
    }
}