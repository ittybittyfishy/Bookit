package com.example.booknook

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// handles displaying the list of books within a collection in a RecyclerView
class BookAdapterCollection (private val books: List<BookItemCollection>) : RecyclerView.Adapter<BookAdapterCollection.BookViewHolder>()
{
    // class that holds the views for each book item in the RecyclerView
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ImageView to display the book's cover image
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        // TextView to display the book's title
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        // TextView to display the book's authors
        val bookAuthors: TextView = itemView.findViewById(R.id.bookAuthors)
    }

    // function is called when the RecyclerView needs a new ViewHolder to represent a book item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Inflate the layout for an individual book item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_collection, parent, false)
        // Create and return a new BookViewHolder, passing in the inflated view
        return BookViewHolder(view)
    }

    // function is called to display the data at the specified position in the RecyclerView
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // Get the book data for the current position
        val book = books[position]
        // Set the title of the book in the TextView
        holder.bookTitle.text = book.title
        // Set the authors of the book in the TextView, joined by commas
        holder.bookAuthors.text = book.authors.joinToString(", ")

        // Load the book's image into the ImageView using Glide (an image loading library)
        Glide.with(holder.itemView.context)
            .load(book.imageLink)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.bookImage)
    }

    // This function returns the total number of books to display in the RecyclerView
    override fun getItemCount(): Int = books.size
}