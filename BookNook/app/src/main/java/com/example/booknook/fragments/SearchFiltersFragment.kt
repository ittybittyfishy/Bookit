package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.booknook.R
import android.widget.GridLayout
import com.example.booknook.MainActivity

class SearchFiltersFragment : Fragment() {

    private lateinit var submitButton: Button
    private var currentQuery: String? = null
    private lateinit var includeGenresSection: LinearLayout
    private lateinit var excludeGenresSection: LinearLayout
    private lateinit var includeToggleButton: Button
    private lateinit var excludeToggleButton: Button
    private var availableGenres: ArrayList<String>? = null // Available genres from the search

    val languageMap = mapOf(
        "english" to "en",
        "japanese" to "ja",
        "french" to "fr",
        "german" to "de",
        "spanish" to "es",
        "chinese" to "zh",
        "korean" to "ko"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_search_filters, container, false)

        // Initialize UI elements
        val languageEditText: EditText = view.findViewById(R.id.languageEditText)
        val ratingSpinner = view.findViewById<Spinner>(R.id.ratingSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.rating_ranges,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ratingSpinner.adapter = adapter

        currentQuery = arguments?.getString("currentQuery")
        availableGenres = arguments?.getStringArrayList("availableGenres") // Retrieve passed genres

        includeGenresSection = view.findViewById(R.id.includeGenresSection)
        excludeGenresSection = view.findViewById(R.id.excludeGenresSection)
        includeToggleButton = view.findViewById(R.id.includeGenresToggleButton)
        excludeToggleButton = view.findViewById(R.id.excludeGenresToggleButton)

        includeToggleButton.setOnClickListener {
            toggleVisibility(includeGenresSection, includeToggleButton)
        }

        excludeToggleButton.setOnClickListener {
            toggleVisibility(excludeGenresSection, excludeToggleButton)
        }

        // Populate the genres in both include and exclude sections
        if (!availableGenres.isNullOrEmpty()) {
            populateGenreCheckboxes(includeGenresSection, "Include")
            populateGenreCheckboxes(excludeGenresSection, "Exclude")
        } else {
            Log.e("SearchFiltersFragment", "No genres available for filtering")
        }

        submitButton = view.findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val genresPair = getSelectedGenres()
            performSearchWithGenres(genresPair)
        }

        return view
    }

    // Function to toggle the visibility of genre sections (show/hide)
    private fun toggleVisibility(section: LinearLayout, toggleButton: Button) {
        if (section.visibility == View.GONE) {
            section.visibility = View.VISIBLE
            toggleButton.text = toggleButton.text.toString().replace("▼", "▲")
        } else {
            section.visibility = View.GONE
            toggleButton.text = toggleButton.text.toString().replace("▲", "▼")
        }
    }

    // Function to dynamically create checkboxes for each genre
    private fun populateGenreCheckboxes(container: LinearLayout, type: String) {
        val gridLayout = if (type == "Include") {
            container.findViewById<GridLayout>(R.id.includeGenresGridLayout)
        } else {
            container.findViewById<GridLayout>(R.id.excludeGenresGridLayout)
        }

        // Check if available genres are not null and not empty
        availableGenres?.let {
            if (it.isNotEmpty()) {
                gridLayout.removeAllViews() // Clear any previous checkboxes
                it.forEach { genre ->
                    val checkBox = CheckBox(context) // Create a new checkbox
                    checkBox.text = genre
                    checkBox.textSize = 18f
                    checkBox.tag = genre
                    gridLayout.addView(checkBox) // Add checkbox to the GridLayout
                }
            } else {
                Log.d("SearchFiltersFragment", "No genres available to populate checkboxes")
            }
        }
    }

    // Function to retrieve the selected genres from checkboxes
    private fun getSelectedGenres(): Pair<MutableList<String>, MutableList<String>> {
        val includeGenres = mutableListOf<String>()
        val excludeGenres = mutableListOf<String>()

        // Retrieve checkboxes from the include grid
        val includeGrid = includeGenresSection.findViewById<GridLayout>(R.id.includeGenresGridLayout)
        for (i in 0 until includeGrid.childCount) {
            val child = includeGrid.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                includeGenres.add(child.tag as String)
            }
        }

        // Retrieve checkboxes from the exclude grid
        val excludeGrid = excludeGenresSection.findViewById<GridLayout>(R.id.excludeGenresGridLayout)
        for (i in 0 until excludeGrid.childCount) {
            val child = excludeGrid.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                excludeGenres.add(child.tag as String)
            }
        }

        return Pair(includeGenres, excludeGenres)
    }

    private fun performSearchWithGenres(genresPair: Pair<MutableList<String>, MutableList<String>>) {
        val (includeGenres, excludeGenres) = genresPair
        val searchFragment = SearchFragment()
        val bundle = Bundle()

        val userInputLanguage = view?.findViewById<EditText>(R.id.languageEditText)?.text.toString().trim()
        val languageFilter = languageMap[userInputLanguage.lowercase()] ?: userInputLanguage

        val selectedRatingRange = view?.findViewById<Spinner>(R.id.ratingSpinner)?.selectedItem.toString()
        val ratingRange = parseRatingRange(selectedRatingRange)

        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList(includeGenres))
        bundle.putStringArrayList("excludeGenres", ArrayList(excludeGenres))
        bundle.putString("languageFilter", languageFilter)

        if (ratingRange != null) {
            bundle.putFloat("minRating", ratingRange.first)
            bundle.putFloat("maxRating", ratingRange.second)
        } else {
            bundle.putFloat("minRating", 0f)
            bundle.putFloat("maxRating", 5f)
        }

        searchFragment.arguments = bundle
        (activity as MainActivity).replaceFragment(searchFragment, "Search Results")
    }

    // Function to parse the rating range selected from the spinner
    private fun parseRatingRange(selectedRange: String): Pair<Float, Float>? {
        return when (selectedRange) {
            "All Ratings" -> Pair(0.0f, 5.0f)
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
