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
import androidx.fragment.app.DialogFragment
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindGroupFragment  : Fragment() {

    // Declaring variables for the EditText and Button UI elements
    private lateinit var searchEdit: EditText
    private lateinit var searchButton: ImageButton

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

    }

}