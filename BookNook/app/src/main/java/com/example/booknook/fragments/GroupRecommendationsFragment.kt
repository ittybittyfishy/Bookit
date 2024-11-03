package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        // Opens page to add recommendations when button is pressed
        addRecommendationButton.setOnClickListener {
            val addRecommendationFragment = AddRecommendationFragment()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            addRecommendationFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(addRecommendationFragment, "Add Recommendation")
        }

        // Sets up recycler view to display recommendations
        recommendationsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        recommendationsAdapter = RecommendationsAdapter(recommendationsList)
        recommendationsRecyclerView.adapter = recommendationsAdapter

        fetchRecommendations()
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

}