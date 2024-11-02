// AchievementsFragment.kt
package com.example.booknook.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

class AchievementsFragment : Fragment() {

    // Firestore and FirebaseAuth instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // UI elements for XP and Level
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var currentXpTextView: TextView
    private lateinit var xpNeededTextView: TextView
    private lateinit var levelNumberTextView: TextView
    private lateinit var levelProgressText: TextView
    private lateinit var xpRangeTextView: TextView // New TextView for XP Range

    // Achievement UI elements (CardViews)
    private lateinit var firstChapterCard: CardView
    private lateinit var readingRookieCard: CardView
    private lateinit var storySeekerCard: CardView
    private lateinit var novelNavigatorCard: CardView
    private lateinit var bookEnthusiastCard: CardView
    private lateinit var legendaryLibrarianCard: CardView
    private lateinit var bookGodCard: CardView // New CardView for Book God
    // Add other CardViews as needed...

    // Progress bars for achievements
    private lateinit var firstChapterProgressBar: ProgressBar
    private lateinit var readingRookieProgressBar: ProgressBar
    private lateinit var storySeekerProgressBar: ProgressBar
    private lateinit var novelNavigatorProgressBar: ProgressBar
    private lateinit var bookEnthusiastProgressBar: ProgressBar
    private lateinit var legendaryLibrarianProgressBar: ProgressBar
    private lateinit var bookGodProgressBar: ProgressBar // New ProgressBar for Book God
    // Add other ProgressBars as needed...

    // Progress TextViews for achievements
    private lateinit var firstChapterProgressText: TextView
    private lateinit var firstChapterCurrentProgressText: TextView
    private lateinit var firstChapterMaxProgressText: TextView

    private lateinit var readingRookieProgressText: TextView
    private lateinit var readingRookieCurrentProgressText: TextView
    private lateinit var readingRookieMaxProgressText: TextView

    private lateinit var storySeekerProgressText: TextView
    private lateinit var storySeekerCurrentProgressText: TextView
    private lateinit var storySeekerMaxProgressText: TextView

    private lateinit var novelNavigatorProgressText: TextView
    private lateinit var novelNavigatorCurrentProgressText: TextView
    private lateinit var novelNavigatorMaxProgressText: TextView

    private lateinit var bookEnthusiastProgressText: TextView
    private lateinit var bookEnthusiastCurrentProgressText: TextView
    private lateinit var bookEnthusiastMaxProgressText: TextView

    private lateinit var legendaryLibrarianProgressText: TextView
    private lateinit var legendaryLibrarianCurrentProgressText: TextView
    private lateinit var legendaryLibrarianMaxProgressText: TextView

    private lateinit var bookGodProgressText: TextView // New ProgressTextView for Book God
    private lateinit var bookGodCurrentProgressText: TextView // New Current Progress TextView
    private lateinit var bookGodMaxProgressText: TextView // New Max Progress TextView
    // Add other Progress TextViews as needed...

    // Achievement Icons, Titles, and Requirements (ImageView and TextView)
    private lateinit var firstChapterIcon: ImageView
    private lateinit var firstChapterTitle: TextView
    private lateinit var firstChapterRequirement: TextView

    private lateinit var readingRookieIcon: ImageView
    private lateinit var readingRookieTitle: TextView
    private lateinit var readingRookieRequirement: TextView

    private lateinit var storySeekerIcon: ImageView
    private lateinit var storySeekerTitle: TextView
    private lateinit var storySeekerRequirement: TextView

    private lateinit var novelNavigatorIcon: ImageView
    private lateinit var novelNavigatorTitle: TextView
    private lateinit var novelNavigatorRequirement: TextView

    private lateinit var bookEnthusiastIcon: ImageView
    private lateinit var bookEnthusiastTitle: TextView
    private lateinit var bookEnthusiastRequirement: TextView

    private lateinit var legendaryLibrarianIcon: ImageView
    private lateinit var legendaryLibrarianTitle: TextView
    private lateinit var legendaryLibrarianRequirement: TextView

    private lateinit var bookGodIcon: ImageView // New Icon for Book God
    private lateinit var bookGodTitle: TextView // New Title for Book God
    private lateinit var bookGodRequirement: TextView // New Requirement for Book God

    // UI elements for Fantasy Explorer
    private lateinit var fantasyExplorerCard: CardView
    private lateinit var fantasyExplorerProgressBar: ProgressBar
    private lateinit var fantasyExplorerProgressText: TextView
    private lateinit var fantasyExplorerCurrentProgressText: TextView
    private lateinit var fantasyExplorerMaxProgressText: TextView
    private lateinit var fantasyExplorerIcon: ImageView
    private lateinit var fantasyExplorerTitle: TextView
    private lateinit var fantasyExplorerRequirement: TextView

    // UI elements for History Achievement
    private lateinit var historyCard: CardView
    private lateinit var historyProgressBar: ProgressBar
    private lateinit var historyProgressText: TextView
    private lateinit var historyCurrentProgressText: TextView
    private lateinit var historyMaxProgressText: TextView
    private lateinit var historyIcon: ImageView
    private lateinit var historyTitle: TextView
    private lateinit var historyRequirement: TextView

    // UI elements for Mystery Solver Achievement
    private lateinit var mysteryCard: CardView
    private lateinit var mysteryProgressBar: ProgressBar
    private lateinit var mysteryProgressText: TextView
    private lateinit var mysteryCurrentProgressText: TextView
    private lateinit var mysteryMaxProgressText: TextView
    private lateinit var mysteryIcon: ImageView
    private lateinit var mysteryTitle: TextView
    private lateinit var mysteryRequirement: TextView


    // UI elements for Psych Achievement
    private lateinit var psychCard: CardView
    private lateinit var psychProgressBar: ProgressBar
    private lateinit var psychProgressText: TextView
    private lateinit var psychCurrentProgressText: TextView
    private lateinit var psychMaxProgressText: TextView
    private lateinit var psychIcon: ImageView
    private lateinit var psychTitle: TextView
    private lateinit var psychRequirement: TextView


