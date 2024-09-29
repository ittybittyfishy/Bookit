package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.BookAdapter
import com.example.booknook.BookItem
import com.example.booknook.MainActivity
import com.example.booknook.R
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class SearchFragment : Fragment(), BookAdapter.RecyclerViewEvent {

    // Declare variables
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var filterButton: Button
    private lateinit var sortByButton: Button

    private var bookList: MutableList<BookItem> = mutableListOf()
    private var isLoading = false
    private var currentQuery: String? = null
    private var startIndex = 0
    private var includeGenres: ArrayList<String> = arrayListOf()
    private var excludeGenres: ArrayList<String> = arrayListOf()
    private var languageFilter: String? = null
    private var minimumRating: Float = 0f
    private val availableGenres: MutableSet<String> = mutableSetOf() // Store available genres

    private lateinit var noResultsTextView: TextView // For when no filter results are found

    // Add a limit for genre collection to prevent excessive API calls
    private val genreCollectionLimit = 50 // Adjust as needed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize UI elements
        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)
        filterButton = view.findViewById(R.id.filtersButton)
        sortByButton = view.findViewById(R.id.sortByButton)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter

        // Scroll listener to load more books as you scroll
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Existing code remains the same
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Load more books when reaching near the end of the list
                if (!isLoading && totalItemCount <= lastVisibleItem + 5) {
                    Log.d("SearchFragment", "Scrolled to bottom, loading more books")
                    loadBooks(currentQuery ?: "", fetchAllGenres = false)
                }
            }
        })

        // Retrieve passed arguments
        currentQuery = arguments?.getString("currentQuery")
        includeGenres = arguments?.getStringArrayList("includeGenres") ?: arrayListOf()
        excludeGenres = arguments?.getStringArrayList("excludeGenres") ?: arrayListOf()
        languageFilter = arguments?.getString("languageFilter")
        minimumRating = arguments?.getFloat("minRating") ?: 0f
        val maxRating = arguments?.getFloat("maxRating") ?: 5f

        Log.d("SearchFragment", "Received arguments:")
        Log.d("SearchFragment", "currentQuery: $currentQuery")
        Log.d("SearchFragment", "includeGenres: $includeGenres")
        Log.d("SearchFragment", "excludeGenres: $excludeGenres")
        Log.d("SearchFragment", "languageFilter: $languageFilter")
        Log.d("SearchFragment", "minRating: $minimumRating")
        Log.d("SearchFragment", "maxRating: $maxRating")

        searchEditText.setText(currentQuery)

        searchButton.setOnClickListener {
            performSearch(true) // Fetch all books and collect genres
        }

        // Updated filterButton listener to collect genres before navigating
        filterButton.setOnClickListener {
            currentQuery = searchEditText.text.toString()
            if (currentQuery.isNullOrBlank()) {
                Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Clear previous data
            availableGenres.clear()
            bookList.clear()
            bookAdapter.notifyDataSetChanged()
            startIndex = 0

            Log.d("SearchFragment", "Filter button clicked. Current query: $currentQuery")

            // Load books and collect genres
            collectGenresAndNavigate()
        }

        sortByButton.setOnClickListener {
            showSortPopup(it)
        }

        if (!currentQuery.isNullOrBlank()) {
            performSearch(true) // Load all genres
        }

        return view
    }

    // Function to collect genres by fetching multiple pages
    private fun collectGenresAndNavigate() {
        val totalBooksToFetch = genreCollectionLimit
        var booksFetched = 0
        var tempStartIndex = 0
        val tempAvailableGenres = mutableSetOf<String>()

        fun fetchNextBatch() {
            isLoading = true
            (activity as MainActivity).searchBooks(
                currentQuery!!,
                tempStartIndex,
                languageFilter.takeIf { it?.isNotBlank() == true }
            ) { books: List<BookItem>? ->
                isLoading = false
                if (books != null && books.isNotEmpty()) {
                    books.forEach { book ->
                        val genres = book.volumeInfo.categories?.map { it.trim() } ?: emptyList()
                        tempAvailableGenres.addAll(genres)
                    }
                    booksFetched += books.size
                    tempStartIndex += books.size

                    Log.d("SearchFragment", "Fetched ${books.size} books for genre collection")
                    Log.d("SearchFragment", "Collected genres so far: $tempAvailableGenres")

                    if (booksFetched < totalBooksToFetch && books.size > 0) {
                        // Fetch next batch
                        fetchNextBatch()
                    } else {
                        // Done fetching genres
                        availableGenres.clear()
                        availableGenres.addAll(tempAvailableGenres)
                        Log.d("SearchFragment", "Final collected genres: $availableGenres")
                        navigateToFilters()
                    }
                } else {
                    // No more books to fetch
                    availableGenres.clear()
                    availableGenres.addAll(tempAvailableGenres)
                    Log.d("SearchFragment", "Final collected genres: $availableGenres")
                    navigateToFilters()
                }
            }
        }

        // Start fetching genres
        fetchNextBatch()
    }

    // Function to load books based on the query and filters
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

        // Perform the book search query (this interacts with your API or backend)
        (activity as MainActivity).searchBooks(
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

                if (filteredBooks.isEmpty()) {
                    if (startIndex == 0) {
                        updateNoResultsVisibility(true)
                    }
                } else {
                    updateNoResultsVisibility(false)
                    val startPosition = bookList.size
                    bookList.addAll(filteredBooks)
                    bookAdapter.notifyItemRangeInserted(startPosition, filteredBooks.size)

                    // Increment the startIndex based on the original book list, not just the filtered list
                    startIndex += books.size
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

    // Function to update the visibility of the no results text view
    private fun updateNoResultsVisibility(isVisible: Boolean) {
        noResultsTextView.visibility = if (isVisible) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    // Function to navigate to the filter fragment
    private fun navigateToFilters() {
        Log.d("SearchFragment", "Navigating to filters with genres: $availableGenres")

        val filterFragment = SearchFiltersFragment()
        val bundle = Bundle()
        bundle.putString("currentQuery", searchEditText.text.toString())
        bundle.putStringArrayList("availableGenres", ArrayList(availableGenres)) // Pass genres
        filterFragment.arguments = bundle

        (activity as MainActivity).replaceFragment(filterFragment, "Search Filters")
    }

    private fun performSearch(fetchAllGenres: Boolean = false) {
        currentQuery = searchEditText.text.toString()
        if (currentQuery.isNullOrBlank()) {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SearchFragment", "Performing search with query: $currentQuery")

        noResultsTextView.visibility = View.GONE // Hide "No results found" initially
        recyclerView.visibility = View.VISIBLE // Show RecyclerView initially

        val itemCount = bookList.size
        if (itemCount > 0) {
            bookList.clear()
            bookAdapter.notifyItemRangeRemoved(0, itemCount)
        }

        // Reset the pagination
        startIndex = 0

        // Clear previous genres
        availableGenres.clear()

        // Load books and collect genres
        loadBooks(currentQuery!!, fetchAllGenres = true)
    }

    override fun onItemClick(position: Int) {
        val bookItem = bookList[position]
        val bookDetailsFragment = BookDetailsFragment()
        val bundle = Bundle()

        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        bundle.putString(
            "bookAuthor",
            bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
        )
        bundle.putString(
            "bookImage",
            bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        )
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)

        bookDetailsFragment.arguments = bundle
        (activity as MainActivity).replaceFragment(bookDetailsFragment, bookItem.volumeInfo.title)
    }

    private fun showSortPopup(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.fragment_sortby, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAsDropDown(anchorView, 0, 0)

        popupView.findViewById<TextView>(R.id.sort_by_high_rating).setOnClickListener {
            sortBooksByRating(highToLow = true)
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.sort_by_low_rating).setOnClickListener {
            sortBooksByRating(highToLow = false)
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.sort_by_rating_az).setOnClickListener {
            sortBooksByTitle(ascending = true)
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.sort_by_rating_za).setOnClickListener {
            sortBooksByTitle(ascending = false)
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.sort_by_author).setOnClickListener {
            sortBooksByAuthor(ascending = true)
            popupWindow.dismiss()
        }

        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun sortBooksByRating(highToLow: Boolean) {
        if (highToLow) {
            bookList.sortByDescending { it.volumeInfo.averageRating }
        } else {
            bookList.sortBy { it.volumeInfo.averageRating }
        }
        bookAdapter.notifyDataSetChanged()
    }

    private fun sortBooksByTitle(ascending: Boolean) {
        if (ascending) {
            bookList.sortBy { it.volumeInfo.title }
        } else {
            bookList.sortByDescending { it.volumeInfo.title }
        }
        bookAdapter.notifyDataSetChanged()
    }

    private fun sortBooksByAuthor(ascending: Boolean) {
        if (ascending) {
            bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" }
        } else {
            bookList.sortByDescending { it.volumeInfo.authors?.firstOrNull() ?: "" }
        }
        bookAdapter.notifyDataSetChanged()
    }
}
