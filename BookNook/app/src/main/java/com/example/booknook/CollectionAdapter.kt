package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CollectionAdapter(private val bookGroups: List<BookGroup>,
                        private val listener: BookAdapter.RecyclerViewEvent) :
    RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val group = bookGroups[position]
        holder.collectionName.text = group.name
        holder.booksRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.booksRecyclerView.adapter = BookAdapter(group.books, listener)
    }

    override fun getItemCount(): Int = bookGroups.size

    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionName: TextView = itemView.findViewById(R.id.collectionName)
        val booksRecyclerView: RecyclerView = itemView.findViewById(R.id.booksRecyclerView)
    }
}