    // Add other Icons, Titles, and Requirements as needed...

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        // Initialize UI components for XP and Level
        xpProgressBar = view.findViewById(R.id.xpProgressBar)
        levelNumberTextView = view.findViewById(R.id.level_number)
        levelProgressText = view.findViewById(R.id.level_progress_text)
        currentXpTextView = view.findViewById(R.id.currentXpTextView)
        xpNeededTextView = view.findViewById(R.id.xpNeededTextView)
        xpRangeTextView = view.findViewById(R.id.xp_range_text_view)

        // Initialize CardViews for achievements
        firstChapterCard = view.findViewById(R.id.first_chapter_card)
        readingRookieCard = view.findViewById(R.id.reading_rookie_card)
        storySeekerCard = view.findViewById(R.id.story_seeker_card)
        novelNavigatorCard = view.findViewById(R.id.novel_navigator_card)
        bookEnthusiastCard = view.findViewById(R.id.book_enthusiast_card)
        legendaryLibrarianCard = view.findViewById(R.id.legendary_librarian_card)
        bookGodCard = view.findViewById(R.id.book_god_card) // Initialize Book God CardView
        // Initialize other CardViews as needed...

        // Initialize ProgressBars for achievements
        firstChapterProgressBar = view.findViewById(R.id.first_chapter_progress_bar)
        readingRookieProgressBar = view.findViewById(R.id.reading_rookie_progress_bar)
        storySeekerProgressBar = view.findViewById(R.id.story_seeker_progress_bar)
        novelNavigatorProgressBar = view.findViewById(R.id.novel_navigator_progress_bar)
        bookEnthusiastProgressBar = view.findViewById(R.id.book_enthusiast_progress_bar)
        legendaryLibrarianProgressBar = view.findViewById(R.id.legendary_librarian_progress_bar)
        bookGodProgressBar = view.findViewById(R.id.book_god_progress_bar) // Initialize Book God ProgressBar
        // Initialize other ProgressBars as needed...

        // Initialize Progress TextViews for achievements
        firstChapterProgressText = view.findViewById(R.id.first_chapter_progress_text)
        firstChapterCurrentProgressText = view.findViewById(R.id.first_chapter_current_progress_text)
        firstChapterMaxProgressText = view.findViewById(R.id.first_chapter_max_progress_text)

        readingRookieProgressText = view.findViewById(R.id.reading_rookie_progress_text)
        readingRookieCurrentProgressText = view.findViewById(R.id.readingRookieCurrentProgressText)
        readingRookieMaxProgressText = view.findViewById(R.id.readingRookieMaxProgressText)

        storySeekerProgressText = view.findViewById(R.id.story_seeker_progress_text)
        storySeekerCurrentProgressText = view.findViewById(R.id.story_seeker_current_progress_text)
        storySeekerMaxProgressText = view.findViewById(R.id.story_seeker_max_progress_text)

        novelNavigatorProgressText = view.findViewById(R.id.novel_navigator_progress_text)
        novelNavigatorCurrentProgressText = view.findViewById(R.id.novel_navigator_current_progress_text)
        novelNavigatorMaxProgressText = view.findViewById(R.id.novel_navigator_max_progress_text)

        bookEnthusiastProgressText = view.findViewById(R.id.book_enthusiast_progress_text)
        bookEnthusiastCurrentProgressText = view.findViewById(R.id.book_enthusiast_current_progress_text)
        bookEnthusiastMaxProgressText = view.findViewById(R.id.book_enthusiast_max_progress_text)

        legendaryLibrarianProgressText = view.findViewById(R.id.legendary_librarian_progress_text)
        legendaryLibrarianCurrentProgressText = view.findViewById(R.id.legendary_librarian_current_progress_text)
        legendaryLibrarianMaxProgressText = view.findViewById(R.id.legendary_librarian_max_progress_text)

        bookGodProgressText = view.findViewById(R.id.book_god_progress_text) // Initialize Book God ProgressTextView
        bookGodCurrentProgressText = view.findViewById(R.id.book_god_current_progress_text) // Initialize Book God Current Progress TextView
        bookGodMaxProgressText = view.findViewById(R.id.book_god_max_progress_text) // Initialize Book God Max Progress TextView
        // Initialize other Progress TextViews as needed...

        // Initialize Achievement Icons, Titles, and Requirements
        firstChapterIcon = view.findViewById(R.id.first_chapter_icon)
        firstChapterTitle = view.findViewById(R.id.first_chapter_title)
        firstChapterRequirement = view.findViewById(R.id.first_chapter_requirement)

        readingRookieIcon = view.findViewById(R.id.reading_rookie_icon)
        readingRookieTitle = view.findViewById(R.id.reading_rookie_title)
        readingRookieRequirement = view.findViewById(R.id.reading_rookie_requirement)

        storySeekerIcon = view.findViewById(R.id.story_seeker_icon)
        storySeekerTitle = view.findViewById(R.id.story_seeker_title)
        storySeekerRequirement = view.findViewById(R.id.story_seeker_requirement)

        novelNavigatorIcon = view.findViewById(R.id.novel_navigator_icon)
        novelNavigatorTitle = view.findViewById(R.id.novel_navigator_title)
        novelNavigatorRequirement = view.findViewById(R.id.novel_navigator_requirement)

        bookEnthusiastIcon = view.findViewById(R.id.book_enthusiast_icon)
        bookEnthusiastTitle = view.findViewById(R.id.book_enthusiast_title)
        bookEnthusiastRequirement = view.findViewById(R.id.book_enthusiast_requirement)

        legendaryLibrarianIcon = view.findViewById(R.id.legendary_librarian_icon)
        legendaryLibrarianTitle = view.findViewById(R.id.legendary_librarian_title)
        legendaryLibrarianRequirement = view.findViewById(R.id.legendary_librarian_requirement)

        bookGodIcon = view.findViewById(R.id.book_god_icon) // Initialize Book God Icon
        bookGodTitle = view.findViewById(R.id.book_god_title) // Initialize Book God Title
        bookGodRequirement = view.findViewById(R.id.book_god_requirement) // Initialize Book God Requirement

        // Initialize Fantasy Explorer UI components
        fantasyExplorerCard = view.findViewById(R.id.fantasy_explorer_card)
        fantasyExplorerProgressBar = view.findViewById(R.id.fantasy_explorer_progress_bar)
        fantasyExplorerProgressText = view.findViewById(R.id.fantasy_explorer_progress_text)
        fantasyExplorerCurrentProgressText = view.findViewById(R.id.fantasy_explorer_current_progress_text)
        fantasyExplorerMaxProgressText = view.findViewById(R.id.fantasy_explorer_max_progress_text)
        fantasyExplorerIcon = view.findViewById(R.id.fantasy_explorer_icon)
        fantasyExplorerTitle = view.findViewById(R.id.fantasy_explorer_title)
        fantasyExplorerRequirement = view.findViewById(R.id.fantasy_explorer_requirement)

