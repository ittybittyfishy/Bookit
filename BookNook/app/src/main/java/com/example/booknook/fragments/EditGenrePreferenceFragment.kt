package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EditGenrePreferenceFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically for a better UI experience
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Full width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Wrap content height
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_genre_preference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI elements
        val chipGroup: ChipGroup = view.findViewById(R.id.chip_group_groups)
        val saveButton: Button = view.findViewById(R.id.confirmGenresButton)
        val cancelButton: Button = view.findViewById(R.id.cancelGenresButton)

        // Load any existing genre preferences and set them as selected (if applicable)
        loadExistingGenrePreferences(chipGroup)

        // Set click listeners
        saveButton.setOnClickListener {
            val selectedTags = getSelectedTags(chipGroup)

            // Validate that at least one genre is selected (optional, based on your needs)
            if (selectedTags.isNotEmpty()) {
                saveGenrePreferences(selectedTags)
            } else {
                Toast.makeText(requireContext(), "Please select at least one genre", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()  // Close the dialog without saving
        }
    }

    // Method to load existing genre preferences and pre-select chips (if any)
    private fun loadExistingGenrePreferences(chipGroup: ChipGroup) {
        // Retrieve the user's existing genre preferences from Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Access Firestore instance
            val db = FirebaseFirestore.getInstance()
            // Retrieve the user's document from the "users" collection
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    // Get the existing genre preferences from Firestore, defaulting to an empty list if none
                    val existingGenres = document.get("genrePreferences") as? List<String> ?: emptyList()
                    // Loop through the ChipGroup to pre-select the chips that match existing genres
                    for (i in 0 until chipGroup.childCount) {
                        // Get each chip from the ChipGroup
                        val chip = chipGroup.getChildAt(i) as? Chip
                        // If the chip is not null and its text matches an existing genre, mark it as checked
                        if (chip != null && existingGenres.contains(chip.text.toString())) {
                            chip.isChecked = true
                        }
                    }
                }
                .addOnFailureListener {
                    // Display a failure message if there was an issue retrieving the preferences
                    Toast.makeText(requireContext(), "Failed to load preferences", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Show an error message if the user is not logged in
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to save selected genre preferences to Firestore
    private fun saveGenrePreferences(selectedGenres: List<String>) {
        // Get the current user's ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            // Update the user's document with the selected genre preferences
            db.collection("users").document(userId)
                .update(mapOf("genrePreferences" to selectedGenres, "isFirstLogin" to false))
                .addOnSuccessListener {
                    // Display a success message once the preferences are saved
                    Toast.makeText(activity, "Preferences saved", Toast.LENGTH_SHORT).show()
                    dismiss()  // Close the dialog after saving
                }
                .addOnFailureListener { e ->
                    // Error retrieving genres
                    Log.e("EditGenrePreferenceFragment", "Error saving preferences: ${e.message}")
                    Toast.makeText(activity, "Failed to save preferences", Toast.LENGTH_SHORT).show()
                }
        } else {
            // user not logged in
            Toast.makeText(activity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to retrieve selected genres from the ChipGroup
    private fun getSelectedTags(chipGroup: ChipGroup): List<String> {
        // Initialize a list to store the selected tags
        val selectedTags = mutableListOf<String>()
        // Loop through each child view in the ChipGroup
        for (i in 0 until chipGroup.childCount) {
            // Get each chip from the ChipGroup
            val chip = chipGroup.getChildAt(i) as? Chip
            // If the chip is checked, add its text to the selected tags list
            if (chip?.isChecked == true) {
                selectedTags.add(chip.text.toString())
            }
        }
        // Return the list of selected tags
        return selectedTags
    }
}