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

// Define a Fragment class for searching books
class SearchFragment : Fragment() {

    // Declare variables for UI elements and data
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private var bookList: MutableList<BookItem> = mutableListOf()
    private var isLoading = false
    private var currentQuery: String? = null
    private var startIndex = 0

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize the UI elements
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Set up RecyclerView with a LinearLayoutManager and the adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList)
        recyclerView.adapter = bookAdapter

        // Set click listener for the search button
        searchButton.setOnClickListener {
            performSearch()
        }

        // Set listener for the keyboard's search action
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Add scroll listener to the RecyclerView for infinite scrolling
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                // Load more books when the user scrolls near the end of the list
                if (!isLoading && totalItemCount <= (lastVisibleItem + 2)) {
                    loadMoreBooks()
                }
            }
        })

        // Return the created view
        return view
    }

    // Method to load books based on the current query and start index
    private fun loadBooks() {
        currentQuery?.let { query ->
            isLoading = true
            // Call the MainActivity's searchBooks method to fetch books
            (activity as MainActivity).searchBooks(query, startIndex) { books ->
                isLoading = false
                if (books != null) {
                    // Save the current size of the book list before adding new books
                    val startPosition = bookList.size
                    // Add the fetched books to the list
                    bookList.addAll(books)
                    // Notify the adapter of the new items inserted
                    bookAdapter.notifyItemRangeInserted(startPosition, books.size)
                    startIndex += books.size
                } else {
                    // Show a message if no books are found
                    Toast.makeText(activity, "No books found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Method to load more books when the user scrolls down
    private fun loadMoreBooks() {
        if (currentQuery != null) {
            loadBooks()
        }
    }

    // Method to perform a new search
    private fun performSearch() {
        val query = searchEditText.text.toString()
        if (query.isNotBlank()) {
            // Set the current query and reset the book list and start index
            currentQuery = query
            // Notify adapter of the items removed
            val itemCount = bookList.size
            bookList.clear()
            bookAdapter.notifyItemRangeRemoved(0, itemCount)
            startIndex = 0
            loadBooks()
        } else {
            // Show a message if the search query is blank
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
        }
    }
}
