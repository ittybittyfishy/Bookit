package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.CollectionAdapter.CollectionViewHolder
import com.example.booknook.fragments.EditCollectionFragment

class CollectionCustomAdapter(private val collections: List<CollectionCustomItem>,
                              private val onBookClick: (BookItemCollection) -> Unit
) : RecyclerView.Adapter<CollectionCustomAdapter.CollectionViewHolder>()
{

    // ViewHolder class that holds the views for each item in the RecyclerView
    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TextView to display the name of the collection
        val collectionName: TextView = itemView.findViewById(R.id.collectionName)
        // RecyclerView inside each collection item to display the books in that collection
        val booksRecyclerView: RecyclerView = itemView.findViewById(R.id.booksRecyclerView)
        // textview to display summary
        val collectionDes: TextView = itemView.findViewById(R.id.collectionDes)
        // Edit button
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)

    }

    // This function is called when the RecyclerView needs a new ViewHolder to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.example.booknook.CollectionCustomAdapter.CollectionViewHolder {
        // Inflate the layout for an individual collection item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection_custom, parent, false)
        // Create and return a new ViewHolder, passing in the inflated view
        return com.example.booknook.CollectionCustomAdapter.CollectionViewHolder(view)
    }

    // display the data at the specified position in the RecyclerView
    override fun onBindViewHolder(holder: com.example.booknook.CollectionCustomAdapter.CollectionViewHolder, position: Int) {
        // Get the collection data for the current position
        val collection = collections[position]
        // Set the name of the collection in the TextView
        holder.collectionName.text = collection.collectionName
        // Set up the RecyclerView inside this collection item to display the books
        holder.collectionDes.text = collection.summary
        // Use a horizontal LinearLayoutManager to lay out the books side by side
        holder.booksRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        // Set the adapter for the books RecyclerView, passing in the list of books in this collection
        holder.booksRecyclerView.adapter = BookAdapterCollection(collection.books, onBookClick)

        // Set the click listener on the edit button
        holder.editButton.setOnClickListener {
            val editFragment = EditCollectionFragment.newInstance(collection.collectionName, collection.summary)
            editFragment.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "EditCollectionDialog")
        }
    }
    // This function returns the total number of collections to display
    override fun getItemCount(): Int = collections.size
}