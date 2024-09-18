package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookAdapter
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.BookItem
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent

// Fragment for searching books
class SearchFragment : Fragment(), BookAdapter.RecyclerViewEvent {

    // Declare UI elements
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter

    // List to hold book items and variables for pagination
    private var bookList: MutableList<BookItem> = mutableListOf()
    private var isLoading = false
    private var currentQuery: String? = null
    private var startIndex = 0

    // Called to have the fragment instantiate its user interface view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize the UI elements using findViewById
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Set click listener for the search button
        searchButton.setOnClickListener {
            performSearch() // Perform search when the button is clicked
        }

        // Set listener for the keyboard's search action
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch() // Perform search when the search action is triggered
                true
            } else {
                false
            }
        }

        // Set up RecyclerView with a LinearLayoutManager and the adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter


        // Add scroll listener to the RecyclerView for infinite scrolling
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= (lastVisibleItem + 2)) {
                    loadMoreBooks() // Load more books when nearing the end of the list
                }
            }
        })

        return view
    }

    // Load books from the query starting from startIndex
    private fun loadBooks() {
        currentQuery?.let { query ->
            isLoading = true
            (activity as MainActivity).searchBooks(query, startIndex) { books ->
                isLoading = false
                if (books != null) {
                    val startPosition = bookList.size
                    bookList.addAll(books)
                    bookAdapter.notifyItemRangeInserted(startPosition, books.size)
                    startIndex += books.size // Update the start index for the next batch of books
                } else {
                    Toast.makeText(activity, "No books found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Load more books for infinite scrolling
    private fun loadMoreBooks() {
        if (currentQuery != null) {
            loadBooks()
        }
    }

    // Perform the search when the search button or search action is triggered
    private fun performSearch() {
        val query = searchEditText.text.toString()
        if (query.isNotBlank()) {
            currentQuery = query
            val itemCount = bookList.size
            bookList.clear() // Clear the existing book list
            bookAdapter.notifyItemRangeRemoved(0, itemCount)
            startIndex = 0 // Reset the start index
            loadBooks() // Load books based on the new query
        } else {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
        }
    }

    // Opens a book's details in another page upon clicking on it
    override fun onItemClick(position: Int) {
        val bookItem = bookList[position]
        // TO-DO: Finish page with book details
        val bookDetailsFragment = BookDetailsFragment()
        val bundle = Bundle() // Bundle to store data that will be transferred to the fragment
        val isbn = bookItem.volumeInfo.industryIdentifiers
            ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
            ?.identifier ?: "No ISBN"

        // Adds data into the bundle
        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        bundle.putString("bookAuthor", bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        bundle.putString("bookImage", bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"))
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)
        bundle.putString("bookIsbn", isbn)

        bookDetailsFragment.arguments = bundle  // sets bookDetailsFragment's arguments to the data in bundle
        (activity as MainActivity).replaceFragment(bookDetailsFragment, bookItem.volumeInfo.title)  // Opens a new fragment
    }
}