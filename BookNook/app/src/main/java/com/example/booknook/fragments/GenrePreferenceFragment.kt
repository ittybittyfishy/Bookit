package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.booknook.R
import com.example.booknook.MainActivity

// Define a Fragment class for selecting genre preferences
class GenrePreferenceFragment : Fragment() {

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_genre_preferences, container, false)
    }

    // Method called after the view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI elements
        val chipGroup: ChipGroup = view.findViewById(R.id.chip_group_genres)
        val saveButton: Button = view.findViewById(R.id.button_save)

        // Set click listener for the save button
        saveButton.setOnClickListener {
            val selectedGenres = getSelectedGenres(chipGroup)
            if (selectedGenres.isEmpty()) {
                Toast.makeText(activity, "Please select at least one genre", Toast.LENGTH_SHORT).show()
            } else {
                saveGenrePreferences(selectedGenres)
            }
        }
    }

    // Method to get selected genres from the ChipGroup
    private fun getSelectedGenres(chipGroup: ChipGroup): List<String> {
        val selectedGenres = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedGenres.add(chip.text.toString())
            }
        }
        return selectedGenres
    }

    // Method to save the selected genre preferences to Firestore
    private fun saveGenrePreferences(selectedGenres: List<String>) {
        // Get the current user's ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            // Update the user's document with the selected genre preferences
            db.collection("users").document(userId)
                .update("genrePreferences", selectedGenres, "isFirstLogin", false)
                .addOnSuccessListener {
                    // Show a success message
                    Toast.makeText(activity, "Preferences saved", Toast.LENGTH_SHORT).show()
                    // Navigate to HomeFragment
                    (activity as? MainActivity)?.replaceFragment(HomeFragment(), "Home")
                }
                .addOnFailureListener {
                    // Show a failure message
                    Toast.makeText(activity, "Failed to save preferences", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(activity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
