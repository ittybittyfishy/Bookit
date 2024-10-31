package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booknook.MainActivity
import com.example.booknook.R

class GroupRecommendationsFragment : Fragment() {
    private lateinit var addRecommendationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addRecommendationButton = view.findViewById(R.id.addRecommendationButton)

        // Opens page to add recommendations when button is pressed
        addRecommendationButton.setOnClickListener {
            val addRecommendationFragment = AddRecommendationFragment()
            (activity as MainActivity).replaceFragment(addRecommendationFragment, "Add Recommendation")
        }
    }

}