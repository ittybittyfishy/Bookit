package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.booknook.MainActivity
import com.example.booknook.R

class SearchFiltersFragment : Fragment() {

    // Declare variables for UI elements and data
    private lateinit var submitButton: Button // Button to submit the search filters
    private var currentQuery: String? = null // Store the current search query
    private lateinit var includeGenresSection: LinearLayout // Section for genres to include
    private lateinit var excludeGenresSection: LinearLayout // Section for genres to exclude
    private lateinit var includeToggleButton: Button // Toggle button to show/hide include genres section
    private lateinit var excludeToggleButton: Button // Toggle button to show/hide exclude genres section
    private var availableGenres: ArrayList<String>? = null // List of available genres

    val languageMap = mapOf(
        "english" to "en",
        "japanese" to "ja",
        "french" to "fr",
        "german" to "de",
        "spanish" to "es",
        "chinese" to "zh",
        "korean" to "ko",
        // Add more languages as needed
    )


    // This function is called to create the view for the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_search_filters, container, false)

        // Find and set up the language filter input (EditText)
        val languageEditText: EditText = view.findViewById(R.id.languageEditText)

        // Set up the rating spinner (drop-down menu) with rating ranges
        val ratingSpinner = view.findViewById<Spinner>(R.id.ratingSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.rating_ranges, // Array of rating options
            android.R.layout.simple_spinner_item // Layout for spinner items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ratingSpinner.adapter = adapter

        // Get passed arguments for current query and available genres
        currentQuery = arguments?.getString("currentQuery")
        availableGenres = arguments?.getStringArrayList("availableGenres")

        // Find the genre sections for including and excluding genres
        includeGenresSection = view.findViewById(R.id.includeGenresSection)
        excludeGenresSection = view.findViewById(R.id.excludeGenresSection)

        // Find the toggle buttons to show/hide genre sections
        includeToggleButton = view.findViewById(R.id.includeGenresToggleButton)
        excludeToggleButton = view.findViewById(R.id.excludeGenresToggleButton)

        // Set up click listeners for toggle buttons to show/hide genre sections
        includeToggleButton.setOnClickListener {
            toggleVisibility(includeGenresSection, includeToggleButton)
        }

        excludeToggleButton.setOnClickListener {
            toggleVisibility(excludeGenresSection, excludeToggleButton)
        }

        // Dynamically add genre checkboxes to include and exclude sections
        populateGenreCheckboxes(includeGenresSection, "Include")
        populateGenreCheckboxes(excludeGenresSection, "Exclude")

        // Set up the submit button to trigger the search with selected genres
        submitButton = view.findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val genresPair = getSelectedGenres() // Get selected genres from checkboxes
            performSearchWithGenres(genresPair) // Perform the search with those genres
        }

