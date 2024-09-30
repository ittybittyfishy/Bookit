package com.example.booknook.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookAdapter
import com.example.booknook.BookItem
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.utils.GenreUtils

class SearchFragment : Fragment(), BookAdapter.RecyclerViewEvent {

    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var filtersButton: Button
    private lateinit var sortByButton: Button
    private lateinit var clearFiltersButton: Button
    private lateinit var noResultsTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter

    private var bookList: MutableList<BookItem> = mutableListOf()
    private var isLoading = false
    private var isSearching = false

    private var startIndex = 0
    private var currentQuery: String? = null

    private var includeGenres: MutableList<String> = mutableListOf()
    private var excludeGenres: MutableList<String> = mutableListOf()
    private var languageFilter: String? = null
    private var minRating: Float = 0f
    private var maxRating: Float = 5f

    private lateinit var scrollListener: RecyclerView.OnScrollListener

    private var currentSortCriteria: String = "default"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        filtersButton = view.findViewById(R.id.filtersButton)
        sortByButton = view.findViewById(R.id.sortByButton)
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton)
        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter

        setupRecyclerViewScrollListener()

        filtersButton.alpha = 0.5f
        sortByButton.alpha = 0.5f

        searchButton.setOnClickListener {
            performSearch()
        }

        filtersButton.setOnClickListener {
            if (currentQuery.isNullOrBlank()) {
                Toast.makeText(activity, "Please enter a search query first", Toast.LENGTH_SHORT).show()
            } else {
                navigateToFilters()
            }
        }

        sortByButton.setOnClickListener {
            if (currentQuery.isNullOrBlank()) {
                Toast.makeText(activity, "Please enter a search query first", Toast.LENGTH_SHORT).show()
            } else {
                showSortByMenu()
            }
        }

        clearFiltersButton.setOnClickListener {
            clearFilters()
        }

        handleArguments()
    }

    override fun onResume() {
        super.onResume()
        // Check for the "clearSearch" flag passed from MainActivity
        arguments?.getBoolean("clearSearch")?.let { shouldClearSearch ->
            if (shouldClearSearch) {
                clearSearchQuery()
            }
        }
    }

    // Function to clear the search query and reset filters
    private fun clearSearchQuery() {
        searchEditText.text.clear() // Clear the search query text
        bookList.clear() // Clear any previous results
        bookAdapter.notifyDataSetChanged() // Notify the adapter
        noResultsTextView.visibility = View.GONE // Hide "no results" message
        filtersButton.isEnabled = false
        filtersButton.alpha = 0.5f
        sortByButton.isEnabled = false
        sortByButton.alpha = 0.5f
    }

    // New function to clear filters
    private fun clearFilters() {
        // Clear filters like language, rating, and genre selections
        languageFilter = null
        minRating = 0f
        maxRating = 5f
        includeGenres.clear()
        excludeGenres.clear()

        // Reset the UI states, such as disabling the filters and sort buttons
        filtersButton.alpha = 0.5f
        filtersButton.isEnabled = false
        sortByButton.alpha = 0.5f
        sortByButton.isEnabled = false

        // Hide the clear filters button since filters have been cleared
        clearFiltersButton.visibility = View.GONE

        Toast.makeText(activity, "Filters cleared", Toast.LENGTH_SHORT).show()

        // Update the filter button text to "Filters"
        updateFiltersButtonText()

        // Optionally, refresh the search results without filters
        performSearch()
    }

    private fun setupRecyclerViewScrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    // If user cannot scroll further and loading is not already in progress, load more books
                    loadBooks(currentQuery ?: "")
                }
            }
        }

        recyclerView.addOnScrollListener(scrollListener)
    }


    private fun handleArguments() {
        val args = arguments
        if (args != null) {
            currentQuery = args.getString("currentQuery")
            includeGenres = args.getStringArrayList("includeGenres")?.toMutableList() ?: mutableListOf()
            excludeGenres = args.getStringArrayList("excludeGenres")?.toMutableList() ?: mutableListOf()
            languageFilter = args.getString("languageFilter")
            minRating = args.getFloat("minRating", 0f)
            maxRating = args.getFloat("maxRating", 5f)

            searchEditText.setText(currentQuery)

            // Show the clear filters button if filters are applied
            if (includeGenres.isNotEmpty() || excludeGenres.isNotEmpty() || languageFilter != null || minRating > 0f || maxRating < 5f) {
                clearFiltersButton.visibility = View.VISIBLE
            } else {
                clearFiltersButton.visibility = View.GONE
            }

            updateFiltersButtonText()

            performSearch()
        }
    }

    private fun updateFiltersButtonText() {
        // Count the number of active filters
        var filterCount = 0

        if (includeGenres.isNotEmpty()) filterCount += 1
        if (excludeGenres.isNotEmpty()) filterCount += 1
        if (!languageFilter.isNullOrBlank()) filterCount += 1
        if (minRating > 0f || maxRating < 5f) filterCount += 1

        // Update the Filters button text based on the number of active filters
        filtersButton.text = when (filterCount) {
            0 -> "Filters"
            else -> "Filters ($filterCount)"
        }
    }

    private fun performSearch() {
        if (isSearching) {
            return
        }
        isSearching = true
        searchButton.isEnabled = false

        currentQuery = searchEditText.text.toString()
        if (currentQuery.isNullOrBlank()) {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            isSearching = false
            searchButton.isEnabled = true
            return
        }

        startIndex = 0
        bookList.clear()
        bookAdapter.notifyDataSetChanged()
        updateNoResultsVisibility(false)

        loadBooks(currentQuery!!) {
            filtersButton.isEnabled = true
            filtersButton.alpha = 1.0f
            sortByButton.isEnabled = true
            sortByButton.alpha = 1.0f

            isSearching = false
            searchButton.isEnabled = true
        }
    }

    private fun loadBooks(query: String, onBooksLoaded: (() -> Unit)? = null) {
        if (isLoading) return

        isLoading = true
        val localLanguageFilter = languageFilter ?: ""
        val localMaxRating = maxRating
        val localMinRating = minRating

        val localIncludeGenres = includeGenres.map { GenreUtils.normalizeGenre(it) }.toSet()
        val localExcludeGenres = excludeGenres.map { GenreUtils.normalizeGenre(it) }.toSet()

        Log.d("SearchFragment", "Search Query: $query")
        Log.d("SearchFragment", "Include Genres: $localIncludeGenres")
        Log.d("SearchFragment", "Exclude Genres: $localExcludeGenres")
        Log.d("SearchFragment", "Language Filter: $localLanguageFilter")
        Log.d("SearchFragment", "Rating Range: $localMinRating - $localMaxRating")
        Log.d("SearchFragment", "Start Index: $startIndex") // Keep track of start index for debugging

        val mainActivity = activity as? MainActivity
        if (mainActivity == null) {
            isLoading = false
            onBooksLoaded?.invoke()
            return
        }

        // Pass the languageFilter and startIndex when searching books for pagination
        mainActivity.searchBooks(query, startIndex, localLanguageFilter.takeIf { it.isNotBlank() }) { books: List<BookItem>? ->
            isLoading = false

            if (books != null) {
                val filteredBooks = books.filter { book ->
                    val bookGenres = book.volumeInfo.categories?.flatMap { category ->
                        category.split("/", "&").map { GenreUtils.normalizeGenre(it) }
                    }?.toSet() ?: emptySet()

                    val rating = book.volumeInfo.averageRating ?: 0f
                    val ratingInRange = rating in localMinRating..localMaxRating

                    val genreIncluded = if (localIncludeGenres.isNotEmpty()) {
                        bookGenres.any { genre -> localIncludeGenres.contains(genre) }
                    } else true

                    val genreExcluded = if (localExcludeGenres.isNotEmpty()) {
                        bookGenres.none { genre -> localExcludeGenres.contains(genre) }
                    } else true

                    genreIncluded && genreExcluded && ratingInRange
                }

                startIndex += books.size // Update the startIndex to load more in the next call

                if (filteredBooks.isNotEmpty()) {
                    bookList.addAll(filteredBooks)
                    // Sort the entire bookList after adding the new books
                    sortBooks(currentSortCriteria)
                    updateNoResultsVisibility(false)
                } else {
                    if (startIndex == books.size) {
                        Log.d("SearchFragment", "No books found after filtering.")
                        updateNoResultsVisibility(true)
                    }
                }
            } else {
                if (startIndex == 0) {
                    Log.d("SearchFragment", "No books returned from search.")
                    updateNoResultsVisibility(true)
                }
            }

            onBooksLoaded?.invoke()
        }
    }



    private fun navigateToFilters() {
        val filterFragment = SearchFiltersFragment()
        val bundle = Bundle()
        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList(includeGenres))
        bundle.putStringArrayList("excludeGenres", ArrayList(excludeGenres))
        bundle.putString("languageFilter", languageFilter)
        bundle.putFloat("minRating", minRating)
        bundle.putFloat("maxRating", maxRating)
        filterFragment.arguments = bundle

        (activity as? MainActivity)?.replaceFragment(filterFragment, "Search Filters")
    }

    private fun updateNoResultsVisibility(show: Boolean) {
        noResultsTextView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showSortByMenu() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_sortby, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()

        val sortByHighRating: TextView = dialogView.findViewById(R.id.sort_by_high_rating)
        val sortByLowRating: TextView = dialogView.findViewById(R.id.sort_by_low_rating)
        val sortByTitleAZ: TextView = dialogView.findViewById(R.id.sort_by_rating_az)
        val sortByTitleZA: TextView = dialogView.findViewById(R.id.sort_by_rating_za)
        val sortByAuthor: TextView = dialogView.findViewById(R.id.sort_by_author)

        sortByHighRating.setOnClickListener {
            currentSortCriteria = "high_rating"
            sortBooks(currentSortCriteria)
            alertDialog.dismiss()
        }

        sortByLowRating.setOnClickListener {
            currentSortCriteria = "low_rating"
            sortBooks(currentSortCriteria)
            alertDialog.dismiss()
        }

        sortByTitleAZ.setOnClickListener {
            currentSortCriteria = "title_az"
            sortBooks(currentSortCriteria)
            alertDialog.dismiss()
        }

        sortByTitleZA.setOnClickListener {
            currentSortCriteria = "title_za"
            sortBooks(currentSortCriteria)
            alertDialog.dismiss()
        }

        sortByAuthor.setOnClickListener {
            currentSortCriteria = "author"
            sortBooks(currentSortCriteria)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun sortBooks(criteria: String) {
        when (criteria) {
            "high_rating" -> {
                bookList.sortByDescending { it.volumeInfo.averageRating ?: 0f }
                sortByButton.text = "Rating: High to Low ▼"
            }
            "low_rating" -> {
                bookList.sortBy { it.volumeInfo.averageRating ?: 0f }
                sortByButton.text = "Rating: Low to High ▼"
            }
            "title_az" -> {
                bookList.sortBy { it.volumeInfo.title ?: "" }
                sortByButton.text = "Title: A to Z ▼"
            }
            "title_za" -> {
                bookList.sortByDescending { it.volumeInfo.title ?: "" }
                sortByButton.text = "Title: Z to A ▼"
            }
            "author" -> {
                bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" }
                sortByButton.text = "Author: A to Z ▼"
            }
        }
        bookAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }


    override fun onItemClick(position: Int) {
        val bookItem = bookList[position]
        val bookDetailsFragment = BookDetailsFragment()
        val bundle = Bundle()

        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        bundle.putString("bookAuthor", bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        bundle.putString(
            "bookImage",
            bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        )
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)

        bookDetailsFragment.arguments = bundle
        (activity as? MainActivity)?.replaceFragment(bookDetailsFragment, bookItem.volumeInfo.title)
    }
}
