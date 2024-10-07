package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindGroupFragment  : Fragment() {

    // Declaring variables for the EditText and Button UI elements
    private lateinit var searchEdit: EditText
    private lateinit var createGroupButton: Button
    private lateinit var searchButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEdit = view.findViewById(R.id.searchEdit)
        searchButton = view.findViewById(R.id.searchButton)
        createGroupButton = view.findViewById(R.id.createGroupButton)


        createGroupButton.setOnClickListener {
            val createGroup = CreateGroupFragment()
            createGroup.show(parentFragmentManager, "CreateGroupDialog")
        }

    }

}