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

class SearchFragment : Fragment() {

    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(activity)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotBlank()) {
                (activity as MainActivity).searchBooks(query) { books ->
                    if (books != null) {
                        bookAdapter = BookAdapter(books)
                        recyclerView.adapter = bookAdapter
                    } else {
                        Toast.makeText(activity, "No books found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
