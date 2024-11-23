package com.example.booknook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.fragments.ConfirmRecommendationBookDetailsFragment
import com.example.booknook.fragments.ConfirmRecommendationFragment

// Adapter class that connects the data (BookItems) with the RecyclerView to display each book.
class BookRecommendationBookDetailsAdapter(
    private val bookList: List<BookItem>, // List of books to display
    private val isbn: String, // ISBN for the book
    private val listener: RecyclerViewEvent // Listener for handling click events on books
) : RecyclerView.Adapter<BookRecommendationBookDetailsAdapter.BookViewHolder>() {

    // List of predefined collections that users can assign books to.
    private val standardCollections = listOf("Select Collection", "Reading", "Finished", "Want to Read", "Dropped", "Remove")

    // Called when RecyclerView needs a new ViewHolder. ViewHolder represents each item view.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Inflate the item layout for each book
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_recommendation, parent, false)
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

        // Handle the "Select Book" button click event
        holder.selectBookButton.setOnClickListener {
            // Takes user to the confirm page to confirm their book for recommendation
            val ConfirmRecommendationBookDetailsFragment = ConfirmRecommendationBookDetailsFragment()
            val description = book.volumeInfo.description
            val recIsbn = book.volumeInfo.industryIdentifiers
                ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
                ?.identifier ?: "No ISBN"
            val bundle = Bundle().apply {
                putString("isbn", isbn) // Use isbn of parent book
                putString("recIsbn", recIsbn)
                putString("bookImage", book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"))
                putString("bookTitle", book.volumeInfo.title)
                putString("bookAuthor", book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
                putStringArrayList("bookAuthorsList", ArrayList(book.volumeInfo.authors ?: listOf("Unknown Author")))
                putString("bookDescription", description)
                putStringArrayList("bookGenres", ArrayList(book.volumeInfo.categories ?: listOf("Unknown Genre")))
                putFloat("bookRating", book.volumeInfo.averageRating ?: 0f)
            }
            ConfirmRecommendationBookDetailsFragment.arguments = bundle
            (holder.itemView.context as MainActivity).replaceFragment(ConfirmRecommendationBookDetailsFragment, "Add Recommendation", showBackButton = false)
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
        val selectBookButton: Button = itemView.findViewById(R.id.selectBookButton)

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
}