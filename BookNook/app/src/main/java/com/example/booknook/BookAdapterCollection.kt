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
        // EditText to display the book pages
        val pages: EditText = itemView.findViewById(R.id.pages)
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
        // Set the pages of the book in the EditView
        holder.pages.setText(book.pages.toString())

        // Load the book's image into the ImageView using Glide (an image loading library)
        Glide.with(holder.itemView.context)
            .load(book.imageLink)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.bookImage)

        // Handle increment and decrement page actions
        holder.itemView.findViewById<Button>(R.id.addPages).setOnClickListener {
            updatePageCount(book, holder.pages, 1)
        }

        holder.itemView.findViewById<Button>(R.id.subtractPages).setOnClickListener {
            updatePageCount(book, holder.pages, -1)
        }
    }

    // Function to increment/decrement pages
    private fun updatePageCount(book: BookItemCollection, pageView: EditText, change: Int) {
        // Parse current pages from EditText
        val currentPages = pageView.text.toString().toIntOrNull() ?: 0

        // Update locally and in the database
        val newPages = currentPages + change
        pageView.setText(newPages.toString())

        // Update in Firestore
        updatePagesInFirestore(book, newPages)
    }

    // Function to update the pages in Firestore for both standard and custom collections
    private fun updatePagesInFirestore(book: BookItemCollection, newPages: Int) {
        // Get Firestore reference
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Update in standard collections
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val standardCollections = document.get("standardCollections") as? Map<String, Any>
            standardCollections?.forEach { (collectionName, books) ->
                if (books is List<*>) {
                    // Find the book by title and authors
                    val bookInCollection = books.find { it is Map<*, *> &&
                            it["title"] == book.title &&
                            (it["authors"] as? List<*>)?.containsAll(book.authors) == true
                    }
                    if (bookInCollection != null) {
                        // Remove the old book
                        db.collection("users").document(userId)
                            .update("standardCollections.$collectionName", FieldValue.arrayRemove(bookInCollection))

                        // Modify pages and re-add the book
                        val updatedBook = (bookInCollection as Map<String, Any>).toMutableMap()
                        updatedBook["pages"] = newPages
                        db.collection("users").document(userId)
                            .update("standardCollections.$collectionName", FieldValue.arrayUnion(updatedBook))
                    }
                }
            }
        }

        // 2. Update in custom collections
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
            customCollections?.forEach { (collectionName, collectionData) ->
                val booksInCustom = collectionData["books"] as? List<Map<String, Any>> ?: return@forEach
                // Find the book by title and authors
                val bookInCustom = booksInCustom.find {
                    it["title"] == book.title &&
                            (it["authors"] as? List<*>)?.containsAll(book.authors) == true
                }
                if (bookInCustom != null) {
                    // Remove the old book and update the pages
                    val updatedBook = bookInCustom.toMutableMap()
                    updatedBook["pages"] = newPages
                    val updatedBooks = booksInCustom.toMutableList().apply {
                        remove(bookInCustom)
                        add(updatedBook)
                    }

                    // Update the custom collection in Firestore
                    db.collection("users").document(userId)
                        .update("customCollections.$collectionName.books", updatedBooks)
                }
            }
        }
    }



    // This function returns the total number of books to display in the RecyclerView
    override fun getItemCount(): Int = books.size
}