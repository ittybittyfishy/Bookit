package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
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
    private lateinit var searchEdit: EditText // Input field for the search query
    private lateinit var searchButton: ImageButton // Button to initiate the search
    private lateinit var recyclerView: RecyclerView  // RecyclerView to display the list of groups
    private lateinit var groupAdapter: GroupAdapter // Adapter for populating the RecyclerView
    private val groupList = mutableListOf<GroupItem>() // List to hold group items retrieved from Firestore
    private lateinit var sortGroups: Spinner

    // Navigation buttons for switching between different fragments
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

        // Initialize UI elements for searching groups
        searchEdit = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        // Set up click listener for the search button to perform the search operatiom
        searchButton.setOnClickListener {
            val searchText = searchEdit.text.toString().trim() // Get and trim the input text
            searchGroups(searchText)
        }

        // Initialize navigation buttons and set click listeners
        myGroups = view.findViewById(R.id.myGroups)
        manage = view.findViewById(R.id.manageGroups)

        // Navigate to "My Groups" fragment when the button is clicked
        myGroups.setOnClickListener()
        {
            val groupsFragment = GroupsFragment()
            (activity as MainActivity).replaceFragment(groupsFragment, "My Groups")
        }

        // Navigate to "Manage Groups" fragment when the button is clicked
        manage.setOnClickListener()
        {
            val manageGroupsFragment = ManageGroupsFragment()
            (activity as MainActivity).replaceFragment(manageGroupsFragment, "Manage Groups")
        }

        // Initialize the RecyclerView for displaying group items
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter and set click listeners for group items
        groupAdapter = GroupAdapter(groupList) { groupItem ->
            // Handle group item click
            if (groupItem.private)
            {
                // If the group is private, show the GroupPrivateFragment
                val groupPrivate = GroupPrivateFragment().apply {
                    arguments = Bundle().apply {
                        putString("GROUP_ID", groupItem.id)
                        putString("GROUP_CREATOR_ID", groupItem.createdBy)
                    }
                }
                groupPrivate.show(parentFragmentManager, "GroupPrivateDialog")
            }
            else {
                // If the group is public, open the group profile
                openGroupHomepage(groupItem)
            }
        }
        recyclerView.adapter = groupAdapter// Set the adapter for the RecyclerView

        sortGroups = view.findViewById(R.id.sortGroups)

        // deploy spinner
        setupSortSpinner()

        // Load all groups by default when fragment is opened
        loadGroupsFromFirestore()

    }

    // Veronica Nguyen
    // Function to open the group homepage
    private fun openGroupHomepage(groupItem: GroupItem) {
        val findGroupHomepageFragment = FindGroupHomepageFragment()
        val bundle = Bundle()
        // Passes the group id and group creator id to the fragment
        bundle.putString("GROUP_ID", groupItem.id)
        bundle.putString("GROUP_CREATOR_ID", groupItem.createdBy)
        findGroupHomepageFragment.arguments = bundle
        // Navigates to the homepage of the group
        (activity as MainActivity).replaceFragment(findGroupHomepageFragment, "${groupItem.groupName}")
    }

    // Method to load all groups from Firestore
    private fun loadGroupsFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Retrieve all group documents from the "groups" collection
        db.collection("groups").get()
            .addOnSuccessListener { documents ->
                groupList.clear()  // Clear any existing data

                for (document in documents) {
                    // Retrieve the members list from the document
                    val members = document.get("members") as? List<String>
                    // Load groups that the user is not already a member of
                    if (members != null && !(members.contains(userId))) {
                        // Convert document to GroupItem and add it to the list
                        val group = document.toObject(GroupItem::class.java).copy(id = document.id)
                        groupList.add(group) // Add the group to the list
                    }
                }

                groupAdapter.notifyDataSetChanged()  // Update RecyclerView with new data
            }
            .addOnFailureListener { e ->
                Log.w("GroupsFragment", "Error loading groups: ${e.message}")
            }
    }

    // Method to search for groups based on the user's query
    private fun searchGroups(query: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .get()
            .addOnSuccessListener { documents ->
                groupList.clear() // Clear existing list to prepare for search results

                for (document in documents) {
                    // Convert document to GroupItem
                    val group = document.toObject(GroupItem::class.java).copy(id = document.id)

                    // Check if the group name contains the query (case-insensitive)
                    if (group.groupName.contains(query, ignoreCase = true)) {
                        groupList.add(group)
                    }
                }

                groupAdapter.notifyDataSetChanged() // Refresh the RecyclerView to show search results
            }
            .addOnFailureListener { e ->
                Log.w("FindGroupFragment", "Error searching groups: ${e.message}")
            }
    }

    // Setup the spinner for sorting options and handle the selection event
    private fun setupSortSpinner() {
        // Create an ArrayAdapter using the string array and custom spinner item layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.groups_sort_options,
            R.layout.item_collections_spinner_layout  // Custom layout for the spinner display
        )
        // Apply the adapter to the spinner
        adapter.setDropDownViewResource(R.layout.item_collections_spinner_dropdown) // The layout for dropdown items
        sortGroups.adapter = adapter

        // Handle selection changes as before
        sortGroups.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSortOption = parent.getItemAtPosition(position).toString()
                sortGroups(selectedSortOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action if nothing is selected
            }
        }
    }

    // Function to sort books within each custom collection based on the selected option
    private fun sortGroups(sortOption: String) {
        when (sortOption) {
            "Name A-Z" -> {
                // Sort groupList alphabetically by group name
                groupList.sortBy { it.groupName.lowercase() }
            }
            "Name Z-A" -> {
                // Sort groupList in reverse alphabetical order by group name
                groupList.sortByDescending { it.groupName.lowercase() }
            }
            "Members ↑" -> {
                // Sort groupList alphabetically by creater name
                groupList.sortBy {it.members.size}
            }
            "Members ↓" -> {
                groupList.sortByDescending {it.members.size}
            }
        }

        // Notify the adapter about the updated data
        groupAdapter.notifyDataSetChanged()
    }

}