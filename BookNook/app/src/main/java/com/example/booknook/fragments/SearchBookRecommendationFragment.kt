package com.example.booknook.fragments

// Import necessary Android libraries for handling UI components and functionality
import android.app.AlertDialog // For creating dialog windows
import android.os.Bundle // For passing data between components
import android.util.Log // For logging debug information
import android.view.* // For handling view-related operations
import android.widget.* // For using widgets like Button, EditText, TextView
import androidx.fragment.app.Fragment // Base class for fragments
import androidx.recyclerview.widget.LinearLayoutManager // For arranging items in RecyclerView
import androidx.recyclerview.widget.RecyclerView // For displaying scrollable lists
import com.example.booknook.BookAdapter
import com.example.booknook.BookItem // Data class representing a book
import com.example.booknook.BookRecommendationAdapter
import com.example.booknook.MainActivity // Main activity of the app
import com.example.booknook.R // Resource file (layouts, strings, etc.)
import com.example.booknook.utils.GenreUtils // Utility class for genre-related operations


class SearchBookRecommendationFragment : Fragment(), BookRecommendationAdapter.RecyclerViewEvent {

    // Declare UI components that will be used in the fragment
    private lateinit var searchButton: ImageButton // Button to initiate a search
    private lateinit var searchEditText: EditText // Text input for search queries
    private lateinit var filtersButton: Button // Button to open filter options
    private lateinit var sortByButton: Button // Button to open sort options
    private lateinit var clearFiltersButton: Button // Button to clear applied filters
    private lateinit var noResultsTextView: TextView // TextView to display "No results" message
    private lateinit var recyclerView: RecyclerView // RecyclerView to display list of books
    private lateinit var bookRecommendationAdapter: BookRecommendationAdapter // Adapter to manage data for RecyclerView

    // List to hold the books fetched from the API
    private var bookList: MutableList<BookItem> = mutableListOf()
    private var isLoading = false // Flag to indicate if data is currently loading
    private var isSearching = false // Flag to indicate if a search is in progress

    private var startIndex = 0 // Index to keep track of pagination
    private var currentQuery: String? = null // Current search query

    // Lists to hold genres to include and exclude in the search
    private var includeGenres: MutableList<String> = mutableListOf()
    private var excludeGenres: MutableList<String> = mutableListOf()
    private var languageFilter: String? = null // Language filter for the search
    private var minRating: Float = 0f // Minimum rating filter
    private var maxRating: Float = 5f // Maximum rating filter

    private lateinit var scrollListener: RecyclerView.OnScrollListener // Listener to detect scroll events

