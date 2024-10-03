// File: SearchFiltersFragment.kt
package com.example.booknook.fragments

// Import necessary Android libraries for working with views, layouts, and UI elements
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.* // For working with UI elements (buttons, layouts, etc.)
import android.widget.* // For working with buttons, edit texts, and more
import androidx.fragment.app.Fragment // For creating a fragment in Android
import com.example.booknook.MainActivity // Importing the MainActivity to call its functions
import com.example.booknook.R // Importing layout resources (like activity_search_filters.xml)
import com.example.booknook.utils.GenreUtils // Utility class for handling genre-related operations
import com.example.booknook.BookItem // Data class representing a book item

// This fragment handles the UI and logic for setting up and applying search filters
class SearchFiltersFragment : Fragment() {

    // Declare variables for UI components (buttons, layouts, etc.)
    private lateinit var submitButton: Button // Button to submit the search with filters
    private var currentQuery: String? = null // Stores the current search query (optional)
    private lateinit var includeGenresSection: LinearLayout // Section for genres to include in the search
    private lateinit var excludeGenresSection: LinearLayout // Section for genres to exclude from the search
    private lateinit var includeToggleButton: Button // Button to toggle the inclusion of genres
    private lateinit var excludeToggleButton: Button // Button to toggle the exclusion of genres
    private lateinit var languageEditText: EditText // Input field for specifying a language filter
    private var availableGenres: ArrayList<String>? = null // Holds the list of available genres for the filters
    private lateinit var genresProgressBar: ProgressBar // Progress bar shown while genres are being fetched
    private var genreResultsMap: MutableMap<String, Int> = mutableMapOf() // To store genre results count (e.g., number of books per genre)

    // Map for converting language names (like "English") to their respective language codes (like "en")
    private val languageMap = mapOf(
        "english" to "en",
        "french" to "fr",
        "spanish" to "es",
        "german" to "de",
        "italian" to "it",
        "portuguese" to "pt",
        "chinese" to "zh",
        "japanese" to "ja",
        "korean" to "ko",
        "russian" to "ru"
        // Add more languages here if necessary
    )

    // Called when the fragment's view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate (create) the layout for this fragment (activity_search_filters.xml)
        val view = inflater.inflate(R.layout.activity_search_filters, container, false)

        // Initialize the spinner (dropdown) for selecting rating ranges
        val ratingSpinner = view.findViewById<Spinner>(R.id.ratingSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.rating_ranges, // The array of rating ranges defined in XML
            android.R.layout.simple_spinner_item // Use a default spinner layout
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ratingSpinner.adapter = adapter // Set the adapter to the spinner so it can display the options

        // Get the current search query passed from other fragments or activities (if available)
        currentQuery = arguments?.getString("currentQuery")

        // Initialize the sections for genres to include and exclude in the search
        includeGenresSection = view.findViewById(R.id.includeGenresSection)
        excludeGenresSection = view.findViewById(R.id.excludeGenresSection)

        // Initialize the toggle buttons for showing or hiding the include/exclude genre sections
        includeToggleButton = view.findViewById(R.id.includeGenresToggleButton)
        excludeToggleButton = view.findViewById(R.id.excludeGenresToggleButton)

        // Initialize the input field for specifying a language filter
        languageEditText = view.findViewById(R.id.languageEditText)

        // Set up the click listener for the "include genres" toggle button
        includeToggleButton.setOnClickListener {
            toggleVisibility(includeGenresSection, includeToggleButton) // Show or hide the section when clicked
        }

        // Set up the click listener for the "exclude genres" toggle button
        excludeToggleButton.setOnClickListener {
            toggleVisibility(excludeGenresSection, excludeToggleButton) // Show or hide the section when clicked
        }

        // Initialize the submit button and set its click listener
        submitButton = view.findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val genresPair = getSelectedGenres() // Get the selected include/exclude genres
            performSearchWithGenres(genresPair) // Perform the search with selected genres
        }

