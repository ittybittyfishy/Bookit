package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var loggedInTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        // Initialize FirebaseAuth and FirebaseFirestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        loggedInTextView = view.findViewById(R.id.loggedInTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get users UID
        val userId = auth.currentUser?.uid

        // If user is logged in, fetch username from Firebase
        userId?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username")

                    // Set the text to display "logged in as username"
                    loggedInTextView.text = "Logged in as\n$username"
                } else {
                    // Handle case where user document does not exist
                    loggedInTextView.text = "Username not found"
                }
            }
                .addOnFailureListener { exception ->
                    // Handle errors
                    loggedInTextView.text = "Error: ${exception.message}"
                }
        }
    }
}