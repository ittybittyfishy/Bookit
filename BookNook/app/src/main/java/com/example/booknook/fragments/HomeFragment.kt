package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookItemCollection
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.example.booknook.BookRecommendationAdapter

class HomeFragment : Fragment() {

    private lateinit var loggedInTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    //Yunjong Noh
    //Showing personal book Recommendation
    // declare UI componets
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookRecommendationAdapter
    private lateinit var bookList: List<BookItemCollection>

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

        //Yunjong Noh
        //Initalize view of personal recommendation
        recyclerView = view.findViewById(R.id.recyclerViewRecommendations)
        // Setup RecyclerView with horizontal layout
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

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
            }
                .addOnFailureListener { exception ->
                    loggedInTextView.text = "Error: ${exception.message}"
                }
        }

        //Yunjong Noh
        // Load book recommendation data (For fow, placeholder as an example)
        bookList = listOf(
            BookItemCollection("Book Title 1", listOf("Author 1"), "R.drawable.placeholder_image", 300, genres = listOf("Fiction")),
            BookItemCollection("Book Title 2", listOf("Author 2"), "R.drawable.placeholder_image", 250, genres = listOf("Mystery")),
            BookItemCollection("Book Title 3", listOf("Author 3"), "R.drawable.placeholder_image", 400, genres = listOf("Science Fiction"))
        )

        // Set up the RecyclerView adapter
        bookAdapter = BookRecommendationAdapter(bookList)
        recyclerView.adapter = bookAdapter
    }
}
