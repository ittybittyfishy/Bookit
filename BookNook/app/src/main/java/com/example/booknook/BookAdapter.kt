package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.example.booknook.BookItem

class BookAdapter(private val bookList: List<BookItem>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.title.text = book.volumeInfo.title
        holder.authors.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"

        // Use HTTPS for the image URL
        val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // Ensure the placeholder image exists in res/drawable
            .error(R.drawable.placeholder_image) // Show placeholder image if loading fails
            .into(holder.bookImage)
    }

    override fun getItemCount(): Int = bookList.size

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val authors: TextView = itemView.findViewById(R.id.bookAuthors)
        val bookImage: ImageView = itemView.findViewById(R.id.book_image)
    }
}