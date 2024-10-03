package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EditCollectionFragment : DialogFragment() {

    private lateinit var nameEdit: EditText
    private lateinit var summaryEdit: EditText
    private lateinit var saveButton: Button
    private lateinit var removeButton: Button
    private var collectionName: String? = null
    private var collectionSummary: String? = null

    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEdit = view.findViewById(R.id.nameEdit)
        summaryEdit = view.findViewById(R.id.summaryEdit)
        saveButton = view.findViewById(R.id.saveButton)
        removeButton = view.findViewById(R.id.removeCollection)

        // Set initial values if editing
        nameEdit.setText(collectionName)
        summaryEdit.setText(collectionSummary)

        // Save edited collection
        saveButton.setOnClickListener {
            val newName = nameEdit.text.toString()
            val newSummary = summaryEdit.text.toString()

            if (newName.isNotEmpty() && newSummary.isNotEmpty()) {
                editCollection(newName, newSummary)
            } else {
                Toast.makeText(activity, "Please enter a collection name and summary", Toast.LENGTH_SHORT).show()
            }
        }

        // Remove collection
        removeButton.setOnClickListener {
            removeCollection()
        }
    }

    private fun editCollection(newName: String, newSummary: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Check if the name is being changed
        if (newName != collectionName) {
            // Copy old collection's books
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val collectionData = document.get("customCollections.$collectionName") as? Map<String, Any>
                    if (collectionData != null) {
                        // Add the new collection with the same books but updated summary
                        val updatedCollection = collectionData.toMutableMap().apply {
                            put("summary", newSummary)  // Update the summary
                        }
                        db.collection("users").document(userId)
                            .update("customCollections.$newName", updatedCollection)
                            .addOnSuccessListener {
                                // After successful addition, remove the old collection
                                collectionName?.let { name ->
                                    removeOldCollection(userId, name)
                                }
                            }
                    }
                }
        } else {
            // If the name is not changed, just update the summary
            db.collection("users").document(userId)
                .update("customCollections.$newName.summary", newSummary)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Collection updated", Toast.LENGTH_SHORT).show()
                    dismiss()  // Close the dialog
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to update collection", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Helper function to remove the old collection after renaming
    private fun removeOldCollection(userId: String, oldName: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("customCollections.$oldName", FieldValue.delete())
            .addOnSuccessListener {
                Toast.makeText(activity, "Old collection removed", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to remove old collection", Toast.LENGTH_SHORT).show()
            }
    }

    // Remove the collection from Firestore
    private fun removeCollection() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Remove the collection from customCollections
        db.collection("users").document(userId)
            .update("customCollections.$collectionName", FieldValue.delete())  // Remove the collectionName from the customCollections map
            .addOnSuccessListener {
                Toast.makeText(activity, "Collection deleted", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to delete collection", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(collectionName: String, summary: String): EditCollectionFragment {
            val fragment = EditCollectionFragment()
            fragment.collectionName = collectionName
            fragment.collectionSummary = summary
            return fragment
        }
    }
}