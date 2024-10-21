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

    // Declare UI elements for editing name, summary, saving, and removing a collection
    private lateinit var nameEdit: EditText
    private lateinit var summaryEdit: EditText
    private lateinit var saveButton: Button
    private lateinit var removeButton: Button

    // Store the current collection name and summary passed to this fragment
    private var collectionName: String? = null
    private var collectionSummary: String? = null

    // Called when the dialog starts, allowing us to set the dialog's size
    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    // Inflate the layout for this fragment, specifying the UI elements in XML
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_collection, container, false)
    }

    // Called after the view has been created, allowing us to initialize the UI components
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize EditText fields and buttons
        nameEdit = view.findViewById(R.id.nameEdit)
        summaryEdit = view.findViewById(R.id.summaryEdit)
        saveButton = view.findViewById(R.id.saveButton)
        removeButton = view.findViewById(R.id.removeCollection)

        // Set the initial values for the collection name and summary, if available
        nameEdit.setText(collectionName)
        summaryEdit.setText(collectionSummary)

        // Handle save button click, where we update or rename the collection
        saveButton.setOnClickListener {
            // Get new name and summary from the user input
            val newName = nameEdit.text.toString()
            val newSummary = summaryEdit.text.toString()

            // Validate that neither the name nor summary is empty
            if (newName.isNotEmpty() && newSummary.isNotEmpty()) {
                editCollection(newName, newSummary) // Update the collection
            } else {
                Toast.makeText(activity, "Please enter a collection name and summary", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle remove button click to delete the collection
        removeButton.setOnClickListener {
            removeCollection() // Remove the collection from Firestore
        }
    }

    // Function to update or rename a collection in Firestore
    private fun editCollection(newName: String, newSummary: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // If the collection name has changed, rename it
        if (newName != collectionName) {
            // Copy books from the old collection name to the new one
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val collectionData = document.get("customCollections.$collectionName") as? Map<String, Any>
                    if (collectionData != null) {
                        // Add the new collection with the updated summary
                        val updatedCollection = collectionData.toMutableMap().apply {
                            put("summary", newSummary)  // Update the summary
                        }
                        db.collection("users").document(userId)
                            .update("customCollections.$newName", updatedCollection)
                            .addOnSuccessListener {
                                db.collection("users").document(userId)
                                    .update("numCollections", FieldValue.increment(1))
                                // After successful addition, remove the old collection
                                collectionName?.let { name ->
                                    // Remove the old collection by its previous name
                                    removeOldCollection(userId, name)
                                }
                            }
                    }
                }
        } else {
            // If the name is not changed, just update the summary
            db.collection("users").document(userId)
                .update("customCollections.$newName.summary", newSummary) // Update the summary in Firestore
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
        // Delete the old collection by its previous name
        db.collection("users").document(userId)
            .update("customCollections.$oldName", FieldValue.delete()) // Remove the old collection from Firestore
            .addOnSuccessListener {
                db.collection("users").document(userId)
                    .update("numCollections", FieldValue.increment(-1))
                Toast.makeText(activity, "Old collection removed", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to remove old collection", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to remove the collection entirely from Firestore
    private fun removeCollection() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Delete the collection from the user's customCollections in Firestore
        db.collection("users").document(userId)
            .update("customCollections.$collectionName", FieldValue.delete())  // Remove the collection
            .addOnSuccessListener {
                db.collection("users").document(userId)
                    .update("numCollections", FieldValue.increment(-1))
                Toast.makeText(activity, "Collection deleted", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to delete collection", Toast.LENGTH_SHORT).show()
            }
    }

    // Companion object to create an instance of the fragment with collection name and summary passed as arguments
    // This is a convenient method to create an instance of the EditCollectionFragment with a specific collectionName and summary already set.
    companion object {
        fun newInstance(collectionName: String, summary: String): EditCollectionFragment {
            val fragment = EditCollectionFragment()
            fragment.collectionName = collectionName // Set the collection name
            fragment.collectionSummary = summary // Set the collection summary
            return fragment // Return a new instance of the fragment
        }
    }
}