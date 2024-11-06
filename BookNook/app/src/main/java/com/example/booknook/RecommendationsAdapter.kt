package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

// Define an adapter for displaying book recommendations in a RecyclerView
class RecommendationsAdapter(
    private val recommendationsList: List<Map<String, Any>>, // List of recommendations
    private val groupId: String,
    private val userId: String
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    // ViewHolder class that represents each recommendation item
    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookAuthors: TextView = itemView.findViewById(R.id.bookAuthors)
        val upvoteButton: ImageButton = itemView.findViewById(R.id.upvoteButton)
        val numberUpvotes: TextView = itemView.findViewById(R.id.numberUpvotes)
    }

    // Inflate the item layout and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    // Bind data to each item (book image, title, authors)
    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendationsList[position]

        // Bind book title
        holder.bookTitle.text = recommendation["title"] as? String ?: "Unknown Title"

        // Bind book authors
        holder.bookAuthors.text = recommendation["authors"] as? String ?: "Unknown Author"

        // Load and bind book image with Glide
        val imageUrl = recommendation["image"] as? String
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // Placeholder image
            .error(R.drawable.placeholder_image) // Error image if loading fails
            .into(holder.bookImage)

        val recommendationId = recommendation["recommendationId"] as? String ?: return
        var currentUpvotes = recommendation["numUpvotes"] as? Long ?: 0
        val upvotedByUsers = recommendation["upvotedByUsers"] as? Map<String, Boolean> ?: emptyMap()

        var userHasUpvoted = upvotedByUsers[userId] == true  // Stores if the user has already upvoted the book
        holder.numberUpvotes.text = currentUpvotes.toString()  // Updates text view with number of upvotes

        // Handle upvote button click
        holder.upvoteButton.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val recommendationRef = db.collection("groups").document(groupId)
                .collection("recommendations").document(recommendationId)

            // If the user has already upvoted this book and presses the upvote button
            if (userHasUpvoted) {
                // Remove upvote
                recommendationRef.update(
                    "numUpvotes", currentUpvotes - 1,  // Decrements value in database
                    "upvotedByUsers.$userId", null
                ).addOnSuccessListener {
                    currentUpvotes -= 1  // Decrement the current upvotes
                    userHasUpvoted = false  // Toggles user's upvoted status
                    // Update UI
                    holder.numberUpvotes.text = currentUpvotes.toString()
                }
            } else {
                // Add upvote
                recommendationRef.update(
                    "numUpvotes", currentUpvotes + 1,  // Increments value in database
                    "upvotedByUsers.$userId", true
                ).addOnSuccessListener {
                    currentUpvotes += 1  // Increment current upvotes
                    userHasUpvoted = true  // Toggles user's upvoted status
                    // Update UI
                    holder.numberUpvotes.text = currentUpvotes.toString()
                }
            }
        }
    }

    // Return the total number of recommendations
    override fun getItemCount(): Int = recommendationsList.size
}
