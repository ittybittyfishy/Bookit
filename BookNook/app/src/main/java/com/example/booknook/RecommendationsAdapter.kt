package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Define an adapter for displaying book recommendations in a RecyclerView
class RecommendationsAdapter(
    private val recommendationsList: List<Map<String, Any>> // List of recommendations
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    // ViewHolder class that represents each recommendation item
    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookAuthors: TextView = itemView.findViewById(R.id.bookAuthors)
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
    }

    // Return the total number of recommendations
    override fun getItemCount(): Int = recommendationsList.size
}
