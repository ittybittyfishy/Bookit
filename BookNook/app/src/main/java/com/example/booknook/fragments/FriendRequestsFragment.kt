package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequest
import com.example.booknook.FriendRequestAdapter
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendRequestsFragment : Fragment() {
    // Declare elements
    private lateinit var friendsButton: Button
    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var friendRequestsRecyclerView: RecyclerView
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private lateinit var friendRequests: MutableList<FriendRequest>
    private lateinit var numFriendRequests: TextView
    private lateinit var db: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_requests_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        friendsButton = view.findViewById(R.id.friends_button)
        blockedButton = view.findViewById(R.id.blocked_button)
        numFriendRequests = view.findViewById(R.id.num_friend_reqs)

        // Switches to Friends fragment upon click of button
        friendsButton.setOnClickListener {
            val friendsFragment = FriendsFragment()
            (activity as MainActivity).replaceFragment(friendsFragment, "Friends")
        }

        // Switches to Blocked fragment upon click of button
        blockedButton.setOnClickListener {
            val blockedFragment = BlockedUsersFragment()
            (activity as MainActivity).replaceFragment(blockedFragment, "Friends")
        }

        // References recycler view
        friendRequestsRecyclerView = view.findViewById(R.id.friend_reqs_recycler_view)
        friendRequestsRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays friend requests in 2 columns

        loadFriendRequests()  // Calls function to get friend requests
    }

    // Function to load all of the user's friend requests
    private fun loadFriendRequests() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Gets the current user
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(activity,"Error loading friend requests", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val friendRequests = documentSnapshot.get("friendRequests") as? List<Map<String, Any>>
                        if (friendRequests != null) {
                            // Gets the number of friend requests
                            val numRequests = friendRequests.size
                            // Update text to display the number of friend requests
                            numFriendRequests.text = "You have ($numRequests) friend request(s)"
                            // Maps each friend request to a FriendRequest object
                            val friendRequestList = friendRequests.map { request ->
                                FriendRequest(
                                    username = request["senderUsername"] as String,
                                    senderId = request["senderId"] as String
                                )
                            }
                            // Calls adapter with list of FriendRequest objects and functions to handle accepting and rejecting requests
                            friendRequestsRecyclerView.adapter = FriendRequestAdapter(
                                friendRequestList,
                                onAcceptClick = { acceptFriendRequest(it, currentUserId) },
                                onRejectClick = { rejectFriendRequest(it, currentUserId) }
                            )
                        }
                    }
                }
        }
    }
    // Function to accept a friend request
    private fun acceptFriendRequest(request: FriendRequest, currentUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUserId)  // Gets the current user's document
        val senderRef = db.collection("users").document(request.senderId)  // Gets the sender's document

        userRef.get().addOnSuccessListener { document ->
            val currentUsername = document.getString("username")  // Gets the current user's username

            // Add the friend to the user's friends collection
            userRef.update("friends", FieldValue.arrayUnion(
                mapOf(
                    "friendId" to request.senderId,
                    "friendUsername" to request.username
                )
            )).addOnSuccessListener {
                FirebaseAuth.getInstance().currentUser?.let { currentUser ->
                    // Add the current user as a friend to sender's friends collection as well
                    senderRef.update("friends", FieldValue.arrayUnion(
                        mapOf(
                            "friendId" to currentUserId,
                            "friendUsername" to currentUsername
                        )
                    ))
                }
            }
        }

        // Removes the friend request after finished
        userRef.update("friendRequests", FieldValue.arrayRemove(
            mapOf(
                "senderId" to request.senderId,
                "senderUsername" to request.username,
                "receiverId" to currentUserId,
                "status" to "pending"
            )
        ))
        Toast.makeText(activity, "${request.username} added as friend", Toast.LENGTH_SHORT).show()
    }

    // Function to reject a friend request
    private fun rejectFriendRequest(request: FriendRequest, currentUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUserId)

        // Removes the friend request
        userRef.update("friendRequests", FieldValue.arrayRemove(
            mapOf(
                "senderId" to request.senderId,
                "senderUsername" to request.username,
                "receiverId" to currentUserId,
                "status" to "pending"
            )
        ))
        Toast.makeText(activity, "${request.username} declined", Toast.LENGTH_SHORT).show()
    }
}


