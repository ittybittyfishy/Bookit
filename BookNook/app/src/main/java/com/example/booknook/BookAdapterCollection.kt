package com.example.booknook

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.booknook.fragments.EditTagsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.FragmentActivity

// handles displaying the list of books within a collection in a RecyclerView
class BookAdapterCollection (private val books: List<BookItemCollection>,
                             private val onBookClick: (BookItemCollection) -> Unit
) : RecyclerView.Adapter<BookAdapterCollection.BookViewHolder>()
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

        // Contains the tags
        val tagContainer: LinearLayout = itemView.findViewById(R.id.tagContainer)
        // Button to add tags
        val addTags: Button = itemView.findViewById(R.id.addTags)
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
        holder.itemView.findViewById<ImageButton>(R.id.addPages).setOnClickListener {
            updatePageCount(book, holder.pages, 1)
        }

        holder.itemView.findViewById<ImageButton>(R.id.subtractPages).setOnClickListener {
            updatePageCount(book, holder.pages, -1)
        }

        // Clear any old tags
        holder.tagContainer.removeAllViews()

        // Load tags in book item
        if (book.tags.isEmpty()) {
            // If no tags then load a button to add tags
            holder.addTags.visibility = View.VISIBLE
        } else {
            // if there are tags then hide the add tag button and make tags seperate objects
            holder.addTags.visibility = View.GONE
            book.tags.forEach { tag ->
                val tagView = createTagView(tag, holder.itemView.context, book, position) // Create a custom TextView for each tag
                holder.tagContainer.addView(tagView)
            }
        }

        // Handle add/edit tags
        holder.addTags.setOnClickListener {
            // Open dialog or activity to add/edit tags
            val fragmentManager = (holder.itemView.context as FragmentActivity).supportFragmentManager
            val addTagsDialog = EditTagsFragment(book) { tags ->
                // Update tags locally
                book.tags = tags

                // Update Firestore
                updateTagsInFirestore(book, tags)

                // Notify adapter to refresh
                notifyItemChanged(position)
            }
            // pulls up dialog to add tags
            addTagsDialog.show(fragmentManager, "AddTagsDialog")
        }

        // Handle book item click
        holder.itemView.setOnClickListener {
            onBookClick(book)
        }

    }

    // Function to increment/decrement pages
    private fun updatePageCount(book: BookItemCollection, pageView: EditText, change: Int) {
        // Parse current pages from EditText
        val currentPages = pageView.text.toString().toIntOrNull() ?: 0

        // Update locally and in the database
        val newPages = currentPages + change
        pageView.setText(newPages.toString())

        // Update the `BookItemCollection` object so sorting does not reset it
        book.pages = newPages

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
                        // Remove the old book page values
                        db.collection("users").document(userId)
                            .update("standardCollections.$collectionName", FieldValue.arrayRemove(bookInCollection))

                        // Modify pages
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
                    // update the pages
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

    // Create TextViews for each tag in the list
    private fun createTagView(tag: String, context: Context, book: BookItemCollection, position: Int): TextView {
        val tagView = TextView(context)
        tagView.text = tag
        tagView.setBackgroundResource(R.drawable.tag_background) // Custom drawable for rectangle background
        tagView.setPadding(15, 15, 15, 15)

        // Set custom text color for the tag
        tagView.setTextColor(ContextCompat.getColor(context, R.color.tag_text))

        // Make the tag clickable
        tagView.setOnClickListener {
            // Open dialog to edit the tags
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val editTagsDialog = EditTagsFragment(book) { tags ->
                // Update tags locally
                book.tags = tags

                // Update Firestore
                updateTagsInFirestore(book, tags)

                // Notify adapter to refresh the item view
                notifyItemChanged(position)
            }
            editTagsDialog.show(fragmentManager, "EditTagsDialog")
        }

        return tagView
    }

    // Updates tags in firestore
    private fun updateTagsInFirestore(book: BookItemCollection, tags: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Update in standard collections
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val standardCollections = document.get("standardCollections") as? Map<String, Any>
            standardCollections?.forEach { (collectionName, books) ->
                if (books is List<*>) {
                    val bookInCollection = books.find { it is Map<*, *> &&
                            it["title"] == book.title &&
                            (it["authors"] as? List<*>)?.containsAll(book.authors) == true
                    }
                    if (bookInCollection != null) {
                        val updatedBook = (bookInCollection as Map<String, Any>).toMutableMap()
                        updatedBook["tags"] = tags

                        // 1. Remove the old book from the collection
                        db.collection("users").document(userId)
                            .update("standardCollections.$collectionName", FieldValue.arrayRemove(bookInCollection))
                            .addOnSuccessListener {
                                // 2. Add the updated book with the new tags
                                db.collection("users").document(userId)
                                    .update("standardCollections.$collectionName", FieldValue.arrayUnion(updatedBook))
                                    .addOnSuccessListener {
                                        updateFavoriteTag(userId)
                                    }
                            }
                            .addOnFailureListener {
                                // Handle failure if needed
                            }
                    }
                }
            }
        }

        // 2. Update in custom collections
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
            customCollections?.forEach { (collectionName, collectionData) ->
                val booksInCustom = collectionData["books"] as? List<Map<String, Any>> ?: return@forEach
                val bookInCustom = booksInCustom.find {
                    it["title"] == book.title &&
                            (it["authors"] as? List<*>)?.containsAll(book.authors) == true
                }
                if (bookInCustom != null) {
                    val updatedBook = bookInCustom.toMutableMap()
                    updatedBook["tags"] = tags
                    val updatedBooks = booksInCustom.toMutableList().apply {
                        remove(bookInCustom)
                        add(updatedBook)
                    }

                    db.collection("users").document(userId)
                        .update("customCollections.$collectionName.books", updatedBooks)
                        .addOnSuccessListener {
                            updateFavoriteTag(userId)
                        }
                }
            }
        }
    }

    // Veronica Nguyen
    // Function to get the user's favorite tag
    private fun updateFavoriteTag(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()  // Gets users collection
            .addOnSuccessListener { document ->
                val tagCount = mutableMapOf<String, Int>() // Map to count number of times a tag appears

                // Gets user's standard collections
                val standardCollections = document.get("standardCollections") as? Map<String, List<Map<String, Any>>>
                // Loops through each standard collection
                standardCollections?.values?.forEach { bookList ->
                    // Loops through each book in a collection
                    bookList.forEach { book ->
                        val tags = book["tags"] as? List<String> ?: listOf()  // Retrieves tags from a book
                        // Loops through each tag of a book
                        tags.forEach { tag ->
                            // Increments the count of that tag by 1
                            tagCount[tag] = tagCount.getOrDefault(tag, 0) + 1
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
                        val tags = book["tags"] as? List<String> ?: listOf()  // Retrieves tags from a book
                        // Loops through each tag of a book
                        tags.forEach { tag ->
                            // Increments the count of that tag by 1
                            tagCount[tag] = tagCount.getOrDefault(tag, 0) + 1
                        }
                    }
                }

                // Sort tags by count in descending order and take the most frequent one
                val favoriteTag = tagCount.entries.maxByOrNull { it.value }?.key

                // Update user's favoriteTag field in Firestore
                db.collection("users").document(userId).update("favoriteTag", favoriteTag)
            }


    }

    // This function returns the total number of books to display in the RecyclerView
    override fun getItemCount(): Int = books.size
}