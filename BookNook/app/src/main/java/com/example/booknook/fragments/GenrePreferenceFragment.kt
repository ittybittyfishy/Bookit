package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.booknook.R
import com.example.booknook.MainActivity

class GenrePreferenceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_genre_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fictionCheckBox: CheckBox = view.findViewById(R.id.checkbox_fiction)
        val nonFictionCheckBox: CheckBox = view.findViewById(R.id.checkbox_nonfiction)
        val saveButton: Button = view.findViewById(R.id.button_save)

        saveButton.setOnClickListener {
            val selectedGenres = mutableListOf<String>()
            if (fictionCheckBox.isChecked) selectedGenres.add("Fiction")
            if (nonFictionCheckBox.isChecked) selectedGenres.add("Non-fiction")
            // Add checks for other genres

            saveGenrePreferences(selectedGenres)
        }
    }

    private fun saveGenrePreferences(selectedGenres: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .update("genrePreferences", selectedGenres, "isFirstLogin", false)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Preferences saved", Toast.LENGTH_SHORT).show()
                    // Navigate to HomeFragment
                    (activity as? MainActivity)?.replaceFragment(HomeFragment(), "Home")
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to save preferences", Toast.LENGTH_SHORT).show()
                }
        }
    }
}