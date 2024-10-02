package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booknook.MainActivity
import com.example.booknook.R

class GroupsFragment : Fragment() {

    lateinit var findGroups: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initalize buttons
        findGroups = view.findViewById(R.id.findGroups)

        // set listener
        findGroups.setOnClickListener()
        {
            val findGroupFragment = FindGroupFragment()
            (activity as MainActivity).replaceFragment(findGroupFragment, "Find Group")
        }
    }
}