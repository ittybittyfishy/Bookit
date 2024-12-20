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

        val genres = ArrayList(book.volumeInfo.categories ?: listOf("Unknown Genre"))
        val description = book.volumeInfo.description
        val rating = book.volumeInfo.averageRating
        val isbn = book.volumeInfo.industryIdentifiers
            ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
            ?.identifier ?: "No ISBN"
        val authorList = ArrayList(book.volumeInfo.authors ?: listOf("Unknown Author"))


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

        findBookCollection(holder.itemView.context, book.volumeInfo.title, book.volumeInfo.authors?.joinToString(", ")) { collection ->
            if (collection != null) {
                holder.standardCollectionText.apply {
                    text = "In Collection: $collection"
                    visibility = View.VISIBLE
                }
            } else {
                holder.standardCollectionText.visibility = View.GONE
            }
        }

        // Set up the spinner (drop-down menu) with standard collections.
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            standardCollections
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerSelectCollection.adapter = adapter // Assign the adapter to the spinner

        // Set the listener to handle selection changes
        holder.spinnerSelectCollection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        5 -> { // "Remove" selected
                            removeBookFromStandardCollection(
                                holder.itemView.context,
                                book.volumeInfo.title,
                                holder.authors.text.toString()
                            )
                        }

                        1, 2, 3, 4 -> { // Other valid collections selected
                            val selectedCollection = standardCollections[position]
                            saveBookToCollection(
                                holder.itemView.context,
                                book.volumeInfo.title,
                                holder.authors.text.toString(),
                                imageUrl,
                                selectedCollection,
                                genres,
                                description,
                                rating,
                                isbn,
                                authorList

                            )
                        }
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
        val standardCollectionText: TextView = itemView.findViewById(R.id.standardCollection)

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

    private fun findBookCollection(
        context: Context,
        title: String,
        authors: String?,
        callback: (String?) -> Unit // Callback to return collection name
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Loop through standard collections to find the book
                for (collection in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                    booksInCollection?.let {
                        for (existingBook in it) {
                            if (existingBook["title"] == title &&
                                existingBook["authors"] == (authors?.split(", ") ?: listOf("Unknown Author"))
                            ) {
                                return@runTransaction collection // Return the collection name
                            }
                        }
                    }
                }
                null // Return null if not found in any collection
            }.addOnSuccessListener { collection ->
                callback(collection as? String) // Pass the collection name to the callback
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to find book collection: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(null) // Pass null if there's an error
            }
        } else {
            callback(null) // Return null if user is not logged in
        }
    }

    // Olivia Fishbough
    // Method to save the book to a selected standard collection in Firestore.
    // only allows a user to save it in ONE standard collection
    private fun saveBookToCollection(
        context: Context,
        title: String,
        authors: String,
        bookImage: String?,
        newCollectionName: String,
        genres: List<String>?,
        description: String?,
        rating: Float?,
        isbn: String?,
        bookAuthorsList: List<String>?
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID
        if (userId != null) {
            val db = FirebaseFirestore.getInstance() // Reference to Firestore


            // Create a map of the book's details to be saved.
            val book = hashMapOf(
                "title" to title,
                "authors" to bookAuthorsList,
                "authorsList" to bookAuthorsList,
                "imageLink" to bookImage,
                "genres" to genres,
                "description" to description,
                "rating" to rating,
                "isbn" to isbn
            )


            val userDocRef = db.collection("users").document(userId)


            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val username = snapshot.getString("username") ?: "Unknown User"  // Gets username
                val profileImageUrl = snapshot.getString("profileImageUrl")

                // Remove the book from old collections if it exists
                for (collection in standardCollections) {
                    if (collection != "Select Collection" && collection != newCollectionName) {
                        val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                        booksInCollection?.let {
                            for (existingBook in it) {
                                if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                    transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))


                                    // Decrement numBooksRead if the book was in the "Finished" collection
                                    if (collection == "Finished") {
                                        transaction.update(userDocRef, "numBooksRead", FieldValue.increment(-1))
                                    }
                                    break
                                }
                            }
                        }
                    }
                }


                // Add the book to the new collection
                transaction.update(userDocRef, "standardCollections.$newCollectionName", FieldValue.arrayUnion(book))

                // Veronica Nguyen
                // Gets the user's joined groups
                val groupIds = snapshot.get("joinedGroups") as? List<String> ?: emptyList()

                if (newCollectionName == "Finished") {
                    transaction.update(userDocRef, "numBooksRead", FieldValue.increment(1))

                    // Loops through each group the user is a member of
                    groupIds.forEach { groupId ->
                        val groupUpdatesRef = db.collection("groups").document(groupId).collection("memberUpdates")

                        // Generate a document ID
                        val updateId = groupUpdatesRef.document().id

                        // Data to store for group updates in the group they're in
                        val updateData = hashMapOf(
                            "updateId" to updateId, // Include the ID in the data
                            "userId" to userId,
                            "username" to username,
                            "profileImageUrl" to profileImageUrl,
                            "type" to "finishBook",
                            "timestamp" to FieldValue.serverTimestamp(),
                            "bookTitle" to title
                        )

                        // Write the data with the predefined document ID
                        val specificDocRef = groupUpdatesRef.document(updateId)
                        transaction.set(specificDocRef, updateData)
                    }
                }

                if (newCollectionName == "Reading") {
                    // Loops through each group the user is a member of
                    groupIds.forEach { groupId ->
                        val groupUpdatesRef = db.collection("groups").document(groupId).collection("memberUpdates")

                        // Generate a document ID
                        val updateId = groupUpdatesRef.document().id

                        // Data to store for group updates in the group they're in
                        val updateData = hashMapOf(
                            "updateId" to updateId, // Include the ID in the data
                            "userId" to userId,
                            "username" to username,
                            "profileImageUrl" to profileImageUrl,
                            "type" to "startBook",
                            "timestamp" to FieldValue.serverTimestamp(),
                            "bookTitle" to title
                        )

                        // Write the data with the predefined document ID
                        val specificDocRef = groupUpdatesRef.document(updateId)
                        transaction.set(specificDocRef, updateData)
                    }
                }


                null // Indicate successful transaction
            }.addOnSuccessListener {
                // Veronica Nguyen
                calculateTopGenres(userId, context)  // Update top genres when adding book to default collection
                Toast.makeText(context, context.getString(R.string.book_added_to_collection, newCollectionName), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, context.getString(R.string.failed_to_add_book, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Olivia Fishbough
    // Method to show a dialog where the user can select a custom collection.
    private fun showCustomCollectionDialog(context: Context, book: BookItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(userId)

            // Fetch the user's custom collections and identify collections the book is already in.
            userDocRef.get().addOnSuccessListener { document ->
                val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
                val collectionsBookIsIn = mutableListOf<String>()

                // Identify collections containing the book.
                customCollections?.forEach { (collectionName, collectionData) ->
                    val booksInCollection = collectionData["books"] as? List<Map<String, Any>>
                    booksInCollection?.forEach { bookData ->
                        if (bookData["title"] == book.volumeInfo.title &&
                            bookData["authors"] == book.volumeInfo.authors
                        ) {
                            collectionsBookIsIn.add(collectionName)
                        }
                    }
                }

                // Check if there are any custom collections to display.
                if (!customCollections.isNullOrEmpty()) {
                    val customCollectionNames = customCollections.keys.toMutableList()
                    customCollectionNames.add("Remove from ALL Custom Collections")

                    // Filter out collections the book is already in
                    val filteredCollections = customCollectionNames.filterNot { it in collectionsBookIsIn }
                    val displayCollectionNames = filteredCollections.toMutableList()

                    // Show a message if all collections contain the book
                    if (displayCollectionNames.isEmpty()) {
                        Toast.makeText(context, "Book is already in all custom collections.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Build and show the dialog with custom collections.
                    AlertDialog.Builder(context)
                        .setTitle("Select Custom Collection")
                        .setItems(displayCollectionNames.toTypedArray()) { dialog, which ->
                            val selectedCollectionName = displayCollectionNames[which]

                            // Check if the user selected the option to remove from custom collections.
                            if (selectedCollectionName == "Remove from ALL Custom Collections") {
                                removeBookFromCustomCollections(userId, book, context) // Remove the book from custom collections
                                calculateTopGenres(userId, context)  // Calculate top genres when removing book from custom collections
                            } else {
                                // Add the book to the selected custom collection.
                                addBookToCustomCollection(userId, book, selectedCollectionName, context) // Add the book to the selected custom collection
                            }
                        }
                        .setNegativeButton("Cancel", null) // Cancel button to dismiss the dialog
                        .show() // Display the dialog
                } else {
                    // Inform the user if no custom collections are found.
                    Toast.makeText(context, "No custom collections found.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Handle failure to load custom collections.
                Toast.makeText(context, "Error loading custom collections.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Olivia Fishbough
    // Method to add a book to a custom collection in Firestore.
    // User can add a book to multiple collections
    private fun addBookToCustomCollection(
        userId: String,
        book: BookItem,
        collectionName: String,
        context: Context
    ) {
        val db = FirebaseFirestore.getInstance()


        // Extract additional book information
        val isbn = book.volumeInfo.industryIdentifiers
            ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
            ?.identifier ?: "No ISBN"
        val description = book.volumeInfo.description ?: "No description available"
        val rating = book.volumeInfo.averageRating ?: 0f
        val authorsList = ArrayList(book.volumeInfo.authors ?: listOf("Unknown Author"))
        val genres = ArrayList(book.volumeInfo.categories ?: listOf("Unknown Genre"))


        // Prepare book data for the collection.
        val bookData = hashMapOf(
            "title" to book.volumeInfo.title,
            "authors" to authorsList,
            "imageLink" to book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
            "pages" to 0, // Initialize pages count
            "tags" to emptyList<String>(),
            "genres" to genres,
            "description" to description,
            "rating" to rating,
            "isbn" to isbn
        )


        // Update Firestore to add the book to the custom collection.
        db.collection("users").document(userId)
            .update("customCollections.$collectionName.books", FieldValue.arrayUnion(bookData))
            .addOnSuccessListener {
                calculateTopGenres(userId, context)  // Update top genres when adding book to custom collection
                Toast.makeText(context, "Book added to $collectionName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add book to $collectionName", Toast.LENGTH_SHORT).show()
            }
    }

    // Olivia Fishbough
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

            // Firestore transaction to remove the book from collections if found
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Loop through the standard collections and remove the book.
                for (collection in listOf("Reading", "Finished", "Want to Read", "Dropped")) {
                    val booksInCollection = snapshot.get("standardCollections.$collection") as? List<Map<String, Any>>
                    booksInCollection?.let {
                        for (existingBook in it) {
                            if (existingBook["title"] == title && existingBook["authors"] == authors.split(", ")) {
                                // Remove the book from the collection.
                                transaction.update(userDocRef, "standardCollections.$collection", FieldValue.arrayRemove(existingBook))

                                // Veronica Nguyen
                                // If the book was in "Finished", decrement numBooksRead
                                if (collection == "Finished") {
                                    transaction.update(userDocRef, "numBooksRead", FieldValue.increment(-1))
                                }
                                break // Exit loop after removing the book
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

    // Olivia Fishbough
    // Method to remove a book from custom collections.
    // removes them from ALL collections
    private fun removeBookFromCustomCollections(userId: String, book: BookItem, context: Context) {
        val db = FirebaseFirestore.getInstance()

        // Get book title and authors for comparison.
        val bookTitle = book.volumeInfo.title
        val bookAuthors = book.volumeInfo.authors

        // Fetch custom collections and remove the book from all of them.
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val customCollections = document.get("customCollections") as? Map<String, Any>

                // Check if custom collections exist before attempting to remove.
                if (!customCollections.isNullOrEmpty()) {
                    val batch = db.batch() // Initialize a Firestore batch for atomic updates

                    // Loop through each custom collection.
                    for (collectionName in customCollections.keys) {
                        val collectionRef = db.collection("users").document(userId)
                        val books = (customCollections[collectionName] as? Map<String, Any>)?.get("books") as? List<Map<String, Any>>

                        // Filter and remove the matching book from each collection.
                        if (!books.isNullOrEmpty()) {
                            val booksToRemove = books.filter { bookMap ->
                                val title = bookMap["title"] as? String
                                val authors = bookMap["authors"] as? List<String>
                                title == bookTitle && authors == bookAuthors // Match book by title and authors
                            }

                            // For each matching book, update Firestore to remove it from the collection.
                            for (bookToRemove in booksToRemove) {
                                batch.update(collectionRef, "customCollections.$collectionName.books", FieldValue.arrayRemove(bookToRemove))
                            }
                        }
                    }
                    // Commit the batch update to Firestore, ensuring all removals happen together
                    batch.commit()
                        .addOnSuccessListener {
                            // Notify the user when the book is successfully removed from all collections.
                            Toast.makeText(context, "Book removed from all custom collections.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            // Notify the user if there is an issue with removing the book.
                            Toast.makeText(context, "Failed to remove book from custom collections.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Inform the user that no custom collections were found.
                    Toast.makeText(context, "No custom collections found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Handle any errors while fetching custom collections from Firestore.
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

                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to retrieve collections: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}
