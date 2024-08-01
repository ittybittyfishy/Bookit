package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CollectionAdapter (private val collectionList: List<CollectionItem>) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>()
{
    // This method is called when the RecyclerView needs a new ViewHolder to represent an item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        // Inflate the item layout and create a ViewHolder instance
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        return CollectionViewHolder(view)
    }

    // This method is called to bind data to the ViewHolder.
    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val collection = collectionList[position]
        holder.collectionName.text = collection.name
    }

    // This method returns the total number of items in the data set held by the adapter.
    override fun getItemCount(): Int = collectionList.size

    // ViewHolder class to hold references to the views in each item.
    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionName: TextView = itemView.findViewById(R.id.collectionName)
    }
}
