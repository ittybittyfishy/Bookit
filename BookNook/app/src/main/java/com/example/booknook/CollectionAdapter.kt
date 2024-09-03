package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// adapter handles displaying the list of book collections in a RecyclerView
class CollectionAdapter(private val collections: List<CollectionItem>) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>()
{
    // ViewHolder class that holds the views for each item in the RecyclerView
    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TextView to display the name of the collection
        val collectionName: TextView = itemView.findViewById(R.id.collectionName)
        // RecyclerView inside each collection item to display the books in that collection
        val booksRecyclerView: RecyclerView = itemView.findViewById(R.id.booksRecyclerView)
    }

    // This function is called when the RecyclerView needs a new ViewHolder to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        // Inflate the layout for an individual collection item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        // Create and return a new ViewHolder, passing in the inflated view
        return CollectionViewHolder(view)
    }

    // display the data at the specified position in the RecyclerView
    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        // Get the collection data for the current position
        val collection = collections[position]
        // Set the name of the collection in the TextView
        holder.collectionName.text = collection.collectionName
        // Set up the RecyclerView inside this collection item to display the books
        // Use a horizontal LinearLayoutManager to lay out the books side by side
        holder.booksRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        // Set the adapter for the books RecyclerView, passing in the list of books in this collection
        holder.booksRecyclerView.adapter = BookAdapterCollection(collection.books)
    }
    // This function returns the total number of collections to display
    override fun getItemCount(): Int = collections.size
}
