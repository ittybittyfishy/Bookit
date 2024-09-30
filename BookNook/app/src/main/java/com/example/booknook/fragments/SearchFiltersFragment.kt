// File: SearchFiltersFragment.kt
package com.example.booknook.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.example.booknook.utils.GenreUtils
import com.example.booknook.BookItem

class SearchFiltersFragment : Fragment() {

    private lateinit var submitButton: Button
    private var currentQuery: String? = null
    private lateinit var includeGenresSection: LinearLayout
    private lateinit var excludeGenresSection: LinearLayout
    private lateinit var includeToggleButton: Button
    private lateinit var excludeToggleButton: Button
    private lateinit var languageEditText: EditText // Add language filter input field
    private var availableGenres: ArrayList<String>? = null
    private lateinit var genresProgressBar: ProgressBar
    private var genreResultsMap: MutableMap<String, Int> = mutableMapOf() // Holds count of results per genre

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_search_filters, container, false)

        // Initialize UI elements
        val ratingSpinner = view.findViewById<Spinner>(R.id.ratingSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.rating_ranges,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ratingSpinner.adapter = adapter

        currentQuery = arguments?.getString("currentQuery")

        includeGenresSection = view.findViewById(R.id.includeGenresSection)
        excludeGenresSection = view.findViewById(R.id.excludeGenresSection)
        includeToggleButton = view.findViewById(R.id.includeGenresToggleButton)
        excludeToggleButton = view.findViewById(R.id.excludeGenresToggleButton)
        languageEditText = view.findViewById(R.id.languageEditText) // Initialize language filter input field

        includeToggleButton.setOnClickListener {
            toggleVisibility(includeGenresSection, includeToggleButton)
        }

        excludeToggleButton.setOnClickListener {
            toggleVisibility(excludeGenresSection, excludeToggleButton)
        }

        submitButton = view.findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val genresPair = getSelectedGenres()
            performSearchWithGenres(genresPair)
        }

        // Initialize the ProgressBar
        genresProgressBar = view.findViewById(R.id.genresProgressBar)

        // Disable UI elements
        submitButton.isEnabled = false
        includeToggleButton.isEnabled = false
        excludeToggleButton.isEnabled = false

        // Show the ProgressBar
        genresProgressBar.visibility = View.VISIBLE

        // Start fetching genres
        fetchGenres()

        return view
    }

    private fun fetchGenres() {
        val handler = Handler(Looper.getMainLooper())
        val fetchGenresTimeout = 10000L // 10 seconds

        val timeoutRunnable = Runnable {
            if (genresProgressBar.visibility == View.VISIBLE) {
                Toast.makeText(activity, "Fetching genres is taking too long", Toast.LENGTH_SHORT).show()
                genresProgressBar.visibility = View.GONE
                submitButton.isEnabled = true
                includeToggleButton.isEnabled = true
                excludeToggleButton.isEnabled = true
            }
        }
        handler.postDelayed(timeoutRunnable, fetchGenresTimeout)

        val mainActivity = activity as? MainActivity
        if (mainActivity == null) {
            Toast.makeText(activity, "Failed to fetch genres", Toast.LENGTH_SHORT).show()
            genresProgressBar.visibility = View.GONE
            handler.removeCallbacks(timeoutRunnable)
            return
        }

        // Fetch genres from the main activity
        mainActivity.fetchGenresForQuery(currentQuery ?: "") { genres: Set<String>? ->
            handler.removeCallbacks(timeoutRunnable)
            activity?.runOnUiThread {
                genresProgressBar.visibility = View.GONE
                if (genres != null) {
                    availableGenres = ArrayList<String>(genres)
                    Log.d("SearchFiltersFragment", "Fetched Genres: $availableGenres")

                    // Fetch actual genre results count
                    fetchGenreResultsCount { genreResults ->
                        if (availableGenres != null && availableGenres!!.isNotEmpty()) {
                            populateGenreCheckboxes(includeGenresSection, "Include")
                            populateGenreCheckboxes(excludeGenresSection, "Exclude")
                        }
                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch genres", Toast.LENGTH_SHORT).show()
                }
                submitButton.isEnabled = true
                includeToggleButton.isEnabled = true
                excludeToggleButton.isEnabled = true
            }
        }
    }

    // Fetch actual results count for each genre based on current search results
    private fun fetchGenreResultsCount(onResultsFetched: (Map<String, Int>) -> Unit) {
        val mainActivity = activity as? MainActivity
        if (mainActivity == null || currentQuery.isNullOrBlank()) {
            onResultsFetched(emptyMap())
            return
        }

        // Fetch books based on the current query to determine genre counts
        mainActivity.searchBooks(currentQuery!!, 0, null) { books: List<BookItem>? ->
            if (books != null) {
                val results = mutableMapOf<String, Int>()
                availableGenres?.forEach { genre ->
                    val normalizedGenre = GenreUtils.normalizeGenre(genre)
                    val count = books.count { book ->
                        book.volumeInfo.categories?.any { category ->
                            GenreUtils.normalizeGenre(category).contains(normalizedGenre)
                        } ?: false
                    }
                    if (count > 0) {
                        results[genre] = count
                    }
                }
                genreResultsMap = results
                onResultsFetched(results)
            } else {
                onResultsFetched(emptyMap())
            }
        }
    }

    private fun toggleVisibility(section: LinearLayout, toggleButton: Button) {
        if (section.visibility == View.GONE) {
            section.visibility = View.VISIBLE
            toggleButton.text = toggleButton.text.toString().replace("▼", "▲")
        } else {
            section.visibility = View.GONE
            toggleButton.text = toggleButton.text.toString().replace("▲", "▼")
        }
    }

    private fun populateGenreCheckboxes(container: LinearLayout, type: String) {
        val genreLinearLayout = if (type == "Include") {
            container.findViewById<LinearLayout>(R.id.includeGenresLinearLayout)
        } else {
            container.findViewById<LinearLayout>(R.id.excludeGenresLinearLayout)
        }

        genreLinearLayout.removeAllViews()

        availableGenres?.let { genres ->
            if (genres.isNotEmpty()) {
                genres.forEach { genre ->
                    // Only add genres that have results
                    if (genreHasResults(genre)) {
                        val checkBox = CheckBox(context)
                        checkBox.text = genre
                        checkBox.textSize = 18f
                        checkBox.tag = genre

                        // Ensure checkboxes are not pre-checked unless explicitly selected
                        checkBox.isChecked = false

                        Log.d("SearchFiltersFragment", "Adding Checkbox for Genre: $genre with results: ${genreResultsMap[genre]}")

                        genreLinearLayout.addView(checkBox)
                    }
                }
            }
        }
    }

    // Check if a genre has actual results (based on fetched results)
    private fun genreHasResults(genre: String): Boolean {
        val resultsCount = genreResultsMap[genre] ?: 0
        return resultsCount > 0 // Only show genres that have at least 1 result
    }

    private fun getSelectedGenres(): Pair<MutableList<String>, MutableList<String>> {
        val includeGenres = mutableListOf<String>()
        val excludeGenres = mutableListOf<String>()

        val includeLinearLayout = includeGenresSection.findViewById<LinearLayout>(R.id.includeGenresLinearLayout)
        for (i in 0 until includeLinearLayout.childCount) {
            val child = includeLinearLayout.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                includeGenres.add(child.tag as String)
            }
        }

        val excludeLinearLayout = excludeGenresSection.findViewById<LinearLayout>(R.id.excludeGenresLinearLayout)
        for (i in 0 until excludeLinearLayout.childCount) {
            val child = excludeLinearLayout.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                excludeGenres.add(child.tag as String)
            }
        }

        // Log the selected genres for debugging
        Log.d("SearchFiltersFragment", "User Selected Include Genres: $includeGenres")
        Log.d("SearchFiltersFragment", "User Selected Exclude Genres: $excludeGenres")

        return Pair(includeGenres, excludeGenres)
    }

    private fun performSearchWithGenres(genresPair: Pair<MutableList<String>, MutableList<String>>) {
        val (includeGenres, excludeGenres) = genresPair
        val searchFragment = SearchFragment()
        val bundle = Bundle()

        val selectedRatingRange = view?.findViewById<Spinner>(R.id.ratingSpinner)?.selectedItem.toString()
        val ratingRange = parseRatingRange(selectedRatingRange)

        // Normalize genres before passing to SearchFragment
        val normalizedIncludeGenres = includeGenres.map { GenreUtils.normalizeGenre(it) }.toMutableList()
        val normalizedExcludeGenres = excludeGenres.map { GenreUtils.normalizeGenre(it) }.toMutableList()

        Log.d("SearchFiltersFragment", "Performing Search with Include Genres: $normalizedIncludeGenres, Exclude Genres: $normalizedExcludeGenres")

        // Get the language filter from the input field
        val languageFilter = languageEditText.text.toString() // Store the language filter

        // Pass filters to SearchFragment
        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList(normalizedIncludeGenres))
        bundle.putStringArrayList("excludeGenres", ArrayList(normalizedExcludeGenres))
        bundle.putString("languageFilter", languageFilter) // Pass the language filter
        if (ratingRange != null) {
            bundle.putFloat("minRating", ratingRange.first)
            bundle.putFloat("maxRating", ratingRange.second)
        } else {
            bundle.putFloat("minRating", 0f)
            bundle.putFloat("maxRating", 5f)
        }

        searchFragment.arguments = bundle

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.menu_container, searchFragment, "SearchFragment")
            .addToBackStack("SearchFragment")
            .commit()
    }

    private fun parseRatingRange(selectedRange: String): Pair<Float, Float>? {
        return when (selectedRange) {
            "0.0 to 0.9" -> Pair(0.0f, 0.9f)
            "1.0 to 1.9" -> Pair(1.0f, 1.9f)
            "2.0 to 2.9" -> Pair(2.0f, 2.9f)
            "3.0 to 3.9" -> Pair(3.0f, 3.9f)
            "4.0 to 4.9" -> Pair(4.0f, 4.9f)
            "5.0 only" -> Pair(5.0f, 5.0f)
            else -> null
        }
    }
}
