package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.booknook.R
import android.widget.TextView
import com.example.booknook.MainActivity

class SortByFragment : Fragment() {

    private lateinit var sortByHighRating: TextView
    private lateinit var sortByLowRating: TextView
    private lateinit var sortByTitleAZ: TextView
    private lateinit var sortByTitleZA: TextView
    private lateinit var sortByAuthor: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sortby, container, false)

        sortByHighRating = view.findViewById(R.id.sort_by_high_rating)
        sortByLowRating = view.findViewById(R.id.sort_by_low_rating)
        sortByTitleAZ = view.findViewById(R.id.sort_by_rating_az)
        sortByTitleZA = view.findViewById(R.id.sort_by_rating_za)
        sortByAuthor = view.findViewById(R.id.sort_by_author)

        // Set click listeners for sorting actions
        sortByHighRating.setOnClickListener {
            // Add sorting logic if necessary
            (activity as MainActivity).sortBooks("high_rating")
        }

        sortByLowRating.setOnClickListener {
            (activity as MainActivity).sortBooks("low_rating")
        }

        sortByTitleAZ.setOnClickListener {
            (activity as MainActivity).sortBooks("title_az")
        }

        sortByTitleZA.setOnClickListener {
            (activity as MainActivity).sortBooks("title_za")
        }

        sortByAuthor.setOnClickListener {
            (activity as MainActivity).sortBooks("author")
        }

        return view
    }
}
