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
            // Handle account button click
            val requestsFragment = FriendRequestsTab()
            (activity as MainActivity).replaceFragment(requestsFragment, "Friends")
        }

        blockedButton.setOnClickListener {
            // Handle account button click
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

    private fun searchUser(username: String) {
        // To-do: Display user's profile after looking up their username
    }
}