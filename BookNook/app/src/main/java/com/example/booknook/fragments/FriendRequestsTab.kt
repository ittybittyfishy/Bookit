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
import com.google.firebase.firestore.FirebaseFirestore

class FriendRequestsTab : Fragment() {
    // Declare elements
    private lateinit var friendsButton: Button
    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button
    private lateinit var friendRequestsRecyclerView: RecyclerView
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private lateinit var friendRequests: MutableList<FriendRequest>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_requests_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        friendsButton = view.findViewById(R.id.friends_button)
        blockedButton = view.findViewById(R.id.blocked_button)

        // Set listeners
        friendsButton.setOnClickListener {
            // Handle "Friends" button click
            val friendsFragment = FriendsFragment()
            (activity as MainActivity).replaceFragment(friendsFragment, "Friends")
        }

        blockedButton.setOnClickListener {
            // Handle "Blocked" button click
            val blockedFragment = BlockedFriendsTab()
            (activity as MainActivity).replaceFragment(blockedFragment, "Friends")
        }

        // Initalize recycler view for friend requests
        friendRequestsRecyclerView = view.findViewById(R.id.friend_reqs_recycler_view)
        friendRequestsRecyclerView.layoutManager = LinearLayoutManager(activity)

        friendRequests = mutableListOf()  // Initialize friend requests list

        // Set up the friend request adapter with arguments
        friendRequestAdapter = FriendRequestAdapter(friendRequests,
            onAcceptClick = { friendRequest -> acceptFriendRequest(friendRequest) },
            onRejectClick = { friendRequest -> rejectFriendRequest(friendRequest) }
        )

        friendRequestsRecyclerView.adapter = friendRequestAdapter // set the adapter to the recycler view
        fetchFriendRequests()  // calls function to get friend requests
    }

    private fun acceptFriendRequest(friendRequest: FriendRequest) {
        TODO("Not yet implemented")
    }

    private fun rejectFriendRequest(friendRequest: FriendRequest) {
        TODO("Not yet implemented")
    }

    private fun fetchFriendRequests() {
        TODO("Not yet implemented")
    }
}