    private var currentSortCriteria: String = "default" // Current sorting criteria

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_book_recommendation, container, false)
    }

    // Called after the view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupId = arguments?.getString("groupId")
        Log.d("SearchBookRecommendationsFragment", "Fetching recommendations for groupId: $groupId")

        // Initialize UI components by finding them in the inflated view
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        filtersButton = view.findViewById(R.id.filtersButton)
        sortByButton = view.findViewById(R.id.sortByButton)
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton)
        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Set up the RecyclerView with a LinearLayoutManager and attach the adapter
        recyclerView.layoutManager = LinearLayoutManager(activity) // Arrange items vertically
        bookRecommendationAdapter =
            groupId?.let { BookRecommendationAdapter(bookList, it, this) }!! // Initialize the adapter with the book list

        recyclerView.adapter = bookRecommendationAdapter // Attach the adapter to the RecyclerView

        // Set up a listener to detect when the user scrolls to the bottom of the list
        setupRecyclerViewScrollListener()

        // Set the opacity of the filters and sort buttons to indicate they are disabled initially
        filtersButton.alpha = 0.5f
        sortByButton.alpha = 0.5f

        // Set up click listener for the search button to initiate a search
        searchButton.setOnClickListener {
            performSearch() // Call the function to perform search
        }

        // Set up click listener for the filters button to navigate to filter options
        filtersButton.setOnClickListener {
            if (currentQuery.isNullOrBlank()) { // Check if there is a search query
                Toast.makeText(activity, "Please enter a search query first", Toast.LENGTH_SHORT).show()
            } else {
                navigateToFilters() // Navigate to the filters fragment
            }
        }

        // Set up click listener for the sort button to show sort options
        sortByButton.setOnClickListener {
            if (currentQuery.isNullOrBlank()) { // Check if there is a search query
                Toast.makeText(activity, "Please enter a search query first", Toast.LENGTH_SHORT).show()
            } else {
                showSortByMenu() // Show the sort options menu
            }
        }

        // Set up click listener for the clear filters button to reset all filters
        clearFiltersButton.setOnClickListener {
            clearFilters() // Call the function to clear filters
        }

        // Handle any arguments passed to this fragment (e.g., filters applied)
        handleArguments()
    }

    // Called when the fragment becomes visible again
    override fun onResume() {
        super.onResume()
        // Check if there is a flag to clear the search query
        arguments?.getBoolean("clearSearch")?.let { shouldClearSearch ->
            if (shouldClearSearch) {
                clearSearchQuery() // Clear the search query if the flag is set
            }
        }
    }

    // Function to clear the search query and reset the UI
    private fun clearSearchQuery() {
        searchEditText.text.clear() // Clear the text input for search
        bookList.clear() // Clear the list of books
        bookRecommendationAdapter.notifyDataSetChanged() // Notify the adapter that data has changed
        noResultsTextView.visibility = View.GONE // Hide the "No results" message
        filtersButton.isEnabled = false // Disable the filters button
        filtersButton.alpha = 0.5f // Set opacity to indicate disabled state
        sortByButton.isEnabled = false // Disable the sort button
        sortByButton.alpha = 0.5f // Set opacity to indicate disabled state
    }

    // Function to clear all applied filters
    private fun clearFilters() {
        // Reset all filter variables to their default states
        languageFilter = null
        minRating = 0f
        maxRating = 5f
        includeGenres.clear()
        excludeGenres.clear()

        // Reset the UI elements related to filters and sort buttons
        filtersButton.alpha = 0.5f // Set opacity to indicate disabled state
        filtersButton.isEnabled = false // Disable the filters button
        sortByButton.alpha = 0.5f // Set opacity to indicate disabled state
        sortByButton.isEnabled = false // Disable the sort button

        // Hide the clear filters button since filters have been reset
        clearFiltersButton.visibility = View.GONE

        // Show a toast message to inform the user that filters have been cleared
        Toast.makeText(activity, "Filters cleared", Toast.LENGTH_SHORT).show()

        // Update the text on the filters button to reflect the reset state
        updateFiltersButtonText()

        // Optionally, perform a search without any filters to refresh the results
        performSearch()
    }

    // Function to set up a scroll listener on the RecyclerView for pagination
    private fun setupRecyclerViewScrollListener() {
        // Create an anonymous inner class for the scroll listener
        scrollListener = object : RecyclerView.OnScrollListener() {
            // Called when the RecyclerView has been scrolled
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the bottom of the list and if data is not already loading
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    loadBooks(currentQuery ?: "") // Load more books if conditions are met
                }
            }
        }

        // Attach the scroll listener to the RecyclerView
        recyclerView.addOnScrollListener(scrollListener)
    }

    // Function to handle any arguments passed to the fragment (e.g., from filters)
    private fun handleArguments() {
        val args = arguments // Retrieve the arguments bundle
        if (args != null) { // Check if arguments are not null
            // Extract filter parameters from the arguments
            currentQuery = args.getString("currentQuery")
            includeGenres = args.getStringArrayList("includeGenres")?.toMutableList() ?: mutableListOf()
            excludeGenres = args.getStringArrayList("excludeGenres")?.toMutableList() ?: mutableListOf()
            languageFilter = args.getString("languageFilter")
            minRating = args.getFloat("minRating", 0f)
            maxRating = args.getFloat("maxRating", 5f)

            // Set the search EditText with the current query
            searchEditText.setText(currentQuery)

            // Show the clear filters button if any filters are applied
            if (includeGenres.isNotEmpty() || excludeGenres.isNotEmpty() || languageFilter != null || minRating > 0f || maxRating < 5f) {
                clearFiltersButton.visibility = View.VISIBLE
            } else {
                clearFiltersButton.visibility = View.GONE
            }

            // Update the text on the filters button based on active filters
            updateFiltersButtonText()

            // Perform the search with the applied filters
            performSearch()
        }
    }

    // Function to update the text on the filters button to show the number of active filters
    private fun updateFiltersButtonText() {
        var filterCount = 0 // Counter for active filters

        // Increment the counter for each active filter
        if (includeGenres.isNotEmpty()) filterCount += 1
        if (excludeGenres.isNotEmpty()) filterCount += 1
        if (!languageFilter.isNullOrBlank()) filterCount += 1
        if (minRating > 0f || maxRating < 5f) filterCount += 1

        // Update the button text based on the number of active filters
        filtersButton.text = when (filterCount) {
            0 -> "Filters" // No active filters
            else -> "Filters ($filterCount)" // Show the number of active filters
        }
    }

    // Function to initiate a search based on the current query and filters
    private fun performSearch() {
        if (isSearching) { // Prevent multiple simultaneous searches
            return
        }
        isSearching = true // Set the searching flag to true
        searchButton.isEnabled = false // Disable the search button to prevent multiple clicks

        // Get the search query from the EditText
        currentQuery = searchEditText.text.toString()
        if (currentQuery.isNullOrBlank()) { // Check if the query is empty
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            isSearching = false // Reset the searching flag
            searchButton.isEnabled = true // Re-enable the search button
            return
        }

        startIndex = 0 // Reset the start index for pagination
        bookList.clear() // Clear the existing list of books
        bookRecommendationAdapter.notifyDataSetChanged() // Notify the adapter about data changes
        updateNoResultsVisibility(false) // Hide the "No results" message

        // Load books based on the search query
        loadBooks(currentQuery!!) { // The !! operator asserts that currentQuery is not null
            filtersButton.isEnabled = true // Enable the filters button
            filtersButton.alpha = 1.0f // Set opacity to indicate enabled state
            sortByButton.isEnabled = true // Enable the sort button
            sortByButton.alpha = 1.0f // Set opacity to indicate enabled state

            isSearching = false // Reset the searching flag
            searchButton.isEnabled = true // Re-enable the search button
        }
    }

    // Function to load books based on search query and various filters
    private fun loadBooks(query: String, onBooksLoaded: (() -> Unit)? = null) {
        // Prevent further calls if a book search is already in progress
        if (isLoading) return

        // Set loading status to true
        isLoading = true

        // Local copies of the filter parameters to ensure they don't change mid-execution
        val localLanguageFilter = languageFilter ?: "" // Language filter or an empty string if null
        val localMaxRating = maxRating // Maximum rating filter
        val localMinRating = minRating // Minimum rating filter

        // Normalize the genre filters (map included and excluded genres)
        val localIncludeGenres = includeGenres.map { GenreUtils.normalizeGenre(it) }.toSet()
        val localExcludeGenres = excludeGenres.map { GenreUtils.normalizeGenre(it) }.toSet()

        // Debugging logs to track filter values
        Log.d("SearchFragment", "Search Query: $query")
        Log.d("SearchFragment", "Include Genres: $localIncludeGenres")
        Log.d("SearchFragment", "Exclude Genres: $localExcludeGenres")
        Log.d("SearchFragment", "Language Filter: $localLanguageFilter")
        Log.d("SearchFragment", "Rating Range: $localMinRating - $localMaxRating")
        Log.d("SearchFragment", "Start Index: $startIndex") // Track start index for pagination

        // Get the activity as MainActivity to access its methods, or stop if it's null
        val mainActivity = activity as? MainActivity
        if (mainActivity == null) {
            isLoading = false // Set loading status to false
            onBooksLoaded?.invoke() // Invoke the callback if provided
            return // Exit the function if activity is null
        }

        // Pass the language filter and startIndex for pagination when searching books
        mainActivity.searchBooks(query, startIndex, localLanguageFilter.takeIf { it.isNotBlank() }) { books: List<BookItem>? ->
            isLoading = false // Reset loading status after search results are returned

            // If books are returned
            if (books != null) {
                // Log each book's title and genres
                books.forEach { book ->
                    Log.d("SearchFragment", "Book Title: ${book.volumeInfo.title}, Genres: ${book.volumeInfo.categories?.joinToString(", ") ?: "No genres"}")
                }

                // Filter the books based on genre and rating criteria
                val filteredBooks = books.filter { book ->
                    // Normalize and collect the genres of the book
                    val bookGenres = book.volumeInfo.categories?.flatMap { category ->
                        category.split("/", "&").map { GenreUtils.normalizeGenre(it) }
                    }?.toSet() ?: emptySet()

                    // Check if the book's rating falls within the range
                    val rating = book.volumeInfo.averageRating ?: 0f
                    val ratingInRange = rating in localMinRating..localMaxRating

                    // Check if the book contains any of the included genres
                    val genreIncluded = if (localIncludeGenres.isNotEmpty()) {
                        bookGenres.any { genre -> localIncludeGenres.contains(genre) }
                    } else true

                    // Ensure the book doesn't contain any excluded genres
                    val genreExcluded = if (localExcludeGenres.isNotEmpty()) {
                        bookGenres.none { genre -> localExcludeGenres.contains(genre) }
                    } else true

                    // Return true if the book matches all criteria
                    genreIncluded && genreExcluded && ratingInRange
                }

                // Update the startIndex to load more books in the next search call
                startIndex += books.size

                // If there are filtered books, add them to the bookList and sort the list
                if (filteredBooks.isNotEmpty()) {
                    bookList.addAll(filteredBooks)
                    sortBooks(currentSortCriteria) // Sort books based on the current sorting criteria
                    updateNoResultsVisibility(false) // Hide 'no results' message
                } else {
                    // If no books found after filtering, show 'no results' message
                    if (startIndex == books.size) {
                        Log.d("SearchFragment", "No books found after filtering.")
                        updateNoResultsVisibility(true)
                    }
                }
            } else {
                // If no books were returned at all, show 'no results' message for the first page
                if (startIndex == 0) {
                    Log.d("SearchFragment", "No books returned from search.")
                    updateNoResultsVisibility(true)
                }
            }

            // Invoke the callback function if provided, to notify that loading is complete
            onBooksLoaded?.invoke()
        }
    }


    // Function to navigate to the filters fragment where users can set search filters
    private fun navigateToFilters() {
        val filterFragment = SearchFiltersFragment() // Create an instance of SearchFiltersFragment
        val bundle = Bundle() // Bundle to pass data to the fragment

        // Put the current search parameters into the bundle
        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList(includeGenres))
        bundle.putStringArrayList("excludeGenres", ArrayList(excludeGenres))
        bundle.putString("languageFilter", languageFilter)
        bundle.putFloat("minRating", minRating)
        bundle.putFloat("maxRating", maxRating)
        filterFragment.arguments = bundle // Set the arguments for the fragment

        // Replace the current fragment with the filters fragment
        (activity as? MainActivity)?.replaceFragment(filterFragment, "Search Filters")
    }

    // Function to show or hide the "No results" message
    private fun updateNoResultsVisibility(show: Boolean) {
        noResultsTextView.visibility = if (show) View.VISIBLE else View.GONE
    }

    // Function to show the sort options menu in a dialog
    private fun showSortByMenu() {
        // Inflate the layout for the sort options dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_sortby, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView) // Set the custom view for the dialog

        val alertDialog = builder.create() // Create the AlertDialog instance

        // Initialize the sort option TextViews from the dialog layout
        val sortByHighRating: TextView = dialogView.findViewById(R.id.sort_by_high_rating)
        val sortByLowRating: TextView = dialogView.findViewById(R.id.sort_by_low_rating)
        val sortByTitleAZ: TextView = dialogView.findViewById(R.id.sort_by_rating_az)
        val sortByTitleZA: TextView = dialogView.findViewById(R.id.sort_by_rating_za)
        val sortByAuthor: TextView = dialogView.findViewById(R.id.sort_by_author)

        // Set up click listeners for each sort option
        sortByHighRating.setOnClickListener {
            currentSortCriteria = "high_rating" // Set sort criteria
            sortBooks(currentSortCriteria) // Sort the books
            alertDialog.dismiss() // Close the dialog
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

        alertDialog.show() // Display the sort options dialog
    }

    // Function to sort the list of books based on the selected criteria
    private fun sortBooks(criteria: String) {
        when (criteria) { // Determine sorting based on criteria
            "high_rating" -> {
                bookList.sortByDescending { it.volumeInfo.averageRating ?: 0f } // Sort high to low
                sortByButton.text = "Rating: High to Low ▼" // Update button text
            }
            "low_rating" -> {
                bookList.sortBy { it.volumeInfo.averageRating ?: 0f } // Sort low to high
                sortByButton.text = "Rating: Low to High ▼"
            }
            "title_az" -> {
                bookList.sortBy { it.volumeInfo.title ?: "" } // Sort titles A to Z
                sortByButton.text = "Title: A to Z ▼"
            }
            "title_za" -> {
                bookList.sortByDescending { it.volumeInfo.title ?: "" } // Sort titles Z to A
                sortByButton.text = "Title: Z to A ▼"
            }
            "author" -> {
                bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" } // Sort authors A to Z
                sortByButton.text = "Author: A to Z ▼"
            }
        }
        bookRecommendationAdapter.notifyDataSetChanged() // Notify the adapter about data changes
        recyclerView.scrollToPosition(0) // Scroll back to the top of the list
    }

    // Veronica Nguyen
    // Opens a book's details in another page upon clicking on it
    override fun onItemClick(position: Int) {
        val groupId = arguments?.getString("groupId")
        val bookItem = bookList[position]
        val bookDetailsRecommendationFragment = BookDetailsRecommendationFragment()
        val bundle = Bundle() // Bundle to store data that will be transferred to the fragment
        // Yunjong Noh
        // get ISBN number to manage books collection data
        val isbn = bookItem.volumeInfo.industryIdentifiers
            ?.find { it.type == "ISBN_13" || it.type == "ISBN_10" }
            ?.identifier ?: "No ISBN"
        val description = bookItem.volumeInfo.description  // Gets the book's description

        // Adds data into the bundle
        bundle.putString("groupId", groupId)
        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        // Puts authors in a string separated by commas
        bundle.putString("bookAuthor", bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        // Puts authors in a string array list to store database
        bundle.putStringArrayList("bookAuthorsList", ArrayList(bookItem.volumeInfo.authors ?: listOf("Unknown Author")))
        bundle.putString("bookImage", bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"))
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)
        bundle.putString("bookIsbn", isbn)
        bundle.putString("bookDescription", description)
        bundle.putStringArrayList("bookGenres", ArrayList(bookItem.volumeInfo.categories ?: listOf("Unknown Genre")))

        bookDetailsRecommendationFragment.arguments = bundle  // sets bookDetailsFragment's arguments to the data in bundle
        (activity as MainActivity).replaceFragment(bookDetailsRecommendationFragment, "Book Details", showBackButton = true)  // Opens a new fragment
    }

}