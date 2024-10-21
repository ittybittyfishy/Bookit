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
                        activity?.let { context ->
                            Toast.makeText(context, "Error loading blocked users", Toast.LENGTH_SHORT)
                                .show()
                        }
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

}