        // Initialize the progress bar, which shows while genres are being fetched
        genresProgressBar = view.findViewById(R.id.genresProgressBar)

        // Initially disable the buttons and show the progress bar while genres are loading
        submitButton.isEnabled = false
        includeToggleButton.isEnabled = false
        excludeToggleButton.isEnabled = false
        genresProgressBar.visibility = View.VISIBLE

        // Fetch the available genres based on the current query and filters
        fetchGenres()

        return view // Return the view (UI) for the fragment
    }

    // Function to fetch genres based on the current query, language filter, and rating range
    private fun fetchGenres() {
        val handler = Handler(Looper.getMainLooper()) // Used to handle operations on the UI thread
        val fetchGenresTimeout = 10000L // Set a 10-second timeout for fetching genres

        // Runnable to show a message if fetching genres takes too long (over 10 seconds)
        val timeoutRunnable = Runnable {
            if (genresProgressBar.visibility == View.VISIBLE) {
                Toast.makeText(activity, "Fetching genres is taking too long", Toast.LENGTH_SHORT).show()
                genresProgressBar.visibility = View.GONE // Hide the progress bar
                submitButton.isEnabled = true // Re-enable the buttons
                includeToggleButton.isEnabled = true
                excludeToggleButton.isEnabled = true
            }
        }
        handler.postDelayed(timeoutRunnable, fetchGenresTimeout) // Schedule the timeout check

        // Get the MainActivity (the parent activity) to access its methods for fetching genres
        val mainActivity = activity as? MainActivity
        if (mainActivity == null) {
            Toast.makeText(activity, "Failed to fetch genres", Toast.LENGTH_SHORT).show()
            genresProgressBar.visibility = View.GONE
            handler.removeCallbacks(timeoutRunnable)
            return
        }

        // Get the language input from the user and convert it to a language code if necessary
        val languageInput = languageEditText.text.toString().trim()
        val languageFilter = languageMap[languageInput] ?: languageInput // Use code if available, otherwise keep input

        // Get the selected rating range from the spinner
        val selectedRatingRange = view?.findViewById<Spinner>(R.id.ratingSpinner)?.selectedItem.toString()
        val ratingRange = parseRatingRange(selectedRatingRange)
        val minRating = ratingRange?.first ?: 0f // Minimum rating (default 0)
        val maxRating = ratingRange?.second ?: 5f // Maximum rating (default 5)

        // Call the fetchGenresForQuery method from MainActivity to get genres
        mainActivity.fetchGenresForQuery(currentQuery ?: "", languageFilter, minRating, maxRating) { genres: Set<String>? ->
            handler.removeCallbacks(timeoutRunnable) // Remove the timeout handler since genres were fetched
            activity?.runOnUiThread { // Run the UI update on the main thread
                genresProgressBar.visibility = View.GONE // Hide the progress bar
                if (genres != null) {
                    availableGenres = ArrayList<String>(genres) // Store the available genres
                    Log.d("SearchFiltersFragment", "Fetched Genres: $availableGenres")

                    // Fetch the actual genre result counts and update the UI
                    fetchGenreResultsCount { genreResults ->
                        availableGenres = availableGenres?.filter { genreResults.containsKey(it) } as ArrayList<String>
                        if (availableGenres != null && availableGenres!!.isNotEmpty()) {
                            // Populate the genre checkboxes for including and excluding genres
                            populateGenreCheckboxes(includeGenresSection, "Include")
                            populateGenreCheckboxes(excludeGenresSection, "Exclude")
                        }
                    }
                } else {
                    Toast.makeText(activity, "Failed to fetch genres", Toast.LENGTH_SHORT).show()
                }
                // Re-enable the UI elements (buttons) after fetching the genres
                submitButton.isEnabled = true
                includeToggleButton.isEnabled = true
                excludeToggleButton.isEnabled = true
            }
        }
    }

    // Function to fetch the count of books per genre based on the current query
    private fun fetchGenreResultsCount(onResultsFetched: (Map<String, Int>) -> Unit) {
        val mainActivity = activity as? MainActivity
        if (mainActivity == null || currentQuery.isNullOrBlank()) {
            onResultsFetched(emptyMap()) // Return an empty map if there's no query
            return
        }

        // Fetch books using the current search query and pass the results to a callback function
        mainActivity.searchBooks(currentQuery!!, 0, null) { books: List<BookItem>? ->
            if (books != null) {
                val results = mutableMapOf<String, Int>()
                availableGenres?.forEach { genre ->
                    val normalizedGenre = GenreUtils.normalizeGenre(genre) // Normalize the genre name
                    // Count the number of books in this genre
                    val count = books.count { book ->
                        book.volumeInfo.categories?.any { category ->
                            GenreUtils.normalizeGenre(category).contains(normalizedGenre)
                        } ?: false
                    }
                    if (count > 0) {
                        results[genre] = count // Add the genre and its count to the results map
                    }
                }
                genreResultsMap = results // Update the genreResultsMap with the fetched results
                onResultsFetched(results) // Pass the results back through the callback
            } else {
                onResultsFetched(emptyMap()) // Return an empty map if no books were found
            }
        }
    }

    // Function to show or hide a section when a toggle button is clicked
    private fun toggleVisibility(section: LinearLayout, toggleButton: Button) {
        // If the section is hidden, show it and update the toggle button to "▲"
        if (section.visibility == View.GONE) {
            section.visibility = View.VISIBLE
            toggleButton.text = toggleButton.text.toString().replace("▼", "▲")
        } else { // Otherwise, hide the section and update the toggle button to "▼"
            section.visibility = View.GONE
            toggleButton.text = toggleButton.text.toString().replace("▲", "▼")
        }
    }

    // Function to populate checkboxes for available genres in the UI
    private fun populateGenreCheckboxes(container: LinearLayout, type: String) {
        // Find the layout that will hold the checkboxes (include or exclude section)
        val genreLinearLayout = if (type == "Include") {
            container.findViewById<LinearLayout>(R.id.includeGenresLinearLayout)
        } else {
            container.findViewById<LinearLayout>(R.id.excludeGenresLinearLayout)
        }

        genreLinearLayout.removeAllViews() // Remove any existing checkboxes

        availableGenres?.let { genres ->
            if (genres.isNotEmpty()) {
                // For each available genre, create a checkbox
                genres.forEach { genre ->
                    if (genreHasResults(genre)) { // Only add checkboxes for genres with results
                        val checkBox = CheckBox(context)
                        checkBox.text = genre // Set the text of the checkbox to the genre name
                        checkBox.textSize = 18f // Set the font size
                        checkBox.tag = genre // Set the tag for identifying the checkbox later
                        checkBox.isChecked = false // By default, the checkbox is not checked

                        Log.d("SearchFiltersFragment", "Adding Checkbox for Genre: $genre with results: ${genreResultsMap[genre]}")

                        // Add the checkbox to the layout
                        genreLinearLayout.addView(checkBox)
                    }
                }
            }
        }
    }

    // Function to check if a genre has results (i.e., books)
    private fun genreHasResults(genre: String): Boolean {
        val resultsCount = genreResultsMap[genre] ?: 0
        return resultsCount > 0 // Only show genres with at least one result
    }

    // Function to get the genres selected by the user in both include and exclude sections
    private fun getSelectedGenres(): Pair<MutableList<String>, MutableList<String>> {
        val includeGenres = mutableListOf<String>() // List of genres to include
        val excludeGenres = mutableListOf<String>() // List of genres to exclude

        // Get the selected genres from the include section
        val includeLinearLayout = includeGenresSection.findViewById<LinearLayout>(R.id.includeGenresLinearLayout)
        for (i in 0 until includeLinearLayout.childCount) {
            val child = includeLinearLayout.getChildAt(i)
            if (child is CheckBox && child.isChecked) { // Check if the checkbox is checked
                includeGenres.add(child.tag as String) // Add the genre to the include list
            }
        }

        // Get the selected genres from the exclude section
        val excludeLinearLayout = excludeGenresSection.findViewById<LinearLayout>(R.id.excludeGenresLinearLayout)
        for (i in 0 until excludeLinearLayout.childCount) {
            val child = excludeLinearLayout.getChildAt(i)
            if (child is CheckBox && child.isChecked) { // Check if the checkbox is checked
                excludeGenres.add(child.tag as String) // Add the genre to the exclude list
            }
        }

        Log.d("SearchFiltersFragment", "User Selected Include Genres: $includeGenres")
        Log.d("SearchFiltersFragment", "User Selected Exclude Genres: $excludeGenres")

        return Pair(includeGenres, excludeGenres) // Return both the include and exclude genres
    }

    // Function to perform a search based on selected genres, rating, and language
    private fun performSearchWithGenres(genresPair: Pair<MutableList<String>, MutableList<String>>) {
        val (includeGenres, excludeGenres) = genresPair // Destructure the genres pair
        val searchFragment = SearchFragment() // Create a new SearchFragment to perform the search
        val bundle = Bundle() // Bundle to pass the search parameters to the fragment

        // Get the selected rating range from the spinner
        val selectedRatingRange = view?.findViewById<Spinner>(R.id.ratingSpinner)?.selectedItem.toString()
        val ratingRange = parseRatingRange(selectedRatingRange)

        // Normalize the include and exclude genres (convert them to a standard format)
        val normalizedIncludeGenres = includeGenres.map { GenreUtils.normalizeGenre(it) }.toMutableList()
        val normalizedExcludeGenres = excludeGenres.map { GenreUtils.normalizeGenre(it) }.toMutableList()

        Log.d("SearchFiltersFragment", "Performing Search with Include Genres: $normalizedIncludeGenres, Exclude Genres: $normalizedExcludeGenres")

        // Get the language input and convert it to a language code if necessary
        val languageInput = languageEditText.text.toString().trim()
        val languageFilter = languageMap[languageInput] ?: languageInput

        // Add the search parameters (genres, rating, language) to the bundle
        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList(normalizedIncludeGenres))
        bundle.putStringArrayList("excludeGenres", ArrayList(normalizedExcludeGenres))
        bundle.putString("languageFilter", languageFilter)
        if (ratingRange != null) {
            bundle.putFloat("minRating", ratingRange.first)
            bundle.putFloat("maxRating", ratingRange.second)
        } else {
            bundle.putFloat("minRating", 0f) // Default to 0 if no rating range is selected
            bundle.putFloat("maxRating", 5f) // Default to 5 if no rating range is selected
        }

        // Replace the current fragment with the SearchFragment, passing the search parameters
        searchFragment.arguments = bundle

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.menu_container, searchFragment, "SearchFragment")
            .addToBackStack("SearchFragment") // Add this transaction to the backstack so users can navigate back
            .commit() // Commit the transaction to display the new fragment
    }

    // Helper function to parse the rating range selected by the user
    private fun parseRatingRange(selectedRange: String): Pair<Float, Float>? {
        return when (selectedRange) {
            "0.0 to 0.9" -> Pair(0.0f, 0.9f) // Return a pair with the min and max values
            "1.0 to 1.9" -> Pair(1.0f, 1.9f)
            "2.0 to 2.9" -> Pair(2.0f, 2.9f)
            "3.0 to 3.9" -> Pair(3.0f, 3.9f)
            "4.0 to 4.9" -> Pair(4.0f, 4.9f)
            "5.0 only" -> Pair(5.0f, 5.0f)
            else -> null // Return null if no valid range is selected
        }
    }
}
