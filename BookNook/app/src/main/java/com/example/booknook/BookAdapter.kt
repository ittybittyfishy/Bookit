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

// Adapter class that connects the data (BookItems) with the RecyclerView to display each book.
class BookAdapter(
    private val bookList: List<BookItem>, // List of books to display
    private val listener: RecyclerViewEvent // Listener for handling click events on books
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    // List of predefined collections that users can assign books to.
    private val standardCollections = listOf("Select Collection", "Reading", "Finished", "Want to Read", "Dropped", "Remove")

    // Called when RecyclerView needs a new ViewHolder. ViewHolder represents each item view.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Inflate the item layout for each book
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view) // Create and return a new BookViewHolder instance
    }

    // Binds the data (book) to the view at the given position in the RecyclerView.
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position] // Get the book at the current position

        // Set the title of the book in the view.
        holder.title.text = book.volumeInfo.title

        // Set the authors of the book, or display "Unknown Author" if no authors are available.
        holder.authors.text = book.volumeInfo.authors?.joinToString(", ")
            ?: holder.itemView.context.getString(R.string.unknown_author)

        // Load the book's thumbnail image using the Glide library.
        // Glide is used to load images from a URL and handle image downloading/caching efficiently.
        val imageUrl = book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image) // Show placeholder image while loading
            .error(R.drawable.placeholder_image) // Show placeholder if the image fails to load
            .into(holder.bookImage)

        // Set the rating for the book (average rating) in the RatingBar widget.
        holder.rating.rating = book.volumeInfo.averageRating ?: 0f

        // Set the genres of the book, or display "Unknown Genres" if no genres are available.
        holder.genres.text = holder.itemView.context.getString(
            R.string.genres,
            book.volumeInfo.categories?.joinToString(", ")
                ?: holder.itemView.context.getString(R.string.unknown_genres)
        )

        // Set up the spinner (drop-down menu) with standard collections.
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            standardCollections
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerSelectCollection.adapter = adapter // Assign the adapter to the spinner

        val genres = book.volumeInfo.categories ?: listOf("Unknown Genre")

        // Handle the spinner's item selection event.
        holder.spinnerSelectCollection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                // Called when an item is selected from the spinner.
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 5) { // "Remove" collection selected
                        // Remove the book from the collection
                        removeBookFromStandardCollection(
                            holder.itemView.context,
                            book.volumeInfo.title,
                            holder.authors.text.toString()
                        )
                    } else if (position != 0) { // Any valid collection selected (not "Select Collection")
                        val selectedCollection = standardCollections[position]
                        saveBookToCollection(
                            holder.itemView.context,
                            book.volumeInfo.title,
                            holder.authors.text.toString(),
                            imageUrl,
                            selectedCollection,
                            genres
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // Handle the "Add to Custom Collection" button click event.
        holder.btnAddToCustomCollection.setOnClickListener {
            showCustomCollectionDialog(holder.itemView.context, book) // Show a dialog to select custom collection
        }
    }

    // Returns the total number of items (books) in the list.
    override fun getItemCount(): Int = bookList.size

    // Interface for handling click events on RecyclerView items.
    interface RecyclerViewEvent {
        fun onItemClick(position: Int)
    }

    // ViewHolder class that holds references to the UI elements for each book item.
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val title: TextView = itemView.findViewById(R.id.bookTitle) // Title of the book
        val authors: TextView = itemView.findViewById(R.id.bookAuthors) // Authors of the book
        val bookImage: ImageView = itemView.findViewById(R.id.book_image) // Thumbnail image of the book
        val rating: RatingBar = itemView.findViewById(R.id.bookRating) // Rating of the book
        val genres: TextView = itemView.findViewById(R.id.bookGenres) // Genres of the book
        val spinnerSelectCollection: Spinner = itemView.findViewById(R.id.spinnerSelectCollection) // Spinner to select collections
        val btnAddToCustomCollection: Button = itemView.findViewById(R.id.btnAddToCustomCollection) // Button to add to custom collection

        // Init block to set click listener for each book item.
        init {
            itemView.setOnClickListener(this)
        }

        // When a book item is clicked, this method is triggered.
        override fun onClick(v: View?) {
            val position = bindingAdapterPosition // Get the position of the clicked item
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)  // Notify the listener of the item click
            }
        }
    }

    // Method to save the book to a selected collection in Firestore.
    private fun saveBookToCollection(
        context: Context,
        title: String,
        authors: String,
        bookImage: String?,
        newCollectionName: String,
        genres: List<String>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID
        if (userId != null) {
            val db = FirebaseFirestore.getInstance() // Reference to Firestore

            // Create a map of the book's details to be saved.
            val book = hashMapOf(
                "title" to title,
                "authors" to authors.split(", "),
                "imageLink" to bookImage,
                "genres" to genres
            )

            val userDocRef = db.collection("users").document(userId) // Reference to the user's document

            // Firestore transaction to update the database.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef) // Get current document

                // Loop through standard collections and remove the book from old collections.
                for (collection in standardCollections) {
                    if (collection != "Select Collection" && collection != newCollectionName) {
                        val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                        booksInCollection?.let {
                            for (existingBook in it) {
                                if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                    transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))
                                    break
                                }
                            }
                        }
                    }
                }

                // Add the book to the new collection.
                transaction.update(userDocRef, "standardCollections.$newCollectionName", FieldValue.arrayUnion(book))
                null
            }.addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when adding book to default collection
                Toast.makeText(context, context.getString(R.string.book_added_to_collection, newCollectionName), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, context.getString(R.string.failed_to_add_book, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to show a dialog where the user can select a custom collection.
    private fun showCustomCollectionDialog(context: Context, book: BookItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Fetch the user's custom collections from Firestore.
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val customCollections = document.get("customCollections") as? Map<String, Any>

                    if (!customCollections.isNullOrEmpty()) {
                        // Prepare a dialog to display the custom collections.
                        val customCollectionNames = customCollections.keys.toMutableList()
                        customCollectionNames.add("Remove from Custom Collections")

                        AlertDialog.Builder(context)
                            .setTitle("Select Custom Collection")
                            .setItems(customCollectionNames.toTypedArray()) { dialog, which ->
                                val selectedCollectionName = customCollectionNames[which]
                                if (selectedCollectionName == "Remove from Custom Collections") {
                                    removeBookFromCustomCollections(userId, book, context) // Remove the book from custom collections
                                    calculateTopGenres(userId, context)  // Calculate top genres when removing book from custom collections
                                } else {
                                    addBookToCustomCollection(userId, book, selectedCollectionName, context) // Add the book to the selected custom collection
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

    // Method to add a book to a custom collection in Firestore.
    private fun addBookToCustomCollection(userId: String, book: BookItem, collectionName: String, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Prepare book data for the collection.
        val bookData = hashMapOf(
            "title" to book.volumeInfo.title,
            "authors" to book.volumeInfo.authors,
            "imageLink" to book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
            "pages" to 0,
            "tags" to emptyList<String>(),
            "genres" to (book.volumeInfo.categories ?: listOf("Unknown Genre"))
        )

        // Update Firestore to add the book to the custom collection.
        db.collection("users").document(userId)
            .update("customCollections.$collectionName.books", FieldValue.arrayUnion(bookData))
            .addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when adding book to custom collection
                Toast.makeText(context, "Book added to $collectionName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add book to $collectionName", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to remove a book from standard collections.
    private fun removeBookFromStandardCollection(
        context: Context,
        title: String,
        authors: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Firestore transaction to remove the book from collections.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Loop through the standard collections and remove the book.
                for (collection in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                    booksInCollection?.let {
                        for (existingBook in it) {
                            if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))
                                break
                            }
                        }
                    }
                }
                null
            }.addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when removing book from standard collection
                Toast.makeText(context, "Book removed from standard collection", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to remove book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to remove a book from custom collections.
    private fun removeBookFromCustomCollections(userId: String, book: BookItem, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Get book title and authors for comparison.
        val bookTitle = book.volumeInfo.title
        val bookAuthors = book.volumeInfo.authors

        // Fetch custom collections and remove the book from all of them.
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val customCollections = document.get("customCollections") as? Map<String, Any>

                if (!customCollections.isNullOrEmpty()) {
                    val batch = db.batch()

                    // Loop through each custom collection.
                    for (collectionName in customCollections.keys) {
                        val collectionRef = db.collection("users").document(userId)
                        val books = (customCollections[collectionName] as? Map<String, Any>)?.get("books") as? List<Map<String, Any>>

                        // Filter and remove the matching book from each collection.
                        if (!books.isNullOrEmpty()) {
                            val booksToRemove = books.filter { bookMap ->
                                val title = bookMap["title"] as? String
                                val authors = bookMap["authors"] as? List<String>
                                title == bookTitle && authors == bookAuthors
                            }

                            // Update Firestore.
                            for (bookToRemove in booksToRemove) {
                                batch.update(collectionRef, "customCollections.$collectionName.books", FieldValue.arrayRemove(bookToRemove))
                            }
                        }
                    }

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

    // Veronica Nguyen
    // Function to get the user's top 3 genres
    fun calculateTopGenres(userId: String, context: Context) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()  // Gets users collection
            .addOnSuccessListener { document ->
                val genreCount = mutableMapOf<String, Int>() // Map to count number of times a genre appears

                // Gets user's standard collections
                val standardCollections = document.get("standardCollections") as? Map<String, List<Map<String, Any>>>
                // Loops through each standard collection
                standardCollections?.values?.forEach { bookList ->
                    // Loops through each book in a collection
                    bookList.forEach { book ->
                        val genres = book["genres"] as? List<String> ?: listOf("Unknown Genre")  // Retrieves genres from a book
                        // Loops through each genre of a book
                        genres.forEach { genre ->
                            if (genre != "Unknown Genre") { // Excludes "Unknown Genre" from top genres calculation
                                // Sets default number of a genre's occurrence to 0 and retrieves its current count
                                // Increments the count of that genre by 1
                                genreCount[genre] = genreCount.getOrDefault(genre, 0) + 1
                            }
                        }
                    }
                }

                // Gets user's custom collections
                val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
                    // Loops through each custom collection
                    customCollections?.forEach { (_, collectionData) ->  //  Ignores key parameter of lambda expression
                        val books = collectionData["books"] as? List<Map<String, Any>>  // Gets the books in collection
                        // Loops through each book in a collection
                        books?.forEach { book ->
                            val genres = book["genres"] as? List<String> ?: listOf("Unknown Genre")  // Retrieves genres from a book
                            // Loops through each genre of a book
                            genres.forEach { genre ->
                                if (genre != "Unknown Genre") { // Excludes "Unknown Genre" from top genres calculation
                                    // Sets default number of a genre's occurrence to 0 and retrieves its current count
                                    // Increments the count of that genre by 1
                                    genreCount[genre] = genreCount.getOrDefault(genre, 0) + 1
                            }
                        }
                    }
                }

                // Sort genres by count in descending order and take the top 3
                val topGenres = genreCount.entries.sortedByDescending { it.value }.take(3).map { it.key }

                // Update user's topGenres field in Firestore
                db.collection("users").document(userId).update("topGenres", topGenres)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Top genres updated: $topGenres", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update top genres: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to retrieve collections: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
