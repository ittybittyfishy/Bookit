package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookAdapter
import com.example.booknook.BookItem
import com.example.booknook.MainActivity
import com.example.booknook.R

class SearchFragment : Fragment(), BookAdapter.RecyclerViewEvent {

    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var filtersButton: Button
    private lateinit var sortByButton: Button
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
        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Initially disable filter and sort buttons until a search is made
        filtersButton.isEnabled = false
        filtersButton.alpha = 0.5f
        sortByButton.isEnabled = false
        sortByButton.alpha = 0.5f

        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter

        setupRecyclerViewScrollListener()
        recyclerView.addOnScrollListener(scrollListener)

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

        handleArguments()
    }


    private fun setupRecyclerViewScrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !isLoading) {
                    val visibleItemCount = recyclerView.layoutManager?.childCount ?: 0
                    val totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
                    val pastVisibleItems =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                            ?: 0

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loadBooks(currentQuery ?: "")
                    }
                }
            }
        }
    }

    private fun handleArguments() {
        val args = arguments
        if (args != null) {
            currentQuery = args.getString("currentQuery")
            includeGenres = args.getStringArrayList("includeGenres") ?: mutableListOf()
            excludeGenres = args.getStringArrayList("excludeGenres") ?: mutableListOf()
            languageFilter = args.getString("languageFilter")
            minRating = args.getFloat("minRating", 0f)
            maxRating = args.getFloat("maxRating", 5f)

            searchEditText.setText(currentQuery)

            // Update the filters button text based on the applied filters
            updateFiltersButtonText()

            performSearch()
        }
    }

    private fun updateFiltersButtonText() {
        val filtersApplied = mutableListOf<String>()

        if (includeGenres.isNotEmpty() || excludeGenres.isNotEmpty()) {
            val genresList = mutableListOf<String>()
            if (includeGenres.isNotEmpty()) {
                genresList.add("Include: ${includeGenres.joinToString(", ")}")
            }
            if (excludeGenres.isNotEmpty()) {
                genresList.add("Exclude: ${excludeGenres.joinToString(", ")}")
            }
            filtersApplied.add("Genres: ${genresList.joinToString("; ")}")
        }

        if (!languageFilter.isNullOrBlank()) {
            filtersApplied.add("Language: $languageFilter")
        }

        if (minRating > 0f || maxRating < 5f) {
            filtersApplied.add("Rating: $minRating - $maxRating")
        }

        val filterText = if (filtersApplied.isNotEmpty()) {
            "Filters (${filtersApplied.joinToString(", ")})"
        } else {
            "Filters"
        }

        // Limit the length of the button text to prevent it from becoming too long
        filtersButton.text = if (filterText.length > 30) {
            filterText.substring(0, 27) + "...)"
        } else {
            filterText
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
            // Enable filter and sort buttons after the first search is performed
            filtersButton.isEnabled = true
            filtersButton.alpha = 1.0f
            sortByButton.isEnabled = true
            sortByButton.alpha = 1.0f

            isSearching = false
            searchButton.isEnabled = true
        }
    }

    private fun loadBooks(
        query: String,
        onBooksLoaded: (() -> Unit)? = null
    ) {
        if (isLoading) return

        isLoading = true
        val localLanguageFilter = languageFilter ?: ""
        val localMaxRating = maxRating
        val localMinRating = minRating

        val localIncludeGenres = includeGenres.map { it.trim().lowercase() }.toSet()
        val localExcludeGenres = excludeGenres.map { it.trim().lowercase() }.toSet()

        val mainActivity = activity as? MainActivity
        if (mainActivity == null) {
            isLoading = false
            onBooksLoaded?.invoke()
            return
        }

        mainActivity.searchBooks(
            query,
            startIndex,
            localLanguageFilter.takeIf { it.isNotBlank() }
        ) { books: List<BookItem>? ->
            isLoading = false

            if (books != null) {
                val filteredBooks = books.filter { book ->
                    val bookGenres = book.volumeInfo.categories?.flatMap { category ->
                        category.split("/", "&").map { it.trim().lowercase() }
                    }?.toSet() ?: emptySet()

                    val rating = book.volumeInfo.averageRating ?: 0f

                    val genreIncluded = if (localIncludeGenres.isNotEmpty()) {
                        // Book's genres should be a subset of the includeGenres
                        bookGenres.isNotEmpty() && bookGenres.all { genre ->
                            localIncludeGenres.contains(genre)
                        }
                    } else true

                    val genreExcluded = if (localExcludeGenres.isNotEmpty()) {
                        // None of the book's genres should be in excludeGenres
                        bookGenres.none { genre ->
                            localExcludeGenres.contains(genre)
                        }
                    } else true

                    val ratingInRange = rating in localMinRating..localMaxRating

                    genreIncluded && genreExcluded && ratingInRange
                }

                startIndex += books.size

                if (filteredBooks.isEmpty()) {
                    if (startIndex == books.size) {
                        updateNoResultsVisibility(true)
                    }
                } else {
                    updateNoResultsVisibility(false)
                    val startPosition = bookList.size
                    bookList.addAll(filteredBooks)
                    if (currentSortCriteria != "default") {
                        sortBooks(currentSortCriteria)
                    } else {
                        bookAdapter.notifyItemRangeInserted(startPosition, filteredBooks.size)
                    }
                }
            } else {
                if (startIndex == 0) {
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
