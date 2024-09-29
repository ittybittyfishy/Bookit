package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
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
import android.widget.PopupWindow
import android.widget.TextView
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
    private var includeGenres: ArrayList<String>? = null
    private var excludeGenres: ArrayList<String>? = null
    private var languageFilter: String? = null
    private var minimumRating: Float = 0f
    private val availableGenres: MutableSet<String> = mutableSetOf() // Store available genres

    private lateinit var noResultsTextView: TextView // For when no filter results are found

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Retrieve passed arguments
        currentQuery = arguments?.getString("currentQuery")
        includeGenres = arguments?.getStringArrayList("includeGenres") ?: arrayListOf()  // Ensure it's not null
        excludeGenres = arguments?.getStringArrayList("excludeGenres") ?: arrayListOf()  // Ensure it's not null
        languageFilter = arguments?.getString("languageFilter")
        minimumRating = arguments?.getFloat("minRating") ?: 0f
        val maxRating = arguments?.getFloat("maxRating") ?: 5f

        Log.d("SearchFragment", "Current Query: $currentQuery")
        Log.d("SearchFragment", "Included Genres: $includeGenres")
        Log.d("SearchFragment", "Excluded Genres: $excludeGenres")
        Log.d("SearchFragment", "Language Filter: $languageFilter")
        Log.d("SearchFragment", "Rating Range: $minimumRating to $maxRating")

        searchEditText.setText(currentQuery)

        // Set up scroll listener for RecyclerView to load more books as we scroll down
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisibleItem + 5)) {
                    loadMoreBooks() // Load more books when we are near the bottom
                }
            }
        })

        searchButton.setOnClickListener {
            performSearch()
        }

        filterButton.setOnClickListener {
            navigateToFilters()
        }

        sortByButton.setOnClickListener {
            showSortPopup(it)
        }

        if (!currentQuery.isNullOrBlank()) {
            performSearch()
        }

        return view
    }

    // Function to load books based on the query and filters
    private fun loadBooks(query: String) {
        if (isLoading) return

        isLoading = true
        val localLanguageFilter = languageFilter ?: ""
        val localMaxRating = arguments?.getFloat("maxRating") ?: 5f

        // Create local immutable copies of includeGenres and excludeGenres
        val localIncludeGenres = includeGenres ?: arrayListOf()
        val localExcludeGenres = excludeGenres ?: arrayListOf()

        // Perform the book search query (this interacts with your API or backend)
        (activity as MainActivity).searchBooks(query, startIndex, localLanguageFilter.takeIf { it.isNotBlank() }) { books ->
            isLoading = false

            if (books != null && books.isNotEmpty()) {
                // Extract available genres from books and add them to availableGenres set
                books.forEach { book ->
                    book.volumeInfo.categories?.forEach { genre ->
                        availableGenres.add(genre)
                    }
                }

                // Apply genre filters
                val filteredBooks = books.filter { book ->
                    val genres = book.volumeInfo.categories?.map { it.trim().lowercase() } ?: emptyList()

                    Log.d("SearchFragment", "Genres for Book: $genres")
                    Log.d("SearchFragment", "Include Genres: $localIncludeGenres")
                    Log.d("SearchFragment", "Exclude Genres: $localExcludeGenres")

                    // Check if the book should be included based on included genres
                    val genreIncluded = if (localIncludeGenres.isNotEmpty()) {
                        // Allow partial matches by checking if any of the book's genres contain the include genre (case-insensitive)
                        localIncludeGenres.any { selectedGenre ->
                            genres.any { genre -> genre.contains(selectedGenre.lowercase().trim()) }
                        }
                    } else {
                        true // If no genres to include, allow all books
                    }

                    // Check if the book should be excluded based on excluded genres
                    val genreExcluded = if (localExcludeGenres.isNotEmpty()) {
                        // Exclude if any genre matches exactly
                        localExcludeGenres.none { excludedGenre ->
                            genres.contains(excludedGenre.lowercase().trim())
                        }
                    } else {
                        true // If no genres to exclude, allow all books
                    }

                    Log.d("SearchFragment", "Book included: $genreIncluded, Book excluded: $genreExcluded")
                    genreIncluded && genreExcluded
                }

                // If no books matched, show "No results found"
                if (filteredBooks.isEmpty()) {
                    updateNoResultsVisibility(true)
                } else {
                    updateNoResultsVisibility(false)

                    val startPosition = bookList.size
                    bookList.addAll(filteredBooks)
                    bookAdapter.notifyItemRangeInserted(startPosition, filteredBooks.size)
                    startIndex += books.size // Increment the start index to load more books
                }
            } else {
                updateNoResultsVisibility(true)
            }
        }
    }

    // Function to update the visibility of the no results text view
    private fun updateNoResultsVisibility(isVisible: Boolean) {
        noResultsTextView.visibility = if (isVisible) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    // Function to navigate to the filter fragment
    private fun navigateToFilters() {
        val filterFragment = SearchFiltersFragment()
        val bundle = Bundle()
        bundle.putString("currentQuery", searchEditText.text.toString())
        bundle.putStringArrayList("availableGenres", ArrayList(availableGenres)) // Pass available genres to filter fragment
        filterFragment.arguments = bundle

        (activity as MainActivity).replaceFragment(filterFragment, "Search Filters")
    }

    private fun loadMoreBooks() {
        if (!isLoading && currentQuery != null) {
            loadBooks(currentQuery!!)  // Continue loading more books with the current query
        }
    }

    private fun performSearch() {
        currentQuery = searchEditText.text.toString()
        if (currentQuery.isNullOrBlank()) {
            Toast.makeText(activity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            return
        }

        noResultsTextView.visibility = View.GONE // Hide "No results found" initially
        recyclerView.visibility = View.VISIBLE // Show RecyclerView initially

        val itemCount = bookList.size
        if (itemCount > 0) {
            bookList.clear()
            bookAdapter.notifyItemRangeRemoved(0, itemCount)
        }

        startIndex = 0 // Reset the start index for a new search
        loadBooks(currentQuery!!)
    }

    override fun onItemClick(position: Int) {
        val bookItem = bookList[position]
        val bookDetailsFragment = BookDetailsFragment()
        val bundle = Bundle()

        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        bundle.putString("bookAuthor", bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        bundle.putString("bookImage", bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"))
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)

        bookDetailsFragment.arguments = bundle
        (activity as MainActivity).replaceFragment(bookDetailsFragment, bookItem.volumeInfo.title)
    }

    // Function to display the sorting popup
    private fun showSortPopup(anchorView: View) {
        // Inflate the sortby.xml layout
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.fragment_sortby, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Show the PopupWindow below the anchor (sortByButton)
        popupWindow.showAsDropDown(anchorView, 0, 0)

        // Handle the sorting options
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

        // Allow the user to click outside to dismiss the popup
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    // Function to sort books by rating (High to Low or Low to High)
    private fun sortBooksByRating(highToLow: Boolean) {
        if (highToLow) {
            bookList.sortByDescending { it.volumeInfo.averageRating }
        } else {
            bookList.sortBy { it.volumeInfo.averageRating }
        }
        bookAdapter.notifyDataSetChanged() // Notify adapter of the change
    }

    // Function to sort books by title (A-Z or Z-A)
    private fun sortBooksByTitle(ascending: Boolean) {
        if (ascending) {
            bookList.sortBy { it.volumeInfo.title }
        } else {
            bookList.sortByDescending { it.volumeInfo.title }
        }
        bookAdapter.notifyDataSetChanged() // Notify adapter of the change
    }

    // Function to sort books by author name (A-Z or Z-A)
    private fun sortBooksByAuthor(ascending: Boolean) {
        if (ascending) {
            bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" }
        } else {
            bookList.sortByDescending { it.volumeInfo.authors?.firstOrNull() ?: "" }
        }
        bookAdapter.notifyDataSetChanged() // Notify adapter of the change
    }
}
