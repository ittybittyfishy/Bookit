package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.booknook.MainActivity
import com.example.booknook.R

class AddRecommendationBookDetailsFragment: Fragment() {
    private lateinit var searchBookButton: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_recommendation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isbn = arguments?.getString("isbn")
        Log.d("AddRecommendationFragment", "isbn: $isbn")

        // Initialize buttons and views
        searchBookButton = view.findViewById(R.id.searchBookButton)

        // Opens page to search book when search button is pressed
        searchBookButton.setOnClickListener {
            val SearchBookRecommendationBookDetailsFragment = SearchBookRecommendationBookDetailsFragment()
            val bundle = Bundle()
            bundle.putString("isbn", isbn)
            SearchBookRecommendationBookDetailsFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(SearchBookRecommendationBookDetailsFragment, "Search", showBackButton = true)
        }
    }
}