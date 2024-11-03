package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import com.google.firebase.firestore.auth.User

class FriendsFragment : Fragment() {

    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var searchButton: ImageButton
    private lateinit var collapseOnlineButton: ImageButton
    private lateinit var collapseOfflineButton: ImageButton
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
        searchButton = view.findViewById(R.id.search_button)
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

        // Handle search friend by username button click
        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUser(query)  // Searches for user
            } else {
                Toast.makeText(activity, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        onlineFriendsRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays online friends in 2 columns
        offlineFriendsRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays offline friends in 2 columns

        // Handles when collapse/expand button for online friends is clicked
        collapseOnlineButton.setOnClickListener {
            if (onlineFriendsRecyclerView.visibility == View.GONE) {  // If the view is currently collapsed
                onlineFriendsRecyclerView.visibility = View.VISIBLE  // Make the online friends visible
                collapseOnlineButton.setImageResource(R.drawable.collapse_button)  // Show collapse button
            } else {  // If the view is currently expanded
                onlineFriendsRecyclerView.visibility = View.GONE  // Sets view to invisible to collapse friends
                collapseOnlineButton.setImageResource(R.drawable.expand_button)  // Show expand button
            }
        }

        // Handles when collapse/expand button for offline friends is clicked
        collapseOfflineButton.setOnClickListener {
            if (offlineFriendsRecyclerView.visibility == View.GONE) {  // If the view is currently collapsed
                offlineFriendsRecyclerView.visibility = View.VISIBLE  // Make the offline friends visible
                collapseOfflineButton.setImageResource(R.drawable.collapse_button)  // Show collapse button
            } else {  // If the view is currently expanded
                offlineFriendsRecyclerView.visibility = View.GONE  // Sets view to invisible to collapse friends
                collapseOfflineButton.setImageResource(R.drawable.expand_button)  // Show expand button
            }
        }
        loadFriends()  // Loads the user's friends
    }

    // Function to load the user's friends
    private fun loadFriends() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                .addSnapshotListener { documentSnapshot, e ->  // Lists for changes in user's document
                    if (e != null) {
                        activity?.let { context ->
                            Toast.makeText(context, "Error loading friends", Toast.LENGTH_SHORT).show()
                        }
                        return@addSnapshotListener  // Returns early if there is an error
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Gets the user's friends list
                        val friends = documentSnapshot.get("friends") as? List<Map<String, Any>>
                        // Creates list for online and offline friends
                        if (friends != null) {
                            val onlineFriends = mutableListOf<Friend>()
                            val offlineFriends = mutableListOf<Friend>()

                            // Loops through each friend
                            friends.forEach { friend ->
                                val friendId = friend["friendId"] as String
                                val friendUsername = friend["friendUsername"] as String

                                // Checks to see if the friend is online or offline
                                db.collection("users").document(friendId).get()
                                    .addOnSuccessListener { friendDocument ->
                                        val isOnline = friendDocument.getBoolean("isOnline") ?: false

                                        // Friend information
                                        val friendInfo = Friend(
                                            friendId = friendId,
                                            friendUsername = friendUsername
                                        )

                                        // Add friend to the appropriate list based on isOnline status
                                        if (isOnline) {
                                            onlineFriends.add(friendInfo)
                                        } else {
                                            offlineFriends.add(friendInfo)
                                        }

                                        // Update recycler views of each list and allows navigation to friend's profile
                                        onlineFriendsRecyclerView.adapter = FriendAdapter(onlineFriends) { selectedFriend ->
                                            openFriendProfile(selectedFriend)
                                        }
                                        offlineFriendsRecyclerView.adapter = FriendAdapter(offlineFriends) { selectedFriend ->
                                            openFriendProfile(selectedFriend)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(activity, "Error loading friend data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                }
        }
    }

    // Function to navigate to the friend's profile when clicking on them
    private fun openFriendProfile(selectedFriend: Friend) {
        val friendProfileFragment = FriendProfileFragment()
        val bundle = Bundle()
        bundle.putString("receiverId", selectedFriend.friendId)
        bundle.putString("receiverUsername", selectedFriend.friendUsername)
        friendProfileFragment.arguments = bundle
        (activity as MainActivity).replaceFragment(friendProfileFragment, "Profile")
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
                val blockedUsers = senderDoc?.get("blockedUsers") as? List<Map<String, Any>> ?: emptyList()

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

                                // Checks to see if the user is blocked
                                val isBlocked = blockedUsers.any { blockedUser ->
                                    blockedUser["blockedUserId"] == receiverId
                                }

                                if (isBlocked) {
                                    // Navigate to BlockedUserProfileFragment
                                    val blockedProfileFragment = BlockedUserProfileFragment()
                                    val bundle = Bundle()
                                    bundle.putString("blockedUserId", receiverId)
                                    bundle.putString("blockedUsername", username)
                                    blockedProfileFragment.arguments = bundle
                                    (activity as MainActivity).replaceFragment(blockedProfileFragment, "Profile")
                                    Toast.makeText(activity, "User is blocked", Toast.LENGTH_SHORT).show()
                                } else if (senderId == receiverId) {
                                    // User can't send a friend request to themselves
                                    Toast.makeText(activity, "Can't add yourself as friend", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Pulls up user's profile after looking up their username
                                    val friendProfileFragment = FriendProfileFragment()
                                    val bundle = Bundle()
                                    bundle.putString("receiverId", receiverId)
                                    friendProfileFragment.arguments = bundle  // Sends receiver id to the friend profile fragment
                                    (activity as MainActivity).replaceFragment(friendProfileFragment, "Profile")
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
}