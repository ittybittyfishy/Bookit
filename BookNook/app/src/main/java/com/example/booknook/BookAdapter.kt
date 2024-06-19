package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
    }

    override fun getItemCount(): Int = bookList.size

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val authors: TextView = itemView.findViewById(R.id.bookAuthors)
    }
}
