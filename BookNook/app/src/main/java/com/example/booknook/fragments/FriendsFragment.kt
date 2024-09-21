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
import com.example.booknook.Friend
import com.example.booknook.FriendAdapter
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
    private lateinit var collapseOnlineButton: Button
    private lateinit var collapseOfflineButton: Button
    private lateinit var searchBar: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var onlineFriendsRecyclerView: RecyclerView
    private lateinit var offlineFriendsRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        // Initialize buttons and views
        requestsButton = view.findViewById(R.id.requests_button)
        blockedButton = view.findViewById(R.id.blocked_button)
        searchButton = view.findViewById(R.id.search_friend_button)
        searchBar = view.findViewById(R.id.search_friend_bar)
        collapseOnlineButton = view.findViewById(R.id.collapse_online_button)
        collapseOfflineButton = view.findViewById(R.id.collapse_offline_button)
        onlineFriendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        offlineFriendsRecyclerView = view.findViewById(R.id.offline_friends_recycler_view)

        // Set listeners for button click
        requestsButton.setOnClickListener {
            // Handle requests button click
            val requestsFragment = FriendRequestsFragment()
            (activity as MainActivity).replaceFragment(requestsFragment, "Friends")
        }

        blockedButton.setOnClickListener {
            // Handle blocked button click
            val blockedFragment = BlockedUsersFragment()
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

        onlineFriendsRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays online friends in 2 columns
        offlineFriendsRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays offline friends in 2 columns

        // Handles when collapse button for online friends is clicked
        collapseOnlineButton.setOnClickListener {
            if (onlineFriendsRecyclerView.visibility == View.GONE) {  // If the view is current collapsed
                onlineFriendsRecyclerView.visibility = View.VISIBLE  // Make the online friends visible
                collapseOnlineButton.text = "Collapse"  // Change text in button to "Collapse"
            } else {
                onlineFriendsRecyclerView.visibility = View.GONE  // If the view is currently expanded
                collapseOnlineButton.text = "Expand"  // Change text in button to "Expand"
            }
        }

        // Handles when collapse button for offline friends is clicked
        collapseOfflineButton.setOnClickListener {
            if (offlineFriendsRecyclerView.visibility == View.GONE) {  // If the view is current collapsed
                offlineFriendsRecyclerView.visibility = View.VISIBLE  // Make the offline friends visible
                collapseOfflineButton.text = "Collapse"  // Change text in button to "Collapse"
            } else {
                offlineFriendsRecyclerView.visibility = View.GONE  // If the view is currently expanded
                collapseOfflineButton.text = "Expand"  // Change text in button to "Expand"
            }
        }

        loadFriends()  // Loads the user's friends
    }

    // Function to load the user's friends
    private fun loadFriends() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Gets the current user
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(activity,"Error loading friends", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val friends = documentSnapshot.get("friends") as? List<Map<String, Any>>
                        if (friends != null) {
                            val onlineFriends = mutableListOf<Friend>()
                            val offlineFriends = mutableListOf<Friend>()

                            friends.forEach { friend ->
                                val isOnline = friend["isOnline"] as? Boolean ?: false  // casts value to boolean or false if the cast fails
                                val friendInfo = Friend(
                                    friendId = friend["friendId"] as String,
                                    friendUsername = friend["friendUsername"] as String
                                )
                                if (isOnline) {
                                    onlineFriends.add(friendInfo)
                                } else {
                                    offlineFriends.add(friendInfo)
                                }
                            }

                            val onlineFriendsRecyclerView = view?.findViewById<RecyclerView>(R.id.friends_recycler_view)
                            val offlineFriendsRecyclerView = view?.findViewById<RecyclerView>(R.id.offline_friends_recycler_view)

                            // Calls adapter with list of FriendRequest objects and functions to handle accepting and rejecting requests
                            onlineFriendsRecyclerView?.adapter = FriendAdapter(onlineFriends)
                            offlineFriendsRecyclerView?.adapter = FriendAdapter(offlineFriends)

                        }
                    }
                }
        }
    }

    // Function to search for a user with their username
    private fun searchUser(username: String) {
        val db = FirebaseFirestore.getInstance()
        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        if (senderId == null) {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(senderId).get()
            .addOnSuccessListener { senderDoc ->
                // Gets the user's current friends
                val allFriends = senderDoc?.get("friends") as? List<Map<String, Any>> ?: emptyList()

                db.collection("users").whereEqualTo("username", username).get()  // checks for username in documents in users collection
                    .addOnCompleteListener { searchTask ->
                        if (searchTask.isSuccessful)
                        {
                            val result: QuerySnapshot? = searchTask.result  // gets result from Task object, which can be null
                            if (result != null && !result.isEmpty) {  // checks if result is null and contains at least one document
                                val userDocument = result.documents[0]  // retrieves first document
                                val receiverId = userDocument.id  // retrieves the receiver user's id
                                val userName = userDocument.getString("username")  // retrieves "username" field of receiver

                                // Checks to see if they are already the current user's friend
                                val alreadyFriend = allFriends.any { friend ->  // Checks all friends in user's database
                                    friend["friendId"] == receiverId  // If any of the user's friends matches the receiverID
                                }

                                if (alreadyFriend) {
                                    // User can't send friend request if they're already friends
                                    Toast.makeText(activity, "$userName already added as friend", Toast.LENGTH_SHORT).show()
                                } else if (senderId == receiverId) {
                                    // User can't send a friend request to themselves
                                    Toast.makeText(activity, "Can't add yourself as friend", Toast.LENGTH_SHORT).show()
                                } else {
                                    // To-do: Pull up user's profile after looking up their username instead of requesting right away
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
    }

    // Function to send a friend request
    private fun sendFriendRequest(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user

        if (currentUser != null) {
            val senderId = currentUser.uid  // senderId is the current user
            val senderRef = db.collection("users").document(senderId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    // creates a map of friend request details
                    val friendRequest = hashMapOf(
                        "senderId" to senderId,
                        "senderUsername" to senderUsername,
                        "receiverId" to receiverId,
                        "status" to "pending"
                    )

                    // Update receiver's friend requests array in database
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