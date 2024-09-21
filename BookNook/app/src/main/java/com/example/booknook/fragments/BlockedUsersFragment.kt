package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BlockedUser
import com.example.booknook.BlockedUserAdapter
import com.example.booknook.Friend
import com.example.booknook.FriendAdapter
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class BlockedUsersFragment : Fragment() {
    private lateinit var friendsButton: Button
    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var searchButton: Button
    private lateinit var searchBar: EditText
    private lateinit var blockedRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blocked_users_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        // Initialize buttons and views
        friendsButton = view.findViewById(R.id.friends_button)
        requestsButton = view.findViewById(R.id.requests_button)
        searchButton = view.findViewById(R.id.block_user_search_button)
        searchBar = view.findViewById(R.id.block_user_search)

        // Switches to Friends fragment upon click of button
        friendsButton.setOnClickListener {
            val friendsFragment = FriendsFragment()
            (activity as MainActivity).replaceFragment(friendsFragment, "Friends")
        }

        // Switches to Requests fragment upon click of button
        requestsButton.setOnClickListener {
            // Handle account button click
            val requestsFragment = FriendRequestsFragment()
            (activity as MainActivity).replaceFragment(requestsFragment, "Friends")
        }

        // Handle search for user by username button click
        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUser(query)  // Searches for user
            } else {
                Toast.makeText(activity, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        blockedRecyclerView = view.findViewById(R.id.blocked_recycler_view)
        blockedRecyclerView.layoutManager = GridLayoutManager(context, 2) // Displays blocked user in 2 columns

        loadBlockedUsers()  // loads the blocked users
    }

    // Function to load all of the user's blocked users
    private fun loadBlockedUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Gets the current user
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(activity,"Error loading blocked users", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    // If user is found
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Gets user's blocked users as a list
                        val blockedUsers = documentSnapshot.get("blockedUsers") as? List<Map<String, Any>>
                        if (blockedUsers != null) {
                            // Maps each friend request to a BlockedUser object
                            val blockedUserList = blockedUsers.map { blockedUser ->
                                BlockedUser(
                                    blockedUserId = blockedUser["blockedUserId"] as String,
                                    blockedUsername = blockedUser["blockedUsername"] as String
                                )
                            }
                            // Calls adapter with list of BlockedUser objects and functions to handle accepting and rejecting requests
                            blockedRecyclerView.adapter = BlockedUserAdapter(blockedUserList)
                        }
                    }
                }
        }
    }

    // Function to search for a user with their username
    // Temporary just to test blocking users and adding in database
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
                            blockUser(receiverId)  // calls function to block a user
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
            val senderRef = db.collection("users").document(senderId)  // gets the sender's document
            val blockedUserRef = db.collection("users").document(receiverId)  // gets the blocked user's document

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")  // gets the sender's username

                if (senderUsername != null) {
                    blockedUserRef.get().addOnSuccessListener { blockedUserDoc ->
                        val blockedUsername = blockedUserDoc?.getString("username")  // gets the blocked user's username

                        if (blockedUsername != null) {
                            // creates a map of blocked user's details
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
                            Toast.makeText(activity, "Blocked user's username not found", Toast.LENGTH_SHORT).show()
                        }
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