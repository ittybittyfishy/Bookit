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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GroupRecommendationsFragment : Fragment() {
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    private val recommendationsList = mutableListOf<Map<String, Any>>()
    private lateinit var addRecommendationButton: Button
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var sortBooksSpinner: Spinner
    private var isDataLoaded = false

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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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
        recommendationsAdapter = groupId?.let { RecommendationsAdapter(recommendationsList, it, userId) }!!
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
        if (isDataLoaded) return // Skip fetching if already loaded

        val db = FirebaseFirestore.getInstance()
        val groupId = arguments?.getString("groupId")
        if (groupId != null) {
            recommendationsList.clear()
            db.collection("groups").document(groupId)
                .collection("recommendations")
                .orderBy("numUpvotes", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val recommendation = document.data
                        recommendationsList.add(recommendation)
                    }
                    recommendationsAdapter.notifyDataSetChanged()
                    isDataLoaded = true // Mark data as loaded
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
            5 -> recommendationsList.sortByDescending { (it["numUpvotes"] as? Long) ?: 0L } // Upvotes (High to Low)
            6 -> recommendationsList.sortBy { (it["numUpvotes"] as? Long) ?: 0L } // Upvotes (Low to High)
        }

        recommendationsAdapter.notifyDataSetChanged() // Refreshes the adapter
    }

}