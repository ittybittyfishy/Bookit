package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.MainActivity
import com.example.booknook.R

class GroupRecommendationsFragment : Fragment() {
    private lateinit var addRecommendationButton: Button
    private lateinit var recommendationsRecyclerView: RecyclerView

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
        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView)

        // Opens page to add recommendations when button is pressed
        addRecommendationButton.setOnClickListener {
            val addRecommendationFragment = AddRecommendationFragment()
            (activity as MainActivity).replaceFragment(addRecommendationFragment, "Add Recommendation")
        }

        recommendationsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        recommendationsRecyclerView = recommendationsAdapter
    }

}