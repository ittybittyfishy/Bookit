package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Yunjong Noh
// Adapter class for displaying book recommendations in a RecyclerView
class BookRecommendationAdapter(private val books: List<BookItemCollection>) : RecyclerView.Adapter<BookRecommendationAdapter.BookViewHolder>() {

    // ViewHolder class that holds the views for each book item
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookCoverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val bookRatingTextView: TextView = itemView.findViewById(R.id.bookRatingTextView)
        val bookRatingCountTextView: TextView = itemView.findViewById(R.id.bookRatingCountTextView)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val dislikeButton: ImageButton = itemView.findViewById(R.id.dislikeButton)
    }

    // Called when RecyclerView needs a new ViewHolder for a book item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {

        // Inflate the layout for each book item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_personal_recommendation, parent, false)
        return BookViewHolder(view)
    }

    // Binds data to the views in the ViewHolder for each book item
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // Get the book data at the current position
        val book = books[position]

        // Set the book title in the TextView
        holder.bookTitleTextView.text = book.title

        // Set the rating text and count; since no rating is available, set it to "N/A"
        holder.bookRatingTextView.text = "(N/A)"
        holder.bookRatingCountTextView.text = "N/A"

        // Load the book cover image using Glide; if no image is available, use a placeholder
        Glide.with(holder.itemView.context)
            .load(book.imageLink)  // Load image from link
            .placeholder(R.drawable.placeholder_image)  // Placeholder image while loading
            .error(R.drawable.placeholder_image)  // Placeholder if loading fails
            .into(holder.bookCoverImageView) // Set the image into the ImageView

        // Place for :Additional book data like authors, tags, or genres can be handled here if needed in future
    }

    // Returns the total number of book items in the list
    override fun getItemCount(): Int = books.size
}
