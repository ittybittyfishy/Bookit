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

        collectionNameEditText = view.findViewById(R.id.collectionNameEditText)
        collectionSummaryEditText = view.findViewById(R.id.collectionSummaryEditText)
        createButton = view.findViewById(R.id.createButton)

        createButton.setOnClickListener {
            val collectionName = collectionNameEditText.text.toString()
            val collectionSummary = collectionSummaryEditText.text.toString()

            if (collectionName.isNotEmpty() && collectionSummary.isNotEmpty())
            {
                createCollection(collectionName, collectionSummary)
            }
            else
            {
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