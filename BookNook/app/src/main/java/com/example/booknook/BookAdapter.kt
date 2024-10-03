package com.example.booknook

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Adapter class for handling a list of BookItems in a RecyclerView
class BookAdapter(private val bookList: List<BookItem>,
                  private val listener: RecyclerViewEvent) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    // List of standard collections to display in the spinner
    private val standardCollections = listOf("Select Collection", "Reading", "Finished", "Want to Read", "Dropped", "Remove")
    // initialize button

    // Called when RecyclerView needs a new ViewHolder of the given type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }


    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        // Set the title of the book
        holder.title.text = book.volumeInfo.title

        // Set the authors of the book or a default text if authors are unknown
        holder.authors.text = book.volumeInfo.authors?.joinToString(", ")
            ?: holder.itemView.context.getString(R.string.unknown_author)

        // Load the book's thumbnail image using Glide
        val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
            .error(R.drawable.placeholder_image) // Image to display on error
            .into(holder.bookImage)

        // Set the rating of the book
        holder.rating.rating = book.volumeInfo.averageRating ?: 0f

        // Set the genres of the book or a default text if genres are unknown
        holder.genres.text = holder.itemView.context.getString(
            R.string.genres,
            book.volumeInfo.categories?.joinToString(", ")
                ?: holder.itemView.context.getString(R.string.unknown_genres)
        )

        // Set up the spinner with standard collections
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            standardCollections
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerSelectCollection.adapter = adapter

        // Handle spinner item selection
        holder.spinnerSelectCollection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 5)
                    {
                        removeBookFromStandardCollection(
                            holder.itemView.context,
                            book.volumeInfo.title,
                            holder.authors.text.toString()
                        )
                    }
                    else if (position != 0) { // Check if the selected position is not the default one
                        val selectedCollection = standardCollections[position]
                        saveBookToCollection(
                            holder.itemView.context,
                            book.volumeInfo.title,
                            holder.authors.text.toString(),
                            imageUrl,
                            selectedCollection
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // Set OnClickListener for the "Add to Custom Collection" button
        holder.btnAddToCustomCollection.setOnClickListener {
            showCustomCollectionDialog(holder.itemView.context, book)
        }
    }

    // Returns the total number of items in the data set held by the adapter
    override fun getItemCount(): Int = bookList.size

    // Handles book clicking in the RecyclerView
    interface RecyclerViewEvent {
        fun onItemClick(position: Int)
    }

    // ViewHolder class to hold the views for each book item
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val authors: TextView = itemView.findViewById(R.id.bookAuthors)
        val bookImage: ImageView = itemView.findViewById(R.id.book_image)
        val rating: RatingBar = itemView.findViewById(R.id.bookRating)
        val genres: TextView = itemView.findViewById(R.id.bookGenres)
        val spinnerSelectCollection: Spinner = itemView.findViewById(R.id.spinnerSelectCollection)
        val btnAddToCustomCollection: Button = itemView.findViewById(R.id.btnAddToCustomCollection)

        // Veronica Nguyen
        // Sets the ViewHolder itself as click listener for itemView
        init {
            itemView.setOnClickListener(this)
        }

        // Veronica Nguyen
        // Checks if item's position in RecyclerView is valid
        override fun onClick(v: View?) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)  // Calls function to open book details on another page
            }
        }
    }

    private fun saveBookToCollection(
        context: Context,
        title: String,
        authors: String,
        bookImage: String?,
        newCollectionName: String
    ) {
        // Get the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Get a reference to the Firestore database
            val db = FirebaseFirestore.getInstance()

            // Create a map representing the book
            val book = hashMapOf(
                "title" to title,
                "authors" to authors.split(", "),
                "imageLink" to bookImage
            )

            // Reference to the user's document
            val userDocRef = db.collection("users").document(userId)

            // Firestore transaction
            db.runTransaction { transaction ->
                // Retrieve the current document snapshot
                val snapshot = transaction.get(userDocRef)

                // Check each standard collection to see if the book is in any of them
                for (collection in standardCollections) {
                    if (collection != "Select Collection" && collection != newCollectionName) {
                        val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>

                        // Debugging: print the books retrieved
                        Log.d("FirestoreData", "Books in $collection: $booksInCollection")

                        booksInCollection?.let {
                            for (existingBook in it) {
                                if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                    // Remove the book from the old collection
                                    transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))
                                    break
                                }
                            }
                        }
                    }
                }

                // Add the book to the new collection
                transaction.update(userDocRef, "standardCollections.$newCollectionName", FieldValue.arrayUnion(book))

                // Indicate successful completion
                null
            }.addOnSuccessListener {
                // Show a success message once the book has been added to the collection
                Toast.makeText(context, context.getString(R.string.book_added_to_collection, newCollectionName), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                // Show an error message if the transaction fails
                Toast.makeText(context, context.getString(R.string.failed_to_add_book, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to show the dialog for selecting a custom collection
    private fun showCustomCollectionDialog(context: Context, book: BookItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Fetch custom collections from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val customCollections = document.get("customCollections") as? Map<String, Any>

                    if (!customCollections.isNullOrEmpty()) {
                        // Show the dialog to choose a custom collection
                        val customCollectionNames = customCollections.keys.toMutableList()
                        customCollectionNames.add("Remove from Custom Collections")

                        AlertDialog.Builder(context)
                            .setTitle("Select Custom Collection")
                            .setItems(customCollectionNames.toTypedArray()) { dialog, which ->
                                val selectedCollectionName = customCollectionNames[which]
                                if (selectedCollectionName == "Remove from Custom Collections") {
                                    // Logic to remove the book from all custom collections
                                    removeBookFromCustomCollections(userId, book, context)
                                }else {
                                    // Logic to add the book to the selected custom collection
                                    addBookToCustomCollection(userId, book, selectedCollectionName, context)
                                }

                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        Toast.makeText(context, "No custom collections found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error loading custom collections.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Method to add the selected book to the custom collection
    private fun addBookToCustomCollection(userId: String, book: BookItem, collectionName: String, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Prepare the book data to add
        val bookData = hashMapOf(
            "title" to book.volumeInfo.title,
            "authors" to book.volumeInfo.authors,
            "imageLink" to book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
            "pages" to 0,
            "tags" to emptyList<String>()
        )

        // Update the Firestore document
        db.collection("users").document(userId)
            .update("customCollections.$collectionName.books", FieldValue.arrayUnion(bookData))
            .addOnSuccessListener {
                Toast.makeText(context, "Book added to $collectionName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add book to $collectionName", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeBookFromStandardCollection(
        context: Context,
        title: String,
        authors: String
    ) {
        // Get the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Get a reference to the Firestore database
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Firestore transaction
            db.runTransaction { transaction ->
                // Retrieve the current document snapshot
                val snapshot = transaction.get(userDocRef)

                // Loop through the standard collections and remove the book from any collection it's found in
                for (collection in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>

                    // If the book exists in the current collection, remove it
                    booksInCollection?.let {
                        for (existingBook in it) {
                            if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))
                                break
                            }
                        }
                    }
                }
                // Return null to complete the transaction
                null
            }.addOnSuccessListener {
                // Show a success message once the book has been removed from the collection
                Toast.makeText(context, "Book removed from standard collection", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                // Show an error message if the transaction fails
                Toast.makeText(context, "Failed to remove book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeBookFromCustomCollections(userId: String, book: BookItem, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Prepare the book data for comparison (without including 'pages')
        val bookTitle = book.volumeInfo.title
        val bookAuthors = book.volumeInfo.authors

        // Fetch the user's custom collections from Firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val customCollections = document.get("customCollections") as? Map<String, Any>

                if (!customCollections.isNullOrEmpty()) {
                    // Use a Firestore batch to update multiple collections at once
                    val batch = db.batch()

                    // Loop through each custom collection
                    for (collectionName in customCollections.keys) {
                        val collectionRef = db.collection("users").document(userId)

                        // Fetch the books in the current collection
                        val books = (customCollections[collectionName] as? Map<String, Any>)?.get("books") as? List<Map<String, Any>>

                        if (!books.isNullOrEmpty()) {
                            // Filter out the books that match by title and authors (ignoring 'pages')
                            val booksToRemove = books.filter { bookMap ->
                                val title = bookMap["title"] as? String
                                val authors = bookMap["authors"] as? List<String>
                                title == bookTitle && authors == bookAuthors
                            }

                            // Remove the matched books from the collection
                            for (bookToRemove in booksToRemove) {
                                batch.update(collectionRef, "customCollections.$collectionName.books", FieldValue.arrayRemove(bookToRemove))
                            }
                        }
                    }

                    // Commit the batch operation
                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Book removed from all custom collections.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to remove book from custom collections.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "No custom collections found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading custom collections.", Toast.LENGTH_SHORT).show()
            }
    }
}
