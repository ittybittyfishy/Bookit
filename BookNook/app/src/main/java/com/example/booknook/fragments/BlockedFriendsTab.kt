package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth

class BlockedFriendsTab : Fragment() {
    private lateinit var friendsButton: Button
    private lateinit var requestsButton: Button
    private lateinit var blockedButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blocked_friends_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        friendsButton = view.findViewById(R.id.friends_button)
        requestsButton = view.findViewById(R.id.requests_button)

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
    }
}