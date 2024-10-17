package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.GroupAdapter
import com.example.booknook.GroupItem
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindGroupFragment  : Fragment() {

    // Declaring variables for the EditText and Button UI elements
    private lateinit var searchEdit: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private val groupList = mutableListOf<GroupItem>()

    // navigation buttons
    private lateinit var myGroups:  Button
    private lateinit var manage: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Page UI elements
        searchEdit = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        // Set up search button click listener to perform search
        searchButton.setOnClickListener {
            val searchText = searchEdit.text.toString().trim()
            searchGroups(searchText)
        }

        // navigation buttons
        myGroups = view.findViewById(R.id.myGroups)
        manage = view.findViewById(R.id.manageGroups)

        myGroups.setOnClickListener()
        {
            val groupsFragment = GroupsFragment()
            (activity as MainActivity).replaceFragment(groupsFragment, "My Groups")
        }

        manage.setOnClickListener()
        {
            val manageGroupsFragment = ManageGroupsFragment()
            (activity as MainActivity).replaceFragment(manageGroupsFragment, "Manage Groups")
        }

        // Recycler View
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        groupAdapter = GroupAdapter(groupList) { groupItem ->
            // Handle group item click
            Toast.makeText(context, "Clicked: ${groupItem.groupName}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = groupAdapter

        // Load all groups by default when fragment is opened
        loadGroupsFromFirestore()

    }

    private fun loadGroupsFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("groups").get()
            .addOnSuccessListener { documents ->
                groupList.clear()  // Clear any existing data

                for (document in documents) {
                    // Retrieve the members list from the document
                    val members = document.get("members") as? List<String>
                    // Doesn't load groups that the user is already a part of
                    if (members != null && !(members.contains(userId))) {
                        val group = document.toObject(GroupItem::class.java).copy(id = document.id)
                        groupList.add(group)
                    }
                }

                groupAdapter.notifyDataSetChanged()  // Update RecyclerView with new data
            }
            .addOnFailureListener { e ->
                Log.w("GroupsFragment", "Error loading groups: ${e.message}")
            }
    }

    private fun searchGroups(query: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .get()
            .addOnSuccessListener { documents ->
                groupList.clear() // Clear existing list

                for (document in documents) {
                    val group = document.toObject(GroupItem::class.java).copy(id = document.id)

                    // Check if the group name contains the query (case-insensitive)
                    if (group.groupName.contains(query, ignoreCase = true)) {
                        groupList.add(group)
                    }
                }

                groupAdapter.notifyDataSetChanged() // Refresh the RecyclerView
            }
            .addOnFailureListener { e ->
                Log.w("FindGroupFragment", "Error searching groups: ${e.message}")
            }
    }

}