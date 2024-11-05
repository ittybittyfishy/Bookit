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
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.RecommendationsAdapter
import com.google.firebase.firestore.FirebaseFirestore

class GroupRecommendationsFragment : Fragment() {
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    private val recommendationsList = mutableListOf<Map<String, Any>>()
    private lateinit var addRecommendationButton: Button
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var sortBooksSpinner: Spinner

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
        val groupId = arguments?.getString("groupId")

        addRecommendationButton = view.findViewById(R.id.addRecommendationButton)
        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView)
        sortBooksSpinner = view.findViewById(R.id.sortBooks)

        setupSortSpinner()  // Sets up the spinner to sort books

        // Opens page to add recommendations when button is pressed
        addRecommendationButton.setOnClickListener {
            val addRecommendationFragment = AddRecommendationFragment()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            addRecommendationFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(addRecommendationFragment, "Add Recommendation", showBackButton = true)
        }

        // Sets up recycler view to display recommendations
        recommendationsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        recommendationsAdapter = RecommendationsAdapter(recommendationsList)
        recommendationsRecyclerView.adapter = recommendationsAdapter

        fetchRecommendations()
    }

    // Set up the custom spinner design to sort books
    private fun setupSortSpinner() {
        // Create an ArrayAdapter using the custom layout for the spinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.recommendations_sort_options,   // Array resource for options
            R.layout.item_collections_spinner_layout   // Custom layout for selected item
        )
        // Apply the adapter to the spinner
        adapter.setDropDownViewResource(R.layout.item_collections_spinner_dropdown)
        sortBooksSpinner.adapter = adapter

        // Handle selection changes
        sortBooksSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id:Long) {
                sortRecommendations(position)  // Calls function to sort the books
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action if nothing is selected
            }
        }
    }

    // Function to fetch the recommendations from database
    private fun fetchRecommendations() {
        val db = FirebaseFirestore.getInstance()

        val groupId = arguments?.getString("groupId")
        if (groupId != null) {
            db.collection("groups").document(groupId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Gets the recommendations field and adds it to recommendations list
                        val recommendations = document["recommendations"] as? List<Map<String, Any>>
                        if (recommendations != null) {
                            recommendationsList.clear()
                            recommendationsList.addAll(recommendations)
                            recommendationsAdapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("GroupRecommendations", "Error getting recommendations", e)
                }
        }
    }

    // Function to sort the recommendations
    private fun sortRecommendations(sortOption: Int) {
        when (sortOption) {
            1 -> recommendationsList.sortBy { it["title"] as? String } // Title (A-Z)
            2 -> recommendationsList.sortByDescending { it["title"] as? String } // Title (Z-A)
            3 -> recommendationsList.sortBy { it["authors"] as? String } // Author (A-Z)
            4 -> recommendationsList.sortByDescending { it["authors"] as? String } // Author (Z-A)
        }
        recommendationsAdapter.notifyDataSetChanged() // Refreshes the adapter
    }

}