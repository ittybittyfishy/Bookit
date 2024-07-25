package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.example.booknook.BookItem
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

//a view is a UI element that appears on the screen
//inflate means to create
//book adapter provides instructions for recyclerview to correctly display data
//create viewholder, bind the data, provide total number of items in data set

//connect booklist with the views that display them in recyclerview
class BookAdapter(private val bookList: List<BookItem>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    //layout inflater creates views from .xml layouts
    //R contains references to resources
    //parent is the viewgroup the view will be attached to
    //parent.context ensures the new view inherits the correct info from parent for consistent interface

    val db = FirebaseFirestore.getInstance()

    // For dropdown menu to sort standard collections
    private val standardCollections = listOf("Select Collection","Reading", "Finished", "Want to Read", "Dropped")

    //creates new views for each item on the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    //overrides method in recyclerview.adapter
    //holder:bookViewHolder represents the contents of the item at the given position
    //binds data to each view, loads images using glide and sets placeholder image
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position] //accesses the book data at the specified position

        holder.title.text = book.volumeInfo.title //displays the title of the book in the title textvieww of the viewholder
        //displays the authors, if none it says unknown author
        holder.authors.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"

        // Use HTTPS for the image URL
        //this accesses the books image
        val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        //load the image with glide, first initialize with the itemview context
        Glide.with(holder.itemView.context)
            .load(imageUrl) //tell glide to load image
            .placeholder(R.drawable.placeholder_image) // Ensure the placeholder image exists in res/drawable
            .error(R.drawable.placeholder_image) // Show placeholder image if loading fails
            .into(holder.bookImage) //tells glide to load image into bookimage

        // Extract ISBN
        val isbn = book.volumeInfo.industryIdentifiers
            ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
            ?.identifier ?: "No ISBN"

        // Set up Spinner for book collection selection
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            standardCollections
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerSelectCollection.adapter = adapter

        // Set the click listener or item selected listener to save the book to the collection
        holder.spinnerSelectCollection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) { // Ensure it's not the hint item
                    val selectedCollection = standardCollections[position]
                    saveBookToCollection(holder.itemView.context, book, selectedCollection, isbn)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    //return size of booklist
    override fun getItemCount(): Int = bookList.size

    //helper class that hold references to the views within a single item layout
    //makes it easy to access views without having to repeatedly calling it
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle) //display title of book
        val authors: TextView = itemView.findViewById(R.id.bookAuthors) //display author
        val bookImage: ImageView = itemView.findViewById(R.id.book_image) //display book cover
        val spinnerSelectCollection: Spinner = itemView.findViewById(R.id.spinnerSelectCollection)
    }

    private fun saveBookToCollection(context: Context, book: BookItem, collectionName: String, isbn: String)
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()


            db.collection("users").document(userId)
                .update("standardCollections.$collectionName", FieldValue.arrayUnion(isbn))
                .addOnSuccessListener {
                    Toast.makeText(context, "Book added to $collectionName collection.", Toast.LENGTH_SHORT ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to add book: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }
}