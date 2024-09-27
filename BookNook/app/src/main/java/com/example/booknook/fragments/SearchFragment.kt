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
    private val loadedBookIdentifiers = mutableSetOf<String>()
    private val availableGenres: MutableSet<String> = mutableSetOf() // Store available genres

    private lateinit var noResultsTextView: TextView //for when no filter results are found


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        noResultsTextView = view.findViewById(R.id.noResultsTextView) //when no results are found
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.recyclerView)
        filterButton = view.findViewById(R.id.filtersButton)
        sortByButton = view.findViewById(R.id.sortByButton)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisibleItem + 5)) {
                    loadMoreBooks()
                }
            }
        })

        currentQuery = arguments?.getString("currentQuery")
        includeGenres = arguments?.getStringArrayList("includeGenres")
        excludeGenres = arguments?.getStringArrayList("excludeGenres")
        languageFilter = arguments?.getString("languageFilter")
        minimumRating = arguments?.getFloat("minRating") ?: 0f
        val maxRating = arguments?.getFloat("maxRating") ?: 5f

        searchEditText.setText(currentQuery)

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

    private fun loadBooks(query: String) {
        if (isLoading) return

        isLoading = true

        val includeGenresList = includeGenres ?: emptyList()
        val excludeGenresList = excludeGenres ?: emptyList()

        val localLanguageFilter = languageFilter ?: ""
        val localMaxRating = arguments?.getFloat("maxRating") ?: 5f

        (activity as MainActivity).searchBooks(query, startIndex, localLanguageFilter.takeIf { it.isNotBlank() }) { books ->
            isLoading = false

            if (books != null && books.isNotEmpty()) {
                val filteredBooks = books.filter { book ->
                    val genres = book.volumeInfo.categories ?: emptyList()

                    val genreIncluded = includeGenresList.isEmpty() || includeGenresList.any { includeGenre ->
                        genres.any { it.equals(includeGenre, ignoreCase = true) }
                    }
                    val genreExcluded = excludeGenresList.isNotEmpty() && excludeGenresList.any { excludeGenre ->
                        genres.any { it.equals(excludeGenre, ignoreCase = true) }
                    }

                    val bookRating = book.volumeInfo.averageRating ?: 0f
                    val matchesRating = if (bookRating == 0f && minimumRating == 0f) {
                        true
                    } else {
                        bookRating in minimumRating..localMaxRating
                    }

                    val matchesLanguage = localLanguageFilter.isBlank() || book.volumeInfo.language == localLanguageFilter

                    val bookIdentifier = book.volumeInfo.industryIdentifiers?.firstOrNull()?.identifier
                    val fallbackIdentifier = "${book.volumeInfo.title}-${book.volumeInfo.authors?.firstOrNull() ?: "Unknown Author"}"
                    val isUnique = (bookIdentifier != null && loadedBookIdentifiers.add(bookIdentifier)) ||
                            loadedBookIdentifiers.add(fallbackIdentifier)

                    availableGenres.addAll(genres) // Collect genres from each book

                    genreIncluded && !genreExcluded && matchesRating && matchesLanguage && isUnique
                }

                if (filteredBooks.isEmpty()) {
                    noResultsTextView.visibility = View.VISIBLE // Show "No results found"
                    recyclerView.visibility = View.GONE // Hide RecyclerView
                } else {
                    noResultsTextView.visibility = View.GONE // Hide "No results found"
                    recyclerView.visibility = View.VISIBLE // Show RecyclerView

                    val startPosition = bookList.size
                    bookList.addAll(filteredBooks)
                    bookAdapter.notifyItemRangeInserted(startPosition, filteredBooks.size)

                    startIndex += books.size
                }
            } else {
                noResultsTextView.visibility = View.VISIBLE // Show "No results found"
                recyclerView.visibility = View.GONE // Hide RecyclerView
            }
        }
    }


    private fun loadMoreBooks() {
        currentQuery?.let {
            loadBooks(it)
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

        startIndex = 0
        loadBooks(currentQuery!!)
    }


    private fun navigateToFilters() {
        val filterFragment = SearchFiltersFragment()
        val bundle = Bundle()

        bundle.putString("currentQuery", searchEditText.text.toString())
        bundle.putStringArrayList("availableGenres", ArrayList(availableGenres)) // Pass only available genres
        filterFragment.arguments = bundle

        (activity as MainActivity).replaceFragment(filterFragment, "Search Filters")
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

        //low rating
        popupView.findViewById<TextView>(R.id.sort_by_low_rating).setOnClickListener {
            sortBooksByRating(highToLow = false)
            popupWindow.dismiss()
        }

        //sort a - z
        popupView.findViewById<TextView>(R.id.sort_by_rating_az).setOnClickListener {
            sortBooksByTitle(ascending = true)
            popupWindow.dismiss()
        }

        //sort z - a
        popupView.findViewById<TextView>(R.id.sort_by_rating_za).setOnClickListener {
            sortBooksByTitle(ascending = false)
            popupWindow.dismiss()
        }

        //sort by author
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