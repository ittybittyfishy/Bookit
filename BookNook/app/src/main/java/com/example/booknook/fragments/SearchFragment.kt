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
    private var isSearching = false // Flag to prevent multiple searches

    // Pagination variables
    private var startIndex = 0
    private var currentQuery: String? = null

    // Filters
    private var includeGenres: MutableList<String> = mutableListOf()
    private var excludeGenres: MutableList<String> = mutableListOf()
    private var languageFilter: String? = null

    // Available genres for the current search query
    private var availableGenres: MutableSet<String> = mutableSetOf()

    // Scroll listener for RecyclerView
    private lateinit var scrollListener: RecyclerView.OnScrollListener

    // Sorting criteria
    private var currentSortCriteria: String = "default"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    // Initialize UI components and set up listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        filtersButton = view.findViewById(R.id.filtersButton)
        sortByButton = view.findViewById(R.id.sortByButton)
        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter

        // Set up infinite scrolling
        setupRecyclerViewScrollListener()
        recyclerView.addOnScrollListener(scrollListener)

        searchButton.setOnClickListener {
            Log.d("SearchFragment", "Search button clicked")
            performSearch()
        }

        filtersButton.setOnClickListener {
            Log.d("SearchFragment", "Filters button clicked. Current query: $currentQuery")
            if (currentQuery.isNullOrBlank()) {
                Toast.makeText(activity, "Please enter a search query first", Toast.LENGTH_SHORT).show()
            } else {
                collectGenresAndNavigate()
            }
        }

        sortByButton.setOnClickListener {
            showSortByMenu()
        }

        // Handle arguments passed from the filters
        handleArguments()
    }

    private fun setupRecyclerViewScrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !isLoading) { // Check if scrolling down and not already loading
                    val visibleItemCount = recyclerView.layoutManager?.childCount ?: 0
                    val totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
                    val pastVisibleItems =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                            ?: 0

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        Log.d("SearchFragment", "Scrolled to bottom, loading more books")
                        loadBooks(currentQuery ?: "", fetchAllGenres = false)
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

            Log.d("SearchFragment", "Received arguments:")
            Log.d("SearchFragment", "currentQuery: $currentQuery")
            Log.d("SearchFragment", "includeGenres: $includeGenres")
            Log.d("SearchFragment", "excludeGenres: $excludeGenres")
            Log.d("SearchFragment", "languageFilter: $languageFilter")
            Log.d("SearchFragment", "minRating: ${args.getFloat("minRating")}")
            Log.d("SearchFragment", "maxRating: ${args.getFloat("maxRating")}")

            searchEditText.setText(currentQuery)
            performSearch()
        }
    }

    private fun performSearch(fetchAllGenres: Boolean = false) {
        if (isSearching) {
            // Prevent multiple searches at the same time
            return
        }
        isSearching = true
        searchButton.isEnabled = false // Disable the search button

        currentQuery = searchEditText.text.toString()
        if (currentQuery.isNullOrBlank()) {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            isSearching = false
            searchButton.isEnabled = true // Re-enable the search button
            return
        }

        // Reset variables for new search
        startIndex = 0
        bookList.clear()
        bookAdapter.notifyDataSetChanged()
        availableGenres.clear()
        updateNoResultsVisibility(false)

        loadBooks(currentQuery!!, fetchAllGenres = true) {
            // This callback is called when books are loaded
            isSearching = false
            searchButton.isEnabled = true // Re-enable the search button
        }
    }

    private fun loadBooks(
        query: String,
        fetchAllGenres: Boolean = false,
        onBooksLoaded: (() -> Unit)? = null
    ) {
        if (isLoading) return

        isLoading = true
        val localLanguageFilter = languageFilter ?: ""
        val localMaxRating = arguments?.getFloat("maxRating") ?: 5f
        val localMinRating = arguments?.getFloat("minRating") ?: 0f

        Log.d("SearchFragment", "Loading books with query: $query, startIndex: $startIndex")

        val mainActivity = activity as? MainActivity
        if (mainActivity == null) {
            Log.w("SearchFragment", "Activity is null, cannot perform searchBooks")
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
                Log.d("SearchFragment", "Fetched ${books.size} books from API")

                if (fetchAllGenres) {
                    // Collect all available genres from the fetched books
                    books.forEach { book ->
                        val genres = book.volumeInfo.categories?.map { it.trim() } ?: emptyList()
                        availableGenres.addAll(genres)
                    }
                    Log.d("SearchFragment", "Collected genres: $availableGenres")
                }

                val filteredBooks = books.filter { book ->
                    val genres = book.volumeInfo.categories?.map { it.trim().lowercase() } ?: emptyList()
                    val rating = book.volumeInfo.averageRating ?: 0f

                    // Exact matching for genres
                    val genreIncluded = if (includeGenres.isNotEmpty()) {
                        includeGenres.any { selectedGenre ->
                            genres.any { genre -> genre.equals(selectedGenre, ignoreCase = true) }
                        }
                    } else true

                    val genreExcluded = if (excludeGenres.isNotEmpty()) {
                        excludeGenres.none { excludedGenre ->
                            genres.any { genre -> genre.equals(excludedGenre, ignoreCase = true) }
                        }
                    } else true

                    // Check if rating is within the range
                    val ratingInRange = rating in localMinRating..localMaxRating

                    genreIncluded && genreExcluded && ratingInRange
                }

                Log.d("SearchFragment", "Filtered books count: ${filteredBooks.size}")

                // Increment the startIndex based on the original book list, not just the filtered list
                startIndex += books.size

                if (filteredBooks.isEmpty()) {
                    if (startIndex == books.size) { // startIndex was 0 before incrementing
                        updateNoResultsVisibility(true)
                    }
                    // No need to fetch more books here; scrolling will fetch more
                } else {
                    updateNoResultsVisibility(false)
                    val startPosition = bookList.size
                    bookList.addAll(filteredBooks)
                    // Apply sorting if a sort criteria is selected
                    if (currentSortCriteria != "default") {
                        sortBooks(currentSortCriteria)
                    } else {
                        bookAdapter.notifyItemRangeInserted(startPosition, filteredBooks.size)
                    }
                }
            } else {
                Log.d("SearchFragment", "No books fetched from API")
                if (startIndex == 0) {
                    updateNoResultsVisibility(true)
                }
            }

            // Call the callback if provided
            onBooksLoaded?.invoke()
        }
    }

    private fun collectGenresAndNavigate() {
        val currentQuery = searchEditText.text.toString()
        if (currentQuery.isBlank()) {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            return
        }

        // Reset variables
        availableGenres.clear()
        var booksFetched = 0
        var startIndexForGenres = 0
        val totalBooksToFetch = 50

        fun fetchNextBatch() {
            val mainActivity = activity as? MainActivity
            if (mainActivity == null) {
                Log.w("SearchFragment", "Activity is null, cannot perform searchBooks")
                return
            }

            mainActivity.searchBooks(
                currentQuery,
                startIndexForGenres,
                languageFilter = null
            ) { books: List<BookItem>? ->
                if (books != null && books.isNotEmpty()) {
                    Log.d("SearchFragment", "Fetched ${books.size} books for genre collection")
                    books.forEach { bookItem ->
                        val genres = bookItem.volumeInfo.categories?.map { it.trim() } ?: emptyList()
                        availableGenres.addAll(genres)
                    }
                    booksFetched += books.size
                    startIndexForGenres += books.size
                    Log.d("SearchFragment", "Collected genres so far: $availableGenres")
                    if (booksFetched < totalBooksToFetch && books.size > 0) {
                        // Fetch next batch
                        fetchNextBatch()
                    } else {
                        // Done fetching genres
                        Log.d("SearchFragment", "Final collected genres: $availableGenres")
                        navigateToFilters()
                    }
                } else {
                    // No more books to fetch
                    Log.d("SearchFragment", "No more books to fetch for genres")
                    navigateToFilters()
                }
            }
        }

        // Start fetching genres
        fetchNextBatch()
    }

    private fun navigateToFilters() {
        Log.d("SearchFragment", "Navigating to filters with genres: $availableGenres")
        val filterFragment = SearchFiltersFragment()
        val bundle = Bundle()
        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("availableGenres", ArrayList(availableGenres))
        filterFragment.arguments = bundle

        (activity as? MainActivity)?.replaceFragment(filterFragment, "Search Filters")
    }

    private fun updateNoResultsVisibility(show: Boolean) {
        noResultsTextView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showSortByMenu() {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_sortby, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()

        // Initialize the TextViews from the custom layout
        val sortByHighRating: TextView = dialogView.findViewById(R.id.sort_by_high_rating)
        val sortByLowRating: TextView = dialogView.findViewById(R.id.sort_by_low_rating)
        val sortByTitleAZ: TextView = dialogView.findViewById(R.id.sort_by_rating_az    )
        val sortByTitleZA: TextView = dialogView.findViewById(R.id.sort_by_rating_za)
        val sortByAuthor: TextView = dialogView.findViewById(R.id.sort_by_author)

        // Set click listeners for each sorting option
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

        // Show the dialog
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
