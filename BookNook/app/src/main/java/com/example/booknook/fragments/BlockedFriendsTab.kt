package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class BlockedFriendsTab : Fragment() {
    private lateinit var friendsButton: Button
    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var searchButton: Button
    private lateinit var searchBar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blocked_friends_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons and views
        friendsButton = view.findViewById(R.id.friends_button)
        requestsButton = view.findViewById(R.id.requests_button)
        searchButton = view.findViewById(R.id.block_user_search_button)
        searchBar = view.findViewById(R.id.block_user_search)

        // Set listeners
        friendsButton.setOnClickListener {
            // Handle account button click
            val friendsFragment = FriendsFragment()
            (activity as MainActivity).replaceFragment(friendsFragment, "Friends")
        }

        requestsButton.setOnClickListener {
            // Handle account button click
            val requestsFragment = FriendRequestsTab()
            (activity as MainActivity).replaceFragment(requestsFragment, "Friends")
        }

        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUser(query)
            } else {
                Toast.makeText(activity, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to search for a user with their username
    private fun searchUser(username: String) {
        val db = FirebaseFirestore.getInstance()
        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        db.collection("users").whereEqualTo("username", username).get()  // checks for username in documents in users collection
            .addOnCompleteListener { searchTask ->
                if (searchTask.isSuccessful)
                {
                    val result: QuerySnapshot? = searchTask.result  // gets result from Task object, which can be null
                    if (result != null && !result.isEmpty) {  // checks if result is null and contains at least one document
                        val userDocument = result.documents[0]  // retrieves first document
                        val receiverId = userDocument.id  // retrieves the receiver user's id
                        val userName = userDocument.getString("username")  // retrieves "username" field of receiver
                        if (senderId == receiverId) {
                            Toast.makeText(activity, "Can't block yourself", Toast.LENGTH_SHORT).show()
                        } else {
                            // To-do: Pull up user's profile after looking up their username instead of requesting right away
                            blockUser(receiverId)  // calls function to send a friend request
                            Toast.makeText(activity, "User found: $userName", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "User not found", Toast.LENGTH_SHORT).show()  // displays if user doesn't exist
                    }
                } else {
                    Toast.makeText(activity, "Error in retrieving documents", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Temporary test function to block a user
    private fun blockUser(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user

        if (currentUser != null) {
            val senderId = currentUser.uid  // senderId is the current user
            val senderRef = db.collection("users").document(senderId)
            val blockedUsername = db.collection("users").document(receiverId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    // creates a map of blocked user details
                    val blockedUser = hashMapOf(
                        "blockedUserId" to receiverId,
                        "blockedUsername" to blockedUsername
                    )

                    // Update current user's blocked users array in database
                    db.collection("users").document(senderId)
                        .update("blockedUsers", FieldValue.arrayUnion(blockedUser))
                        .addOnSuccessListener {
                            Toast.makeText(activity, "User blocked", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e -> Toast.makeText(activity, "Failed to block user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "Sender username not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed to block user", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}