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

class SearchFiltersFragment : Fragment() {

    private lateinit var submitButton: Button
    private var currentQuery: String? = null
    private lateinit var includeGenresSection: LinearLayout
    private lateinit var excludeGenresSection: LinearLayout
    private lateinit var includeToggleButton: Button
    private lateinit var excludeToggleButton: Button
    private var availableGenres: ArrayList<String>? = null

    private lateinit var genresProgressBar: ProgressBar

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
                // Enable UI elements
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

        mainActivity.fetchGenresForQuery(currentQuery ?: "") { genres: Set<String>? ->
            handler.removeCallbacks(timeoutRunnable)
            activity?.runOnUiThread {
                genresProgressBar.visibility = View.GONE
                if (genres != null) {
                    availableGenres = ArrayList<String>(genres)
                    populateGenreCheckboxes(includeGenresSection, "Include")
                    populateGenreCheckboxes(excludeGenresSection, "Exclude")
                } else {
                    Toast.makeText(activity, "Failed to fetch genres", Toast.LENGTH_SHORT).show()
                }
                // Enable UI elements
                submitButton.isEnabled = true
                includeToggleButton.isEnabled = true
                excludeToggleButton.isEnabled = true
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
        val gridLayout = if (type == "Include") {
            container.findViewById<GridLayout>(R.id.includeGenresGridLayout)
        } else {
            container.findViewById<GridLayout>(R.id.excludeGenresGridLayout)
        }

        gridLayout.removeAllViews()

        availableGenres?.let { genres ->
            if (genres.isNotEmpty()) {
                genres.forEach { genre ->
                    val checkBox = CheckBox(context)
                    checkBox.text = genre
                    checkBox.textSize = 18f
                    checkBox.tag = genre

                    gridLayout.addView(checkBox)
                }
            }
        }
    }

    private fun getSelectedGenres(): Pair<MutableList<String>, MutableList<String>> {
        val includeGenres = mutableListOf<String>()
        val excludeGenres = mutableListOf<String>()

        val includeGrid = includeGenresSection.findViewById<GridLayout>(R.id.includeGenresGridLayout)
        for (i in 0 until includeGrid.childCount) {
            val child = includeGrid.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                includeGenres.add(child.tag as String)
            }
        }

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

        val userInputLanguage =
            view?.findViewById<EditText>(R.id.languageEditText)?.text.toString().trim()
        val languageFilter = languageMap[userInputLanguage.lowercase()] ?: userInputLanguage

        val selectedRatingRange =
            view?.findViewById<Spinner>(R.id.ratingSpinner)?.selectedItem.toString()
        val ratingRange = parseRatingRange(selectedRatingRange)

        bundle.putString("currentQuery", currentQuery)
        bundle.putStringArrayList("includeGenres", ArrayList<String>(includeGenres))
        bundle.putStringArrayList("excludeGenres", ArrayList<String>(excludeGenres))
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
