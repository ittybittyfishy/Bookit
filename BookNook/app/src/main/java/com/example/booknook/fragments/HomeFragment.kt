package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//Yunjong Noh
//Showing personal book Recommendation
class HomeFragment : Fragment() {
    // declare UI componets
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

        // 첫 번째 책
        val bookCoverImageView1: ImageView = view.findViewById(R.id.bookCoverImageView1)
        val bookTitleTextView1: TextView = view.findViewById(R.id.bookTitleTextView1)
        val bookAuthorsTextView1: TextView = view.findViewById(R.id.bookAuthorsTextView1)
        val bookGenresTextView1: TextView = view.findViewById(R.id.bookGenresTextView1)

        bookTitleTextView1.text = "Book Title 1"
        bookAuthorsTextView1.text = "Author 1"
        bookGenresTextView1.text = "Fiction"
        Glide.with(this).load(R.drawable.placeholder_image).into(bookCoverImageView1)

        // 두 번째 책
        val bookCoverImageView2: ImageView = view.findViewById(R.id.bookCoverImageView2)
        val bookTitleTextView2: TextView = view.findViewById(R.id.bookTitleTextView2)
        val bookAuthorsTextView2: TextView = view.findViewById(R.id.bookAuthorsTextView2)
        val bookGenresTextView2: TextView = view.findViewById(R.id.bookGenresTextView2)

        bookTitleTextView2.text = "Book Title 2"
        bookAuthorsTextView2.text = "Author 2"
        bookGenresTextView2.text = "Mystery"
        Glide.with(this).load(R.drawable.placeholder_image).into(bookCoverImageView2)

        // 세 번째 책
        val bookCoverImageView3: ImageView = view.findViewById(R.id.bookCoverImageView3)
        val bookTitleTextView3: TextView = view.findViewById(R.id.bookTitleTextView3)
        val bookAuthorsTextView3: TextView = view.findViewById(R.id.bookAuthorsTextView3)
        val bookGenresTextView3: TextView = view.findViewById(R.id.bookGenresTextView3)

        bookTitleTextView3.text = "Book Title 3"
        bookAuthorsTextView3.text = "Author 3"
        bookGenresTextView3.text = "Science Fiction"
        Glide.with(this).load(R.drawable.placeholder_image).into(bookCoverImageView3)

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
                    loggedInTextView.text = "Username not found"
                }
            }.addOnFailureListener { exception ->
                loggedInTextView.text = "Error: ${exception.message}"
            }
        }
    }
}
