package com.example.booknook.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.booknook.R

// Constants representing the state of the checkboxes
private const val STATE_IGNORE = 0
private const val STATE_INCLUDE = 1
private const val STATE_EXCLUDE = 2

// Interface for filter application listener
interface FilterListener {
    fun onFiltersApplied(includeTags: List<String>, excludeTags: List<String>, privateGroupOnly: Boolean, publcGroupOnly: Boolean)
}

// Fragment class for filtering groups
class GroupsFilterFragment : DialogFragment() {

    // Map to hold checkbox states
    private val checkboxStates = mutableMapOf<CheckBox, Int>()

    // Button for applying filters
    private lateinit var applyFilters: Button

    // Lists to hold included and excluded tags
    lateinit var includeTags: ArrayList<String>
    lateinit var excludeTags: ArrayList<String>

    // Checkboxes for group type filtering
    lateinit var publicGroupsOnly: CheckBox
    lateinit var privateGroupsOnly: CheckBox

    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle any arguments passed to the fragment (if any)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the include and exclude tag lists
        includeTags = ArrayList()
        excludeTags = ArrayList()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the public and private groups checkboxes
        publicGroupsOnly = view.findViewById(R.id.publicGroups)
        privateGroupsOnly = view.findViewById(R.id.privateGroups)


        // Initialize each checkbox with a state listener
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_action), "Action")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_adult), "Adult")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_adventure), "Adventure")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_biography), "Biography")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_children), "Children")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_comedy), "Comedy")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_comicBook), "Comic Books") // double check
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_crime), "Crime")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_drama), "Drama")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_fantasy), "Fantasy")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_fiction), "Fiction")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_historical), "Historical")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_horror), "Horror")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_jFiction), "Juvenile Fiction")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_lgbt), "LGBTQ+") // double check
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_manga), "Manga")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_mature), "Mature")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_mystery), "Mystery")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_nonfiction), "Nonfiction")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_poetry), "Poetry")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_psychological), "Psychological")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_romance), "Romance")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_schoolLife), "School Life")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_science), "Science")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_scienceFiction), "Science Fiction")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_sports), "Sports")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_supernatural), "Supernatural")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_suspense), "Suspense")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_thriller), "Thriller")
        setupTriStateCheckbox(view.findViewById(R.id.checkbox_tragedy), "Tragedy")

        // Set up the button click listener for applying filters
        applyFilters = view.findViewById(R.id.applyFilters)
        applyFilters.setOnClickListener {
            applyFilters()
        }
    }

    // Method to set up a tri-state checkbox with a click listener
    private fun setupTriStateCheckbox(checkbox: CheckBox, tagName: String) {
        checkboxStates[checkbox] = STATE_IGNORE  // Initialize to "ignore"

        checkbox.setOnClickListener {
            when (checkboxStates[checkbox]) {
                STATE_IGNORE -> {
                    // If is in "ignore" state when clicked changes state to "include"
                    checkboxStates[checkbox] = STATE_INCLUDE
                    checkbox.isChecked = true
                    // Set tint for "include" state
                    checkbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.star_green)) // Tint for "include"
                }
                STATE_INCLUDE -> {
                    // If current state is "include" switches it to "exclude" when clicked
                    checkboxStates[checkbox] = STATE_EXCLUDE
                    checkbox.isChecked = true
                    // Set tint for "exclude" state
                    checkbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.home_pink)) // Tint for "exclude"
                }
                STATE_EXCLUDE -> {
                    // if current state is "exclude" changes state to "ignore" when clicked
                    checkboxStates[checkbox] = STATE_IGNORE
                    checkbox.isChecked = false
                    checkbox.buttonTintList = null // No tint for "ignore"
                }
            }

            // Update the include/exclude tag lists based on the checkbox state
            checkboxStates[checkbox]?.let { it1 -> updateFilterLists(tagName, it1) }
        }
    }

    // Method to update the include and exclude tag lists based on the checkbox state
    private fun updateFilterLists(tagName: String, state: Int) {
        when (state) {
            STATE_INCLUDE -> {
                // Adds tag to includeTag list and removes it from excluded list
                includeTags.add(tagName)
                excludeTags.remove(tagName)
            }
            STATE_EXCLUDE -> {
                // Adds tag to exclude list and removes from include
                excludeTags.add(tagName)
                includeTags.remove(tagName)
            }
            STATE_IGNORE -> {
                // removes it from both lists
                includeTags.remove(tagName)
                excludeTags.remove(tagName)
            }
        }
    }

    // Method to apply the filters and communicate with the listener
    private fun applyFilters() {
        val publicOnly = publicGroupsOnly.isChecked // Check if public groups only is selected
        val privateOnly = privateGroupsOnly.isChecked // Check if private groups only is selected

        // Show a toast if both options are selected
        if (publicOnly && privateOnly)
        {
            Toast.makeText(requireContext(), "Both public groups only and private groups only cannot both be checked", Toast.LENGTH_SHORT).show()
        }
        else {
            // Notify the listener with the selected filters
            (targetFragment as? FilterListener)?.onFiltersApplied(
                includeTags,
                excludeTags,
                privateOnly,
                publicOnly
            )
            dismiss()
        }
    }
}

