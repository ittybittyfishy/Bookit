package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequest
import com.example.booknook.FriendRequestAdapter
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendRequestsTab : Fragment() {
    // Declare elements
    private lateinit var friendsButton: Button
    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var friendRequestsRecyclerView: RecyclerView
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private lateinit var friendRequests: MutableList<FriendRequest>
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

        friendsButton.setOnClickListener {
            val friendsFragment = FriendsFragment()
            (activity as MainActivity).replaceFragment(friendsFragment, "Friends")
        }

        blockedButton.setOnClickListener {
            val blockedFragment = BlockedFriendsTab()
            (activity as MainActivity).replaceFragment(blockedFragment, "Friends")
        }

        friendRequestsRecyclerView = view.findViewById(R.id.friend_reqs_recycler_view)
        friendRequestsRecyclerView.layoutManager = LinearLayoutManager(context)

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
                            val friendRequestList = friendRequests.map { request ->
                                FriendRequest(
                                    username = request["senderUsername"] as String,
                                    senderId = request["senderId"] as String
                                )
                            }
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
        val userRef = db.collection("users").document(currentUserId)
        val senderRef = db.collection("users").document(request.senderId)

        userRef.get().addOnSuccessListener { document ->
            val currentUsername = document.getString("username")

            // Add the friend to the user's friends collection
            userRef.update("friends", FieldValue.arrayUnion(
                mapOf(
                    "friendId" to request.senderId,
                    "friendUsername" to request.username
                )
            )).addOnSuccessListener {
                FirebaseAuth.getInstance().currentUser?.let { currentUser ->
                    senderRef.update("friends", FieldValue.arrayUnion(
                        mapOf(
                            "friendId" to currentUserId,
                            "friendUsername" to currentUsername
                        )
                    ))
                }
            }
        }

        // Removes the friend request
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