        // Initialize History Achievement UI components
        historyCard = view.findViewById(R.id.history_card)
        historyProgressBar = view.findViewById(R.id.history_progress_bar)
        historyProgressText = view.findViewById(R.id.history_progress_text)
        historyCurrentProgressText = view.findViewById(R.id.history_current_progress_text)
        historyMaxProgressText = view.findViewById(R.id.history_max_progress_text)
        historyIcon = view.findViewById(R.id.history_icon)
        historyTitle = view.findViewById(R.id.history_title)
        historyRequirement = view.findViewById(R.id.history_requirement)

        // Initialize Mystery Solver UI components
        mysteryCard = view.findViewById(R.id.mystery_solver_card)
        mysteryProgressBar = view.findViewById(R.id.mystery_solver_progress_bar)
        mysteryProgressText = view.findViewById(R.id.mystery_solver_progress_text)
        mysteryCurrentProgressText = view.findViewById(R.id.mystery_solver_current_progress_text)
        mysteryMaxProgressText = view.findViewById(R.id.mystery_solver_max_progress_text)
        mysteryIcon = view.findViewById(R.id.mystery_solver_icon)
        mysteryTitle = view.findViewById(R.id.mystery_solver_title)
        mysteryRequirement = view.findViewById(R.id.mystery_solver_requirement)

        // Initialize Psych Achievement UI components
        psychCard = view.findViewById(R.id.psych_card)
        psychProgressBar = view.findViewById(R.id.psych_progress_bar)
        psychProgressText = view.findViewById(R.id.psych_progress_text)
        psychCurrentProgressText = view.findViewById(R.id.psych_current_progress_text)
        psychMaxProgressText = view.findViewById(R.id.psych_max_progress_text)
        psychIcon = view.findViewById(R.id.psych_icon)
        psychTitle = view.findViewById(R.id.psych_title)
        psychRequirement = view.findViewById(R.id.psych_requirement)



        // Initialize other Icons, Titles, and Requirements as needed...

        // Load achievements and XP data from Firestore
        userId?.let {
            loadFantasyExplorerAchievement(it)
            loadHistoryAchievement(it)
            loadMysterySolverAchievement(it)
            loadAchievementsData(it)
            loadPsychAchievement(it)
            setupRealtimeUpdates(it) // Optional: For real-time updates
        }

