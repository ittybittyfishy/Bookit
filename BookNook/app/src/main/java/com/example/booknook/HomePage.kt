package com.example.booknook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Retrieve the username from the intent extras
        val username = intent.getStringExtra("username")

        // Find the TextView in the layout
        val loggedInTextView: TextView = findViewById(R.id.loggedInTextView)

        // Set the text to display "logged in as username"
        loggedInTextView.text = "Logged in as\n $username"
    }
}