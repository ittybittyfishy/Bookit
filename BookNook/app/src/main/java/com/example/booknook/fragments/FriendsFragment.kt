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

class FriendsFragment : Fragment() {

    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var searchButton: Button
    private lateinit var searchBar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        requestsButton = view.findViewById(R.id.requests_button)
        blockedButton = view.findViewById(R.id.blocked_button)
        searchButton = view.findViewById(R.id.search_friend_button)
        searchBar = view.findViewById(R.id.search_friend_bar)

        // Set listeners
        requestsButton.setOnClickListener {
            // Handle requests button click
            val requestsFragment = FriendRequestsTab()
            (activity as MainActivity).replaceFragment(requestsFragment, "Friends")
        }

        blockedButton.setOnClickListener {
            // Handle blocked button click
            val blockedFragment = BlockedFriendsTab()
            (activity as MainActivity).replaceFragment(blockedFragment, "Friends")
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

    private fun searchUser(username: String) {  // Function to search for a user with their username
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
                            Toast.makeText(activity, "Can't add yourself as friend", Toast.LENGTH_SHORT).show()
                        } else {
                            // To-do: Pull up user's profile after looking up their username instead of requesting right away
                            //
                            sendFriendRequest(receiverId)  // calls function to send a friend request
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

    private fun sendFriendRequest(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val senderId = currentUser.uid
            val senderRef = db.collection("users").document(senderId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    val friendRequest = hashMapOf(
                        "senderId" to senderId,
                        "senderUsername" to senderUsername,
                        "receiverId" to receiverId,
                        "status" to "pending"
                    )

                    // Update receiver's friend requests array
                    db.collection("users").document(receiverId)
                        .update("friendRequests", FieldValue.arrayUnion(friendRequest))
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Friend request sent", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e -> Toast.makeText(activity, "Failed to send friend request: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "Sender username not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed to send friend request", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


}