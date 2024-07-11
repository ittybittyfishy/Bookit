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

class SearchFragment : Fragment() {

    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private var bookList: MutableList<BookItem> = mutableListOf()
    private var isLoading = false
    private var currentQuery: String? = null
    private var startIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList)
        recyclerView.adapter = bookAdapter

        searchButton.setOnClickListener {
            performSearch()
        }

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

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= (lastVisibleItem + 2)) {
                    loadMoreBooks()
                }
            }
        })

        return view
    }

    private fun loadBooks() {
        currentQuery?.let { query ->
            isLoading = true
            (activity as MainActivity).searchBooks(query, startIndex) { books ->
                isLoading = false
                if (books != null) {
                    bookList.addAll(books)
                    bookAdapter.notifyDataSetChanged()
                    startIndex += books.size
                } else {
                    Toast.makeText(activity, "No books found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMoreBooks() {
        if (currentQuery != null) {
            loadBooks()
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString()
        if (query.isNotBlank()) {
            currentQuery = query
            bookList.clear()
            startIndex = 0
            loadBooks()
        } else {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
        }
    }
}