        return view
    }

    // Function to toggle the visibility of genre sections (show/hide)
    private fun toggleVisibility(section: LinearLayout, toggleButton: Button) {
        if (section.visibility == View.GONE) {
            section.visibility = View.VISIBLE // Show section if it's hidden
            toggleButton.text = toggleButton.text.toString().replace("▼", "▲") // Change arrow direction
        } else {
            section.visibility = View.GONE // Hide section if it's visible
            toggleButton.text = toggleButton.text.toString().replace("▲", "▼") // Change arrow direction
        }
    }

    // Function to dynamically create checkboxes for each genre
    private fun populateGenreCheckboxes(container: LinearLayout, type: String) {
        availableGenres?.forEach { genre ->
            val checkBox = CheckBox(context) // Create a new checkbox
            checkBox.text = genre // Set checkbox label with "Include/Exclude" and genre name
            checkBox.textSize = 18f
            checkBox.tag = genre // Tag it with the genre name for later use
            container.addView(checkBox) // Add the checkbox to the section
        }
    }

    // Function to retrieve the selected genres from checkboxes
    private fun getSelectedGenres(): Pair<MutableList<String>, MutableList<String>> {
        val includeGenres = mutableListOf<String>() // List to store included genres
        val excludeGenres = mutableListOf<String>() // List to store excluded genres

        // Loop through all the children of the include genres section
        for (i in 0 until includeGenresSection.childCount) {
            val child = includeGenresSection.getChildAt(i)
            if (child is GridLayout) { // Check if the child is a GridLayout (could be different container)
                for (j in 0 until child.childCount) {
                    val checkBox = child.getChildAt(j)
                    if (checkBox is CheckBox && checkBox.isChecked) { // If it's a checked checkbox
                        includeGenres.add(checkBox.tag as String) // Add genre to include list
                    }
                }
            }
        }

        // Loop through all the children of the exclude genres section
        for (i in 0 until excludeGenresSection.childCount) {
            val child = excludeGenresSection.getChildAt(i)
            if (child is GridLayout) { // Check if the child is a GridLayout
                for (j in 0 until child.childCount) {
                    val checkBox = child.getChildAt(j)
                    if (checkBox is CheckBox && checkBox.isChecked) { // If it's a checked checkbox
                        excludeGenres.add(checkBox.tag as String) // Add genre to exclude list
                    }
                }
            }
        }

        // Return a pair of lists: one for included genres, one for excluded genres
        return Pair(includeGenres, excludeGenres)
    }

    private fun performSearchWithGenres(genresPair: Pair<MutableList<String>, MutableList<String>>) {
        val (includeGenres, excludeGenres) = genresPair // structure the pair into two lists
        val searchFragment = SearchFragment() // Create a new SearchFragment instance
        val bundle = Bundle() // Create a bundle to pass data to the next fragment

        // Get the language filter input from the EditText
        val userInputLanguage = view?.findViewById<EditText>(R.id.languageEditText)?.text.toString().trim()

        // Convert user input to lowercase and look it up in the languageMap
        val languageFilter = languageMap[userInputLanguage.toLowerCase()] ?: userInputLanguage // If not found, use raw input

        // Get the rating range from the spinner
        val selectedRatingRange = view?.findViewById<Spinner>(R.id.ratingSpinner)?.selectedItem.toString()
        val ratingRange = parseRatingRange(selectedRatingRange)

        // Put all search filters into the bundle
        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList(includeGenres)) // Include genres list
        bundle.putStringArrayList("excludeGenres", ArrayList(excludeGenres)) // Exclude genres list
        bundle.putString("languageFilter", languageFilter) // Add the language code (or raw input)

        // If a rating range was selected, add it to the bundle
        if (ratingRange != null) {
            bundle.putFloat("minRating", ratingRange.first)
            bundle.putFloat("maxRating", ratingRange.second)
        } else {
            // Default rating range if none was selected
            bundle.putFloat("minRating", 0f)
            bundle.putFloat("maxRating", 5f)
        }

        // Pass the bundle of filters to the SearchFragment
        searchFragment.arguments = bundle
        (activity as MainActivity).replaceFragment(searchFragment, "Search Results") // Replace the fragment with search results
    }


    // Function to parse the rating range selected from the spinner
    private fun parseRatingRange(selectedRange: String): Pair<Float, Float>? {
        return when (selectedRange) {
            "0.0 to 0.9" -> Pair(0.0f, 0.9f) //Range for ratings between 0.0 and 0.9
            "1.0 to 1.9" -> Pair(1.0f, 1.9f) // Range for ratings between 1.0 and 1.9
            "2.0 to 2.9" -> Pair(2.0f, 2.9f) // Range for ratings between 2.0 and 2.9
            "3.0 to 3.9" -> Pair(3.0f, 3.9f) // Range for ratings between 3.0 and 3.9
            "4.0 to 4.9" -> Pair(4.0f, 4.9f) // Range for ratings between 4.0 and 4.9
            "5.0 only" -> Pair(5.0f, 5.0f) // Only ratings of 5.0
            else -> null // No rating range selected
        }
    }
}