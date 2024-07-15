package com.example.booknook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // get users UID
        val userId = auth.currentUser?.uid

        // Find the TextView in the layout
        val loggedInTextView: TextView = findViewById(R.id.loggedInTextView)


        // if user is logged in, fetch username from firebase
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