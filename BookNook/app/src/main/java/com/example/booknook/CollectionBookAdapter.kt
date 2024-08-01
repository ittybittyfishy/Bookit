package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CollectionBookAdapter(private val bookList: List<BookItem>) : RecyclerView.Adapter<CollectionBookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.book_image)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookAuthors: TextView = itemView.findViewById(R.id.bookAuthors)
        val bookRating: RatingBar = itemView.findViewById(R.id.bookRating)
        val bookGenres: TextView = itemView.findViewById(R.id.bookGenres)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val bookItem = bookList[position]
        holder.bookTitle.text = bookItem.volumeInfo.title
        holder.bookAuthors.text = bookItem.volumeInfo.authors?.joinToString(", ")
        holder.bookRating.rating = bookItem.volumeInfo.averageRating ?: 0.0f
        holder.bookGenres.text = bookItem.volumeInfo.categories?.joinToString(", ")

        // Load the book image using a library like Glide or Picasso
        bookItem.volumeInfo.imageLinks?.thumbnail?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.bookImage)
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}