        return view
    }

    private fun loadFantasyExplorerAchievement(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Retrieve the list of finished books
                val finishedBooks = (document.get("standardCollections.Finished") as? List<*>)?.mapNotNull { book ->
                    when (book) {
                        is Map<*, *> -> {
                            val genres = book["genres"] as? List<*>
                            genres?.filterIsInstance<String>() // Cast genres to a list of strings
                        }
                        else -> null
                    }
                } ?: emptyList()

                // Count the number of books with the genre "fiction" (case-insensitive)
                val fictionBooksCount = finishedBooks.flatten().count { genre ->
                    genre.equals("Fiction", ignoreCase = true) || genre.equals("Juvenile Fiction", ignoreCase = true)

                }

                // Check if Fantasy Explorer is already achieved
                val isFantasyExplorerAchieved = document.getBoolean("fantasyExplorerAchieved") ?: false

                if (!isFantasyExplorerAchieved && fictionBooksCount >= 10) {
                    // Unlock the achievement
                    unlockFantasyExplorerAchievement(userDocRef)
                } else {
                    // Update the achievement progress UI
                    updateAchievementProgress(
                        progressBar = fantasyExplorerProgressBar,
                        progressTextView = fantasyExplorerProgressText,
                        currentProgressTextView = fantasyExplorerCurrentProgressText,
                        maxProgressTextView = fantasyExplorerMaxProgressText,
                        cardView = fantasyExplorerCard,
                        iconView = fantasyExplorerIcon,
                        titleTextView = fantasyExplorerTitle,
                        requirementTextView = fantasyExplorerRequirement,
                        maxValue = 10,
                        currentValue = fictionBooksCount.coerceAtMost(10),
                        description = "Log 10 fiction books as finished",
                        isAchieved = isFantasyExplorerAchieved || fictionBooksCount >= 10
                    )

                    // Update the current and max progress TextViews
                    fantasyExplorerCurrentProgressText.text = fictionBooksCount.coerceAtMost(10).toString()
                    fantasyExplorerMaxProgressText.text = "10"
                }
            } else {
                Log.e("AchievementsFragment", "No such document for user: $userId")
            }
        }.addOnFailureListener { exception ->
            Log.e("AchievementsFragment", "Error loading Fantasy Explorer achievement: ", exception)
        }
    }


    // Function to unlock Fantasy Explorer Achievement and update UI
    private fun unlockFantasyExplorerAchievement(userDocRef: DocumentReference) {
        userDocRef.update("fantasyExplorerAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Fantasy Explorer Achievement unlocked successfully.")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Fantasy Explorer!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = fantasyExplorerCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = fantasyExplorerProgressBar,
                            progressTextView = fantasyExplorerProgressText,
                            currentProgressTextView = fantasyExplorerCurrentProgressText,
                            maxProgressTextView = fantasyExplorerMaxProgressText,
                            cardView = fantasyExplorerCard,
                            iconView = fantasyExplorerIcon,
                            titleTextView = fantasyExplorerTitle,
                            requirementTextView = fantasyExplorerRequirement,
                            maxValue = 10,
                            currentValue = 10,
                            description = "Log 10 fiction books as finished",
                            isAchieved = true
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Fantasy Explorer achievement: ", exception)
                // Optional: Inform the user about the failure
                if (isAdded && context != null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to unlock Fantasy Explorer. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun loadHistoryAchievement(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Log to check the document content
                Log.d("AchievementsFragment", "Document data: ${document.data}")

                // Retrieve the list of finished books
                val finishedBooks = (document.get("standardCollections.Finished") as? List<*>)?.mapNotNull { book ->
                    when (book) {
                        is Map<*, *> -> {
                            val genres = book["genres"] as? List<*>
                            // Log each book's genres
                            Log.d("AchievementsFragment", "Book genres: $genres")
                            genres?.filterIsInstance<String>() // Cast genres to a list of strings
                        }
                        else -> null
                    }
                } ?: emptyList()

                // Log the flattened list of genres to verify the data
                Log.d("AchievementsFragment", "Flattened genres list: $finishedBooks")

                // Count the number of books with the genre "History" (case-insensitive)
                val historyBooksCount = finishedBooks.flatten().count { genre ->
                    genre.equals("history", ignoreCase = true)
                }

                // Log the count of history books
                Log.d("AchievementsFragment", "History Books Count: $historyBooksCount")

                // Check if History achievement is already achieved
                val isHistoryAchieved = document.getBoolean("historyAchieved") ?: false

                if (!isHistoryAchieved && historyBooksCount >= 10) {
                    // Unlock the achievement
                    unlockHistoryAchievement(userDocRef)
                } else {
                    // Update the achievement progress UI
                    updateAchievementProgress(
                        progressBar = historyProgressBar,
                        progressTextView = historyProgressText,
                        currentProgressTextView = historyCurrentProgressText,
                        maxProgressTextView = historyMaxProgressText,
                        cardView = historyCard,
                        iconView = historyIcon,
                        titleTextView = historyTitle,
                        requirementTextView = historyRequirement,
                        maxValue = 10,
                        currentValue = historyBooksCount.coerceAtMost(10),
                        description = "Read 10 history books",
                        isAchieved = isHistoryAchieved || historyBooksCount >= 10
                    )

                    // Update the current and max progress TextViews
                    historyCurrentProgressText.text = historyBooksCount.coerceAtMost(10).toString()
                    historyMaxProgressText.text = "10"
                }
            } else {
                Log.e("AchievementsFragment", "No such document for user: $userId")
            }
        }.addOnFailureListener { exception ->
            Log.e("AchievementsFragment", "Error loading History achievement: ", exception)
        }
    }

    private fun unlockHistoryAchievement(userDocRef: DocumentReference) {
        userDocRef.update("historyAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "History Achievement unlocked successfully.")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Historian!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = historyCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = historyProgressBar,
                            progressTextView = historyProgressText,
                            currentProgressTextView = historyCurrentProgressText,
                            maxProgressTextView = historyMaxProgressText,
                            cardView = historyCard,
                            iconView = historyIcon,
                            titleTextView = historyTitle,
                            requirementTextView = historyRequirement,
                            maxValue = 10,
                            currentValue = 10,
                            description = "Read 10 history books",
                            isAchieved = true
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking History achievement: ", exception)
                if (isAdded && context != null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to unlock Historian. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun loadMysterySolverAchievement(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Log to check the document content
                Log.d("AchievementsFragment", "Document data: ${document.data}")

                // Retrieve the list of finished books
                val finishedBooks = (document.get("standardCollections.Finished") as? List<*>)?.mapNotNull { book ->
                    when (book) {
                        is Map<*, *> -> {
                            val genres = book["genres"] as? List<*>
                            // Log each book's genres
                            Log.d("AchievementsFragment", "Book genres: $genres")
                            genres?.filterIsInstance<String>() // Cast genres to a list of strings
                        }
                        else -> null
                    }
                } ?: emptyList()

                // Log the flattened list of genres to verify the data
                Log.d("AchievementsFragment", "Flattened genres list: $finishedBooks")

                // Count the number of books with the genres "Crime" or "True Crime" (case-insensitive)
                val mysteryBooksCount = finishedBooks.flatten().count { genre ->
                    genre.equals("Crime", ignoreCase = true) || genre.equals("True Crime", ignoreCase = true)
                }

                // Log the count of mystery books
                Log.d("AchievementsFragment", "Mystery Books Count: $mysteryBooksCount")

                // Check if Mystery Solver achievement is already achieved
                val isMysterySolverAchieved = document.getBoolean("mysterySolverAchieved") ?: false

                if (!isMysterySolverAchieved && mysteryBooksCount >= 10) {
                    // Unlock the achievement
                    unlockMysterySolverAchievement(userDocRef)
                } else {
                    // Update the achievement progress UI
                    updateAchievementProgress(
                        progressBar = mysteryProgressBar,
                        progressTextView = mysteryProgressText,
                        currentProgressTextView = mysteryCurrentProgressText,
                        maxProgressTextView = mysteryMaxProgressText,
                        cardView = mysteryCard,
                        iconView = mysteryIcon,
                        titleTextView = mysteryTitle,
                        requirementTextView = mysteryRequirement,
                        maxValue = 10,
                        currentValue = mysteryBooksCount.coerceAtMost(10),
                        description = "Read 10 mystery or true crime books",
                        isAchieved = isMysterySolverAchieved || mysteryBooksCount >= 10
                    )

                    // Update the current and max progress TextViews
                    mysteryCurrentProgressText.text = mysteryBooksCount.coerceAtMost(10).toString()
                    mysteryMaxProgressText.text = "10"
                }
            } else {
                Log.e("AchievementsFragment", "No such document for user: $userId")
            }
        }.addOnFailureListener { exception ->
            Log.e("AchievementsFragment", "Error loading Mystery Solver achievement: ", exception)
        }
    }

    private fun unlockMysterySolverAchievement(userDocRef: DocumentReference) {
        userDocRef.update("mysterySolverAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Mystery Solver Achievement unlocked successfully.")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Mystery Solver!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = mysteryCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = mysteryProgressBar,
                            progressTextView = mysteryProgressText,
                            currentProgressTextView = mysteryCurrentProgressText,
                            maxProgressTextView = mysteryMaxProgressText,
                            cardView = mysteryCard,
                            iconView = mysteryIcon,
                            titleTextView = mysteryTitle,
                            requirementTextView = mysteryRequirement,
                            maxValue = 10,
                            currentValue = 10,
                            description = "Read 10 mystery or true crime books",
                            isAchieved = true
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Mystery Solver achievement: ", exception)
                if (isAdded && context != null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to unlock Mystery Solver. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }





    // Load achievements data from Firestore
    private fun loadAchievementsData(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // Retrieve the user's document
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Get the number of books marked as finished
                val booksFinished = (document.get("standardCollections.Finished") as? List<*>)?.size ?: 0

                // Calculate total XP
                val totalXp = booksFinished * 20

                // Calculate current level and XP within the current level (Cumulative XP)
                val currentLevel = if (totalXp == 0) 1 else ceil(totalXp / 100.0).toInt()
                val currentXpInLevel = if (totalXp == 0) 0 else totalXp - ((currentLevel - 1) * 100)

                // Calculate books needed for next level
                val xpNeededForNextLevel = 100
                val xpRemaining = xpNeededForNextLevel - currentXpInLevel
                val booksNeeded = if (xpRemaining > 0) ceil(xpRemaining / 20.0).toInt() else 0

                Log.d("AchievementsFragment", "Total XP: $totalXp, Level: $currentLevel, XP in Level: $currentXpInLevel, Books Needed: $booksNeeded")
                Log.d("AchievementsFragment", "Books Finished: $booksFinished")

                // Update the Level and XP UI with absolute values
                updateLevelAndXpUI(
                    totalXp = totalXp,
                    currentLevel = currentLevel,
                    currentXpInLevel = currentXpInLevel,
                    booksNeeded = booksNeeded
                )

                // Update achievements based on books read
                val booksRead = document.getLong("numBooksRead")?.toInt() ?: 0

                Log.d("AchievementsFragment", "Books Read: $booksRead")

                // Update individual achievements
                updateAchievementProgress(
                    progressBar = firstChapterProgressBar,
                    progressTextView = firstChapterProgressText,
                    currentProgressTextView = firstChapterCurrentProgressText,
                    maxProgressTextView = firstChapterMaxProgressText,
                    cardView = firstChapterCard,
                    iconView = firstChapterIcon,
                    titleTextView = firstChapterTitle,
                    requirementTextView = firstChapterRequirement,
                    maxValue = 1,
                    currentValue = booksFinished.coerceAtMost(1),
                    description = "Finish 1 book",
                    isAchieved = booksFinished >= 1
                )

                // Unlock the First Chapter Achievement when the first book is finished
                val isFirstChapterAchieved = document.getBoolean("firstChapterAchieved") ?: false
                if (!isFirstChapterAchieved && booksFinished >= 1) {
                    unlockFirstChapterAchievement(userDocRef)
                } else {
                    // Ensure UI is updated based on current Firestore data
                    updateAchievementProgress(
                        progressBar = firstChapterProgressBar,
                        progressTextView = firstChapterProgressText,
                        currentProgressTextView = firstChapterCurrentProgressText,
                        maxProgressTextView = firstChapterMaxProgressText,
                        cardView = firstChapterCard,
                        iconView = firstChapterIcon,
                        titleTextView = firstChapterTitle,
                        requirementTextView = firstChapterRequirement,
                        maxValue = 1,
                        currentValue = booksFinished.coerceAtMost(1),
                        description = "Finish 1 book",
                        isAchieved = isFirstChapterAchieved || booksFinished >= 1
                    )
                }

                // **Reading Rookie Achievement Implementation**
                val isReadingRookieAchieved = document.getBoolean("readingRookieAchieved") ?: false

                if (!isReadingRookieAchieved && booksRead >= 5) {
                    // Unlock the achievement
                    unlockReadingRookieAchievement(userDocRef)
                } else {
                    // Update the achievement progress
                    updateAchievementProgress(
                        progressBar = readingRookieProgressBar,
                        progressTextView = readingRookieProgressText,
                        currentProgressTextView = readingRookieCurrentProgressText,
                        maxProgressTextView = readingRookieMaxProgressText,
                        cardView = readingRookieCard,
                        iconView = readingRookieIcon,
                        titleTextView = readingRookieTitle,
                        requirementTextView = readingRookieRequirement,
                        maxValue = 5,
                        currentValue = booksRead.coerceAtMost(5),
                        description = "Log 5 books as finished",
                        isAchieved = isReadingRookieAchieved || booksRead >= 5
                    )

                    // Update the current and max progress TextViews
                    readingRookieCurrentProgressText.text = booksRead.coerceAtMost(5).toString()
                    readingRookieMaxProgressText.text = "5"
                }

                // **Story Seeker Achievement Implementation**
                val isStorySeekerAchieved = document.getBoolean("storySeekerAchieved") ?: false

                if (!isStorySeekerAchieved && booksRead >= 10) {
                    // Unlock the achievement
                    unlockStorySeekerAchievement(userDocRef)
                } else {
                    // Update the achievement progress
                    updateAchievementProgress(
                        progressBar = storySeekerProgressBar,
                        progressTextView = storySeekerProgressText,
                        currentProgressTextView = storySeekerCurrentProgressText,
                        maxProgressTextView = storySeekerMaxProgressText,
                        cardView = storySeekerCard,
                        iconView = storySeekerIcon,
                        titleTextView = storySeekerTitle,
                        requirementTextView = storySeekerRequirement,
                        maxValue = 10,
                        currentValue = booksRead.coerceAtMost(10),
                        description = "Log 10 books as finished",
                        isAchieved = isStorySeekerAchieved || booksRead >= 10
                    )

                    // Update the current and max progress TextViews
                    storySeekerCurrentProgressText.text = booksRead.coerceAtMost(10).toString()
                    storySeekerMaxProgressText.text = "10"
                }

                // **Novel Navigator Achievement Implementation**
                val isNovelNavigatorAchieved = document.getBoolean("novelNavigatorAchieved") ?: false

                if (!isNovelNavigatorAchieved && booksRead >= 25) {
                    // Unlock the achievement
                    unlockNovelNavigatorAchievement(userDocRef)
                } else {
                    // Update the achievement progress
                    updateAchievementProgress(
                        progressBar = novelNavigatorProgressBar,
                        progressTextView = novelNavigatorProgressText,
                        currentProgressTextView = novelNavigatorCurrentProgressText,
                        maxProgressTextView = novelNavigatorMaxProgressText,
                        cardView = novelNavigatorCard,
                        iconView = novelNavigatorIcon,
                        titleTextView = novelNavigatorTitle,
                        requirementTextView = novelNavigatorRequirement,
                        maxValue = 25,
                        currentValue = booksRead.coerceAtMost(25),
                        description = "Log 25 books as finished",
                        isAchieved = isNovelNavigatorAchieved || booksRead >= 25
                    )

                    // Update the current and max progress TextViews
                    novelNavigatorCurrentProgressText.text = booksRead.coerceAtMost(25).toString()
                    novelNavigatorMaxProgressText.text = "25"
                }

                // **Book Enthusiast Achievement Implementation**
                val isBookEnthusiastAchieved = document.getBoolean("bookEnthusiastAchieved") ?: false

                if (!isBookEnthusiastAchieved && booksRead >= 50) {
                    // Unlock the achievement
                    unlockBookEnthusiastAchievement(userDocRef)
                } else {
                    // Update the achievement progress
                    updateAchievementProgress(
                        progressBar = bookEnthusiastProgressBar,
                        progressTextView = bookEnthusiastProgressText,
                        currentProgressTextView = bookEnthusiastCurrentProgressText,
                        maxProgressTextView = bookEnthusiastMaxProgressText,
                        cardView = bookEnthusiastCard,
                        iconView = bookEnthusiastIcon,
                        titleTextView = bookEnthusiastTitle,
                        requirementTextView = bookEnthusiastRequirement,
                        maxValue = 50,
                        currentValue = booksRead.coerceAtMost(50),
                        description = "Log 50 books as finished",
                        isAchieved = isBookEnthusiastAchieved || booksRead >= 50
                    )

                    // Update the current and max progress TextViews
                    bookEnthusiastCurrentProgressText.text = booksRead.coerceAtMost(50).toString()
                    bookEnthusiastMaxProgressText.text = "50"
                }

                // **Legendary Librarian Achievement Implementation**
                val isLegendaryLibrarianAchieved = document.getBoolean("legendaryLibrarianAchieved") ?: false

                if (!isLegendaryLibrarianAchieved && booksRead >= 100) {
                    // Unlock the achievement
                    unlockLegendaryLibrarianAchievement(userDocRef)
                } else {
                    // Update the achievement progress
                    updateAchievementProgress(
                        progressBar = legendaryLibrarianProgressBar,
                        progressTextView = legendaryLibrarianProgressText,
                        currentProgressTextView = legendaryLibrarianCurrentProgressText,
                        maxProgressTextView = legendaryLibrarianMaxProgressText,
                        cardView = legendaryLibrarianCard,
                        iconView = legendaryLibrarianIcon,
                        titleTextView = legendaryLibrarianTitle,
                        requirementTextView = legendaryLibrarianRequirement,
                        maxValue = 100,
                        currentValue = booksRead.coerceAtMost(100),
                        description = "Log 100 books as finished",
                        isAchieved = isLegendaryLibrarianAchieved || booksRead >= 100
                    )

                    // Update the current and max progress TextViews
                    legendaryLibrarianCurrentProgressText.text = booksRead.coerceAtMost(100).toString()
                    legendaryLibrarianMaxProgressText.text = "100"
                }

                // **Book God Achievement Implementation**
                val isBookGodAchieved = document.getBoolean("bookGodAchieved") ?: false

                if (!isBookGodAchieved && booksRead >= 100) {
                    // Unlock the achievement
                    unlockBookGodAchievement(userDocRef)
                } else {
                    // Update the achievement progress
                    updateAchievementProgress(
                        progressBar = bookGodProgressBar,
                        progressTextView = bookGodProgressText,
                        currentProgressTextView = bookGodCurrentProgressText,
                        maxProgressTextView = bookGodMaxProgressText,
                        cardView = bookGodCard,
                        iconView = bookGodIcon,
                        titleTextView = bookGodTitle,
                        requirementTextView = bookGodRequirement,
                        maxValue = 100,
                        currentValue = booksRead.coerceAtMost(100),
                        description = "Log 100 books as finished",
                        isAchieved = isBookGodAchieved || booksRead >= 100
                    )

                    // Update the current and max progress TextViews
                    bookGodCurrentProgressText.text = booksRead.coerceAtMost(100).toString()
                    bookGodMaxProgressText.text = "100"
                }

                // Continue updating other achievements based on 'booksRead'
                // ... (Your existing code for other achievements)
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error loading achievements: ", exception)
        }
    }

    // Function to update the Level and XP UI
    private fun updateLevelAndXpUI(
        totalXp: Int,
        currentLevel: Int,
        currentXpInLevel: Int,
        booksNeeded: Int
    ) {
        if (!isAdded) return // Prevent UI updates if fragment is not attached

        // Update level number
        levelNumberTextView.text = "Level $currentLevel"

        // Update XP progress bar
        xpProgressBar.max = 100 // Each level requires 100 XP
        xpProgressBar.progress = currentXpInLevel

        // Calculate XP range
        val lowerBound = (currentLevel - 1) * 100 + 1
        val upperBound = currentLevel * 100

        // Update current XP and XP needed TextViews with absolute values
        currentXpTextView.text = "$totalXp XP"
        xpNeededTextView.text = "$upperBound XP"

        // Update level progress text with XP range
        xpRangeTextView.text = "$lowerBound to $upperBound XP"

        // Update level progress description
        levelProgressText.text = if (booksNeeded > 0) {
            "$booksNeeded more book${if (booksNeeded > 1) "s" else ""} to level ${currentLevel + 1}"
        } else {
            "You are at the highest level!"
        }
    }

    // Unlock the First Chapter Achievement and show a Toast notification
    private fun unlockFirstChapterAchievement(userDocRef: DocumentReference) {
        userDocRef.update("firstChapterAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "First Chapter Achievement unlocked successfully.")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: First Chapter!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = firstChapterCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = firstChapterProgressBar,
                            progressTextView = firstChapterProgressText,
                            currentProgressTextView = firstChapterCurrentProgressText,
                            maxProgressTextView = firstChapterMaxProgressText,
                            cardView = firstChapterCard,
                            iconView = firstChapterIcon,
                            titleTextView = firstChapterTitle,
                            requirementTextView = firstChapterRequirement,
                            maxValue = 1,
                            currentValue = 1,
                            description = "Finish 1 book",
                            isAchieved = true
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking First Chapter achievement: ", exception)
            }
    }

    // Unlock the Reading Rookie Achievement and show a Toast notification
    private fun unlockReadingRookieAchievement(userDocRef: DocumentReference) {
        Log.d("AchievementsFragment", "Attempting to unlock Reading Rookie Achievement")
        userDocRef.update("readingRookieAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Successfully unlocked Reading Rookie Achievement")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Reading Rookie!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = readingRookieCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = readingRookieProgressBar,
                            progressTextView = readingRookieProgressText,
                            currentProgressTextView = readingRookieCurrentProgressText,
                            maxProgressTextView = readingRookieMaxProgressText,
                            cardView = readingRookieCard,
                            iconView = readingRookieIcon,
                            titleTextView = readingRookieTitle,
                            requirementTextView = readingRookieRequirement,
                            maxValue = 5,
                            currentValue = 5,
                            description = "Log 5 books as finished",
                            isAchieved = true
                        )

                        // Update the current and max progress TextViews
                        readingRookieCurrentProgressText.text = "5"
                        readingRookieMaxProgressText.text = "5"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Reading Rookie achievement: ", exception)
            }
    }

    // Unlock the Story Seeker Achievement and show a Toast notification
    private fun unlockStorySeekerAchievement(userDocRef: DocumentReference) {
        Log.d("AchievementsFragment", "Attempting to unlock Story Seeker Achievement")
        userDocRef.update("storySeekerAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Successfully unlocked Story Seeker Achievement")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Story Seeker!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = storySeekerCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = storySeekerProgressBar,
                            progressTextView = storySeekerProgressText,
                            currentProgressTextView = storySeekerCurrentProgressText,
                            maxProgressTextView = storySeekerMaxProgressText,
                            cardView = storySeekerCard,
                            iconView = storySeekerIcon,
                            titleTextView = storySeekerTitle,
                            requirementTextView = storySeekerRequirement,
                            maxValue = 10,
                            currentValue = 10,
                            description = "Log 10 books as finished",
                            isAchieved = true
                        )

                        // Update the current and max progress TextViews
                        storySeekerCurrentProgressText.text = "10"
                        storySeekerMaxProgressText.text = "10"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Story Seeker achievement: ", exception)
            }
    }

    // Unlock the Novel Navigator Achievement and show a Toast notification
    private fun unlockNovelNavigatorAchievement(userDocRef: DocumentReference) {
        Log.d("AchievementsFragment", "Attempting to unlock Novel Navigator Achievement")
        userDocRef.update("novelNavigatorAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Successfully unlocked Novel Navigator Achievement")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Novel Navigator!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = novelNavigatorCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = novelNavigatorProgressBar,
                            progressTextView = novelNavigatorProgressText,
                            currentProgressTextView = novelNavigatorCurrentProgressText,
                            maxProgressTextView = novelNavigatorMaxProgressText,
                            cardView = novelNavigatorCard,
                            iconView = novelNavigatorIcon,
                            titleTextView = novelNavigatorTitle,
                            requirementTextView = novelNavigatorRequirement,
                            maxValue = 25,
                            currentValue = 25,
                            description = "Log 25 books as finished",
                            isAchieved = true
                        )

                        // Update the current and max progress TextViews
                        novelNavigatorCurrentProgressText.text = "25"
                        novelNavigatorMaxProgressText.text = "25"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Novel Navigator achievement: ", exception)
            }
    }

    // Unlock the Book Enthusiast Achievement and show a Toast notification
    private fun unlockBookEnthusiastAchievement(userDocRef: DocumentReference) {
        Log.d("AchievementsFragment", "Attempting to unlock Book Enthusiast Achievement")
        userDocRef.update("bookEnthusiastAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Successfully unlocked Book Enthusiast Achievement")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Book Enthusiast!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = bookEnthusiastCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = bookEnthusiastProgressBar,
                            progressTextView = bookEnthusiastProgressText,
                            currentProgressTextView = bookEnthusiastCurrentProgressText,
                            maxProgressTextView = bookEnthusiastMaxProgressText,
                            cardView = bookEnthusiastCard,
                            iconView = bookEnthusiastIcon,
                            titleTextView = bookEnthusiastTitle,
                            requirementTextView = bookEnthusiastRequirement,
                            maxValue = 50,
                            currentValue = 50,
                            description = "Log 50 books as finished",
                            isAchieved = true
                        )

                        // Update the current and max progress TextViews
                        bookEnthusiastCurrentProgressText.text = "50"
                        bookEnthusiastMaxProgressText.text = "50"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Book Enthusiast achievement: ", exception)
            }
    }

    // Unlock the Legendary Librarian Achievement and show a Toast notification
    private fun unlockLegendaryLibrarianAchievement(userDocRef: DocumentReference) {
        Log.d("AchievementsFragment", "Attempting to unlock Legendary Librarian Achievement")
        userDocRef.update("legendaryLibrarianAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Successfully unlocked Legendary Librarian Achievement")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Legendary Librarian!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = legendaryLibrarianCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = legendaryLibrarianProgressBar,
                            progressTextView = legendaryLibrarianProgressText,
                            currentProgressTextView = legendaryLibrarianCurrentProgressText,
                            maxProgressTextView = legendaryLibrarianMaxProgressText,
                            cardView = legendaryLibrarianCard,
                            iconView = legendaryLibrarianIcon,
                            titleTextView = legendaryLibrarianTitle,
                            requirementTextView = legendaryLibrarianRequirement,
                            maxValue = 100,
                            currentValue = 100,
                            description = "Log 100 books as finished",
                            isAchieved = true
                        )

                        // Update the current and max progress TextViews
                        legendaryLibrarianCurrentProgressText.text = "100"
                        legendaryLibrarianMaxProgressText.text = "100"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Legendary Librarian achievement: ", exception)
            }
    }

    // Unlock the Book God Achievement and show a Toast notification
    private fun unlockBookGodAchievement(userDocRef: DocumentReference) {
        Log.d("AchievementsFragment", "Attempting to unlock Book God Achievement")
        userDocRef.update("bookGodAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Successfully unlocked Book God Achievement")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Book God!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = bookGodCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = bookGodProgressBar,
                            progressTextView = bookGodProgressText,
                            currentProgressTextView = bookGodCurrentProgressText,
                            maxProgressTextView = bookGodMaxProgressText,
                            cardView = bookGodCard,
                            iconView = bookGodIcon,
                            titleTextView = bookGodTitle,
                            requirementTextView = bookGodRequirement,
                            maxValue = 100,
                            currentValue = 100,
                            description = "Log 100 books as finished",
                            isAchieved = true
                        )

                        // Update the current and max progress TextViews
                        bookGodCurrentProgressText.text = "100"
                        bookGodMaxProgressText.text = "100"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Book God achievement: ", exception)
            }
    }

    // Function to update individual achievement progress
    private fun updateAchievementProgress(
        progressBar: ProgressBar,
        progressTextView: TextView,
        currentProgressTextView: TextView,
        maxProgressTextView: TextView,
        cardView: CardView,
        iconView: ImageView,
        titleTextView: TextView,
        requirementTextView: TextView,
        maxValue: Int,
        currentValue: Int,
        description: String,
        isAchieved: Boolean = false
    ) {
        Log.d("AchievementsFragment", "Updating achievement: $description, isAchieved: $isAchieved")
        if (!isAdded || context == null) return // Prevent UI updates if fragment is not attached

        progressBar.max = maxValue
        progressBar.progress = currentValue.coerceAtMost(maxValue)

        if (isAchieved) {
            // Achievement is unlocked
            progressBar.progress = maxValue
            progressTextView.text = "Completed!"
            progressTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            // Set title and requirement text to black when achievement is unlocked
            titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            requirementTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            // Animate card background color change
            val fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card)
            val toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
            Log.d("AchievementsFragment", "Animating from $fromColor to $toColor")
            animateCardBackgroundColor(
                cardView = cardView,
                fromColor = fromColor,
                toColor = toColor
            )

            // Update progress bar tint
            progressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.achievement_unlocked_color)

            // Restore the icon color by removing the gray tint
            iconView.setColorFilter(null)
            iconView.imageTintList = null

            // Hide the current and max progress TextViews
            currentProgressTextView.visibility = View.GONE
            maxProgressTextView.visibility = View.GONE

        } else {
            // Achievement is still locked
            progressTextView.text = description
            progressTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))

            // Set title and requirement text to gray when achievement is locked
            titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            requirementTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))

            // Keep progress bar gray for locked achievements
            progressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey)

            // Apply gray tint to the icon to indicate the locked state
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))

            // Ensure the current and max progress TextViews are visible
            currentProgressTextView.visibility = View.VISIBLE
            maxProgressTextView.visibility = View.VISIBLE
        }
    }

    private fun loadPsychAchievement(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Log to check the document content
                Log.d("AchievementsFragment", "Document data: ${document.data}")

                // Retrieve the list of finished books
                val finishedBooks = (document.get("standardCollections.Finished") as? List<*>)?.mapNotNull { book ->
                    when (book) {
                        is Map<*, *> -> {
                            val genres = book["genres"] as? List<*>
                            // Log each book's genres
                            Log.d("AchievementsFragment", "Book genres: $genres")
                            genres?.filterIsInstance<String>() // Cast genres to a list of strings
                        }
                        else -> null
                    }
                } ?: emptyList()

                // Log the flattened list of genres to verify the data
                Log.d("AchievementsFragment", "Flattened genres list: $finishedBooks")

                // Count the number of books with the genres "self help" or "psychology" (case-insensitive)
                val psychBooksCount = finishedBooks.flatten().count { genre ->
                    genre.equals("self-help", ignoreCase = true) || genre.equals("psychology", ignoreCase = true)
                }

                // Log the count of psychology books
                Log.d("AchievementsFragment", "Psych Books Count: $psychBooksCount")

                // Check if Psych Achievement is already achieved
                val isPsychAchieved = document.getBoolean("psychAchieved") ?: false

                if (!isPsychAchieved && psychBooksCount >= 10) {
                    // Unlock the achievement
                    unlockPsychAchievement(userDocRef)
                } else {
                    // Update the achievement progress UI
                    updateAchievementProgress(
                        progressBar = psychProgressBar,
                        progressTextView = psychProgressText,
                        currentProgressTextView = psychCurrentProgressText,
                        maxProgressTextView = psychMaxProgressText,
                        cardView = psychCard,
                        iconView = psychIcon,
                        titleTextView = psychTitle,
                        requirementTextView = psychRequirement,
                        maxValue = 10,
                        currentValue = psychBooksCount.coerceAtMost(10),
                        description = "Not reached ",
                        isAchieved = isPsychAchieved || psychBooksCount >= 10
                    )

                    // Update the current and max progress TextViews
                    psychCurrentProgressText.text = psychBooksCount.coerceAtMost(10).toString()
                    psychMaxProgressText.text = "10"
                }
            } else {
                Log.e("AchievementsFragment", "No such document for user: $userId")
            }
        }.addOnFailureListener { exception ->
            Log.e("AchievementsFragment", "Error loading Psych Achievement: ", exception)
        }
    }

    private fun unlockPsychAchievement(userDocRef: DocumentReference) {
        userDocRef.update("psychAchieved", true)
            .addOnSuccessListener {
                Log.d("AchievementsFragment", "Psych Achievement unlocked successfully.")
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: Psych Expert!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = psychCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = psychProgressBar,
                            progressTextView = psychProgressText,
                            currentProgressTextView = psychCurrentProgressText,
                            maxProgressTextView = psychMaxProgressText,
                            cardView = psychCard,
                            iconView = psychIcon,
                            titleTextView = psychTitle,
                            requirementTextView = psychRequirement,
                            maxValue = 10,
                            currentValue = 10,
                            description = "Not yet unlocked",
                            isAchieved = true
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AchievementsFragment", "Error unlocking Psych Achievement: ", exception)
                if (isAdded && context != null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to unlock Psych Achievement. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    // Function to animate the background color of a CardView
    private fun animateCardBackgroundColor(cardView: CardView, fromColor: Int, toColor: Int) {
        Log.d("AchievementsFragment", "Animating card background color from $fromColor to $toColor")
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            cardView.setCardBackgroundColor(animatedColor)
            Log.d("AchievementsFragment", "Animated color: $animatedColor")
        }
        colorAnimation.start()
    }

    // Optional: Setup Firestore snapshot listener for real-time updates
    private fun setupRealtimeUpdates(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                if (isAdded && context != null) { // Ensure fragment is attached
                    // Reload achievements data to reflect real-time changes
                    loadAchievementsData(userId)
                }
            } else {
                Log.d("Firestore", "Current data: null")
            }
        }
    }
}
